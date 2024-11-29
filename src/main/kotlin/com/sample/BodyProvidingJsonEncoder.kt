package com.sample

import MessageSigningHttpConnector
import org.reactivestreams.Publisher
import org.springframework.core.ResolvableType
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferFactory
import org.springframework.http.client.reactive.ClientHttpRequest
import org.springframework.http.codec.json.Jackson2JsonEncoder
import org.springframework.util.MimeType
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.util.context.ContextView

/**
 * A Wrapper around the default Jackson2JsonEncoder that captures the serialized body and supplies it to a consumer
 *
 * @author rewolf
 */
class BodyProvidingJsonEncoder(
    private val signer: WebClientAwsSigner,
) : Jackson2JsonEncoder() {
    override fun encode(
        inputStream: Publisher<*>,
        bufferFactory: DataBufferFactory,
        elementType: ResolvableType,
        mimeType: MimeType?,
        hints: MutableMap<String, Any>?,
    ): Flux<DataBuffer> =
        super
            .encode(inputStream, bufferFactory, elementType, mimeType, hints)
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
