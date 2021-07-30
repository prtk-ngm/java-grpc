package com.learning.grpc.client;

import com.dummy.greet.Greet;
import com.dummy.greet.GreetEveryoneRequest;
import com.dummy.greet.GreetEveryoneResponse;
import com.dummy.greet.GreetManyRequest;
import com.dummy.greet.GreetManyResponse;
import com.dummy.greet.GreetRequest;
import com.dummy.greet.GreetResponse;
import com.dummy.greet.GreetServiceGrpc;
import com.dummy.greet.GreetServiceGrpc.GreetServiceStub;
import com.dummy.greet.GreetWithDeadlineRequest;
import com.dummy.greet.GreetWithDeadlineResponse;
import com.dummy.greet.Greeting;
import com.dummy.greet.Greeting.Builder;
import com.dummy.greet.LongGreetRequest;
import com.dummy.greet.LongGreetResponse;
import com.proto.dummy.DummyServiceGrpc;

import io.grpc.Deadline;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.StreamObserver;
import java.io.File;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLException;

public class GreetingClient {

    public static void main(String[] args) throws SSLException {
        System.out.println("gRPC client");
        GreetingClient client = new GreetingClient();
        client.run();

    }

    private void doUnaryCall(ManagedChannel channel){
        GreetServiceGrpc.GreetServiceBlockingStub  greetClient = GreetServiceGrpc
                .newBlockingStub(channel);

        Greeting greeting = Greeting.newBuilder().setFirstName("Prateek").setLastName("Nigam")
                .build();
        GreetRequest request = GreetRequest.newBuilder().setGreeting(greeting).build();
        GreetResponse response = greetClient.greet(request);

        System.out.println("response.result: " + response.getResult());


    }

    private void doClientStreamingCall(ManagedChannel channel){
        GreetServiceGrpc.GreetServiceStub asyncClient = GreetServiceGrpc.newStub(channel);
        CountDownLatch latch = new CountDownLatch(1);
        StreamObserver<LongGreetRequest> requestStreamObserver = asyncClient
                .longGreet(new StreamObserver<LongGreetResponse>() {
            @Override
            public void onNext(LongGreetResponse value) {
                System.out.println("Received response from a server");
                System.out.println(value.getResult());
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {
                System.out.println("Server has completed sending");
                latch.countDown();

            }
        });

        System.out.println("Sending message1");
        requestStreamObserver.onNext(LongGreetRequest.newBuilder()
                .setGreeting(Greeting.newBuilder().setFirstName("Prateek")
                        .build())
                .build());

        System.out.println("Sending message2");
        requestStreamObserver.onNext(LongGreetRequest.newBuilder()
                .setGreeting(Greeting.newBuilder().setFirstName("Pratyang")
                        .build())
                .build());

        System.out.println("Sending message3");
        requestStreamObserver.onNext(LongGreetRequest.newBuilder()
                .setGreeting(Greeting.newBuilder().setFirstName("Sanav")
                        .build())
                .build());
        //tell server finish sending data.
        requestStreamObserver.onCompleted();

        try {
            latch.await(3L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void doServerStreamingCall(ManagedChannel channel){

        GreetServiceGrpc.GreetServiceBlockingStub  greetClient = GreetServiceGrpc
                .newBlockingStub(channel);

        Greeting greeting = Greeting.newBuilder().setFirstName("Prateek").setLastName("Nigam")
                .build();

        GreetManyRequest request  = GreetManyRequest.newBuilder().setGreeting(greeting).build();
        greetClient.greetManyTimes(request).forEachRemaining(r -> System.out.println(r.getResult()));


    }

    private void doBidirectionalStreamingCall(ManagedChannel channel){
        System.out.println("doBidirectionalStreamingCall");
        GreetServiceGrpc.GreetServiceStub asyncClient = GreetServiceGrpc.newStub(channel);
        CountDownLatch latch = new CountDownLatch(1);
        StreamObserver<GreetEveryoneRequest> greetEveryoneRequestStreamObserver
                = asyncClient.greetEveryone(new StreamObserver<GreetEveryoneResponse>() {
            @Override
            public void onNext(GreetEveryoneResponse value) {
                System.out.println("Received response from a server");
                System.out.println(value.getResult());
            }

            @Override
            public void onError(Throwable t) {
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                System.out.println("Server has completed sending");
                latch.countDown();
            }
        });

        Arrays.asList("Prateek","Pratyang","Sanav").forEach(name ->greetEveryoneRequestStreamObserver
                .onNext(GreetEveryoneRequest.newBuilder()
                .setGreeting(Greeting.newBuilder().setFirstName(name)
                        .build())
                .build()));
        //to tell server client finish the sending.
        greetEveryoneRequestStreamObserver.onCompleted();

        try {
            latch.await(3L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }
    public void doUnaryCallWithDeadline(ManagedChannel channel){
        GreetServiceGrpc.GreetServiceBlockingStub  greetClient = GreetServiceGrpc
                .newBlockingStub(channel);


        try{
            Greeting greeting = Greeting.newBuilder().setFirstName("Prateek").setLastName("Nigam")
                    .build();
            System.out.println("Sending request with 500 milliseconds deadline.");
            GreetWithDeadlineResponse response =
                greetClient.withDeadline(Deadline.after(3000,TimeUnit.MILLISECONDS))
                    .greetWithDeadline(GreetWithDeadlineRequest.newBuilder()
                            .setGreeting(greeting).build());
            System.out.println("response:" + response.getResult());

        }catch(StatusRuntimeException ex){

            if(ex.getStatus() == Status.DEADLINE_EXCEEDED){
                System.out.println("Deadline exceeded.");
            }else{
                ex.printStackTrace();
            }

        }

        try{
            Greeting greeting = Greeting.newBuilder().setFirstName("Prateek").setLastName("Nigam")
                    .build();
            System.out.println("Sending request with 100 milliseconds deadline.");
            GreetWithDeadlineResponse response =
                    greetClient.withDeadline(Deadline.after(100,TimeUnit.MILLISECONDS))
                            .greetWithDeadline(GreetWithDeadlineRequest.newBuilder()
                                    .setGreeting(greeting).build());
            System.out.println("response:" + response.getResult());

        }catch(StatusRuntimeException ex){

            if(ex.getStatus() == Status.DEADLINE_EXCEEDED){
                System.out.println("Deadline exceeded.");
            }else{
                ex.printStackTrace();
            }

        }

    }

    private void run() throws SSLException {
        /*ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost",50051)
                .usePlaintext()
                .build();*/

        ManagedChannel secureChannel = NettyChannelBuilder.forAddress("localhost",50051)
                .sslContext(GrpcSslContexts.forClient()
                        .trustManager(new File("ssl/ca.crt")).build())
                .build();
        doUnaryCall(secureChannel);
        //doServerStreamingCall(channel);
        //doClientStreamingCall(channel);
        //doBidirectionalStreamingCall(secureChannel);
       //doUnaryCallWithDeadline(secureChannel);
        System.out.println("Shutdown channel");
        secureChannel.shutdown();

    }
}
