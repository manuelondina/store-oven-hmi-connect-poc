package com.supermarket.ovenupdate.poc.infraestructure.controller;


import com.supermarket.ovenupdate.poc.infraestructure.service.SSEService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;


@RestController
public class SSEEmitterController {

    @Autowired
    private SSEService sseService;

    // API Method to send messages with emitter
    @GetMapping(value = "/sse/connect")
    public synchronized SseEmitter getSseEmitter() {
        return sseService.createAndAddEmitter();
    }
}
