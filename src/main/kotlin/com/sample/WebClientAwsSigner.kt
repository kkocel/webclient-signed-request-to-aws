package com.sample

import org.springframework.util.MultiValueMapAdapter
import org.springframework.web.reactive.function.client.ClientRequest
import reactor.core.publisher.Mono
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider
import software.amazon.awssdk.auth.signer.Aws4Signer
import software.amazon.awssdk.auth.signer.AwsSignerExecutionAttribute
import software.amazon.awssdk.core.interceptor.ExecutionAttributes
import software.amazon.awssdk.http.SdkHttpFullRequest
import software.amazon.awssdk.http.SdkHttpMethod
import software.amazon.awssdk.regions.Region
import java.util.function.Function

class WebClientAwsSigner(
    private val body: String? = null,
    private val signer: Aws4Signer,
    private val awsCredentialsProvider: AwsCredentialsProvider,
    private val serviceName: String,
    private val region: Region,
) : Function<ClientRequest, Mono<ClientRequest>> {

    override fun apply(request: ClientRequest): Mono<ClientRequest> {
        val requestBuilder = SdkHttpFullRequest.builder()
            .method(SdkHttpMethod.fromValue(request.method().name()))
            .uri(request.url())
            .apply {
                if (body != null) {
                    contentStreamProvider { body.byteInputStream() }
                }
            }
            .headers(request.headers())

        val attributes = ExecutionAttributes()
        attributes.putAttribute(
            AwsSignerExecutionAttribute.AWS_CREDENTIALS,
            awsCredentialsProvider.resolveCredentials(),
        )
        attributes.putAttribute(AwsSignerExecutionAttribute.SERVICE_SIGNING_NAME, serviceName)
        attributes.putAttribute(AwsSignerExecutionAttribute.SIGNING_REGION, region)

        val signedAwsRequest = signer.sign(requestBuilder.build(), attributes)
        val signedClientRequest =
            ClientRequest.from(request)
                .headers {
                    it.clear()
                    it.addAll(MultiValueMapAdapter(signedAwsRequest.headers()))
                }
                .build()

        return Mono.just(signedClientRequest)
    }
}
