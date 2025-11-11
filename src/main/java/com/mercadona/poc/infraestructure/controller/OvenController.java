package com.mercadona.poc.infraestructure.controller;

import com.mercadona.poc.application.dto.OvenDTO;
import com.mercadona.poc.application.service.OvenService;
import com.mercadona.poc.infraestructure.exception.IdMismatchException;
import com.mercadona.poc.infraestructure.exception.InvalidOvenException;
import com.mercadona.poc.infraestructure.exception.IpAddressInUseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Objects;
import java.util.Optional;

/**
 * REST controller for oven CRUD operations.
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class OvenController {

    private final OvenService ovenService;

    @PostMapping("/ovens")
    public ResponseEntity<OvenDTO> createOven(@Valid @RequestBody OvenDTO ovenDTO) {
        Optional<OvenDTO> ovenToCheck = ovenService.findOvenByIp(ovenDTO.getIpAddress());
        if(ovenToCheck.isPresent()){
            throw new IpAddressInUseException("La dirección IP " + ovenDTO.getIpAddress() + " ya está en uso.");
        } else {
            try {
                OvenDTO savedOven = ovenService.saveOven(ovenDTO);
                return new ResponseEntity<>(savedOven, HttpStatus.CREATED);
            } catch (DataIntegrityViolationException e) {
                throw new InvalidOvenException("No se ha podido guardar el horno, validación fallida: " + e.getMessage());
            } catch (Exception e) {
                // Captura de otras excepciones no esperadas.
                throw new RuntimeException("Error inesperado: " + e.getMessage());
            }
        }
    }

    @PutMapping("/ovens/{id}")
    public ResponseEntity<OvenDTO> updateOven(@PathVariable Long id,@Valid @RequestBody OvenDTO ovenDTO) {
        Optional<OvenDTO> ovenToCheck = ovenService.getOvenById(id);
        if(ovenToCheck.isPresent()){
            if(!Objects.equals(ovenToCheck.get().getId(), ovenDTO.getId())){
                throw new IpAddressInUseException("La dirección IP " + ovenDTO.getIpAddress() + " ya está en uso.");
            }
        }
        if (!id.equals(ovenDTO.getId())) {
            throw new IdMismatchException("La ID del parámetro (" + id + ") no coincide con la ID del objeto pasado (" + ovenDTO.getId() + ")");
        }
        OvenDTO updatedOven = ovenService.updateOven(ovenDTO);
        return ResponseEntity.ok(updatedOven);
    }

    @PatchMapping("/ovens/{id}")
    public ResponseEntity<OvenDTO> partialUpdateOven(@PathVariable Long id,@Valid @RequestBody OvenDTO ovenDTO) {
        Optional<OvenDTO> ovenToCheck = ovenService.findOvenByIp(ovenDTO.getIpAddress());
        if(ovenToCheck.isPresent()){
            if(!Objects.equals(ovenToCheck.get().getId(), ovenDTO.getId())){
                throw new IpAddressInUseException("La dirección IP " + ovenDTO.getIpAddress() + " ya está en uso.");
            }
        }
        if (!id.equals(ovenDTO.getId())) {
            throw new IdMismatchException("La ID del parámetro (" + id + ") no coincide con la ID del objeto pasado (" + ovenDTO.getId() + ")");
        }
        Optional<OvenDTO> updatedOven = ovenService.partialUpdateOven(ovenDTO);
        if (updatedOven.isPresent()) {
            return ResponseEntity.ok(updatedOven.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/ovens/list")
    public ResponseEntity<Page<OvenDTO>> getAllOvensPaginated(
            @RequestParam(required = false) String globalFilter,
            Pageable pageable) {
        Page<OvenDTO> ovensPage = ovenService.findAllOvens(pageable, globalFilter);
        return ResponseEntity.ok(ovensPage);
    }

    @GetMapping("/ovens/{id}")
    public ResponseEntity<OvenDTO> getOvenById(
            @PathVariable(required = true) Long id) {
        Optional<OvenDTO> ovensPage = ovenService.getOvenById(id);
        if(ovensPage.isPresent()) {
            return ResponseEntity.ok(ovensPage.get());
        }else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(null);
        }
    }


    @DeleteMapping("/ovens/{id}")
    public ResponseEntity<Void> deleteOven(@PathVariable Long id) {
        ovenService.deleteOven(id);
        return ResponseEntity.noContent().build();
    }
}
