package com.mercadona.poc.infraestructure.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import com.mercadona.poc.constants.MessageTypeEnum;
import com.mercadona.poc.infraestructure.dto.LoginDTO;
import com.mercadona.poc.domain.UnzippedFiles;
import com.mercadona.poc.infraestructure.utilities.FileUtilities;
import com.mercadona.poc.infraestructure.utilities.Utilities;

@Slf4j
@RequiredArgsConstructor
@Service
public class SendFilesService {

	private final LoginService loginService;
	private final SendFileToServerService fileServer;
	private final SSEService emitterService;
	private final HMILoaderService hmiLoaderService;
	private final WebScrappingService webScrappingService;

	public void sendCookBook(String folderPath, String ovenName) throws IOException, InterruptedException {
		LoginDTO loginDTO;
		loginDTO = loginService.login(ovenName);
		log.info("Inicio servicio");
		emitterService.sendMessage(ovenName, MessageTypeEnum.INICIANDO);

		List<Path> filePaths = Files.walk(Paths.get(folderPath))
				.filter(Files::isRegularFile)
				.collect(Collectors.toList());

		int uploadCounter = 0;
		int contadorListaFiles = filePaths.size();

		emitterService.sendMessage(ovenName, MessageTypeEnum.SUBIENDO);
		List<UnzippedFiles> unzippedFiles = uploadFilesToOven(filePaths, ovenName, loginDTO);
		List<UnzippedFiles> nonExistentFiles = new ArrayList<>();
		int retries = 0;
		final int maxRetries = 5;
		while (nonExistentFiles.isEmpty() && retries < maxRetries) {
			nonExistentFiles = webScrappingService.webScrapingService(unzippedFiles, loginDTO);
			if (!nonExistentFiles.isEmpty()) {
				log.info("Re-enviando archivos faltantes para {} (Reintentos {}/{})", ovenName, retries, maxRetries);
				List<Path> nonExistentFilesAsPaths = nonExistentFiles.stream()
						.map(unzippedFile -> Paths.get(unzippedFile.getFilePath()))
						.collect(Collectors.toList());
				uploadFilesToOven(nonExistentFilesAsPaths, ovenName, loginDTO);
				retries++;
			} else {
				break;
			}
		}
		if (retries == maxRetries && !nonExistentFiles.isEmpty()) {
			emitterService.sendMessage(ovenName, MessageTypeEnum.ERRORENZIP);
			log.error("Error de lectura. Maximo numero de reintentos.");
		}
		int nonExistentCounter = countUploadedFiles(nonExistentFiles);
		uploadCounter = contadorListaFiles - nonExistentCounter;
		log.info("Total files uploaded to {}: {}/{}", ovenName, uploadCounter, contadorListaFiles);
		emitterService.sendMessageAndCounters(ovenName, MessageTypeEnum.TERMINADO, uploadCounter, contadorListaFiles);

		String encodedUserPassword = Utilities.encodeToBase64(loginDTO.getUser(), loginDTO.getPassword());
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.TEXT_XML);
		headers.add("SOAPAction", "http://tempuri.org/SetValue");
		headers.add("Authorization", encodedUserPassword);
		hmiLoaderService.initHMILoad(ovenName, loginDTO.getOvenHost(), headers);
		log.info("Fin del servicio servicio");

		FileUtilities.deleteAll(filePaths);
	}

	private List<UnzippedFiles> uploadFilesToOven(List<Path> filePaths, String ovenName, LoginDTO loginDTO)
			throws IOException {
		List<UnzippedFiles> unzippedFiles = new ArrayList<>();

		for (Path filePath : filePaths) {
			File file = filePath.toFile();
			if (!file.isDirectory()) {
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
				UnzippedFiles unzippedFile = new UnzippedFiles(fileName, fileSizeString, false, file.getAbsolutePath());
				try {
					fileServer.sendFile(file, loginDTO);
					log.info("Subiendo: {} to oven {}", file.getName(), ovenName);
					Thread.sleep(1500);
				} catch (InterruptedException | IOException e) {
					emitterService.sendMessageAndValue(ovenName, MessageTypeEnum.ERRORARCHIVO, file.getName());
					log.error("File failed: {} to oven {}", file.getName(), ovenName);
				}
				unzippedFiles.add(unzippedFile);
			}
		}

		return unzippedFiles;
	}

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
