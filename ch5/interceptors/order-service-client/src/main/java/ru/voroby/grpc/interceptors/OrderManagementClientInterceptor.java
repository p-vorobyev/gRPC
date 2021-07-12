package ru.voroby.grpc.interceptors;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall;
import io.grpc.MethodDescriptor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OrderManagementClientInterceptor implements ClientInterceptor {
    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method,
                                                               CallOptions callOptions,
                                                               Channel next) {
        final ClientCall<ReqT, RespT> call = next.newCall(method, callOptions);
        return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(next.newCall(method, callOptions)) {
            @Override
            public void sendMessage(ReqT message) {
                log.info("Call Client -> Server, message: \n{}", message);
                super.sendMessage(message);
            }
        };
        /*return new ClientCall<>() {
            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                call.start(responseListener, headers);
            }

            @Override
            public void request(int numMessages) {
                call.request(numMessages);
            }

            @Override
            public void cancel(@Nullable String message, @Nullable Throwable cause) {
                call.cancel(message, cause);
            }

            @Override
            public void halfClose() {
                call.halfClose();
            }

            @Override
            public void sendMessage(ReqT message) {
                log.info("Call Client -> Server, message: \n{}", message);
                call.sendMessage(message);
            }
        };*/
    }
}
