package com.example.dicequestapp.Domain

import com.example.dq_net_library.Domain.Model.NetworkResult
import com.example.dq_net_library.Domain.Model.User.ResponseAuth
import com.example.dq_net_library.Domain.Model.User.ResponseOtpRequest
import com.example.dq_net_library.Domain.Model.User.User
import com.example.dq_net_library.Domain.Model.User.UserResponses
import com.example.dq_net_library.Domain.Repository.Repository
import java.io.File

class UseCase(private val  Repository: Repository) {

    suspend fun registration(
        email: String,
        password: String,
        passwordConfirm:String,
        username: String,
        avatar: File?
    ): NetworkResult<User> {
        return Repository.registration(
            email,
            password,
            passwordConfirm,
            username,
            avatar
        )
    }

    suspend fun loginIn(
        email: String,
        password: String
    ): NetworkResult<ResponseAuth>{
        return  Repository.loginIn(
            email,
            password
        )
    }

    suspend fun updateProfile(
        userId: String,
        token: String,
        userName: String,
        avatarFile: File?
    ): NetworkResult<User> {
        return Repository.updateProfile(
            userId,
            token,
            userName,
            avatarFile
        )
    }

    suspend fun deleteUser(
        id: String
    ): NetworkResult<Unit>{
        return  Repository.deleteUser(id)
    }

    suspend fun getUsers(
        filter: String? = null
    ): NetworkResult<UserResponses>{
        return  Repository.getUsers(filter)
    }

    suspend fun getUser(
        id: String
    ): NetworkResult<User>{
        return  Repository.getUser(id)
    }

    suspend fun otpAuth(
        otpId:String,
        password: String
    ): NetworkResult<ResponseAuth>{
        return Repository.otpAuth(
            otpId,
            password
        )
    }

    suspend fun otpRequest(
        email:String,
    ): NetworkResult<ResponseOtpRequest>{
        return Repository.otpRequest(
            email,
        )
    }

    suspend fun resetPassword(
        email: String
    ): NetworkResult<Unit>{
        return Repository.resetPassword(email)
    }

    suspend fun changePassword(
        token: String,
        password: String,
        newPassword: String
    ): NetworkResult<Unit>{
        return  Repository.changePassword(
            token,
            password,
            newPassword
        )
    }

    fun getImageUrl(collectionId: String, recordId: String, fileName: String): String{
        return Repository.getImageUrl(collectionId,recordId,fileName)
    }
}