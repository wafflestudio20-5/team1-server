package com.wafflytime

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@SpringBootApplication
@EnableJpaAuditing
class WafflyTimeApplication

fun main(args: Array<String>) {
    runApplication<WafflyTimeApplication>(*args)
}