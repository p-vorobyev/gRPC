package ru.voroby.grpc;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import lombok.extern.slf4j.Slf4j;
import ru.voroby.grpc.service.OrderMgtServiceImpl;

import java.io.IOException;

@Slf4j
public class OrderManagementServer {

    private Server server;

    private final int port = 50050;

    private void start() throws IOException {
        server = ServerBuilder.forPort(port)
                .addService(new OrderMgtServiceImpl())
                .build()
                .start();
        log.info("Server started, listening on " + port);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutting down gRPC server since JVM is shutting down.");
            this.stop();
            log.info("Server shut down");
        }));
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
