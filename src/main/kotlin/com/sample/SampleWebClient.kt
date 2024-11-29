package com.sample

import MessageSigningHttpConnector
import com.fasterxml.jackson.databind.ObjectMapper
import io.netty.handler.logging.LogLevel
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.http.codec.ClientCodecConfigurer
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.web.reactive.function.client.ExchangeFunctions
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import reactor.netty.resources.ConnectionProvider
import reactor.netty.resources.ConnectionProvider.DEFAULT_POOL_ACQUIRE_TIMEOUT
import reactor.netty.resources.ConnectionProvider.DEFAULT_POOL_MAX_CONNECTIONS
import reactor.netty.transport.logging.AdvancedByteBufFormat
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider
import software.amazon.awssdk.http.auth.aws.signer.AwsV4HttpSigner
import software.amazon.awssdk.regions.Region
import java.time.Duration.ofMillis
import java.time.Duration.ofSeconds

@Suppress("LongParameterList")
fun webClient(
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
    serviceName: String,
    region: Region,
    awsCredentialsProvider: AwsCredentialsProvider,
): WebClient =
    webClientBuilder
        .clientConnector(
            ReactorClientHttpConnector(
                HttpClient
                    .create(
                        ConnectionProvider
                            .builder(connectionProviderName)
                            .maxIdleTime(ofSeconds(idleTimeoutSeconds))
                            .maxConnections(DEFAULT_POOL_MAX_CONNECTIONS)
                            .pendingAcquireMaxCount(pendingMaxCount)
                            .pendingAcquireTimeout(ofMillis(pendingAcquireTimeout))
                            .build(),
                    ).followRedirect(true)
                    .apply {
                        if (verboseLogging) {
                            wiretap(
                                "reactor.netty.http.client.HttpClient",
                                LogLevel.DEBUG,
                                AdvancedByteBufFormat.TEXTUAL,
                            )
                        }
                    }.responseTimeout(ofSeconds(connectTimeout))
                    .doOnConnected { connection ->
                        connection
                            .addHandlerLast(ReadTimeoutHandler(readTimeout.toInt()))
                            .addHandlerLast(WriteTimeoutHandler(writeTimeout))
                    },
            ),
        ).exchangeFunction(
            ExchangeFunctions.create(
                MessageSigningHttpConnector(),
                ExchangeStrategies
                    .builder()
                    .codecs { clientDefaultCodecsConfigurer: ClientCodecConfigurer ->
                        clientDefaultCodecsConfigurer.defaultCodecs().jackson2JsonEncoder(
                            BodyProvidingJsonEncoder(
                                WebClientAwsSigner(
                                    signer = AwsV4HttpSigner.create(),
                                    awsCredentialsProvider = awsCredentialsProvider,
                                    serviceName = serviceName,
                                    region = region,
                                ),
                            ),
                        )
                        clientDefaultCodecsConfigurer.defaultCodecs().jackson2JsonDecoder(
                            Jackson2JsonDecoder(
                                ObjectMapper(),
                                MediaType.APPLICATION_JSON,
                            ),
                        )
                    }.build(),
            ),
        ).defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .apply {
            if (baseUrl != null) {
                it.baseUrl(baseUrl)
            }
        }.build()

const val AWS_TIMEOUT = 340L

const val PENDING_ACQUISITION_MAX_COUNT = -1
const val READ_TIMEOUT_SECONDS = 30L
const val WRITE_TIMEOUT_SECONDS = 30
