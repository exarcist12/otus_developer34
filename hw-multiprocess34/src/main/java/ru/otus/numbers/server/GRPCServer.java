package ru.otus.numbers.server;

import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import java.io.IOException;
import ru.otus.numbers.NumberResponse;
import ru.otus.numbers.NumbersServiceGrpc;
import ru.otus.numbers.RangeRequest;

public class GRPCServer {
    private static final int SERVER_PORT = 8190;

    public static void main(String[] args) throws IOException, InterruptedException {
        var numbersServiceImpl = new NumbersServiceImpl();

        var server = ServerBuilder.forPort(SERVER_PORT)
                .addService(numbersServiceImpl)
                .build();

        server.start();
        System.out.println("Server started, listening on port: " + SERVER_PORT);
        System.out.println("Waiting for requests...");

        server.awaitTermination();
    }

    static class NumbersServiceImpl extends NumbersServiceGrpc.NumbersServiceImplBase {

        @Override
        public void generateNumbers(RangeRequest request, StreamObserver<NumberResponse> responseObserver) {

            System.out.println("Received request: " + "firstValue="
                    + request.getFirstValue() + ", lastValue="
                    + request.getLastValue());

            for (int i = request.getFirstValue() + 1; i <= request.getLastValue(); i++) {

                NumberResponse response =
                        NumberResponse.newBuilder().setValue(i).build();

                responseObserver.onNext(response);

                System.out.println("Sent number: " + i);

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            responseObserver.onCompleted();
            System.out.println("All numbers sent!");
        }
    }
}
