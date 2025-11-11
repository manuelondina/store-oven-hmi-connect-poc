package com.supermarket.ovenupdate.poc.infraestructure.service;

import static com.supermarket.ovenupdate.poc.constants.PocServerEnum.PORT;
import static com.supermarket.ovenupdate.poc.constants.PocServerEnum.PROTOCOL;
import static com.supermarket.ovenupdate.poc.constants.PocServerEnum.SOAP_RUNTIME;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.supermarket.ovenupdate.poc.constants.MessageTypeEnum;

@Service
public class HMILoaderService {

    @Autowired
    private SSEService sseService;

    private static final Logger logger = LoggerFactory.getLogger(HMILoaderService.class);

    private final RestTemplate restTemplate;

    public HMILoaderService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // Init HMI Load process
    public int initHMILoad(String oven, String ovenURL, HttpHeaders headers)
            throws IOException, InterruptedException {
        try {
            // Check if oven is connected
            URL url = new URL(PROTOCOL + ovenURL + PORT + SOAP_RUNTIME);
            if (!initHMILoadRequest(oven, url, headers)) {
                sseService.sendMessage(oven, MessageTypeEnum.ERRORCONEXIONHMI);
                logger.error("InitHMILoad: Error conectando al HMI.");
                return -1;
            }
            // Set variable in ListaAcciones
            if (!listaAcciones(url, headers)) {
                sseService.sendMessage(oven, MessageTypeEnum.ERRORHMI);
                logger.error("InitHMILoad: Error seteando variable en ListaAcciones.");
                return -1;
            }
            // Execute ListaAcciones
            if (!ejecutarListaAcciones(oven, url, headers)) {
                sseService.sendMessage(oven, MessageTypeEnum.ERRORHMI);
                logger.error("InitHMILoad: Error seteando variable en EjecutarListaAcciones.");
                return -1;
            }
            // Get HMI response
            String hmiResponse = hmiResponse(url, headers);
            // Handle HMI response and return the ID of the LibroRecetas
            if (hmiResponse.equals("2")) {
                int libroRecetas = getLibroRecetas(url, headers);
                if (libroRecetas != -1) {
                    logger.info("Activado el Libro de Recetas: {} para {}", libroRecetas, oven);
                    String libroRecetasString = String.valueOf(libroRecetas);
                    // Send message to the frontend with the ID of the LibroRecetas
                    sseService.sendMessageAndValue(oven, MessageTypeEnum.IDLIBRO, libroRecetasString);
                    return 0;
                }
            } else {
                handleHmiResponseError(oven, hmiResponse);
            }
        } catch (URISyntaxException e) {
            return -1;
        }
        return 0;
    }

    // Handle HMI response for errors of CSV or Lista_1.txt
    private void handleHmiResponseError(String oven, String errorCode) throws IOException {
        if (errorCode.equals("4")) {
            logger.error("Falta Lista_1.txt");
            sseService.sendMessage(oven, MessageTypeEnum.ERRORENLISTA1);
        } else {
            logger.error("Error de CSV: {} en {}", errorCode, oven);
            sseService.sendMessageAndValue(oven, MessageTypeEnum.ERRORENCSV, errorCode);
        }
    }

    // Init HMI Load process method
    private boolean initHMILoadRequest(String oven, URL url, HttpHeaders headers)
            throws IOException, URISyntaxException {
        sseService.sendMessage(oven, MessageTypeEnum.CONECTANDOHMI);
        String requestXml = "<x:Envelope\r\n    xmlns:x=\"http://schemas.xmlsoap.org/soap/envelope/\"\r\n    xmlns:wsd=\"http://tempuri.org/wsdl/\">\r\n    <x:Header/>\r\n    <x:Body>\r\n        <wsd:SetValue>\r\n            <wsd:A>ResultAcciones</wsd:A>\r\n            <wsd:B>''</wsd:B>\r\n        </wsd:SetValue>\r\n    </x:Body>\r\n</x:Envelope>";
        HttpEntity<String> requestEntity = new HttpEntity<>(requestXml, headers);
        URI uri = url.toURI();
        ResponseEntity<String> responseEntity = restTemplate.exchange(uri, HttpMethod.POST, requestEntity,
                String.class);

        String responseBody = responseEntity.getBody();
        if (responseBody != null) {
            int pos = responseBody.indexOf("<Result>", 0);
            String result = responseBody.substring(pos + 8, pos + 9);
            return result.equals("0");
        }
        return false;
    }

    // Set variable in ListaAcciones method
    private boolean listaAcciones(URL url, HttpHeaders headers) throws URISyntaxException {
        String lista1 = "Lista_1";
        String requestXml = "<x:Envelope\r\n    xmlns:x=\"http://schemas.xmlsoap.org/soap/envelope/\"\r\n    xmlns:wsd=\"http://tempuri.org/wsdl/\">\r\n    <x:Header/>\r\n    <x:Body>\r\n        <wsd:SetValue>\r\n            <wsd:A>ListaAcciones</wsd:A>\r\n            <wsd:B>"
                + lista1
                + "</wsd:B>\r\n        </wsd:SetValue>\r\n    </x:Body>\r\n</x:Envelope>";
        HttpEntity<String> requestEntity = new HttpEntity<>(requestXml, headers);
        URI uri = url.toURI();
        ResponseEntity<String> responseEntity = restTemplate.exchange(uri, HttpMethod.POST, requestEntity,
                String.class);

        String responseBody = responseEntity.getBody();
        if (responseBody != null) {
            int pos = responseBody.indexOf("<Result>", 0);
            String result = responseBody.substring(pos + 8, pos + 9);
            return result.equals("0");
        }
        return false;
    }

    // Execute ListaAcciones method
    private boolean ejecutarListaAcciones(String oven, URL url, HttpHeaders headers)
            throws IOException, URISyntaxException {
        sseService.sendMessage(oven, MessageTypeEnum.ACTIVANDOLIBRO);
        String requestXml = "<x:Envelope\r\n    xmlns:x=\"http://schemas.xmlsoap.org/soap/envelope/\"\r\n    xmlns:wsd=\"http://tempuri.org/wsdl/\">\r\n    <x:Header/>\r\n    <x:Body>\r\n        <wsd:SetValue>\r\n            <wsd:A>EjecutarListaAcciones</wsd:A>\r\n            <wsd:B>1</wsd:B>\r\n        </wsd:SetValue>\r\n    </x:Body>\r\n</x:Envelope>";
        HttpEntity<String> requestEntity = new HttpEntity<>(requestXml, headers);
        URI uri = url.toURI();
        ResponseEntity<String> responseEntity = restTemplate.exchange(uri, HttpMethod.POST, requestEntity,
                String.class);

        String responseBody = responseEntity.getBody();
        if (responseBody != null) {
            int pos = responseBody.indexOf("<Result>", 0);
            String result = responseBody.substring(pos + 8, pos + 9);
            return result.equals("0");
        }
        return false;
    }

    // Get HMI response method
    private String hmiResponse(URL url, HttpHeaders headers) throws InterruptedException, URISyntaxException {
        int numReintentos = 150;
        // Wait for HMI response to change from 1 to 2, 3 or 4
        for (int i = 1; i < numReintentos; i++) {
            Thread.sleep(2000);
            String requestXml = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:wsdl=\"http://tempuri.org/wsdl/\">\r\n   <soapenv:Header/>\r\n   <soapenv:Body>\r\n      <wsdl:GetValue>\r\n         <A type=\"\"xsd:string\"\">\"ResultAcciones\"</A>\r\n      </wsdl:GetValue>\r\n   </soapenv:Body>\r\n</soapenv:Envelope>";
            HttpEntity<String> requestEntity = new HttpEntity<>(requestXml, headers);
            URI uri = url.toURI();
            ResponseEntity<String> responseEntity = restTemplate.exchange(uri, HttpMethod.POST, requestEntity,
                    String.class);
            String responseBody = responseEntity.getBody();
            if (responseBody != null) {
                int pos = responseBody.indexOf("<Result>", 0);
                String result = responseBody.substring(pos);
                String[] sSplit = result.split("\\|");
                result = (sSplit.length > 1) ? sSplit[1] : "";
                // Check if HMI response is 2, 3 or 4
                if (result.equals("2")) {
                    return "2";
                } else if (result.equals("4")) {
                    return "4";
                } else if (result.equals("3") && sSplit.length > 2) {
                    StringBuilder restOfString = new StringBuilder(removeFirstDigit(sSplit[2]));
                    for (int j = 3; j < sSplit.length; j++) {
                        restOfString.append("|").append(removeFirstDigit(sSplit[j]));
                    }
                    String[] splitResult = restOfString.toString().split("</Result>");
                    return splitResult[0];
                }
            }
        }
        return null;
    }

    // Remove first digit from string for the HMI response when error it is 3 to not
    // add the 7
    private String removeFirstDigit(String str) {
        if (str.length() > 1) {
            return str.substring(1);
        }
        return str;
    }

    // Get LibroRecetas method that returns the ID of the LibroRecetas
    private int getLibroRecetas(URL url, HttpHeaders headers) throws InterruptedException, URISyntaxException {
        int numReintentos = 150;
        for (int i = 1; i < numReintentos; i++) {
            Thread.sleep(2000);
            String requestXml = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:wsdl=\"http://tempuri.org/wsdl/\">\r\n   <soapenv:Header/>\r\n   <soapenv:Body>\r\n      <wsdl:GetValue>\r\n         <A type=\"\"xsd:string\"\">\"ID_Libro_Recetas\"</A>\r\n      </wsdl:GetValue>\r\n   </soapenv:Body>\r\n</soapenv:Envelope>";
            HttpEntity<String> requestEntity = new HttpEntity<>(requestXml, headers);
            URI uri = url.toURI();
            ResponseEntity<String> responseEntity = restTemplate.exchange(uri, HttpMethod.POST, requestEntity,
                    String.class);
            String responseBody = responseEntity.getBody();
            if (responseBody != null) {
                int startTagPos = responseBody.indexOf("<Result>") + "<Result>".length();
                int endTagPos = responseBody.indexOf("</Result>", startTagPos);
                if (endTagPos != -1) {
                    String result = responseBody.substring(startTagPos, endTagPos).trim();
                    int idRecetas = Integer.parseInt(result);
                    return idRecetas;
                }
            }
        }
        return -1;
    }

}
