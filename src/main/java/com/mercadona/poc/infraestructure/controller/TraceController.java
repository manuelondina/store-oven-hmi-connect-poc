package com.mercadona.poc.infraestructure.controller;

import static com.mercadona.poc.constants.PocConstansEnum.CONTROLLER_START;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.mercadona.poc.infraestructure.service.TraceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mercadona.poc.domain.StartJobRequest;
import com.mercadona.poc.infraestructure.service.SSEService;
import com.mercadona.poc.infraestructure.service.SendFilesService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping("/api")
public class TraceController {

    private static final Logger LOGGER = LoggerFactory.getLogger(TraceController.class);

    @Autowired
    TraceService traceService;

    // API Method to get the current trace
    @ApiOperation(httpMethod = "GET", value = "GET HTTPResponse Value", response = String.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 404, message = "Not Found"),
            @ApiResponse(code = 500, message = "Internal Error")
    })

    // Get Method to get the current trace of the application and return a response
    @GetMapping(value = "/trace")
    public ResponseEntity<String> getCurrentTrace() {
        try {
            LOGGER.info(CONTROLLER_START);
            String trace = traceService.getCurrentTrace();
            return new ResponseEntity<>(trace, HttpStatus.OK);
        } catch (Exception e) {
            LOGGER.error("Error al obtener el rastro actual: {}", e.getMessage());
            return new ResponseEntity<>("Error interno del servidor", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    // Post Method to send the file to the ovens
    @PostMapping("/sendFile")
    @Async("customTaskExecutor")
    public CompletableFuture<ResponseEntity<String>> startJobAsync(@RequestBody StartJobRequest request)
            throws Exception {
        LOGGER.info("Current thread name: {}", Thread.currentThread().getName());

        return CompletableFuture.completedFuture(traceService.startJob(request));
    }


    // Get the list of cook books to send to the frontend
    @GetMapping("/recipes/list")
    public ResponseEntity<List<String>> getRecipeFolders() {
        try {
            List<String> recipes = traceService.getRecipeFolders();
            return new ResponseEntity<>(recipes, HttpStatus.OK);
        } catch (Exception e) {
            LOGGER.error("Error al obtener la lista de carpetas de recetas: {}", e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
