package com.hinnka.tsbrowser.ui.base

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.resume

open class BaseActivity : AppCompatActivity() {

    private val requestCode = AtomicInteger()
    private val callbackMap = mutableMapOf<Int, (resultCode: Int, data: Intent?) -> Unit>()

    lateinit var permissionLauncher: ActivityResultLauncher<Array<out String>>
    var permissionCallback: (Map<String, Boolean>) -> Unit = {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            permissionCallback.invoke(it)
        }
    }

    suspend fun requestPermissions(vararg permissions: String): Map<String, Boolean> {
        return suspendCancellableCoroutine { continuation ->
            permissionCallback = {
                continuation.resume(it)
            }
            permissionLauncher.launch(permissions)
        }
    }

    fun startActivityForResult(intent: Intent, callback: (resultCode: Int, data: Intent?) -> Unit) {
        val code = requestCode.getAndIncrement()
        try {
            startActivityForResult(intent, code)
            callbackMap[code] = callback
        } catch (e: Exception) {
            callback.invoke(Activity.RESULT_CANCELED, null)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackMap.remove(requestCode)?.invoke(resultCode, data)
    }
}