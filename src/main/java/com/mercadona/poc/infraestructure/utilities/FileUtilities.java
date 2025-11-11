package com.mercadona.poc.infraestructure.utilities;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercadona.poc.infraestructure.service.SendFilesService;

public class FileUtilities {

	private static final Logger LOGGER = LoggerFactory.getLogger(SendFilesService.class);

	private FileUtilities() {
	}

	// Delete all files and directories inside the parent folder for each oven copy
	// zip file
	public static void deleteAll(List<Path> listaFiles) {

		// Get the parent folder of the files
		Path parentFolder = listaFiles.get(0).getParent();

		// Check if the listaFiles is not empty
		if (listaFiles.isEmpty()) {
			LOGGER.warn("La lista de ficheros estaba vacia, no se han borrado. {}", parentFolder);
			return;
		}

		try (Stream<Path> pathStream = Files.walk(parentFolder)) {
			// Delete all files and directories inside the parent folder
			pathStream.sorted((p1, p2) -> -p1.compareTo(p2)) // Delete from innermost to outermost
					.forEach(path -> {
						try {
							Files.delete(path);
						} catch (IOException e) {
							LOGGER.error("Error en el borrado. {}", path);
							e.printStackTrace();
						}
					});

			// Delete the parent folder itself
			Files.deleteIfExists(parentFolder);
		} catch (IOException e) {
			LOGGER.error("Error en el borrado.", e);
			e.printStackTrace();
		}
	}

}
