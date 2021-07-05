package ru.voroby.grpc;

import lombok.extern.slf4j.Slf4j;
import ru.voroby.grpc.protos.Order;
import ru.voroby.grpc.protos.StringValue;
import ru.voroby.grpc.service.OrderManagementServiceImpl;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import javax.net.ssl.SSLException;
import java.util.Iterator;
import java.util.Objects;

@Slf4j
@Startup
@Singleton
public class OrderManagementClient {

    //private static final String token = "secret-token";

    @Inject
    private OrderManagementServiceImpl orderManagementService;

    public StringValue addOrder(Order order) {
        final StringValue stringValue = orderManagementService.addOrder(order);
        log.info("addOrder response: {}", stringValue.getValue());

        return stringValue;
    }

    public void getOrder(StringValue stringValue) {
        final Order orderReceived = orderManagementService.getOrder(stringValue);
        log.info("getOrder response: \n{}", orderReceived.toString());
    }

    public Iterator<Order> searchOrders(StringValue stringValue) {
        return orderManagementService.searchOrders(stringValue);
    }

    public void updateOrders() {
        updateOrders(orderManagementService);
    }

    private void processOrders(String... ord) {
        orderManagementService.processOrders(ord);
    }

    @PostConstruct
    public void start() {
        Order order = Order.newBuilder()
                .setId("201")
                .addItems("iPhone XS").addItems("Mac Book Pro")
                .setDestination("San Jose, CA")
                .setPrice(2300).build();
        StringValue stringValue = addOrder(order);

        getOrder(stringValue);

        //searchOrders with stream results
        final StringValue searchValue = StringValue.newBuilder().setValue("Google").build();
        final Iterator<Order> searchOrders = searchOrders(searchValue);
        while (searchOrders.hasNext()) {
            final Order o = searchOrders.next();
            log.info("searchOrders response match: {}, \n {}", o.getId(), o);
        }

        //client streaming for updateOrders
        updateOrders();

        //bi-di streaming for processOrders
        processOrders("102", "103", "104", "105");
    }

    /*public static void main(String[] args) throws SSLException {
        *//*var crt = new File(getFileString("client.crt"));
        var key = new File(getFileString("clientKey.pem"));
        var root = new File(getFileString("myRoot.crt"));
        final SslContext sslContext = GrpcSslContexts.forClient()
                .keyManager(crt, key)
                .trustManager(root).build();
        final ManagedChannel channel = NettyChannelBuilder
                .forAddress("localhost", 50050)
                .sslContext(sslContext).build();*//*
        *//*final ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", 50050)
                .intercept(new OrderManagementClientInterceptor())
                .usePlaintext().build();*//*

        //final var orderManagementService = new OrderManagementServiceImpl(channel, new TokenCallAuth(token));

        var orderClient = new OrderManagementClient();
        Order order = Order.newBuilder()
                .setId("201")
                .addItems("iPhone XS").addItems("Mac Book Pro")
                .setDestination("San Jose, CA")
                .setPrice(2300).build();

        //addOrder
        *//*final StringValue stringValue = orderManagementService.addOrder(order);
        log.info("addOrder response: {}", stringValue.getValue());*//*
        StringValue stringValue = orderClient.addOrder(order);

        //getOrder
        *//*final Order orderReceived = orderManagementService.getOrder(stringValue);
        log.info("getOrder response: \n{}", orderReceived.toString());*//*
        orderClient.getOrder(stringValue);

        //searchOrders with stream results
        final StringValue searchValue = StringValue.newBuilder().setValue("Google").build();
        //final Iterator<Order> searchOrders = orderManagementService.searchOrders(searchValue);
        final Iterator<Order> searchOrders = orderClient.searchOrders(searchValue);
        while (searchOrders.hasNext()) {
            final Order o = searchOrders.next();
            log.info("searchOrders response match: {}, \n {}", o.getId(), o);
        }

        //client streaming for updateOrders
        //updateOrders(orderManagementService);
        orderClient.updateOrders();

        //bi-di streaming for processOrders
        //orderManagementService.processOrders("102", "103", "104", "105");
        orderClient.processOrders("102", "103", "104", "105");
    }*/

    /*private static String getFileString(String resource) {
        return Objects.requireNonNull(OrderManagementClient.class.getClassLoader().getResource(resource)).getFile();
    }*/

    private static void updateOrders(OrderManagementServiceImpl service) {
        var ord = Order.newBuilder()
                .setId("103")
                .addItems("Apple Watch S4")
                .setDestination("San Jose, CA")
                .setPrice(500)
                .build();
        var ord1 = Order.newBuilder()
                .setId("104")
                .addItems("Google Home Mini").addItems("Google Nest Hub")
                .setDestination("Mountain View, CA")
                .setPrice(600)
                .build();
        var ord2 = Order.newBuilder()
                .setId("105")
                .addItems("Amazon Echo")
                .setDestination("San Jose, CA")
                .setPrice(50)
                .build();

        service.updateOrders(ord, ord1, ord2);
    }
}
