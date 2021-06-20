package ru.voroby.grpc.service;

import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import ru.voroby.grpc.protos.CombinedShipment;
import ru.voroby.grpc.protos.Order;
import ru.voroby.grpc.protos.OrderManagementGrpc.OrderManagementImplBase;
import ru.voroby.grpc.protos.StringValue;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class OrderMgtServiceImpl extends OrderManagementImplBase {

    private final Order ord1 = Order.newBuilder()
            .setId("102")
            .addItems("Google Pixel 3A").addItems("Mac Book Pro")
            .setDestination("Mountain View, CA")
            .setPrice(1800)
            .build();

    private final Order ord2 = Order.newBuilder()
            .setId("103")
            .addItems("Apple Watch S4")
            .setDestination("San Jose, CA")
            .setPrice(400)
            .build();

    private final Order ord3 = Order.newBuilder()
            .setId("104")
            .addItems("Google Home Mini").addItems("Google Nest Hub")
            .setDestination("Mountain View, CA")
            .setPrice(400)
            .build();

    private final Order ord4 = Order.newBuilder()
            .setId("105")
            .addItems("Amazon Echo")
            .setDestination("San Jose, CA")
            .setPrice(30)
            .build();

    private final Map<String, Order> orderMap = Stream.of(ord1, ord2, ord3, ord4).collect(Collectors.toMap(Order::getId, order -> order));

    private final Map<String, CombinedShipment> combinedShipmentMap = new HashMap<>();

    private static final int BATCH_SIZE = 3;

    //unary
    @Override
    public void addOrder(Order request, StreamObserver<StringValue> responseObserver) {
        final String id = Optional.of(request.getId()).orElse(UUID.randomUUID().toString());
        log.info("Order added: [id: {}].", id);
        orderMap.put(id, request);
        responseObserver.onNext(StringValue.newBuilder().setValue(id).build());
        responseObserver.onCompleted();
    }

    //unary
    @Override
    public void getOrder(StringValue request, StreamObserver<Order> responseObserver) {
        final String id = request.getValue();
        if (orderMap.containsKey(id)) {
            responseObserver.onNext(orderMap.get(id));
            responseObserver.onCompleted();
            log.info("Order retrieved: [id: {}].", id);
        } else {
            responseObserver.onCompleted();
            log.info("Order [id: {}] - not found.", id);
        }
    }

    // server streaming
    @Override
    public void searchOrders(StringValue request, StreamObserver<Order> responseObserver) {
        final String searchValue = request.getValue();
        orderMap.values().forEach(order -> {
            for (int i = 0; i < order.getItemsCount(); i++) {
                final String item = order.getItems(i);
                if (item.contains(searchValue)) {
                    responseObserver.onNext(order);
                    log.info("Item found: [{}]", item);
                    break;
                }
            }
        });
        responseObserver.onCompleted();
    }

    // client streaming
    @Override
    public StreamObserver<Order> updateOrders(StreamObserver<StringValue> responseObserver) {
        return new StreamObserver<>() {
            private final StringBuilder builder = new StringBuilder().append("Updated IDs: ");

            @Override
            public void onNext(Order value) {
                Optional.of(value).map(order -> {
                    orderMap.put(order.getId(), order);
                    log.info("Order updated: [id: {}].", order.getId());
                    builder.append(order.getId()).append(",");
                    return null;
                }).orElseGet(() -> {
                    log.warn("Null value received for update.");
                    return null;
                });
            }

            @Override
            public void onError(Throwable t) {}

            @Override
            public void onCompleted() {
                final String result = builder.toString();
                final StringValue value = StringValue.newBuilder().setValue(result.substring(0, result.length() - 1)).build();
                responseObserver.onNext(value);
                responseObserver.onCompleted();
            }
        };
    }

    // bi-di streaming
    @Override
    public StreamObserver<StringValue> processOrders(StreamObserver<CombinedShipment> responseObserver) {
        return new StreamObserver<>() {
            private int count = 0;

            @Override
            public void onNext(StringValue value) {
                final Order order = orderMap.get(value.getValue());
                if (order == null) {
                    log.warn("Order not found: [id: {}]", value.getValue());
                    return;
                }

                count++;
                final String destination = order.getDestination();
                CombinedShipment existingShipment = combinedShipmentMap.get(destination);
                if (existingShipment != null) {
                    existingShipment = CombinedShipment.newBuilder(existingShipment).addOrdersList(order).build();
                    combinedShipmentMap.put(destination, existingShipment);
                } else {
                    final CombinedShipment shipment = CombinedShipment.newBuilder()
                            .addOrdersList(order)
                            .setId(UUID.randomUUID() + ":" + destination)
                            .setStatus("Processed")
                            .build();
                    combinedShipmentMap.put(destination, shipment);
                }

                if (count == BATCH_SIZE) {
                    count = 0;
                    combinedShipmentMap.values().forEach(responseObserver::onNext);
                    combinedShipmentMap.clear();
                }
            }

            @Override
            public void onError(Throwable t) {}

            @Override
            public void onCompleted() {
                combinedShipmentMap.values().forEach(responseObserver::onNext);
                responseObserver.onCompleted();
                combinedShipmentMap.clear();
            }
        };
    }
}
