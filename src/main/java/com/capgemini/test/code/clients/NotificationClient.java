package com.capgemini.test.code.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notificationClient", url = "${external.service.url}")
public interface NotificationClient {

    @PostMapping("/email")
    ResponseEntity<String> sendEmail(@RequestBody EmailRequest request);

    @PostMapping("/sms")
    ResponseEntity<String> sendSms(@RequestBody SmsRequest request);
}