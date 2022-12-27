package com.wafflytime

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication


@SpringBootApplication
class WafflyTimeApplication

fun main(args: Array<String>) {
    runApplication<WafflyTimeApplication>(*args)
}