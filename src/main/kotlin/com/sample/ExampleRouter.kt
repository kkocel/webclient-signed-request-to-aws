package com.sample

import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import org.springframework.web.reactive.function.server.body
import org.springframework.web.reactive.function.server.router

class ExampleRouter(private val webClient: WebClient) {
    fun routes() =
        router {
            GET("/example") {
                val exampleRequest = ExampleRequest("bar")
                ok().body(
                    webClient.post().bodyValue(exampleRequest).retrieve().bodyToMono<String>(),
                )
            }
        }
}
