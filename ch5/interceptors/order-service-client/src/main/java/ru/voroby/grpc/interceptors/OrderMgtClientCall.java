package ru.voroby.grpc.interceptors;

import io.grpc.ClientCall;
import io.grpc.Metadata;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;

@Slf4j
public class OrderMgtClientCall<ReqT, RespT> extends ClientCall<ReqT, RespT> {

    @Override
    public void start(Listener<RespT> responseListener, Metadata headers) {

    }

    @Override
    public void request(int numMessages) {

    }

    @Override
    public void cancel(@Nullable String message, @Nullable Throwable cause) {

    }

    @Override
    public void halfClose() {

    }

    @Override
    public void sendMessage(ReqT message) {
        log.info("Call Client -> Server, message: \n{}", message);
    }
}
