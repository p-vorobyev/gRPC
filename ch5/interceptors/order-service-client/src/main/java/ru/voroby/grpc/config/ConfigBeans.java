package ru.voroby.grpc.config;

import io.grpc.CallCredentials;
import io.grpc.ManagedChannel;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyChannelBuilder;
import io.netty.handler.ssl.SslContext;
import ru.voroby.grpc.OrderManagementClient;
import ru.voroby.grpc.interceptors.TokenCallAuth;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.net.ssl.SSLException;
import java.io.File;
import java.util.Objects;

public class ConfigBeans {

    private final String token = "secret-token";

    private SslContext sslContext() throws SSLException {
        var crt = new File(getFileString("client.crt"));
        var key = new File(getFileString("clientKey.pem"));
        var root = new File(getFileString("myRoot.crt"));

        return GrpcSslContexts.forClient()
                .keyManager(crt, key)
                .trustManager(root).build();
    }

    @Produces
    @ApplicationScoped
    public ManagedChannel managedChannel() throws SSLException {
        return NettyChannelBuilder
                .forAddress("localhost", 50050)
                .sslContext(sslContext()).build();
    }

    @Produces
    @ApplicationScoped
    public CallCredentials tokenCallAuth() {
        return new TokenCallAuth(token);
    }

    private static String getFileString(String resource) {
        return Objects.requireNonNull(OrderManagementClient.class.getClassLoader().getResource(resource)).getFile();
    }
}
