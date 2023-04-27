package com.example.howyoudoin.model

data class User(val number: String, val displayName: String){
    constructor() : this("000000000", "default")
}
