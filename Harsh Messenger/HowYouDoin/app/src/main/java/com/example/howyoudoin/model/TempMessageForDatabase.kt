package com.example.howyoudoin.model

class TempMessageForDatabase(var chatId: String) {
    var time: Long = 0L
    var displayTime: String = ""
    var sentBy: String = ""
    var message: String = ""
    var mimeType: String = "text/plain"
    var imageByteArray: List<Int>? = null

    constructor() : this("")//for firebase
    companion object{
        fun convert(msg: Message): TempMessageForDatabase {
            return TempMessageForDatabase(msg.chatId).apply {
                this.message=msg.message
                this.time=msg.time
                this.displayTime=msg.displayTime
                this.sentBy=msg.sentBy
                this.mimeType=msg.mimeType
            }
        }
    }

}