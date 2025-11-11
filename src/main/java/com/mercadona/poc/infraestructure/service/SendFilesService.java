package com.mercadona.poc.infraestructure.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import com.mercadona.poc.constants.MessageTypeEnum;
import com.mercadona.poc.infraestructure.dto.LoginDTO;
import com.mercadona.poc.domain.UnzippedFiles;
import com.mercadona.poc.infraestructure.utilities.FileUtilities;
import com.mercadona.poc.infraestructure.utilities.Utilities;

@Service
public class SendFilesService {

	private static final Logger LOGGER = LoggerFactory.getLogger(SendFilesService.class);

	@Autowired
	private LoginService loginService;

	@Autowired
	private SendFileToServerService fileServer;

	@Autowired
	private SSEService emitterService;

	@Autowired
	private HMILoaderService hmiLoaderService;

	@Autowired
	private WebScrappingService webScrappingService;

	// TODO Delete files from server
	// @Autowired
	// private DeleteFilesFromServer deleteFilesFromServer;

	// Send files to the oven
	public void sendCookBook(String folderPath, String ovenName) throws IOException, InterruptedException {
		LoginDTO loginDTO;
		loginDTO = loginService.login(ovenName);
		LOGGER.info("Inicio servicio");
		emitterService.sendMessage(ovenName, MessageTypeEnum.INICIANDO);

		// Get the list of all files in the folder
		List<Path> filePaths = Files.walk(Paths.get(folderPath))
				.filter(Files::isRegularFile)
				.collect(Collectors.toList());

		// Counters for the emitter service to send the messages of the files uploaded
		int uploadCounter = 0;
		int contadorListaFiles = filePaths.size();

		emitterService.sendMessage(ovenName, MessageTypeEnum.SUBIENDO);
		// List of files to be uploaded
		List<UnzippedFiles> unzippedFiles = uploadFilesToOven(filePaths, ovenName, loginDTO);
		// List of files that were not uploaded
		List<UnzippedFiles> nonExistentFiles = new ArrayList<>();
		// Retry counter
		int retries = 0;
		// Max number of retries
		final int maxRetries = 5;
		// While there are non-existent files and the number of retries is less than the
		// max number of retries
		while (nonExistentFiles.isEmpty() && retries < maxRetries) {
			// Get the list of non-existent files
			nonExistentFiles = webScrappingService.webScrapingService(unzippedFiles, loginDTO);
			// If there are non-existent files then re-send them
			if (!nonExistentFiles.isEmpty()) {
				LOGGER.info("Re-enviando archivos faltantes para {} (Reintentos {}/{})", ovenName, retries, maxRetries);
				// List of files to be uploaded if they are not uploaded
				List<Path> nonExistentFilesAsPaths = nonExistentFiles.stream()
						.map(unzippedFile -> Paths.get(unzippedFile.getFilePath()))
						.collect(Collectors.toList());
				// Upload the non-existent files
				uploadFilesToOven(nonExistentFilesAsPaths, ovenName, loginDTO);
				retries++;
			} else {
				break;
			}
		}
		// If the number of retries is equal to the max number of retries and there are
		// non-existent files
		// then send an error message that there is an error in the zip file.
		if (retries == maxRetries && !nonExistentFiles.isEmpty()) {
			emitterService.sendMessage(ovenName, MessageTypeEnum.ERRORENZIP);
			LOGGER.error("Error de lectura. Maximo numero de reintentos.");
		}
		// Count the number of the non-existent files and subtract it from the total
		// number of files
		int nonExistentCounter = countUploadedFiles(nonExistentFiles);
		// Count the number of files uploaded after the retries.
		uploadCounter = contadorListaFiles - nonExistentCounter;
		// Send the message of the total number of files uploaded after the retries if
		// there are any.
		LOGGER.info("Total files uploaded to {}: {}/{}", ovenName, uploadCounter, contadorListaFiles);
		emitterService.sendMessageAndCounters(ovenName, MessageTypeEnum.TERMINADO, uploadCounter, contadorListaFiles);

		// Encode the user and password to send the request to the HMI
		String encodedUserPassword = Utilities.encodeToBase64(loginDTO.getUser(), loginDTO.getPassword());
		// Set the headers for the request to the HMI
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.TEXT_XML);
		headers.add("SOAPAction", "http://tempuri.org/SetValue");
		headers.add("Authorization", encodedUserPassword);
		// Send the request to the HMI
		hmiLoaderService.initHMILoad(ovenName, loginDTO.getOvenHost(), headers);
		// Finish the service
		LOGGER.info("Fin del servicio servicio");

		// Delete the folder from the server when the service is finished
		FileUtilities.deleteAll(filePaths);
	}

	// Upload the files to the oven in a parallel system
	private List<UnzippedFiles> uploadFilesToOven(List<Path> filePaths, String ovenName, LoginDTO loginDTO)
			throws IOException {
		List<UnzippedFiles> unzippedFiles = new ArrayList<>();

		for (Path filePath : filePaths) {
			File file = filePath.toFile();
			// If the file is not a directory then upload it
			if (!file.isDirectory()) {
				// Convert the file size to a string and add the unit for each case to equal the
				// scraper service
				String fileName = file.getName();
				long fileSize = file.length();
				String fileSizeString;
				if (fileSize <= 9999) {
					fileSizeString = Long.toString(fileSize);
				} else {
					double size = Math.floor(fileSize / 1024.0);
					String unit = "K";
					fileSizeString = String.format("%.0f %s", size, unit);
				}
				// Add the file to the list of unzipped files to compare it with the list of
				// files from the scraper
				UnzippedFiles unzippedFile = new UnzippedFiles(fileName, fileSizeString, false, file.getAbsolutePath());
				// File uploader to HMI
				try {
					fileServer.sendFile(file, loginDTO);
					LOGGER.info("Subiendo: {} to oven {}", file.getName(), ovenName);
					// Thread sleep to avoid the error of the file not being uploaded
					Thread.sleep(1500);
				} catch (InterruptedException | IOException e) {
					emitterService.sendMessageAndValue(ovenName, MessageTypeEnum.ERRORARCHIVO, file.getName());
					LOGGER.error("File failed: {} to oven {}", file.getName(), ovenName);
				}
				unzippedFiles.add(unzippedFile);
			}
		}

		return unzippedFiles;
	}

	// Count the number of files uploaded
	private int countUploadedFiles(List<UnzippedFiles> unzippedFiles) {
		int uploadCounter = 0;
		for (UnzippedFiles unzippedFile : unzippedFiles) {
			if (unzippedFile.isUploaded()) {
				uploadCounter++;
			}
		}
		return uploadCounter;
	}
}
