package com.learning.grpc.server;

import com.dummy.greet.GreetWithDeadlineRequest;
import com.dummy.greet.GreetEveryoneRequest;
import com.dummy.greet.GreetEveryoneResponse;
import com.dummy.greet.GreetManyRequest;
import com.dummy.greet.GreetManyResponse;
import com.dummy.greet.GreetRequest;
import com.dummy.greet.GreetResponse;
import com.dummy.greet.GreetServiceGrpc.GreetServiceImplBase;
import com.dummy.greet.GreetWithDeadlineRequest;
import com.dummy.greet.GreetWithDeadlineResponse;
import com.dummy.greet.LongGreetRequest;
import com.dummy.greet.LongGreetResponse;
import io.grpc.Context;
import io.grpc.stub.StreamObserver;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class GreetServiceImpl extends GreetServiceImplBase {

    public Worker w;

    public void setWorker(Worker w){
        this.w = w;
    }

    public Worker getWorker(){
        return this.w;
    }

    @Override
    public void greet(GreetRequest request, StreamObserver<GreetResponse> responseObserver) {
        String firstName = request.getGreeting().getFirstName();
        String result = "Hello !" + firstName;


        List<Callable<String>> callables = Arrays.asList(
            () -> result, ()-> result);
        System.out.println("Before worker");
        getWorker().start(callables);
        System.out.println("After worker");

        GreetResponse response = GreetResponse.newBuilder().setResult(result).build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();

    }

    public String getServerResponse(){
        StringBuffer output = new StringBuffer();
        try {

            URL url = new URL("https://gorest.co.in/public-api/users");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + conn.getResponseCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));

            String temp ;
            System.out.println("Output from Server .... \n");
            while ((temp = br.readLine()) != null) {
                output.append(temp);
                System.out.println(output);
            }

            conn.disconnect();

        } catch (MalformedURLException e) {

            e.printStackTrace();

        } catch (IOException e) {

            e.printStackTrace();

        }
        return output.toString();
    }

    @Override
    public StreamObserver<LongGreetRequest> longGreet(StreamObserver<LongGreetResponse> responseObserver) {

        StreamObserver<LongGreetRequest> streamObserverOfRequest = new StreamObserver<LongGreetRequest>() {
            String result = "";

            @Override
            public void onNext(LongGreetRequest value) {
                result += ", Hello" + value.getGreeting().getFirstName() + "!";
            }

            @Override
            public void onError(Throwable t) {
                //TO DO
            }

            @Override
            public void onCompleted() {
                responseObserver.onNext(LongGreetResponse.newBuilder()
                        .setResult(result).build());
                responseObserver.onCompleted();
            }
        };

        return streamObserverOfRequest;
    }

    @Override
    public void greetManyTimes(GreetManyRequest request, StreamObserver<GreetManyResponse> responseObserver) {
        String firstName = request.getGreeting().getFirstName();

        try {

            for (int i = 0; i < 10; i++) {
                String result = "Response-" + i + "-Hello!" + firstName;
                GreetManyResponse response = GreetManyResponse.newBuilder().setResult(result).build();
                responseObserver.onNext(response);
                Thread.sleep(1000);

            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            responseObserver.onCompleted();
        }

    }

    @Override
    public StreamObserver<GreetEveryoneRequest> greetEveryone(StreamObserver<GreetEveryoneResponse>
                                                                          responseObserver) {

        StreamObserver<GreetEveryoneRequest> streamObserverOfRequest = new StreamObserver<GreetEveryoneRequest>()
        {

            @Override
            //client send message.
            public void onNext(GreetEveryoneRequest value) {
                String result = "";
                result += "Hello:" + value.getGreeting().getFirstName();
                GreetEveryoneResponse response = GreetEveryoneResponse.newBuilder()
                        .setResult(result).build();
                responseObserver.onNext(response);
            }

            @Override
            //client send an error.
            public void onError(Throwable t) {

            }

            @Override
            //client is done.
            public void onCompleted() {
                System.out.println("streamObserverOfRequest onCompleted");
                responseObserver.onCompleted();
            }


        };
        return streamObserverOfRequest;
    }

    @Override
    public void greetWithDeadline(GreetWithDeadlineRequest request,
                                  StreamObserver<GreetWithDeadlineResponse> responseObserver) {

        Context current  = Context.current();


        try {


            for (int i = 0; i < 3; i++) {
                if(!current.isCancelled()){
                    System.out.println("Sleep for 100 seconds");
                    Thread.sleep(100);

                }else{
                    return;
                }

            }

            GreetWithDeadlineResponse response =
            GreetWithDeadlineResponse.newBuilder().setResult("hello:" + request.getGreeting().getFirstName()).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
        catch(InterruptedException ex){
            ex.printStackTrace();

        }
    }
}
