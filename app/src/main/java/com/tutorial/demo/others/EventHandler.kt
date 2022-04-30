package com.tutorial.demo.others

open class EventHandler<out T>(private val content: T) {


    var hasBeenHandled = false
        private set   //Allow external read but not write

    /*
    * Returns the content and prevents its use again
    */


    fun getContentIfNotHandledOrNull(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }


    /*
    * Returns the content, even if it has been handled
    */
    fun peekContent(): T = content
}

