package com.example.ahilla

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform