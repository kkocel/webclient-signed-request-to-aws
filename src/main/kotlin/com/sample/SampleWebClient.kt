package com.sample

import io.netty.handler.logging.LogLevel
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import reactor.netty.resources.ConnectionProvider.DEFAULT_POOL_ACQUIRE_TIMEOUT
import reactor.netty.resources.ConnectionProvider.DEFAULT_POOL_MAX_CONNECTIONS
import reactor.netty.resources.ConnectionProvider.builder
import reactor.netty.transport.logging.AdvancedByteBufFormat
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider
import software.amazon.awssdk.auth.signer.Aws4Signer
import software.amazon.awssdk.regions.Region
import java.time.Duration.ofMillis
import java.time.Duration.ofSeconds

class SampleWebClient(
    private val awsCredentialsProvider: AwsCredentialsProvider,
    baseUrl: String,
    webClientBuilder: WebClient.Builder,
) {

    private val webclient =
        webClient(
            webClientBuilder = webClientBuilder,
            verboseLogging = false,
            baseUrl = baseUrl,
            connectionProviderName = "aws-service-signed-client",
            pendingMaxCount = PENDING_ACQUISITION_MAX_COUNT,
            readTimeout = READ_TIMEOUT_SECONDS,
            writeTimeout = WRITE_TIMEOUT_SECONDS,
        )

    private fun signedAwsWebClient(body: String? = null): WebClient = webclient
        .mutate().filter(
            ExchangeFilterFunction.ofRequestProcessor(
                WebClientAwsSigner(body, Aws4Signer.create(), awsCredentialsProvider, SERVICE_NAME, REGION),
            ),
        )
        .build()

    @Suppress("LongParameterList")
    private fun webClient(
        webClientBuilder: WebClient.Builder,
        verboseLogging: Boolean,
        baseUrl: String? = null,
        connectionProviderName: String,
        pendingMaxCount: Int,
        connectTimeout: Long = 5,
        readTimeout: Long = 5,
        writeTimeout: Int = 5,
        pendingAcquireTimeout: Long = DEFAULT_POOL_ACQUIRE_TIMEOUT,
        idleTimeoutSeconds: Long = AWS_TIMEOUT,
    ): WebClient = webClientBuilder.build().mutate().clientConnector(
        ReactorClientHttpConnector(
            HttpClient.create(
                builder(connectionProviderName)
                    .maxIdleTime(ofSeconds(idleTimeoutSeconds))
                    .maxConnections(DEFAULT_POOL_MAX_CONNECTIONS)
                    .pendingAcquireMaxCount(pendingMaxCount)
                    .pendingAcquireTimeout(ofMillis(pendingAcquireTimeout))
                    .metrics(true)
                    .build(),
            ).followRedirect(true).apply {
                if (verboseLogging) {
                    wiretap(
                        "reactor.netty.http.client.HttpClient",
                        LogLevel.DEBUG,
                        AdvancedByteBufFormat.TEXTUAL,
                    )
                }
            }.responseTimeout(ofSeconds(connectTimeout))
                .doOnConnected { connection ->
                    connection.addHandlerLast(ReadTimeoutHandler(readTimeout.toInt()))
                        .addHandlerLast(WriteTimeoutHandler(writeTimeout))
                },
        ),
    )
        .apply {
            if (baseUrl != null) {
                it.baseUrl(baseUrl)
            }
        }
        .build()

    companion object {
        const val AWS_TIMEOUT = 340L
        private const val SERVICE_NAME = "es" // elastic search / OpenSearch
        private val REGION = Region.US_EAST_1

        private const val PENDING_ACQUISITION_MAX_COUNT = -1
        private const val READ_TIMEOUT_SECONDS = 30L
        private const val WRITE_TIMEOUT_SECONDS = 30
    }
}
