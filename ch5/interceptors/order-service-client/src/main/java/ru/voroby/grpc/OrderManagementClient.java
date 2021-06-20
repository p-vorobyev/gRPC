package ru.voroby.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import ru.voroby.grpc.protos.Order;
import ru.voroby.grpc.protos.OrderManagementGrpc;
import ru.voroby.grpc.protos.StringValue;

import java.util.Iterator;

@Slf4j
public class OrderManagementClient {
    public static void main(String[] args) {
        final ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", 50050)
                .usePlaintext().build();

        final OrderManagementGrpc.OrderManagementBlockingStub stub = OrderManagementGrpc.newBlockingStub(channel);
        final OrderManagementGrpc.OrderManagementStub asyncStub = OrderManagementGrpc.newStub(channel);

        Order order = Order.newBuilder()
                .setId("201")
                .addItems("iPhone XS").addItems("Mac Book Pro")
                .setDestination("San Jose, CA")
                .setPrice(2300).build();

        //addOrder
        final StringValue stringValue = stub.addOrder(order);
        log.info("addOrder response: {}", stringValue.getValue());

        //getOrder
        final Order orderReceived = stub.getOrder(stringValue);
        log.info("getOrder response: \n{}", orderReceived.toString());

        //searchOrders with stream results
        final StringValue searchValue = StringValue.newBuilder().setValue("Google").build();
        final Iterator<Order> searchOrders = stub.searchOrders(searchValue);
        while (searchOrders.hasNext()) {
            final Order o = searchOrders.next();
            log.info("searchOrders response match: {}, \n {}", o.getId(), o.toString());
        }
    }
}
