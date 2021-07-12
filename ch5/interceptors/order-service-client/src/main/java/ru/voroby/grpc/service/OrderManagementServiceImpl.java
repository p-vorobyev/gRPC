package ru.voroby.grpc.service;

import io.grpc.CallCredentials;
import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import ru.voroby.grpc.protos.CombinedShipment;
import ru.voroby.grpc.protos.Order;
import ru.voroby.grpc.protos.OrderManagementGrpc;
import ru.voroby.grpc.protos.StringValue;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Slf4j
@Stateless
public class OrderManagementServiceImpl {

    private OrderManagementGrpc.OrderManagementBlockingStub stub;

    private OrderManagementGrpc.OrderManagementStub asyncStub;

    public OrderManagementServiceImpl() {
    }

    @Inject
    public OrderManagementServiceImpl(ManagedChannel channel, CallCredentials callCredentials) {
        this.stub = OrderManagementGrpc.newBlockingStub(channel).withCallCredentials(callCredentials);
        this.asyncStub = OrderManagementGrpc.newStub(channel).withCallCredentials(callCredentials);
    }

    public StringValue addOrder(Order order) {
        return stub.addOrder(order);
    }

    public Order getOrder(StringValue value) {
        return stub.getOrder(value);
    }

    public Iterator<Order> searchOrders(StringValue value) {
        return stub.searchOrders(value);
    }

    public void updateOrders(Order... orders) {
        var latch = new CountDownLatch(1);

        final var updateResponseObserver = new StreamObserver<StringValue>() {

            @Override
            public void onNext(StringValue value) {
                log.info(value.getValue());
            }

            @Override
            public void onError(Throwable t) {
            }

            @Override
            public void onCompleted() {
                log.info("Update orders completed.");
                latch.countDown();
            }

        };

        final StreamObserver<Order> updateRequestObserver = asyncStub.updateOrders(updateResponseObserver);
        Arrays.asList(orders).forEach(updateRequestObserver::onNext);
        //for null check
        updateRequestObserver.onNext(null);
        updateRequestObserver.onCompleted();

        try {
            if (!latch.await(5, TimeUnit.SECONDS)) {
                log.warn("Timeout error during orders update");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void processOrders(String... values) {
        var latch = new CountDownLatch(1);
        final var respObserver = new StreamObserver<CombinedShipment>() {
            @Override
            public void onNext(CombinedShipment value) {
                log.info("Combined Shipment: {}, {}", value.getId(), value.getOrdersListList());
            }

            @Override
            public void onError(Throwable t) {
            }

            @Override
            public void onCompleted() {
                log.info("Process orders completed.");
                latch.countDown();
            }
        };

        final StreamObserver<StringValue> reqObserver = asyncStub.processOrders(respObserver);
        Arrays.asList(values).forEach(value -> reqObserver.onNext(StringValue.newBuilder().setValue(value).build()));
        reqObserver.onCompleted();

        try {
            if (!latch.await(5, TimeUnit.SECONDS)) {
                log.warn("Timeout error during process orders");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
