package ru.voroby.grpc;

import io.grpc.Server;
import io.grpc.ServerInterceptors;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyServerBuilder;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.SslContext;
import lombok.extern.slf4j.Slf4j;
import ru.voroby.grpc.interceptors.OrderMgtServerInterceptor;
import ru.voroby.grpc.service.OrderMgtServiceImpl;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

@Slf4j
public class OrderManagementServer {

    private Server server;

    private final int port = 50050;

    private void start() throws IOException {
        var crt = new File(getFileString("server.crt"));
        var key = new File(getFileString("serverKey.pem"));
        var root = new File(getFileString("myRoot.crt"));
        final SslContext sslContext = GrpcSslContexts
                .forServer(crt, key)
                .trustManager(root)
                .clientAuth(ClientAuth.REQUIRE).build();
        server = NettyServerBuilder.forPort(port)
                .sslContext(sslContext)
                .addService(ServerInterceptors.intercept(new OrderMgtServiceImpl(), new OrderMgtServerInterceptor()))
                .build()
                .start();
        /*server = ServerBuilder.forPort(port)
                .addService(ServerInterceptors.intercept(new OrderMgtServiceImpl(), new OrderMgtServerInterceptor()))
                .build()
                .start();*/
        log.info("Server started, listening on " + port);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutting down gRPC server since JVM is shutting down.");
            this.stop();
            log.info("Server shut down");
        }));
    }

    private String getFileString(String resource) {
        return Objects.requireNonNull(getClass().getClassLoader().getResource(resource)).getFile();
    }

    private void blockUntilTermination() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    private void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        final OrderManagementServer server = new OrderManagementServer();
        server.start();
        server.blockUntilTermination();
    }

}
