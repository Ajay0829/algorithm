package com.market.streamline.service;

import com.market.streamline.entity.CandleEntity;
import com.market.streamline.kafka.BOSEventProducer;
import com.market.streamline.model.BOSEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BreakOfStructureService {

    @Autowired
    private BOSEventProducer bosEventProducer;

    public boolean checkForBreakOfStructure(CandleEntity candleEntity) {

        boolean breakOfStructure = false;



        if (breakOfStructure) {
            bosEventProducer.sendBOSEvent(new BOSEvent());
        }
        return breakOfStructure;
    }
}
