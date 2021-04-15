package com.hinnka.tsbrowser.ui.composable.download

import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.hinnka.tsbrowser.R
import com.hinnka.tsbrowser.ui.base.TSAppBar

@Composable
fun DownloadPage() {
    Scaffold(topBar = {
        TSAppBar(title = stringResource(id = R.string.downloads))
    }) {

    }
}