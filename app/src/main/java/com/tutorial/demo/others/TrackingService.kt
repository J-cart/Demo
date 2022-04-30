package com.tutorial.demo.others

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLng
import com.tutorial.demo.R
import com.tutorial.demo.TrackingUtility
import com.tutorial.demo.others.Constants.ACTION_PAUSE_SERVICE
import com.tutorial.demo.others.Constants.ACTION_START_OR_RESUME_SERVICE
import com.tutorial.demo.others.Constants.ACTION_STOP_SERVICE
import com.tutorial.demo.others.Constants.LOCATION_FASTEST_INTERVAL
import com.tutorial.demo.others.Constants.LOCATION_UPDATE_INTERVAL
import com.tutorial.demo.others.Constants.NOTIFICATION_CHANNEL_ID
import com.tutorial.demo.others.Constants.NOTIFICATION_CHANNEL_NAME
import com.tutorial.demo.others.Constants.NOTIFICATION_ID
import com.tutorial.demo.others.Constants.TIMER_UPDATE_INTERVAL
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

typealias polyline = MutableList<LatLng>
typealias polylines = MutableList<polyline>

@AndroidEntryPoint
class TrackingService : LifecycleService() {

    var isFirstRun = true
    var serviceKilled = false

    @Inject
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    @Inject
    lateinit var baseNotificationBuilder: NotificationCompat.Builder

    lateinit var curNotificationBuilder: NotificationCompat.Builder
    private val timeInSeconds = MutableLiveData<Long>()

    companion object {
        val isTracking = MutableLiveData<Boolean>()
        val pathPoints = MutableLiveData<polylines>()
        val timeInMillis = MutableLiveData<Long>()


    }

    private var isTimeEnabled = false
    private var lapTime = 0L
    private var totalTime = 0L
    private var timeStarted = 0L
    private var lastSecondTimestamp = 0L
    private fun startTimer() {
        addEmptyPolyline()
        isTracking.postValue(true)
        timeStarted = System.currentTimeMillis()
        isTimeEnabled = true

        CoroutineScope(Dispatchers.Main).launch {
            while (isTracking.value!!) {
                lapTime = System.currentTimeMillis() - timeStarted

                timeInMillis.postValue(totalTime + lapTime)

                if (timeInMillis.value!! >= lastSecondTimestamp + 1000L) {
                    timeInSeconds.postValue(timeInSeconds.value!! + 1)
                    lastSecondTimestamp += 1000L
                }
                delay(TIMER_UPDATE_INTERVAL)
            }

            totalTime += lapTime
        }
    }


    private fun pauseService() {
        isTracking.postValue(false)
        isTimeEnabled = false
    }

    private fun postInitValue() {
        isTracking.postValue(false)
        pathPoints.postValue(mutableListOf())
        timeInSeconds.postValue(0L)
        timeInMillis.postValue(0L)
    }

    private fun addEmptyPolyline() = pathPoints.value?.apply {
        add(mutableListOf())
        pathPoints.postValue(this)
    } ?: pathPoints.postValue(mutableListOf(mutableListOf()))

    private fun addPathPoint(location: Location?) {
        location?.let {
            val pos = LatLng(location.latitude, location.longitude)

            pathPoints.value?.apply {
                last().add(pos)
                pathPoints.postValue(this)
            }
        }

    }

    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)
            if (isTracking.value!!) {
                result.locations.apply {
                    for (location in this) {
                        addPathPoint(location)
                    }
                }
            }
        }

    }

    @SuppressLint("MissingPermission")
    private fun updateLocationTracking(isTracking: Boolean) {
        if (isTracking) {
            if (TrackingUtility.hasLocationPermission(this)) {
                val request = LocationRequest().apply {
                    interval = LOCATION_UPDATE_INTERVAL
                    fastestInterval = LOCATION_FASTEST_INTERVAL
                    priority = PRIORITY_HIGH_ACCURACY
                }
                fusedLocationProviderClient.requestLocationUpdates(
                    request,
                    locationCallback,
                    Looper.getMainLooper()
                )
            } else {
                fusedLocationProviderClient.removeLocationUpdates(locationCallback)
            }
        }
    }

    private fun updateNotificationTrackingState(isTracking: Boolean) {
        val notificationText = if (isTracking) "Pause" else "Resume"
        val pendingIntent = if (isTracking) {
            val pauseIntent = Intent(this, TrackingService::class.java).apply {
                action = ACTION_PAUSE_SERVICE
            }
            PendingIntent.getService(this, 1, pauseIntent, FLAG_UPDATE_CURRENT)
        } else {
            val resumeIntent = Intent(this, TrackingService::class.java).apply {
                action = ACTION_START_OR_RESUME_SERVICE
            }
            PendingIntent.getService(this, 2, resumeIntent, FLAG_UPDATE_CURRENT)
        }

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

//        this fetches the new notification actions and replaces the old one
        curNotificationBuilder.javaClass.getDeclaredField("mActions").apply {
            isAccessible = true
            set(curNotificationBuilder, ArrayList<NotificationCompat.Action>())
        }

        if (!serviceKilled) {
            curNotificationBuilder = baseNotificationBuilder
                .addAction(R.drawable.ic_pause_black_24dp, notificationText, pendingIntent)
            notificationManager.notify(NOTIFICATION_ID, curNotificationBuilder.build())
        }
    }


    override fun onCreate() {
        super.onCreate()
        postInitValue()
        curNotificationBuilder = baseNotificationBuilder
        fusedLocationProviderClient = FusedLocationProviderClient(this)
        isTracking.observe(this, Observer {
            updateLocationTracking(it)
            updateNotificationTrackingState(it)
        })
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        intent?.let {
            when (it.action) {
                ACTION_START_OR_RESUME_SERVICE ->
                    if (isFirstRun) {
                        startForegroundService()
                        isFirstRun = false
                    } else {
                        startTimer()
                        Timber.d("Resuming service")
                    }
                ACTION_PAUSE_SERVICE -> {
                    pauseService()
                    Timber.d("Paused service")
                }
                ACTION_STOP_SERVICE -> {
                    stopForegroundService()
                    Timber.d("Stopped service")
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun startForegroundService() {

        startTimer()
        isTracking.postValue(true)
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }

        /* NO LONGER NEEDED SINCE IT HAS BEEN INJECTED FROM THE SERVICE-MODULE OBJECT
          val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
               .setAutoCancel(false)
               .setOngoing(true)
               .setSmallIcon(R.drawable.ic_directions_run_black_24dp)
               .setContentTitle("Running App")
               .setContentText("00:00:00")
               .setContentIntent(getMainActivityPendingIntent())


           startForeground(NOTIFICATION_ID, notificationBuilder.build())
               */

        startForeground(NOTIFICATION_ID, baseNotificationBuilder.build())

        timeInSeconds.observe(this, Observer {
            if (!serviceKilled) {
                val notification = curNotificationBuilder
                    .setContentText(TrackingUtility.getFormattedTimeInMillis(it * 1000L))
                notificationManager.notify(NOTIFICATION_ID, notification.build())
            }
        })
    }


    /* NO LONGER NEEDED SINCE IT HAS BEEN INJECTED TO THE BASE-NOTIFICATION FROM THE SERVICE-MODULE OBJECT
    private fun getMainActivityPendingIntent() = PendingIntent.getActivity(
         this,
         0,
         Intent(this, MainActivity::class.java).also {
             it.action = ACTION_SHOW_TRACKING_FRAGMENT
         },
         FLAG_UPDATE_CURRENT
     )*/
    private fun stopForegroundService() {
        serviceKilled = true
        isFirstRun = true
        pauseService()
        postInitValue()
        stopForeground(true)
        stopSelf()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            IMPORTANCE_LOW
        )

        notificationManager.createNotificationChannel(channel)
    }

}