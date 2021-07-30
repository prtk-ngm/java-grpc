package com.learning.grpc.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.protobuf.services.ProtoReflectionService;
import java.io.IOException;
public class CalcServer {
    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Hello calc service");
        Server server = ServerBuilder.forPort(50052).addService(new AddServiceImpl())
                .addService(ProtoReflectionService.newInstance())
                .build();
        server.start();
        Runtime.getRuntime().addShutdownHook(new Thread( ()->{
            System.out.println("Received server shutdown request");
            server.shutdown();
            System.out.println("Server stopped successfully");
        }));
        server.awaitTermination();
    }
}
