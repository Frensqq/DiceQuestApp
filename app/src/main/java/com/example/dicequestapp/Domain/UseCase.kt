package com.example.dicequestapp.Domain

import com.example.dq_net_library.Domain.Model.Cell.Cell
import com.example.dq_net_library.Domain.Model.Cell.CreateCell
import com.example.dq_net_library.Domain.Model.Cell.ResponsesCell
import com.example.dq_net_library.Domain.Model.Game.AddPlayer
import com.example.dq_net_library.Domain.Model.Game.Game
import com.example.dq_net_library.Domain.Model.Game.GameResponses
import com.example.dq_net_library.Domain.Model.Game.RedactGame
import com.example.dq_net_library.Domain.Model.Game.RequestCreateGame
import com.example.dq_net_library.Domain.Model.NetworkResult
import com.example.dq_net_library.Domain.Model.Player.CreatePlayer
import com.example.dq_net_library.Domain.Model.Player.Player
import com.example.dq_net_library.Domain.Model.Player.RedactPlayer
import com.example.dq_net_library.Domain.Model.Player.ResponsesPlayers
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


    suspend fun createGame(request: RequestCreateGame): NetworkResult<Game>{
        return  Repository.createGame(request)
    }
    suspend fun getGame(id: String): NetworkResult<Game>{
        return  Repository.getGame(id)
    }
    suspend fun getGames(filter: String? = null): NetworkResult<GameResponses>{
        return  Repository.getGames(filter)
    }
    suspend fun addPlayerGames(id: String, request: AddPlayer): NetworkResult<Game>{
        return  Repository.addPlayerGames(id, request)
    }
    suspend fun patchGames(id: String, request: RedactGame): NetworkResult<Game>{
        return  Repository.patchGames(id,request)
    }

    //Player
    suspend fun createPlayer(request: CreatePlayer): NetworkResult<Player>{
        return  Repository.createPlayer(request)
    }
    suspend fun getPlayer(id: String): NetworkResult<Player>{
        return  Repository.getPlayer(id)
    }
    suspend fun getPlayers(filter: String? = null): NetworkResult<ResponsesPlayers>{
        return  Repository.getPlayers(filter)
    }
    suspend fun deletePlayer(id: String): NetworkResult<Unit>{
        return  Repository.deletePlayer(id)
    }
    suspend fun patchPlayer(id: String, request: RedactPlayer): NetworkResult<Player>{
        return  Repository.patchPlayer(id,request)
    }

    //Cell
    suspend fun getCell(id: String): NetworkResult<Cell>{
        return  Repository.getCell(id)
    }
    suspend fun getCells(filter: String? = null,page: Int? = null,perPage: Int? = null): NetworkResult<ResponsesCell>{
        return  Repository.getCells(filter, page = page, perPage = perPage )
    }
    suspend fun deleteCell(id: String): NetworkResult<Unit>{
        return  Repository.deleteCell(id)
    }
    suspend fun createCell(request: CreateCell): NetworkResult<Cell>{
        return  Repository.createCell(request)
    }
}