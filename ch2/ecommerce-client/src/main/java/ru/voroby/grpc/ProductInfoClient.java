package ru.voroby.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import ru.voroby.grpc.protos.Product;
import ru.voroby.grpc.protos.ProductID;
import ru.voroby.grpc.protos.ProductInfoGrpc;

public class ProductInfoClient {
    public static void main(String[] args) {
        ManagedChannel managedChannel = ManagedChannelBuilder.forAddress("localhost", 5050).
                usePlaintext().
                build();

        ProductInfoGrpc.ProductInfoBlockingStub stub = ProductInfoGrpc.newBlockingStub(managedChannel);

        ProductID productID = stub.addProduct(Product.newBuilder()
                .setName("Test Product")
                .setDescription("Iphone 25 TurboMax")
                .setPrice(1500.0f).build());
        System.out.println("Product added: ProductID{" + productID.getValue() + "}");

        Product product = stub.getProduct(productID);
        System.out.println("Product received: " + product.toString());
    }
}
