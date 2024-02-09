# WebClient AWS request signing

Sample code showing how to sign HTTP requests coming to AWS in WebClient.

This code is based on https://github.com/rewolf/blog-hmac-auth-webclient and uses [Aws4Signer](https://sdk.amazonaws.com/java/api/latest/software/amazon/awssdk/auth/signer/Aws4Signer.html) to sign requests.

High-level diagram showing how signing HTTP requests with body works:
![High-level workflow](http://www.plantuml.com/plantuml/proxy?cache=no&fmt=svg&src=https://raw.githubusercontent.com/kkocel/webclient-signed-request-to-aws/main/docs/web-client-signing-sequence.puml)

## Caveats
This sample works only for JSON requests. If you need to sign requests with XML/protobuf content, 
you will need to provide a different implementation of `HttpMessageEncoder` and add it through
[CodecConfigurer](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/http/codec/CodecConfigurer.html).

## How to run

Run the application with and call `/example` endpoint with a GET request.
It will sign the POST request with `ExampleRequest` body and send it to `https://example.com`.

### How to adjust for your project

1. In production, you need to switch [awsCredentialsProvider](https://github.com/kkocel/webclient-signed-request-to-aws/blob/main/src/main/kotlin/com/sample/BeansInitializer.kt#L28) from `AnonymousCredentialsProvider` to 
`DefaultCredentialsProvider` and provide the credentials in the environment. When `AnonymousCredentialsProvider` is used,
 `Aws4Signer` won't addd any headers to the request.
2. In this sample, you can
run `prod` profile to enable `DefaultCredentialsProvider`.

2. Set [AWS Region](https://github.com/kkocel/webclient-signed-request-to-aws/blob/main/src/main/kotlin/com/sample/BeansInitializer.kt#L27).
3. Set [AWS service name](https://github.com/kkocel/webclient-signed-request-to-aws/blob/main/src/main/kotlin/com/sample/BeansInitializer.kt#L25).
4. Set [base URL](https://github.com/kkocel/webclient-signed-request-to-aws/blob/main/src/main/kotlin/com/sample/BeansInitializer.kt#L20).
5. Prepare your own [request body](https://github.com/kkocel/webclient-signed-request-to-aws/blob/main/src/main/kotlin/com/sample/ExampleRouter.kt#L12).
