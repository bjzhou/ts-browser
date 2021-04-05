package com.hinnka.tsbrowser.db

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TabEntity(
    val id: Int,
    val isActive: Boolean,
    val title: String,
    val iconPath: String?,
    val url: String,
    val thumbnailPath: String?,
) : Parcelable

@Parcelize
data class Tabs(
    val list: List<TabEntity>
): Parcelable