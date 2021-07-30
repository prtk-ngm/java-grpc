package com.learning.grpc.server;

import com.dummy.calc.AddRequest;
import com.dummy.calc.AddResponse;
import com.dummy.calc.AddServiceGrpc;
import com.dummy.calc.AverageRequest;
import com.dummy.calc.AverageResponse;
import com.dummy.calc.CurrentMaxRequest;
import com.dummy.calc.CurrentMaxResponse;
import com.dummy.calc.PrimeDecomposeRequest;
import com.dummy.calc.PrimeDecomposeResponse;
import com.dummy.calc.SquareRootRequest;
import com.dummy.calc.SquareRootResponse;
import com.dummy.greet.LongGreetRequest;
import com.dummy.greet.LongGreetResponse;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

public class AddServiceImpl extends AddServiceGrpc.AddServiceImplBase {

    @Override
    public StreamObserver<AverageRequest> average(StreamObserver<AverageResponse> responseObserver) {
        StreamObserver<AverageRequest> streamObserverOfRequest = new StreamObserver<AverageRequest>() {
            int totalNumber = 0;
            int totalSum = 0;

            @Override
            public void onNext(AverageRequest value) {
                totalSum +=  value.getNumber();
                totalNumber += 1;
            }



            @Override
            public void onError(Throwable t) {
                //TO DO
            }

            @Override
            public void onCompleted() {
                int average  = totalSum / totalNumber;
                responseObserver.onNext(AverageResponse.newBuilder().setAverage(average).build());
                responseObserver.onCompleted();
            }
        };

        return streamObserverOfRequest;
    }

    @Override
    public void primeDecompose(PrimeDecomposeRequest request, StreamObserver<PrimeDecomposeResponse>
            responseObserver) {

        int num = request.getNumber();
        int k = 2;

            while (num > 1) {
                if (num % k == 0) {
                    num = num / k;
                    PrimeDecomposeResponse response = PrimeDecomposeResponse.newBuilder()
                            .setResult(k).build();
                    responseObserver.onNext(response);


                } else {
                    k++;
                }
            }
            responseObserver.onCompleted();


    }

    @Override
    public void add(AddRequest request, StreamObserver<AddResponse> responseObserver) {
        AddResponse response = AddResponse.newBuilder().setSum(request.getFirstNumber()
                + request.getSecondNumber()).build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<CurrentMaxRequest> currentMax(
            StreamObserver<CurrentMaxResponse> responseObserver) {

        StreamObserver<CurrentMaxRequest> streamObserverOfRequest = new StreamObserver<CurrentMaxRequest>() {
            int currMax = 0;

            @Override
            public void onNext(CurrentMaxRequest value) {
                if(value.getNumber() > currMax){
                    currMax = value.getNumber();
                }
                CurrentMaxResponse response = CurrentMaxResponse.newBuilder()
                        .setMaxNumber(currMax)
                        .build();
                responseObserver.onNext(response);

            }



            @Override
            public void onError(Throwable t) {
                //TO DO
            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        };

        return streamObserverOfRequest;
    }

    @Override
    public void squareRoot(SquareRootRequest request, StreamObserver<SquareRootResponse> responseObserver) {
        Integer number = request.getNumber();
        if(number > 0){

           SquareRootResponse response = SquareRootResponse.newBuilder()
                   .setSquareRoot(Math.sqrt(number)).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();

        }else{
            responseObserver.onError(
                    Status.INVALID_ARGUMENT.withDescription("Received number is not positive")
                            .augmentDescription("Received Number:" + number).asRuntimeException()
            );
        }
        super.squareRoot(request, responseObserver);
    }
}
