package com.xushu.tools;

import org.springframework.stereotype.Service;

// 票务服务
@Service
public class TicketService {

    // 退票
    public void cancel(String ticketNumber,String name) {
        System.out.println("退票成果");
    }
}
