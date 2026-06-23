package com.example.dicequestapp.Presentation.State

import com.example.dq_net_library.Domain.Model.User.User

data class MainState (

    var isLoading:Boolean = false,
    var isSuccess:Boolean =false,
    var error: String?=null,
    var generalError: String? = null,
    var isNotInternet:Boolean = false,

    var userId: String = "",
    var username: String = "",

    var User: User? = null,

    )