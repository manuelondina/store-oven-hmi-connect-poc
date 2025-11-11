package com.mercadona.poc.infraestructure.service;

import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

@Service
public class CreateFolderService {
    private static final Logger logger = LoggerFactory.getLogger(CreateFolderService.class);

    public String storeFiles(String folderName, MultipartFile[] files) {
        String currentPath = System.getProperty("user.dir");
        String parentDirectory = new File(currentPath).getParent();
        final String pathFolder = parentDirectory + "\\recetas\\";

        try {
            Path folderPath = Paths.get(pathFolder + folderName);

            // Verificar si la carpeta ya existe
            if (Files.exists(folderPath)) {

                // Borrar la carpeta existente
                FileUtils.deleteDirectory(folderPath.toFile());
            }

            // Crear la carpeta con el nombre proporcionado en la ruta especificada.
            Files.createDirectories(folderPath);

            // Iterar sobre los archivos proporcionados y almacenarlos en la carpeta creada.
            for (MultipartFile file : files) {
                Path rutaArchivo = folderPath.resolve(Objects.requireNonNull(file.getOriginalFilename()));
                Files.copy(file.getInputStream(), rutaArchivo, StandardCopyOption.REPLACE_EXISTING);
                logger.info("Copiando archivo {} en la carpeta {}", file.getOriginalFilename(), folderName);
            }

            logger.info("Archivos almacenados exitósamente en " + folderPath);
            return "Archivos almacenados exitósamente en " + folderPath;

        } catch (IOException e) {
            throw new RuntimeException("No se pudo almacenar el archivo. Error: " + e.getMessage());
        }
    }
}

