package com.verbitsky.starter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.verbitsky"})
public class BFFServiceApp {

    public static void main(String[] args) {
        //Uncomment code below to enable reactor debug feature
        //Hooks.onOperatorDebug();//NO SONAR
        SpringApplication.run(BFFServiceApp.class, args);
    }
}
