package com.supermarket.ovenupdate.poc.infraestructure.service;

import static com.supermarket.ovenupdate.poc.constants.PocServerEnum.BOUNDARY;
import static com.supermarket.ovenupdate.poc.constants.PocServerEnum.FILES;
import static com.supermarket.ovenupdate.poc.constants.PocServerEnum.MULTIPART_CONTENT_TYPE;
import static com.supermarket.ovenupdate.poc.constants.PocServerEnum.PROTOCOL;
import static com.supermarket.ovenupdate.poc.constants.PocServerEnum.SIEMENS_COOKIE;
import static com.supermarket.ovenupdate.poc.constants.PocServerEnum.UP_SERVER;
import static com.supermarket.ovenupdate.poc.constants.PocServerEnum.VERBO;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.supermarket.ovenupdate.poc.infraestructure.dto.LoginDTO;

@Service
public class DeleteFilesFromServerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeleteFilesFromServerService.class);

    @Autowired
    private SendFileToServerService fileServer;

    // Delete all files from server
    public void deleteFiles(LoginDTO loginDTO)
            throws IOException {

        HttpURLConnection connectionSend = prepareConnection(loginDTO);

        connectionSend.connect();

        Document doc = Jsoup.connect(connectionSend.getURL().toString())
                .header("Cookie", connectionSend.getRequestProperty("Cookie"))
                .get();

        extractDataFromDocument(doc, loginDTO);

    }

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

    private void extractDataFromDocument(Document doc, LoginDTO loginDTO) throws IOException {
        Element firstDiv = doc.select("div").first();
        Element table = firstDiv.select("table").first();
        Elements rows = table.select("tr");

        for (int i = 0; i < rows.size(); i++) {
            Element row = rows.get(i);
            Elements columns = row.select("td");

            if (columns.size() >= 2) {
                Element nameColumn = columns.get(1);
                String name = nameColumn.text();

                if (!isFileSystem(name)) {
                    LOGGER.info("Name: {}", name);
                    // TODO: Delete file from server still needs to be implemented with a correct
                    // method
                    fileServer.deleteFileFromServer(name, loginDTO);
                }
            }
        }
    }

    // Check if the file is a file system
    private boolean isFileSystem(String name) {
        return name.equalsIgnoreCase("actual.html")
                || name.equalsIgnoreCase("Browse.html")
                || name.equalsIgnoreCase("Browse.html.cms")
                || name.equalsIgnoreCase("control.html")
                || name.equalsIgnoreCase("control.html.cms")
                || name.equalsIgnoreCase("ExportRecipes.html")
                || name.equalsIgnoreCase("grid.css")
                || name.equalsIgnoreCase("grid_mainrecetas.css")
                || name.equalsIgnoreCase("Grid_submenuDistintos.html")
                || name.equalsIgnoreCase("grid_submenu.html")
                || name.equalsIgnoreCase("grid_menu.html")
                || name.equalsIgnoreCase("RemoteControl.html")
                || name.equalsIgnoreCase("RemoteControl.html.cms")
                || name.equalsIgnoreCase("selected.html")
                || name.equalsIgnoreCase("start.html")
                || name.equalsIgnoreCase("start.html.cms")
                || name.equalsIgnoreCase("StatusDetails.html")
                || name.equalsIgnoreCase("StatusDetails.html.cms")
                || name.equals("..")
                || !name.contains(".");
    }

}