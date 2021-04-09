package com.hinnka.tsbrowser.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager

class SecretActivity : MainActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
    }

    override fun finish() {
        startActivity(Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        })
    }
}