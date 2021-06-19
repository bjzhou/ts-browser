package com.hinnka.tsbrowser.persist

import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE
import com.hinnka.tsbrowser.R

object Favorites {
    val default = listOf(
        Favorite(
            "https://www.google.com",
            "Google",
            R.drawable.ic_google,
            0xFF4285F4
        ),
        Favorite(
            "https://mail.google.com",
            "Gmail",
            R.drawable.ic_gmail,
            0xFFD93025
        ),
        Favorite(
            "https://www.youtube.com",
            "Youtube",
            R.drawable.ic_youtube,
            0xFFFF0200
        ),
        Favorite(
            "https://www.amazon.com",
            "Amazon",
            R.drawable.ic_amazon,
            0xFFFEBD68
        ),
        Favorite(
            "https://www.facebook.com",
            "Facebook",
            R.drawable.ic_facebook,
            0xFF1877F2
        ),
        Favorite(
            "https://www.instagram.com",
            "Instagram",
            R.drawable.ic_instagram,
            0xFFEA4E4D
        ),
        Favorite(
            "https://www.twitter.com",
            "Twitter",
            R.drawable.ic_twitter,
            0xFF129AFB
        ),
    )

    fun init() {
        if (LocalStorage.isFavoriteInitialized) return
        AppDatabase.instance.favoriteDao().addAll(*default.toTypedArray())
        LocalStorage.isFavoriteInitialized = true
    }
}

@Entity
data class Favorite(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo var url: String,
    @ColumnInfo var title: String,
) {

    @Ignore
    var iconRes: Int = 0

    @Ignore
    var color: Long = 0

    constructor(
        url: String,
        title: String,
        iconRes: Int = 0,
        color: Long = 0
    ) : this(url = url, title = title) {
        this.iconRes = iconRes
        this.color = color
    }

}

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorite")
    fun getAll(): List<Favorite>

    @Insert(onConflict = REPLACE)
    fun addAll(vararg favorite: Favorite)

    @Insert(onConflict = REPLACE)
    fun insertOrUpdate(favorite: Favorite): Long

    @Delete
    fun delete(favorite: Favorite)
}