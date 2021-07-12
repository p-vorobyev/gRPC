package ru.voroby.grpc.config;

import io.grpc.Server;
import io.grpc.ServerInterceptors;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyServerBuilder;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.SslContext;
import lombok.Getter;
import ru.voroby.grpc.interceptors.OrderMgtServerInterceptor;
import ru.voroby.grpc.service.OrderMgtServiceImpl;

import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.net.ssl.SSLException;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

@Getter
@Singleton
public class ConfigBeans {

    private final int port = 50050;

    @Produces
    @ApplicationScoped
    public Server server() throws IOException {
        return NettyServerBuilder.forPort(port)
                .sslContext(sslContext())
                .addService(ServerInterceptors.intercept(new OrderMgtServiceImpl(), new OrderMgtServerInterceptor()))
                .build();
    }

    private SslContext sslContext() throws SSLException {
        var crt = new File(getFileString("server.crt"));
        var key = new File(getFileString("serverKey.pem"));
        var root = new File(getFileString("myRoot.crt"));

        return GrpcSslContexts
                .forServer(crt, key)
                .trustManager(root)
                .clientAuth(ClientAuth.REQUIRE).build();
    }

    private String getFileString(String resource) {
        return Objects.requireNonNull(getClass().getClassLoader().getResource(resource)).getFile();
    }
}
