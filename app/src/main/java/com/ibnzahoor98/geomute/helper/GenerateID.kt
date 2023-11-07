package com.ibnzahoor98.geomute.helper

import java.util.Locale
import java.util.UUID


class GenerateID {
    companion object{
        @Throws(Exception::class)
        fun create(): String? {
            return UUID.randomUUID().toString().replace("-".toRegex(), "")
                .uppercase(Locale.getDefault())
        }
    }

}