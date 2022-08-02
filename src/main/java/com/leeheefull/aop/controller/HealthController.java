package com.leeheefull.aop.controller;

import com.leeheefull.aop.annotation.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/aop")
public class HealthController {

    @Timer
    @GetMapping("/health")
    public String test() throws InterruptedException {
        Thread.sleep(1000);
        log.info(">>>>>>> Aop Health Controller");
        return "health good";
    }

}
