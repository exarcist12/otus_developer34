package ru.otus.numbers.client;

import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import java.util.concurrent.CountDownLatch;
import ru.otus.numbers.NumberResponse;
import ru.otus.numbers.NumbersServiceGrpc;
import ru.otus.numbers.RangeRequest;

public class GRPCClient {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8190;

    private static int currentValue = 1;
    private static int lastValueFromServer = 0;

    private static boolean isUser = false;

    public static void main(String[] args) {
        System.out.println("Client starts...");

        var channel = ManagedChannelBuilder.forAddress(SERVER_HOST, SERVER_PORT)
                .usePlaintext()
                .build();

        try {
            var stub = NumbersServiceGrpc.newStub(channel);

            RangeRequest request =
                    RangeRequest.newBuilder().setFirstValue(1).setLastValue(30).build();

            System.out.println("Sending request: numbers from 1 to 10");

            var latch = new CountDownLatch(1);
            stub.generateNumbers(request, new StreamObserver<NumberResponse>() {
                @Override
                public void onNext(NumberResponse response) {
                    lastValueFromServer = response.getValue();
                    System.out.println("Numbers from server: " + lastValueFromServer);
                    isUser = false;
                }

                @Override
                public void onError(Throwable t) {
                    System.err.println("Error: " + t.getMessage());
                }

                @Override
                public void onCompleted() {
                    System.out.println("Finished!");
                    latch.countDown();
                }
            });

            System.out.println("Start getting numbers from server...");

            for (int i = 0; i < 50; i++) {
                Thread.sleep(1000);
                currentValue = currentValue + 1;
                if (!isUser) {
                    currentValue = currentValue + lastValueFromServer;
                    isUser = true;
                }
                System.out.println("currentValue: " + currentValue);
            }

            latch.await();

            System.out.println("All numbers received!");

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            channel.shutdown();
        }
    }
}
