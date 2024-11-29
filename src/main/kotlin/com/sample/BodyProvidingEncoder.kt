package com.sample

import org.springframework.core.io.buffer.DataBuffer
import org.springframework.http.client.reactive.ClientHttpRequest
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.util.context.ContextView

class BodyProvidingEncoder(
    private val signer: WebClientAwsSigner,
) {
    fun encode(encodedUpstream: Flux<DataBuffer>): Flux<DataBuffer> =
        encodedUpstream
            .flatMap { db: DataBuffer ->
                Mono
                    .deferContextual { data: ContextView ->
                        Mono.just(
                            data,
                        )
                    }.map { sc: ContextView ->
                        val clientHttpRequest =
                            sc.get<ClientHttpRequest>(MessageSigningHttpConnector.REQUEST_CONTEXT_KEY)
                        signer.signRequestAndInjectHeader(clientHttpRequest, extractBytes(db))
                        db
                    }
            }

    /**
     * Extracts bytes from the DataBuffer and resets the buffer so that it is ready to be re-read by the regular
     * request sending process.
     * @param data data buffer with encoded data
     * @return copied data as a byte array.
     */
    private fun extractBytes(data: DataBuffer): ByteArray {
        val bytes = ByteArray(data.readableByteCount())
        data.read(bytes)
        data.readPosition(0)
        return bytes
    }
}
