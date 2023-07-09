package com.joelkanyi.shared.di

import com.joelkanyi.shared.data.local.createDatabase
import org.koin.dsl.module

fun dataModule() = module {
    single { createDatabase(get()) }
}