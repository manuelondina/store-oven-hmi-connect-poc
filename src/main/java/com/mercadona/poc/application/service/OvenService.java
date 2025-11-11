package com.mercadona.poc.application.service;

import com.mercadona.poc.application.dto.OvenDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


import java.util.List;
import java.util.Optional;

public interface OvenService {

    List<OvenDTO> getAllOvens();

    Page<OvenDTO> findAllOvens(Pageable pageable, String globalFilter);

    OvenDTO saveOven(OvenDTO ovenDTO);

    OvenDTO updateOven(OvenDTO ovenDTO);

    Optional<OvenDTO> partialUpdateOven(OvenDTO ovenDTO);

    void deleteOven(Long ovenId);

    Optional<OvenDTO> getOvenById(Long ovenId);

    List<OvenDTO> getOvensByLocation(String region);

    List<OvenDTO> getOvensByName(String ovenName);

    Optional<OvenDTO> findOvenByIp(String ip);

}
