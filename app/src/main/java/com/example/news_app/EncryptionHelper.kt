package com.example.news_app

import com.google.common.hash.Hashing
import java.nio.charset.Charset
import java.security.MessageDigest

class EncryptionHelper {
    companion object{
        fun getSHA1(string: String): String{
            val hash = Hashing.sha1().hashString(string, Charset.defaultCharset()).toString()
            return hash
        }

        fun getSHA256(string: String): String {
            val bytes = string.toByteArray()
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(bytes)
            return digest.fold("", { str, it -> str + "%02x".format(it) })
        }
    }
}