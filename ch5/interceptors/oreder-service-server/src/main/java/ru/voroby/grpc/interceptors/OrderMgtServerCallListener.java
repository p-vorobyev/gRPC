package ru.voroby.grpc.interceptors;

import io.grpc.ForwardingServerCallListener;
import io.grpc.ServerCall;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OrderMgtServerCallListener<R> extends ForwardingServerCallListener<R> {

    private final ServerCall.Listener<R> delegate;

    OrderMgtServerCallListener(ServerCall.Listener<R> delegate) {
        this.delegate = delegate;
    }

    @Override
    public void onMessage(R message) {
        log.info("Message Received from Client -> Service " + message);
        super.onMessage(message);
    }

    @Override
    protected ServerCall.Listener<R> delegate() {
        return delegate;
    }
}
