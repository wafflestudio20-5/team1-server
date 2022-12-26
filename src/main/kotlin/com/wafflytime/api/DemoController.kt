package com.wafflytime.api

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class DemoController {
    @GetMapping("/")
    fun helloWorld() = "Hello World"
}