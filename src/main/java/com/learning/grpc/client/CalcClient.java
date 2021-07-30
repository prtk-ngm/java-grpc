package com.learning.grpc.client;

import static com.dummy.calc.AddServiceGrpc.AddServiceStub.*;

import com.dummy.calc.AddRequest;
import com.dummy.calc.AddResponse;
import com.dummy.calc.AddServiceGrpc;
import com.dummy.calc.AddServiceGrpc.AddServiceStub;
import com.dummy.calc.AverageRequest;
import com.dummy.calc.AverageResponse;
import com.dummy.calc.CurrentMaxRequest;
import com.dummy.calc.CurrentMaxResponse;
import com.dummy.calc.PrimeDecomposeRequest;
import com.dummy.calc.SquareRootRequest;
import com.dummy.calc.SquareRootResponse;
import com.dummy.greet.GreetEveryoneRequest;
import com.dummy.greet.GreetRequest;
import com.dummy.greet.GreetResponse;
import com.dummy.greet.GreetServiceGrpc;
import com.dummy.greet.Greeting;
import com.dummy.greet.LongGreetRequest;
import com.dummy.greet.LongGreetResponse;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class CalcClient {

    public static void main(String[] args) {
        System.out.println("gRPC calc client");
        CalcClient c = new CalcClient();
        c.run();
    }

    public void run(){

        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost",50052)
                .usePlaintext()
                .build();

        //doClientStreamingCall(channel)
        //doBidirectionalStreamingCall(channel);
        doErrorCall(channel);
        channel.shutdown();
        System.out.println("Client stopped");
    }

    public void doBidirectionalStreamingCall(ManagedChannel channel){
        AddServiceGrpc.AddServiceStub asyncClient = AddServiceGrpc.newStub(channel);
        CountDownLatch latch = new CountDownLatch(1);

        StreamObserver<CurrentMaxRequest> requestStreamObserver = asyncClient
                .currentMax(new StreamObserver<CurrentMaxResponse>() {
                    @Override
                    public void onNext(CurrentMaxResponse value) {
                        System.out.println("Current max from server:" + value.getMaxNumber());
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

                Arrays.asList(10,5,15,1).forEach(number -> requestStreamObserver
                        .onNext(CurrentMaxRequest.newBuilder().setNumber(number).build()));
                requestStreamObserver.onCompleted();
                try {
                    latch.await(3L, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

    }

    private void doServerStreamingCall(ManagedChannel channel){
        AddServiceGrpc.AddServiceBlockingStub  syncClient = AddServiceGrpc
                .newBlockingStub(channel);

        syncClient.primeDecompose(PrimeDecomposeRequest.newBuilder().setNumber(120)
                .build()).forEachRemaining(r -> System.out.println(r.getResult()));

    }

    private void doUniaryCall(ManagedChannel channel){

        AddServiceGrpc.AddServiceBlockingStub  syncClient = AddServiceGrpc
                .newBlockingStub(channel);
        Greeting greeting = Greeting.newBuilder().setFirstName("Prateek").setLastName("Nigam")
                .build();
        AddRequest request = AddRequest.newBuilder().setFirstNumber(10).setSecondNumber(20)
                .build();
        AddResponse response = syncClient.add(request);

        System.out.println("response.sum: " + response.getSum());

    }

    private void doClientStreamingCall(ManagedChannel channel){
        AddServiceGrpc.AddServiceStub asyncClient = AddServiceGrpc.newStub(channel);
        CountDownLatch latch = new CountDownLatch(1);
        StreamObserver<AverageRequest> requestStreamObserver = asyncClient
                .average(new StreamObserver<AverageResponse>() {
                    @Override
                    public void onNext(AverageResponse value) {
                        System.out.println("Received average from a server");
                        System.out.println(value.getAverage());
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

        int number = 10;
        System.out.println("Sending number:"+number);
        requestStreamObserver.onNext(AverageRequest.newBuilder()
                .setNumber(number)
                .build());

        number = 5;
        System.out.println("Sending number:" + number);
        requestStreamObserver.onNext(AverageRequest.newBuilder()
                .setNumber(number)
                .build());

        number = 5;
        System.out.println("Sending number:" + number);
        requestStreamObserver.onNext(AverageRequest.newBuilder()
                .setNumber(number)
                .build());

        //tell server finish sending data.
        requestStreamObserver.onCompleted();

        try {
            latch.await(3L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void doErrorCall(ManagedChannel channel){
        AddServiceGrpc.AddServiceBlockingStub  syncClient = AddServiceGrpc
                .newBlockingStub(channel);
        try {
            syncClient.squareRoot(SquareRootRequest.newBuilder()
                    .setNumber(-1).build());
        }
        catch(StatusRuntimeException ex){
            System.out.println("Got exception from square root call");
            ex.printStackTrace();
        }


    }



}
