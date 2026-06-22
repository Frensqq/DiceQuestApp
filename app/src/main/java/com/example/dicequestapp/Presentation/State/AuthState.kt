package com.example.netlibrary.Presentation.State

import com.example.dq_net_library.Domain.Model.User.ResponseOtpRequest
import com.example.dq_net_library.Domain.Model.User.User


data class AuthState(

    var isLoading:Boolean = false,
    var isSuccess:Boolean =false,
    var error: String?=null,
    var emailError: String? = null,
    var passwordError: String? = null,
    var confirmPasswordError: String? = null,
    var generalError: String? = null,
    var isNotInternet:Boolean = false,


    val username: String = "",
    val email: String = "",
    val password: String = "",
    val passwordConfirm: String = password,

    var user: User? =  null,

    var token: String = "",

    val otpRequest: ResponseOtpRequest? = null,
    val otpCode: String = ""
)


