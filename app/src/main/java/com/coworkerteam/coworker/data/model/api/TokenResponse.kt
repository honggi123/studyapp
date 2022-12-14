package com.coworkerteam.coworker.data.model.api

import com.google.gson.annotations.SerializedName

data class TokenResponse(
    @SerializedName("accessToken")
    val accessToken: String,
    @SerializedName("message")
    val message: String
)