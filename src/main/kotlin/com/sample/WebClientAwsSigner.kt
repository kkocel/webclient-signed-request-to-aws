package com.sample

import org.springframework.http.client.reactive.ClientHttpRequest
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider
import software.amazon.awssdk.auth.signer.Aws4Signer
import software.amazon.awssdk.auth.signer.params.Aws4SignerParams
import software.amazon.awssdk.http.SdkHttpFullRequest
import software.amazon.awssdk.http.SdkHttpMethod
import software.amazon.awssdk.regions.Region

class WebClientAwsSigner(
    private val signer: Aws4Signer,
    private val awsCredentialsProvider: AwsCredentialsProvider,
    private val serviceName: String,
    private val region: Region,
) {
    fun signRequestAndInjectHeader(
        request: ClientHttpRequest,
        body: ByteArray,
    ) {
        val requestBuilder =
            SdkHttpFullRequest.builder()
                .method(SdkHttpMethod.fromValue(request.method.name()))
                .uri(request.uri)
                .apply {
                    contentStreamProvider { body.inputStream() }
                }
                .headers(request.headers)

        val attributes =
            Aws4SignerParams.builder()
                .awsCredentials(awsCredentialsProvider.resolveCredentials())
                .signingName(serviceName)
                .signingRegion(region)
                .build()

        val signedAwsRequest = signer.sign(requestBuilder.build(), attributes)

        val headers = request.headers
        signedAwsRequest.headers().forEach { (name, value) ->
            headers[name] = value
        }
    }
}
