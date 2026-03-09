package com.capgemini.test.code;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class CodeApplication {

  public static void main(String[] args) {
    SpringApplication.run(CodeApplication.class, args);
  }
}
