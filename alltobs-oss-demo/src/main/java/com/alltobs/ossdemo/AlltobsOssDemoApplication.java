package com.alltobs.ossdemo;

import cn.allbs.oss.annotation.EnableAllbsOss;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableAllbsOss
@SpringBootApplication
public class AlltobsOssDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(AlltobsOssDemoApplication.class, args);
    }

}
