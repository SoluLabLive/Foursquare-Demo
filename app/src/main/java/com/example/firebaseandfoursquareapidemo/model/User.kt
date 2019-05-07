package com.example.firebaseandfoursquareapidemo.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class User(
    var id: String? = "",
    var email: String? = "",
    var firstName: String? = "",
    var lastName: String? = ""
)