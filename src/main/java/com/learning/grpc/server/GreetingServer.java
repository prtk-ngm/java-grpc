package com.learning.grpc.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import java.io.File;
import java.io.IOException;

public class GreetingServer {

    public static void main(String[] args) throws IOException, InterruptedException {
       System.out.println("Hello gRPC");

       /*plaintext server
       Server server = ServerBuilder.forPort(50015).addService(new GreetServiceImpl())
               .build();*/


        GreetServiceImpl service = new GreetServiceImpl();
        service.setWorker(new Worker());
        Server server = ServerBuilder.forPort(50051)
                .addService(service)
                .useTransportSecurity(new File("ssl/server.crt"),
                        new File("ssl/server.pem"))
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
