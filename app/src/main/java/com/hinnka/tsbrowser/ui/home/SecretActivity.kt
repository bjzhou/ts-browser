package com.hinnka.tsbrowser.ui.home

import android.os.Bundle
import android.view.WindowManager
import com.hinnka.tsbrowser.persist.Settings

class SecretActivity : MainActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)

        val mnemonic = intent.getStringExtra("mnemonic")
        if (Settings.mnemonic != mnemonic) {
            finish()
        }
    }

    override fun onStop() {
        super.onStop()
        finish()
    }

}