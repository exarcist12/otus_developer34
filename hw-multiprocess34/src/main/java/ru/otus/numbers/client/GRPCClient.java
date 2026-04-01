package ru.otus.numbers.client;

import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import java.util.concurrent.CountDownLatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.numbers.NumberResponse;
import ru.otus.numbers.NumbersServiceGrpc;
import ru.otus.numbers.RangeRequest;

public class GRPCClient {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8190;

    private static int currentValue = 1;
    private static int lastValueFromServer = 0;

    private static boolean isUser = false;

    private static final Logger logger = LoggerFactory.getLogger(GRPCClient.class);
    private static final Object lock = new Object();

    public static void main(String[] args) {
        logger.info("Client starts...");

        var channel = ManagedChannelBuilder.forAddress(SERVER_HOST, SERVER_PORT)
                .usePlaintext()
                .build();

        try {
            var stub = NumbersServiceGrpc.newStub(channel);

            RangeRequest request =
                    RangeRequest.newBuilder().setFirstValue(1).setLastValue(30).build();

            logger.info("Sending request: numbers from 1 to 10");

            var latch = new CountDownLatch(1);
            stub.generateNumbers(request, new StreamObserver<NumberResponse>() {
                @Override
                public void onNext(NumberResponse response) {
                    synchronized (lock) {
                        lastValueFromServer = response.getValue();
                        logger.info("Numbers from server: " + lastValueFromServer);
                        isUser = false;
                    }
                }

                @Override
                public void onError(Throwable t) {
                    latch.countDown();
                    logger.error("Error: " + t.getMessage());
                }

                @Override
                public void onCompleted() {
                    logger.info("Finished!");
                    latch.countDown();
                }
            });

            logger.info("Start getting numbers from server...");

            for (int i = 0; i < 50; i++) {
                Thread.sleep(1000);
                synchronized (lock) {
                    currentValue = currentValue + 1;
                    if (!isUser) {
                        currentValue = currentValue + lastValueFromServer;
                        isUser = true;
                    }
                }
                logger.info("currentValue: " + currentValue);
            }

            latch.await();

            logger.info("All numbers received!");

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            channel.shutdown();
        }
    }
}
