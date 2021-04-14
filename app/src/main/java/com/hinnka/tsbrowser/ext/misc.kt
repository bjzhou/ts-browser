package com.hinnka.tsbrowser.ext

import android.util.Log
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

val exceptionHandler = CoroutineExceptionHandler { context, throwable ->
    Log.e("TSBrowser", "$context execute error, thread: ${Thread.currentThread().name}", throwable)
}

val mainScope = CoroutineScope(SupervisorJob() + Dispatchers.Main + exceptionHandler)
val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO + exceptionHandler)