package com.market.streamline.data;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;

public class StockState {
    private CountDownLatch countDownLatch;
    private LocalDateTime timestamp;

    public StockState() {
        // Default constructor
    }

    public void incrementCurrentEvents() {
        countDownLatch.countDown();
    }

    public StockState(CountDownLatch countDownLatch) {
        this.countDownLatch = countDownLatch;
        this.timestamp = LocalDateTime.now();
    }

    public CountDownLatch getCountDownLatch() {
        return countDownLatch;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
