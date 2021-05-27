package ru.voroby.grpc;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import ru.voroby.grpc.ecommerce.ProductInfoImpl;

import java.io.IOException;

public class ProductInfoServer {
    public static void main(String[] args) throws IOException, InterruptedException {
        Server server = ServerBuilder.forPort(5050)
                .addService(new ProductInfoImpl())
                .build()
                .start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.err.println("Shutting down gRPC server since JVM is " + "shutting down");
            if (server != null) {
                server.shutdown();
            }
            System.err.println("Server shut down");
        }));

        server.awaitTermination();
    }
}
