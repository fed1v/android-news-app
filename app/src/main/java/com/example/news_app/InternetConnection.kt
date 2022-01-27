package com.example.news_app

class InternetConnection {
    companion object{
        fun isConnected(): Boolean {
            val command = "ping -c 1 google.com"
            return try{
                Runtime.getRuntime().exec(command).waitFor() == 0
            } catch(e: Exception){
                false
            }
        }
    }
}