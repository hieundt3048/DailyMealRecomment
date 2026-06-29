package com.example.dailymealrecomment.data.xampp

import com.example.dailymealrecomment.data.model.UserProfile

data class XamppUser(
    val id: Int,
    val name: String,
    val email: String,
    val profileCompleted: Boolean,
)

data class XamppAuthSession(
    val token: String,
    val user: XamppUser,
    val profile: XamppProfile?,
)

data class XamppProfile(
    val profile: UserProfile,
    val dailyCalorieTarget: Int,
)
