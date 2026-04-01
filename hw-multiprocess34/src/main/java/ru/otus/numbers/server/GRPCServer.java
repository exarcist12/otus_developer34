package ru.otus.numbers.server;

import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.numbers.NumberResponse;
import ru.otus.numbers.NumbersServiceGrpc;
import ru.otus.numbers.RangeRequest;

public class GRPCServer {
    private static final int SERVER_PORT = 8190;

    private static final Logger logger = LoggerFactory.getLogger(GRPCServer.class);

    public static void main(String[] args) throws IOException, InterruptedException {
        var numbersServiceImpl = new NumbersServiceImpl();

        var server = ServerBuilder.forPort(SERVER_PORT)
                .addService(numbersServiceImpl)
                .build();

        server.start();
        logger.info("Server started, listening on port: " + SERVER_PORT);
        logger.info("Waiting for requests...");

        server.awaitTermination();
    }

    static class NumbersServiceImpl extends NumbersServiceGrpc.NumbersServiceImplBase {

        @Override
        public void generateNumbers(RangeRequest request, StreamObserver<NumberResponse> responseObserver) {

            logger.info("Received request: " + "firstValue="
                    + request.getFirstValue() + ", lastValue="
                    + request.getLastValue());

            for (int i = request.getFirstValue() + 1; i <= request.getLastValue(); i++) {

                NumberResponse response =
                        NumberResponse.newBuilder().setValue(i).build();

                responseObserver.onNext(response);

                logger.info("Sent number: " + i);

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            responseObserver.onCompleted();
            logger.info("All numbers sent!");
        }
    }
}
