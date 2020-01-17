package com.nineban.finance.recon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.task.configuration.EnableTask;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ReconApplication  {

    public static void main(String[] args) {
        SpringApplication.run(ReconApplication.class, args);
    }

}
