package com.hinnka.tsbrowser.ui.composable.widget

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

class AlertBottomSheet(val params: Params) {

    fun show() {
        drawerState.cancelable = params.cancelable
        drawerState.open {
            val scope = rememberCoroutineScope()
            Column(Modifier.padding(16.dp)) {
                if (params.title.isNotBlank()) {
                    Text(text = params.title, style = MaterialTheme.typography.h6)
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 100.dp)
                        .padding(vertical = 16.dp), contentAlignment = Alignment.CenterStart
                ) {
                    params.view?.let { view ->
                        Box(modifier = Modifier
                            .heightIn(max = 400.dp)
                            .verticalScroll(
                                rememberScrollState()
                            )) {
                            view()
                        }
                    } ?: Text(
                        text = params.message,
                        fontSize = 14.sp
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.End, modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    if (params.negativeString.isNotBlank()) {
                        TextButton(onClick = {
                            params.negativeBlock()
                            scope.launch {
                                drawerState.close()
                            }
                        }) {
                            Text(text = params.negativeString)
                        }
                    }
                    if (params.positiveString.isNotBlank()) {
                        Button(modifier = Modifier.padding(start = 8.dp), onClick = {
                            params.positiveBlock()
                            scope.launch {
                                drawerState.close()
                            }
                        }) {
                            Text(text = params.positiveString)
                        }
                    }
                }
            }
        }
    }

    data class Params(
        var title: String = "",
        var view: (@Composable () -> Unit)? = null,
        var message: String = "",
        var positiveString: String = "",
        var positiveBlock: () -> Unit = {},
        var negativeString: String = "",
        var negativeBlock: () -> Unit = {},
        var cancelable: Boolean = false,
    )

    class Builder(val context: Context) {
        val params = Params()

        fun setTitle(@StringRes res: Int) {
            params.title = context.getString(res)
        }

        fun setView(view: @Composable () -> Unit) {
            params.view = view
        }

        fun setMessage(message: String) {
            params.message = message
        }

        fun setMessage(@StringRes res: Int) {
            params.message = context.getString(res)
        }

        fun setPositiveButton(@StringRes res: Int, block: () -> Unit) {
            params.positiveString = context.getString(res)
            params.positiveBlock = block
        }

        fun setNegativeButton(@StringRes res: Int, block: () -> Unit = {}) {
            params.negativeString = context.getString(res)
            params.negativeBlock = block
        }

        fun setCancelable(cancelable: Boolean) {
            params.cancelable = cancelable
        }

        fun show() {
            val sheet = AlertBottomSheet(params)
            sheet.show()
        }
    }

    companion object {
        val drawerState = BottomDrawerState()

        fun open(showScrim: Boolean = true, content: @Composable ColumnScope.() -> Unit) {
            drawerState.open(showScrim, content)
        }

        suspend fun close() {
            drawerState.close()
        }
    }
}