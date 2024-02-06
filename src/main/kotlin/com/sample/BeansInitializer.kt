package com.sample

import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.support.GenericApplicationContext
import org.springframework.context.support.beans
import org.springframework.web.reactive.function.client.WebClient
import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.regions.Region

internal class BeansInitializer : ApplicationContextInitializer<GenericApplicationContext> {
    override fun initialize(context: GenericApplicationContext) {
        beans {
            bean {
                ExampleRouter(
                    webClient(
                        webClientBuilder = ref<WebClient.Builder>(),
                        verboseLogging = false,
                        baseUrl = "https://example.com",
                        connectionProviderName = "aws-service-signed-client",
                        pendingMaxCount = PENDING_ACQUISITION_MAX_COUNT,
                        readTimeout = READ_TIMEOUT_SECONDS,
                        writeTimeout = WRITE_TIMEOUT_SECONDS,
                        serviceName = "es",
                        // Elastic Search / Open Search
                        region = Region.US_EAST_1,
                        awsCredentialsProvider = ref<AwsCredentialsProvider>(),
                    ),
                ).routes()
            }

            environment(
                { activeProfiles.contains("prod") },
                {
                    bean {
                        DefaultCredentialsProvider.create()
                    }
                },
            )

            environment(
                { !activeProfiles.contains("prod") },
                {
                    bean {
                        AnonymousCredentialsProvider.create()
                    }
                },
            )
        }.initialize(context)
    }
}
