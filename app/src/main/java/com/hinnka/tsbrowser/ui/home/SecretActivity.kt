package com.hinnka.tsbrowser.ui.home

import android.os.Bundle
import android.view.WindowManager

class SecretActivity : MainActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
    }

    override fun onStop() {
        super.onStop()
        finish()
    }

}