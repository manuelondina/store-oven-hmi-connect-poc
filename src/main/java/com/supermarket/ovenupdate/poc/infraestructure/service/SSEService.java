package com.supermarket.ovenupdate.poc.infraestructure.service;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.supermarket.ovenupdate.poc.constants.MessageTypeEnum;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Service
public class SSEService {

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    private static final Logger logger = LoggerFactory.getLogger(HMILoaderService.class);

    public synchronized SseEmitter createAndAddEmitter() {
        SseEmitter emitter = new SseEmitter((long) -1);
        emitters.add(emitter);
        emitter.onCompletion(() -> {
            emitters.remove(emitter);
        });
        return emitter;
    }

    public void sendMessage(String ovenName, String message) throws IOException {
        List<SseEmitter> synchronizedEmitters = Collections.synchronizedList(emitters);

        for (SseEmitter emitter : synchronizedEmitters) {
            try {
                OvenEventData eventData = new OvenEventData(ovenName, message);
                String updatedMessage;
                logger.warn(eventData.getMessage());
                switch (eventData.getMessage()) {
                    case MessageTypeEnum.INICIANDO:
                        updatedMessage = "Iniciando servicio";
                        break;
                    case MessageTypeEnum.SUBIENDO:
                        updatedMessage = "Subiendo archivos...";
                        break;
                    case MessageTypeEnum.UNZIP:
                        updatedMessage = "Descomprimiendo...";
                        break;
                    case MessageTypeEnum.CARGA:
                        updatedMessage = "Empezando carga de horno.";
                        break;
                    case MessageTypeEnum.MONTANDO:
                        updatedMessage = "Montando el Libro de Recetas en HMI.";
                        break;
                    case MessageTypeEnum.CONECTANDOHMI:
                        updatedMessage = "Conectando al HMI";
                        break;
                    case MessageTypeEnum.ACTIVANDOLIBRO:
                        updatedMessage = "Activando Libro de Recetas";
                        break;
                    case MessageTypeEnum.ERRORHMI:
                        updatedMessage = "Error activando el Libro de recetas.";
                        break;
                    case MessageTypeEnum.ERROR:
                        updatedMessage = "Ha ocurrido un error.";
                        break;
                    case MessageTypeEnum.ERRORCONEXIONHMI:
                        updatedMessage = "Existe un problema de conexión al HMI";
                        break;
                    case MessageTypeEnum.ERRORENLISTA1:
                        updatedMessage = "No se encuentra Lista_1.txt";
                        break;
                    case MessageTypeEnum.ERRORENZIP:
                        updatedMessage = "Error de lectura en la carpeta descomprimida";
                        break;
                    default:
                        logger.warn(eventData.getMessage());
                        updatedMessage = "Error desconocido, por favor contacte con el Equipo de TI.";
                        break;
                }

                eventData.setMessage(updatedMessage);
                emitter.send(SseEmitter.event().data(eventData, MediaType.APPLICATION_JSON));
            } catch (IOException e) {
                emitter.completeWithError(e);
            }
        }
    }

    public synchronized void sendMessageAndValue(String ovenName, String message, String value) throws IOException {
        for (SseEmitter emitter : emitters) {
            try {
                OvenEventData eventData = new OvenEventData(ovenName, message);
                String updatedMessage;

                if (eventData.getMessage().equals(MessageTypeEnum.TIEMPO)) {
                    updatedMessage = "Tiempo transcurrido: " + value;
                } else if (eventData.getMessage().equals(MessageTypeEnum.ERRORARCHIVO)) {
                    updatedMessage = "Error en archivo: " + value;
                } else if (eventData.getMessage().equals(MessageTypeEnum.IDLIBRO)) {
                    updatedMessage = "Activado Libro de Recetas: " + value;
                } else if (eventData.getMessage().equals(MessageTypeEnum.ERRORENCSV)) {
                    updatedMessage = "No se encuentran los CSV en las lineas de Lista_1: " + value;
                } else {
                    updatedMessage = "Error desconocido, por favor contacte con el Equipo de TI.";
                }

                eventData.setMessage(updatedMessage);
                emitter.send(SseEmitter.event().data(eventData, MediaType.APPLICATION_JSON));
            } catch (IOException e) {
                emitter.completeWithError(e);
            }
        }
    }

    public synchronized void sendMessageAndCounters(String ovenName, String message, Integer contador,
            Integer contadorListaFiles)
            throws IOException {
        for (SseEmitter emitter : emitters) {
            try {
                OvenEventData eventData = new OvenEventData(ovenName, message);
                String updatedMessage;

                if (eventData.getMessage().equals(MessageTypeEnum.TERMINADO)) {
                    updatedMessage = "Total de archivos procesados: [" + contador + "/" + contadorListaFiles + "]";
                } else {
                    updatedMessage = "Error desconocido, por favor contacte con el Equipo de TI.";
                }

                eventData.setMessage(updatedMessage);
                emitter.send(SseEmitter.event().data(eventData, MediaType.APPLICATION_JSON));
            } catch (IOException e) {
                emitter.completeWithError(e);
            }
        }
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    private static class OvenEventData {
        private String ovenName;
        private String message;
    }
}
