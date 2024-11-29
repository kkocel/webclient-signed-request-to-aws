package com.sample

import org.reactivestreams.Publisher
import org.springframework.core.ResolvableType
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferFactory
import org.springframework.http.codec.json.Jackson2JsonEncoder
import org.springframework.util.MimeType
import reactor.core.publisher.Flux

/**
 * A Wrapper around the default Jackson2JsonEncoder that captures the serialized body and supplies it to a consumer
 *
 * @author rewolf
 */
class BodyProvidingJsonEncoder(
    signer: WebClientAwsSigner,
) : Jackson2JsonEncoder() {
    private val encoder = BodyProvidingEncoder(signer)

    override fun encode(
        inputStream: Publisher<*>,
        bufferFactory: DataBufferFactory,
        elementType: ResolvableType,
        mimeType: MimeType?,
        hints: MutableMap<String, Any>?,
    ): Flux<DataBuffer> = encoder.encode(super.encode(inputStream, bufferFactory, elementType, mimeType, hints))
}
