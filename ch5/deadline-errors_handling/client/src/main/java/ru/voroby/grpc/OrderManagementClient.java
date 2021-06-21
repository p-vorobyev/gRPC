package ru.voroby.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status.Code;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import ru.voroby.grpc.protos.Order;
import ru.voroby.grpc.protos.StringValue;
import ru.voroby.grpc.service.OrderManagementServiceImpl;

@Slf4j
public class OrderManagementClient {
    public static void main(String[] args) {
        final ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", 50050)
                .usePlaintext().build();

        var orderManagementService = new OrderManagementServiceImpl(channel);

        Order order = Order.newBuilder()
                .setId("201")
                .addItems("iPhone XS").addItems("Mac Book Pro")
                .setDestination("San Jose, CA")
                .setPrice(2300).build();

        //addOrder
        try {
            final StringValue stringValue = orderManagementService.addOrder(order);
            log.info("addOrder response: {}", stringValue.getValue());
        } catch (StatusRuntimeException e) {
            final Code code = e.getStatus().getCode();
            if (code == Code.DEADLINE_EXCEEDED) {
                log.warn("Deadline exceeded. : {}", e.getMessage());
                //create new stub because previous caught deadline
                orderManagementService = new OrderManagementServiceImpl(channel);
            } else {
                log.warn("Unspecified error.");
            }
        }

        //getOrder
        try {
            final Order orderReceived = orderManagementService.getOrder(StringValue.newBuilder().setValue("106").build());
            log.info("getOrder response: \n{}", orderReceived.toString());
        } catch (StatusRuntimeException e) {
            final Code code = e.getStatus().getCode();
            if (code == Code.NOT_FOUND) {
                log.warn("Order not found. : {}", e.getMessage());
            } else {
                log.warn("Unspecified error.");
            }
        }
    }
}
