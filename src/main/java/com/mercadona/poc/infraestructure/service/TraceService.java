package com.mercadona.poc.infraestructure.service;

import com.mercadona.poc.constants.MessageTypeEnum;
import com.mercadona.poc.domain.StartJobRequest;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static com.mercadona.poc.constants.PocConstansEnum.STATUS_ACCEPTED;

@Service
public class TraceService {

    @Autowired
    SendFilesService sendFilesService;

    @Autowired
    SSEService emitterService;

    private static final Logger LOGGER = LoggerFactory.getLogger(TraceService.class);


    public String getCurrentTrace() {
        // Logic for getCurrentTrace
        return STATUS_ACCEPTED;
    }

    public ResponseEntity<String> startJob(StartJobRequest request) throws IOException {
        String[] ovenNames = request.getOvenList();
        String pathFolder = determineFolderPath(request.getPathFolder());

        try {
            if (!validateFolderExists(pathFolder)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Folder not found!");
            }

            copyFilesForOvens(pathFolder, ovenNames);
            processFilesForOvensAsync(pathFolder, ovenNames);

            LOGGER.info("Number of threads: {}", Thread.activeCount());
            return ResponseEntity.ok("Number of threads: " + Thread.activeCount());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        } finally {
            deleteFiles(ovenNames);
        }
    }

    public List<String> getRecipeFolders() {
        List<String> recipeFolders = new ArrayList<>();
        String currentPath = System.getProperty("user.dir");
        String parentDirectory = new File(currentPath).getParent();
        final String pathFile = parentDirectory + "/recetas/";
        File recetasDir = new File(pathFile);
        if (recetasDir.exists() && recetasDir.isDirectory()) {
            File[] carpetas = recetasDir.listFiles();

            if (carpetas != null) {
                for (File carpeta : carpetas) {
                    if (carpeta.isDirectory()) {
                        recipeFolders.add(carpeta.getName());
                    }
                }
            }
        }
        return recipeFolders;
    }


    private String determineFolderPath(String pathFolderName) {
        String currentPath = System.getProperty("user.dir");
        String parentDirectory = new File(currentPath).getParent();
        return parentDirectory + "/recetas/" + pathFolderName;
    }

    private boolean validateFolderExists(String pathFolder) {
        File folder = new File(pathFolder);
        if (folder.exists() && folder.isDirectory()) {
            LOGGER.info("Iniciando copiado de carpetas.");
            return true;
        }
        return false;
    }

    private void copyFilesForOvens(String pathFolder, String[] ovenNames) throws Exception {
        for (String ovenName : ovenNames) {
            String ovenCopyFolderName = ovenName + "_" + new File(pathFolder).getName();
            File ovenCopyFolder = new File(new File(pathFolder).getParent(), ovenCopyFolderName);
            copyDirectoryWithVerification(new File(pathFolder), ovenCopyFolder);
        }
        LOGGER.info("Creando copias de carpetas...");
    }

    private void processFilesForOvensAsync(String pathFolder, String[] ovenNames) {
        List<CompletableFuture<Void>> ovenCopyFutures = new ArrayList<>();

        // Process each oven's files asynchronously.
        for (String ovenName : ovenNames) {
            CompletableFuture<Void> ovenCopyFuture = CompletableFuture.runAsync(() -> {
                try {
                    String ovenCopyPath = createOvenCopyPath(pathFolder, ovenName);
                    sendFileAndLogTime(ovenCopyPath, ovenName);
                } catch (IOException | InterruptedException e) {
                    handleAsyncException(e);
                }
            });
            ovenCopyFutures.add(ovenCopyFuture);
        }

        // Wait for all futures to complete.
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(ovenCopyFutures.toArray(new CompletableFuture[0]));
        allFutures.join();
    }

    private void sendFileAndLogTime(String ovenCopyPath, String ovenName) throws IOException, InterruptedException {
        LocalTime time1 = LocalTime.now();

        sendFilesService.sendCookBook(ovenCopyPath, ovenName);

        Duration elapsedTime = Duration.between(time1, LocalTime.now());
        String message = elapsedTime.getSeconds() + " segundos";
        LOGGER.info("Tiempo transcurrido: {}", message);
        emitterService.sendMessageAndValue(ovenName, MessageTypeEnum.TIEMPO, message);
    }

    private void handleAsyncException(Exception e) {
        if (e instanceof IOException) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error occurred during task execution", e);
        } else if (e instanceof InterruptedException) {
            throw new RuntimeException(e);
        }
    }

    // Create a copy of the file for each oven
    private String createOvenCopyPath(String originalFilePath, String ovenName) throws IOException {
        Path originalPath = Paths.get(originalFilePath);
        String originalFileName = originalPath.getFileName().toString();
        String ovenCopyFileName = ovenName + "_" + originalFileName;

        Path ovenCopyPath = originalPath.getParent().resolve(ovenCopyFileName);

        return ovenCopyPath.toString();
    }

    // Delete the duplicated files
    private void deleteFiles(String[] ovenNames) throws IOException {
        String currentPath = System.getProperty("user.dir");
        String parentDirectory = new File(currentPath).getParent();
        final String recetasFolder = parentDirectory + "/recetas/";

        File recetasDir = new File(recetasFolder);
        if (recetasDir.exists() && recetasDir.isDirectory()) {
            File[] subfolders = recetasDir.listFiles();
            if (subfolders != null) {
                for (File subfolder : subfolders) {
                    if (subfolder.isDirectory()) {
                        String folderName = subfolder.getName();
                        for (String ovenName : ovenNames) {
                            String expectedPrefix = ovenName + "_";
                            if (folderName.startsWith(expectedPrefix)) {
                                FileUtils.deleteDirectory(subfolder);
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    // Calculate the hash for a file
    private String calculateSHA256(Path filePath) throws Exception {
        MessageDigest sha256Digest = MessageDigest.getInstance("SHA-256");
        byte[] fileBytes = Files.readAllBytes(filePath);
        byte[] hashBytes = sha256Digest.digest(fileBytes);
        return bytesToHex(hashBytes);
    }

    // Checking both hashes
    private boolean isFileCopySuccessful(Path sourcePath, Path destPath) throws Exception {
        String sourceHash = calculateSHA256(sourcePath);
        String destHash = calculateSHA256(destPath);
        return sourceHash.equals(destHash);
    }

    // Conversion bytes to Hex
    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder(2 * bytes.length);
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xff & bytes[i]);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    // Checking if the copy has been done correctly. If not, retry
    private void copyDirectoryWithVerification(File sourceFolder, File destFolder) throws Exception {
        FileUtils.copyDirectory(sourceFolder, destFolder);

        for (File sourceFile : Objects.requireNonNull(sourceFolder.listFiles())) {
            File destFile = new File(destFolder, sourceFile.getName());

            int retryCount = 0;
            final int MAX_RETRIES = 5;

            while (!isFileCopySuccessful(sourceFile.toPath(), destFile.toPath()) && retryCount < MAX_RETRIES) {
                // La copia falló, volvemos a intentarlo
                FileUtils.copyFile(sourceFile, destFile);
                retryCount++;
            }

            if (!isFileCopySuccessful(sourceFile.toPath(), destFile.toPath())) {
                throw new IOException(
                        "Failed to copy file correctly after " + MAX_RETRIES + " attempts: " + sourceFile.getName());
            }
        }
    }

}
