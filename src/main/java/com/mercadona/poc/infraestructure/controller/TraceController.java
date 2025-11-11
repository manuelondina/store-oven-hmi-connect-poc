package com.mercadona.poc.infraestructure.controller;

import static com.mercadona.poc.constants.PocConstansEnum.CONTROLLER_START;

import java.util.List;

import com.mercadona.poc.infraestructure.service.TraceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mercadona.poc.domain.StartJobRequest;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * REST controller for application trace and file upload operations to ovens.
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class TraceController {

    private final TraceService traceService;

    @ApiOperation(httpMethod = "GET", value = "GET HTTPResponse Value", response = String.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 404, message = "Not Found"),
            @ApiResponse(code = 500, message = "Internal Error")
    })
    @GetMapping(value = "/trace")
    public ResponseEntity<String> getCurrentTrace() {
        try {
            log.info(CONTROLLER_START);
            String trace = traceService.getCurrentTrace();
            return new ResponseEntity<>(trace, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error al obtener el rastro actual: {}", e.getMessage());
            return new ResponseEntity<>("Error interno del servidor", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * Sends files to multiple ovens asynchronously.
     */
    @PostMapping("/sendFile")
    public ResponseEntity<String> startJob(@RequestBody StartJobRequest request) {
        try {
            log.info("Received request to send files to ovens: {}", (Object[]) request.getOvenList());
            ResponseEntity<String> response = traceService.startJob(request);
            return response;
        } catch (Exception e) {
            log.error("Error processing file send request", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing request: " + e.getMessage());
        }
    }


    /**
     * Retrieves list of available recipe folders.
     */
    @GetMapping("/recipes/list")
    public ResponseEntity<List<String>> getRecipeFolders() {
        try {
            List<String> recipes = traceService.getRecipeFolders();
            return ResponseEntity.ok(recipes);
        } catch (Exception e) {
            log.error("Error retrieving recipe folders: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
