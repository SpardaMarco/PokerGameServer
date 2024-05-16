### Secure Channels

As a means of establishing secure communication between both the client and the server, we used SSLSockets. 
SSLSockets are an extension of the Socket class, which is used to create a connection between the client and the server.
They provide an additional layer of security to the connection by using the Secure Sockets Layer (SSL) or Transport Layer Security (TLS) protocols.
The SSLSocket class is used to create a socket that is capable of communicating over a secure connection.
To establish a secure connection between the server and the client, the following steps were taken:
A private key was generated for the server.
```shell
openssl genpkey -algorithm RSA -out server_private_key.pem -pkeyopt rsa_keygen_bits:2048
```
A certificate signing request was then generated for the server, as well as the subsequent self-signed certificate.
```shell
openssl req -new -key server_private_key.pem -out server_certificate_request.csr
openssl x509 -req -days 365 -in server_certificate_request.csr -signkey server_private_key.pem -out server_certificate.pem
```
This certificate was then imported into both the server keystore and the client truststore.
```shell
openssl pkcs12 -export -in server_certificate.pem -inkey server_private_key.pem -out server_keystore.p12
openssl x509 -outform der -in server_certificate.pem -out server_certificate.cer
keytool -importcert -file server_certificate.cer -alias server-cert -keystore client_truststore.jks -storetype JKS
```
A TLS based SSLContext is created for the server and client, using the server keystore and client truststore respectively.
The server and client are then able to establish a secure connection using the SSLSockets.