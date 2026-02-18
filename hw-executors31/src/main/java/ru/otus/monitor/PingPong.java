package ru.otus.monitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PingPong {
    private static final Logger logger = LoggerFactory.getLogger(PingPong.class);

    private int pred = 0;
    private int last = 1;
    int direction = 1;
    int min = 1;
    int max = 10;
    int currentTread = 1;
    boolean increasePred = true;

    private synchronized void action(int numberThread) {

        while (!Thread.currentThread().isInterrupted()) {
            try {
                // spurious wakeup https://en.wikipedia.org/wiki/Spurious_wakeup
                // поэтому не if
                if (last == max) {
                    direction = -1;
                } else if (last == min) {
                    direction = 1;
                }

                if (numberThread != currentTread) {
                    this.wait();
                }
                logger.info(String.valueOf(last));
                sleep();
                currentTread = (currentTread == 1) ? 2 : 1;
                if (numberThread == currentTread) {
                    logger.info(String.valueOf(last));
                }
                sleep();
                notifyAll();
                logger.info("after notify");

                if (increasePred) {
                    pred = pred + direction;
                } else {
                    last = last + direction;
                }
                increasePred = !increasePred;
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public static void main(String[] args) {
        PingPong pingPong = new PingPong();
        new Thread(() -> pingPong.action(1)).start();
        new Thread(() -> pingPong.action(2)).start();
    }

    private static void sleep() {
        try {
            Thread.sleep(1_000);
        } catch (InterruptedException e) {
            logger.error(e.getMessage());
            Thread.currentThread().interrupt();
        }
    }
}
