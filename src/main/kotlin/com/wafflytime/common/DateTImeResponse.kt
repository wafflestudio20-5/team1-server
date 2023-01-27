package com.wafflytime.common

import java.time.LocalDateTime

data class DateTimeResponse(
    val year: Int,
    val month: Int,
    val day: Int,
    val hour: Int,
    val minute: Int,
) {
    constructor() : this(-1, -1, -1, -1, -1)

    companion object {

        fun of(dateTime: LocalDateTime): DateTimeResponse = dateTime.run {
            DateTimeResponse(
                year,
                monthValue,
                dayOfMonth,
                hour,
                minute
            )
        }
    }
}

