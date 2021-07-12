package ru.voroby.grpc;

import io.grpc.Server;
import lombok.extern.slf4j.Slf4j;
import ru.voroby.grpc.config.ConfigBeans;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import java.io.IOException;

@Slf4j
@Startup
@Singleton
public class OrderManagementServer {

    @Inject
    private Server server;

    @Inject
    private ConfigBeans config;

    private void start() {
        try {
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        log.info("Server started, listening on " + config.getPort());
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutting down gRPC server since JVM is shutting down.");
            stop();
            log.info("Server shut down");
        }));
    }

    private void blockUntilTermination() {
        if (server != null) {
            try {
                server.awaitTermination();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    @PostConstruct
    public void init() {
        start();
        blockUntilTermination();
    }

}
