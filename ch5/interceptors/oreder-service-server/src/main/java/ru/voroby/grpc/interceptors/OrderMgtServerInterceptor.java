package ru.voroby.grpc.interceptors;

import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OrderMgtServerInterceptor implements ServerInterceptor {
    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> serverCall,
                                                                 Metadata metadata,
                                                                 ServerCallHandler<ReqT, RespT> serverCallHandler) {
        log.info("======= [Server Interceptor] : Remote Method Invoked - {}",  serverCall.getMethodDescriptor().getFullMethodName());
        ServerCall<ReqT, RespT> call = new OrderMgtServerCall<>(serverCall);

        return new OrderMgtServerCallListener<>(serverCallHandler.startCall(call, metadata));
    }
}
