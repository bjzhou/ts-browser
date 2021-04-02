package com.hinnka.tsbrowser.ext

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

val mainScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)