package com.example.dailymealrecomment.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    var height: Double = 0.0,
    var weight: Double = 0.0,
    var age: Int = 0,
    var gender: String = "Male",
    var activityLevel: String = "Sedentary",
    var goal: String = "Maintain",
    var dietType: String = "Normal",
    var dailyCalorieTarget: Int = 0,
    var proteinTarget: Double = 0.0,
    var carbsTarget: Double = 0.0,
    var fatTarget: Double = 0.0
) : Parcelable
