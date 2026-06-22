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

    var Token: String
        get() = actSystem.getString("Token", "")!!
        set(value) = actSystem.edit().putString("Token", value).apply()

    var Email: String
        get() = actSystem.getString("Email","")!!
        set(value) = actSystem.edit().putString("Email", value).apply()

    var userName: String
        get() = actSystem.getString("userName","")!!
        set(value) = actSystem.edit().putString("userName", value).apply()

    var currentGame: String
        get() = actSystem.getString("currentGame","")!!
        set(value) = actSystem.edit().putString("currentGame", value).apply()

}