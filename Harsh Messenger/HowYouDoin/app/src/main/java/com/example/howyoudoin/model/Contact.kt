package com.example.howyoudoin.model

data class Contact(var name: String, val number: String, val id:String) {
    var lastTime: Long = 0L
    var lastText: String = ""

    constructor() : this("name", "number","default")

    override fun toString(): String {
        return "name :$name, number: $number, lastTime: $lastTime, lastText : $lastText"
    }
}
