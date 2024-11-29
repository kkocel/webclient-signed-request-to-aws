package com.sample

import org.springframework.http.client.reactive.ClientHttpRequest
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider
import software.amazon.awssdk.http.SdkHttpFullRequest
import software.amazon.awssdk.http.SdkHttpMethod
import software.amazon.awssdk.http.auth.aws.signer.AwsV4HttpSigner
import software.amazon.awssdk.regions.Region

class WebClientAwsSigner(
    private val signer: AwsV4HttpSigner,
    private val awsCredentialsProvider: AwsCredentialsProvider,
    private val serviceName: String,
    private val region: Region,
) {
    fun signRequestAndInjectHeader(
        request: ClientHttpRequest,
        body: ByteArray,
    ) {
        val httpAwsRequest =
            SdkHttpFullRequest
                .builder()
                .method(SdkHttpMethod.fromValue(request.method.name()))
                .uri(request.uri)
                .apply {
                    contentStreamProvider { body.inputStream() }
                }.headers(request.headers)
                .build()

        val signedAwsRequest =
            signer.sign { builder ->
                builder
                    .request(httpAwsRequest)
                    .putProperty(AwsV4HttpSigner.REGION_NAME, region.id())
                    .putProperty(AwsV4HttpSigner.SERVICE_SIGNING_NAME, serviceName)
                    .identity(awsCredentialsProvider.resolveCredentials())
            }

        val headers = request.headers
        signedAwsRequest.request().headers().forEach { (name, value) ->
            headers[name] = value
        }
    }
}
