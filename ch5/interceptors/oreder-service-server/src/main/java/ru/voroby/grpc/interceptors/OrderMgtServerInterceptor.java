package ru.voroby.grpc.interceptors;

import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OrderMgtServerInterceptor implements ServerInterceptor {

    private String TOKEN = "secret-token";

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> serverCall,
                                                                 Metadata metadata,
                                                                 ServerCallHandler<ReqT, RespT> serverCallHandler) {
        log.info("======= [Server Interceptor] : Remote Method Invoked - {}",  serverCall.getMethodDescriptor().getFullMethodName());
        String authorization = metadata.get(Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER));

        if (authorization == null) {
            serverCall.close(Status.UNAUTHENTICATED.withDescription("Token is missed in metadata."), metadata);
            return new ServerCall.Listener<>() {};
        }

        if (validToken(authorization)) {
            ServerCall<ReqT, RespT> call = new OrderMgtServerCall<>(serverCall);
            return new OrderMgtServerCallListener<>(serverCallHandler.startCall(call, metadata));
        } else {
            log.warn("Failed authentication!");
            serverCall.close(Status.UNAUTHENTICATED.withDescription("Invalid token."), metadata);
            return new ServerCall.Listener<>() {};
        }
    }

    private boolean validToken(String authorization) {
        String bearer = authorization.substring("Bearer ".length()).trim();
        return bearer.equals(TOKEN);
    }
}
