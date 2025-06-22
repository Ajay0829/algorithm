package com.market;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/batch")
public class MarketBatchController {

    @Autowired
    private MarketBatchStartupRunner marketBatchStartupRunner;

    @PostMapping("/run")
    public String runBatchProcess(@RequestBody BatchProcessRequest request) {
        try {
            marketBatchStartupRunner.runBatchProcess(
                request.getStockSymbol(),
                request.getTimeframe(),
                request.getFrom(),
                request.getTo()
            );
            return "Batch process started successfully.";
        } catch (Exception e) {
            return "Error starting batch process: " + e.getMessage();
        }
    }
}

