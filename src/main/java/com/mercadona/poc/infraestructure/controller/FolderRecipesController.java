package com.mercadona.poc.infraestructure.controller;

import com.mercadona.poc.infraestructure.service.CreateFolderService;
import com.mercadona.poc.infraestructure.utilities.ResponseMessage;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;

@RestController
@RequestMapping("/api/folder")
public class FolderRecipesController {


    private final CreateFolderService createFolderService;

    public FolderRecipesController(CreateFolderService createFolderService) {
        this.createFolderService = createFolderService;
    }

    //Controller to create a recipe folder with custom files
    @PostMapping("/create")
    public ResponseEntity<ResponseMessage> handleFileUpload(@RequestParam("files") MultipartFile[] files, @RequestParam("name") String name) {

        // Verificar si el nombre de la carpeta está vacío
        if (name == null || name.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(new ResponseMessage("El nombre de la carpeta no puede estar vacío."));
        }

        // Verificar si algún archivo tiene un filename vacío
        boolean allFilesEmpty = true;
        for (MultipartFile file : files) {
            if (!Objects.requireNonNull(file.getOriginalFilename()).isEmpty()) {
                allFilesEmpty = false;
                break;
            }
        }
        if (allFilesEmpty) {
            return ResponseEntity.badRequest().body(new ResponseMessage("Deben seleccionarse archivos para cargar y el nombre del archivo no puede estar vacío."));
        }

        String message = createFolderService.storeFiles(name, files);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new ResponseEntity<>(new ResponseMessage(message), headers, HttpStatus.OK);

    }
}