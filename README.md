# WebClient AWS request signing

Sample code showing how to sign HTTP requests coming to AWS in WebClient.

High-level diagram showing how signing HTTP requests with body works:
![your-UML-diagram-name](http://www.plantuml.com/plantuml/proxy?cache=no&src=https://raw.githubusercontent.com/kkocel/webclient-signed-request-to-aws/main/docs/web-client-signing-sequence.puml)

## Caveats
This works only for JSON requests. If you need to sign XML/protobuf requests, 
you will need to provide a different implementation of `HttpMessageEncoder` and add it trough
[CodecConfigurer](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/http/codec/CodecConfigurer.html)

## How to run

Run the application with and call `/example` endpoint with a GET request.
It will sign the POST request with `ExampleRequest` body and send it to `https://example.com`.

In production, you need to switch awsCredentialsProvider from `AnonymousCredentialsProvider` to 
`DefaultCredentialsProvider` and provide the credentials in the environment. In this sample you can
run the sample with `prod` profile.

Also you need to specify AWS Region and AWS service name.
