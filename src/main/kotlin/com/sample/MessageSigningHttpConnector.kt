import org.springframework.http.HttpMethod
import org.springframework.http.client.reactive.ClientHttpRequest
import org.springframework.http.client.reactive.ClientHttpResponse
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import reactor.core.publisher.Mono
import reactor.util.context.Context
import java.net.URI
import java.util.function.Function

class MessageSigningHttpConnector : ReactorClientHttpConnector() {
    override fun connect(
        method: HttpMethod,
        uri: URI,
        requestCallback: Function<in ClientHttpRequest, Mono<Void>>,
    ): Mono<ClientHttpResponse> =
        super.connect(
            method,
            uri,
        ) { incomingRequest: ClientHttpRequest ->
            requestCallback
                .apply(incomingRequest)
                .contextWrite(
                    Context.of(
                        REQUEST_CONTEXT_KEY,
                        incomingRequest,
                    ),
                )
        }

    companion object {
        const val REQUEST_CONTEXT_KEY = "REQUEST_CONTEXT_KEY"
    }
}
