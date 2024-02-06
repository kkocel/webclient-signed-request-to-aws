package com.sample

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SignedRequestToAwsApplication

fun main(args: Array<String>) {
    runApplication<SignedRequestToAwsApplication>(init = { addInitializers(BeansInitializer()) }, args = args)
}
