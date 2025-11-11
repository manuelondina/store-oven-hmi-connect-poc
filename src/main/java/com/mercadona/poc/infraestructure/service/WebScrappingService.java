package com.mercadona.poc.infraestructure.service;

import static com.mercadona.poc.constants.PocServerEnum.BOUNDARY;
import static com.mercadona.poc.constants.PocServerEnum.FILES;
import static com.mercadona.poc.constants.PocServerEnum.MULTIPART_CONTENT_TYPE;
import static com.mercadona.poc.constants.PocServerEnum.PROTOCOL;
import static com.mercadona.poc.constants.PocServerEnum.SIEMENS_COOKIE;
import static com.mercadona.poc.constants.PocServerEnum.UP_SERVER;
import static com.mercadona.poc.constants.PocServerEnum.VERBO;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.mercadona.poc.constants.UpdatingTimeEnum;
import com.mercadona.poc.infraestructure.dto.LoginDTO;
import com.mercadona.poc.domain.UnzippedFiles;
import com.mercadona.poc.domain.WebScraping;

@Service
public class WebScrappingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SendFilesService.class);

    // Web Scrapping Service
    public List<UnzippedFiles> webScrapingService(List<UnzippedFiles> unzippedFiles, LoginDTO loginDTO)
            throws IOException {
        // Prepare the connection using the loginDTO data
        HttpURLConnection connectionSend = prepareConnection(loginDTO);
        // Open the connection
        connectionSend.connect();
        // Get the document from the connection
        Document doc = Jsoup.connect(connectionSend.getURL().toString())
                .header("Cookie", connectionSend.getRequestProperty("Cookie"))
                .get();
        // Extract the data from the document of the web page
        List<WebScraping> extractedData = extractDataFromDocument(doc);

        return getNonExistentFiles(extractedData, unzippedFiles);
    }

    // Prepare the connection method
    private HttpURLConnection prepareConnection(LoginDTO loginDTO) throws IOException {
        URL urlSend = new URL(PROTOCOL + loginDTO.getOvenHost() + FILES + UP_SERVER);
        HttpURLConnection connectionSend = (HttpURLConnection) urlSend.openConnection();
        connectionSend.setRequestProperty("Cookie", SIEMENS_COOKIE + loginDTO.getCookie());
        connectionSend.setRequestProperty("Content-Type", MULTIPART_CONTENT_TYPE + BOUNDARY);
        connectionSend.setRequestMethod(VERBO);
        connectionSend.setChunkedStreamingMode(0);
        connectionSend.setDoInput(true);
        connectionSend.setDoOutput(true);
        return connectionSend;
    }

    // Extract the data from the document of the web page method
    private List<WebScraping> extractDataFromDocument(Document doc) {

        Element firstDiv = doc.select("div").first();
        Element table = firstDiv.select("table").first();
        Elements rows = table.select("tr");
        // Extract the data from the table in Browse File
        List<WebScraping> extractedData = new ArrayList<>();
        // Format the date to compare it with the current date
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEE MMM d HH:mm:ss yyyy");

        // Extract the data from the table checking each column ignoring the first one
        // for each row
        for (int i = 0; i < rows.size(); i++) {
            Element row = rows.get(i);
            Elements columns = row.select("td");

            if (columns.size() >= 2) {
                // Extract the name of the file
                Element nameColumn = columns.get(1);
                String name = nameColumn.text();
                // Check if the file is valid
                if (isValidFile(name)) {
                    // Extract the file size
                    Element sizeColumn = columns.get(2);
                    // Extract the date and time of the file
                    Element dateTimeColumn = columns.get(4);
                    String size = sizeColumn.text();
                    String dateTime = dateTimeColumn.text();

                    try {
                        // Format the date to compare it with the current date
                        LocalDateTime uploadDateTime = LocalDateTime.parse(dateTime, dateFormatter);
                        // Check if the file was uploaded in the last 24 hours
                        LocalDateTime twentyFourHoursBefore = LocalDateTime.now().minusHours(UpdatingTimeEnum.HOURS_BEFORE);
                        // Check if the file was uploaded in the next 24 hours
                        LocalDateTime twentyFourHoursAfter = LocalDateTime.now().plusHours(UpdatingTimeEnum.HOURS_AFTER);
                        // Check if the file was uploaded in the range of 24 hours before and after
                        if (isWithinTimeRange(uploadDateTime, twentyFourHoursBefore, twentyFourHoursAfter)) {
                            WebScraping data = new WebScraping(name, size);
                            extractedData.add(data);
                        }
                    } catch (DateTimeParseException e) {
                        // Handle the exception appropriately
                        e.printStackTrace();
                    }
                }
            }
        }
        return extractedData;
    }

    // Check if the file is valid method
    private boolean isValidFile(String name) {
        return name.equalsIgnoreCase("Lista_1.txt")
                || name.endsWith(".html")
                || name.endsWith(".csv")
                || name.endsWith(".png")
                || name.endsWith(".exe");
    }

    // Check if the file was uploaded in the range of 24 hours before and after
    // method
    private boolean isWithinTimeRange(LocalDateTime dateTime, LocalDateTime before, LocalDateTime after) {
        return dateTime.isAfter(before) && dateTime.isBefore(after);
    }

    // Get the non existent files method
    private List<UnzippedFiles> getNonExistentFiles(List<WebScraping> extractedData,
            List<UnzippedFiles> unzippedFiles) {
        List<UnzippedFiles> nonExistentFiles = new ArrayList<>();

        // Check if the file exists in the list of unzipped files
        for (UnzippedFiles unzippedFile : unzippedFiles) {
            boolean fileExists = false;
            // Check if the file exists in the list of extracted data
            for (WebScraping data : extractedData) {
                if (unzippedFile.getName().equalsIgnoreCase(data.getName()) &&
                        unzippedFile.getSize().equals(data.getSize())) {
                    unzippedFile.setUploaded(true);
                    fileExists = true;
                    break;
                }
            }
            // If the file doesn't exist in the list of extracted data, add it to the list
            // of non existent files
            if (!fileExists) {
                String filePath = unzippedFile.getFilePath();
                unzippedFile.setFilePath(filePath);
                nonExistentFiles.add(unzippedFile);
            }
        }
        return nonExistentFiles;
    }

}
