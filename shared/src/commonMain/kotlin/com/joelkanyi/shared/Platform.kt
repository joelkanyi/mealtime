package com.joelkanyi.shared

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform