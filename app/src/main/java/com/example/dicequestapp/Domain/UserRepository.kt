package com.example.dicequestapp.Domain

import android.content.Context
import android.content.SharedPreferences

object UserRepository {

    private lateinit var actSystem: SharedPreferences

    fun init(context: Context){
        actSystem = context.getSharedPreferences("actSystem", Context.MODE_PRIVATE)
    }

    var Act: Boolean
        get() = actSystem.getBoolean("Act", false)
        set(value) = actSystem.edit().putBoolean("Act",value).apply()

    var UserId: String
        get() = actSystem.getString("UserId", "")!!
        set(value) = actSystem.edit().putString("UserId", value).apply()

    var PlayerId: String
        get() = actSystem.getString("PlayerId", "")!!
        set(value) = actSystem.edit().putString("PlayerId", value).apply()
    var GameId: String
        get() = actSystem.getString("GameId", "")!!
        set(value) = actSystem.edit().putString("GameId", value).apply()

    var Token: String
        get() = actSystem.getString("Token", "")!!
        set(value) = actSystem.edit().putString("Token", value).apply()

    var Email: String
        get() = actSystem.getString("Email","")!!
        set(value) = actSystem.edit().putString("Email", value).apply()

    var userName: String
        get() = actSystem.getString("userName","")!!
        set(value) = actSystem.edit().putString("userName", value).apply()

    var isHost: Boolean
        get() = actSystem.getBoolean("isHost", false)
        set(value) = actSystem.edit().putBoolean("isHost", value).apply()

    var isMultiplayer: Boolean
        get() = actSystem.getBoolean("isMultiplayer", false)
        set(value) = actSystem.edit().putBoolean("isMultiplayer", value).apply()

    var currentGame: String
        get() = actSystem.getString("currentGame","")!!
        set(value) = actSystem.edit().putString("currentGame", value).apply()

}