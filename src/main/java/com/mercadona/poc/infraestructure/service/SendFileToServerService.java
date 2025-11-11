package com.mercadona.poc.infraestructure.service;

import static com.mercadona.poc.constants.PocServerEnum.BOUNDARY;
import static com.mercadona.poc.constants.PocServerEnum.CONTENT_SERVER;
import static com.mercadona.poc.constants.PocServerEnum.CONTENT_SERVER_END;
import static com.mercadona.poc.constants.PocServerEnum.DELETE_FILE;
import static com.mercadona.poc.constants.PocServerEnum.DEL_FILES;
import static com.mercadona.poc.constants.PocServerEnum.FILES;
import static com.mercadona.poc.constants.PocServerEnum.FORMAT_MULTIUSE;
import static com.mercadona.poc.constants.PocServerEnum.FORMAT_MULTIUSE_SEGMENT;
import static com.mercadona.poc.constants.PocServerEnum.MULTIPART_CONTENT_TYPE;
import static com.mercadona.poc.constants.PocServerEnum.PROTOCOL;
import static com.mercadona.poc.constants.PocServerEnum.SIEMENS_COOKIE;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.mercadona.poc.constants.PocServerEnum;
import com.mercadona.poc.infraestructure.dto.LoginDTO;

@Service
public class SendFileToServerService {

	private static final Logger LOGGER = LoggerFactory.getLogger(SendFileToServerService.class);

	// Case 1: Send a file to the oven using the path
	// Send files to the oven method, first we need to login to the oven
	public void sendFile(String pathFile, LoginDTO loginDto) throws IOException {
		URL urlSend = new URL(PROTOCOL + loginDto.getOvenHost() + FILES);
		HttpURLConnection connectionSend = null;
		// Connect to the oven and set the properties
		connectionSend = (HttpURLConnection) urlSend.openConnection();
		connectionSend.setRequestProperty("Cookie", SIEMENS_COOKIE + loginDto.getCookie());
		connectionSend.setRequestProperty("Content-Type", MULTIPART_CONTENT_TYPE + BOUNDARY);
		connectionSend.setRequestMethod(PocServerEnum.VERBO);
		connectionSend.setChunkedStreamingMode(0);
		connectionSend.setDoInput(true);
		connectionSend.setDoOutput(true);
		// Open the connection
		connectionSend.connect();
		// Write the file to the server
		writeFiletoServer(connectionSend, pathFile, loginDto.getMultiUsetoken());

	}

	// Case 2: Send a file to the oven using the file
	// Send files to the oven method, first we need to login to the oven
	public void sendFile(File tempFile, LoginDTO loginDto) throws IOException {
		URL urlSend = new URL(PROTOCOL + loginDto.getOvenHost() + FILES);
		HttpURLConnection connectionSend = null;
		// Connect to the oven and set the properties
		connectionSend = (HttpURLConnection) urlSend.openConnection();
		connectionSend.setRequestProperty("Cookie", SIEMENS_COOKIE + loginDto.getCookie());
		connectionSend.setRequestProperty("Content-Type", MULTIPART_CONTENT_TYPE + BOUNDARY);
		connectionSend.setRequestMethod(PocServerEnum.VERBO);
		connectionSend.setChunkedStreamingMode(0);
		connectionSend.setDoInput(true);
		connectionSend.setDoOutput(true);
		// Open the connection
		connectionSend.connect();
		// Write the file to the server
		writeFiletoServer(connectionSend, tempFile, loginDto.getMultiUsetoken());

	}

	// Write the file to the server method for case 2
	private void writeFiletoServer(HttpURLConnection connectionSend, File tempFile, String multiUseToken)
			throws IOException {
		try (OutputStream writer = connectionSend.getOutputStream()) {
			String transformToBytes = "\r\n--" + BOUNDARY + "\r\n";
			byte[] bytes = transformToBytes.getBytes(StandardCharsets.US_ASCII);
			byte[] bytes2 = transformToBytes.getBytes(StandardCharsets.US_ASCII);
			String format = FORMAT_MULTIUSE + BOUNDARY + FORMAT_MULTIUSE_SEGMENT + multiUseToken;
			byte[] byteFormat = format.getBytes(StandardCharsets.UTF_8);

			writer.write(byteFormat, 0, byteFormat.length);
			writer.write(bytes, 0, bytes.length);

			String format2 = CONTENT_SERVER + tempFile.getAbsolutePath() + CONTENT_SERVER_END;
			byte[] b = format2.getBytes(StandardCharsets.UTF_8);
			writer.write(b, 0, b.length);

			try (InputStream inputStream = new FileInputStream(tempFile)) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				final byte[] byteFichero = new byte[1024];
				int bytesRead;
				while ((bytesRead = inputStream.read(byteFichero)) != -1) {
					baos.write(byteFichero, 0, bytesRead);
				}
				writer.write(baos.toByteArray());
				writer.write(bytes2, 0, bytes2.length);
				writer.flush();
				baos.close();
			} catch (IOException e) {
				// Handle the exception when working with the input stream
				e.printStackTrace();
				LOGGER.error("Error reading the file: {}, {}", tempFile, e.getMessage());
			}
		} catch (IOException e) {
			// Handle the exception when working with the output stream
			e.printStackTrace();
			LOGGER.error("Error writing the file: {}, {}", tempFile, e.getMessage());
		}
	}

	// Write the file to the server method for case 1
	private void writeFiletoServer(HttpURLConnection connectionSend, String pathFile, String multiUseToken)
			throws IOException {
		try (OutputStream writer = connectionSend.getOutputStream()) {
			File tempFile = new File(pathFile);
			String transformToBytes = "\r\n--" + BOUNDARY + "\r\n";
			byte[] bytes = transformToBytes.getBytes(StandardCharsets.US_ASCII);
			byte[] bytes2 = transformToBytes.getBytes(StandardCharsets.US_ASCII);
			String format = FORMAT_MULTIUSE + BOUNDARY + FORMAT_MULTIUSE_SEGMENT + multiUseToken;

			byte[] byteFormat = format.getBytes(StandardCharsets.UTF_8);

			writer.write(byteFormat, 0, byteFormat.length);

			writer.write(bytes, 0, bytes.length);

			String format2 = CONTENT_SERVER + pathFile + CONTENT_SERVER_END;

			byte[] b = format2.getBytes(StandardCharsets.UTF_8);
			writer.write(b, 0, b.length);

			try (InputStream inputStream = new FileInputStream(tempFile)) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				final byte[] byteFichero = new byte[1024];
				int bytesRead;
				while ((bytesRead = inputStream.read(byteFichero)) != -1) {
					baos.write(byteFichero, 0, bytesRead);
				}
				writer.write(baos.toByteArray());
				writer.write(bytes2, 0, bytes2.length);
				writer.flush();
				baos.close();
			} catch (IOException e) {
				// Handle the exception when working with the input stream
				e.printStackTrace();
				LOGGER.error("Error reading the file: {}, {}", pathFile, e.getMessage());
			}
		} catch (IOException e) {
			// Handle the exception when working with the output stream
			e.printStackTrace();
			LOGGER.error("Error writing the file: {}, {}", pathFile, e.getMessage());
		}
	}

	// Delete the file from the server, this method is not used since DELETE request
	// is not allowed in HMI server side
	public void deleteFileFromServer(String fileName, LoginDTO loginDto) throws IOException {
		String modifiedFilename = fileName.replace(" ", "%20");
		String fullUrl = PROTOCOL + loginDto.getOvenHost() + DEL_FILES + modifiedFilename + DELETE_FILE
				+ loginDto.getMultiUsetoken();
		URL urlDelete = new URL(fullUrl);
		HttpURLConnection connectionDelete = null;

		try {
			connectionDelete = (HttpURLConnection) urlDelete.openConnection();
			connectionDelete.setRequestProperty("Cookie", SIEMENS_COOKIE + loginDto.getCookie());
			connectionDelete.setRequestMethod("DELETE");

			connectionDelete.connect();

			int responseCode = connectionDelete.getResponseCode();
			if (responseCode == HttpURLConnection.HTTP_OK) {
				// File deletion successful
				LOGGER.info("File deleted successfully: {}", fullUrl);
			} else {
				// File deletion failed
				LOGGER.error("Failed to delete the file: {}, Response Code: {}", fullUrl, responseCode);
			}
		} catch (IOException e) {
			e.printStackTrace();
			LOGGER.error("Error deleting the file: {}, {}", fullUrl, e.getMessage());
		} finally {
			if (connectionDelete != null) {
				connectionDelete.disconnect();
			}
		}
	}

}
