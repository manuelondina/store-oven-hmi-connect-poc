package com.mercadona.poc.application.service.impl;

import com.mercadona.poc.application.dto.OvenDTO;
import com.mercadona.poc.application.mapper.OvenMapper;
import com.mercadona.poc.application.service.OvenService;
import com.mercadona.poc.domain.Oven;
import com.mercadona.poc.infraestructure.repository.OvenRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
public class OvenServiceImpl implements OvenService {

    private final OvenRepository ovenRepository;

    private final OvenMapper ovenMapper;

    public OvenServiceImpl(OvenRepository ovenRepository, OvenMapper ovenMapper) {
        this.ovenRepository = ovenRepository;
        this.ovenMapper = ovenMapper;
    }

    @Override
    public List<OvenDTO> getAllOvens() {
        return ovenMapper.toDto(ovenRepository.findAll());
    }

    @Override
    public Page<OvenDTO> findAllOvens(Pageable pageable, String globalFilter) {
        Specification<Oven> spec = Specification
                .where(nameLike(globalFilter))
                .or(ipLike(globalFilter))
                .or(locationLike(globalFilter));
        Page<Oven> ovens = ovenRepository.findAll(spec, pageable);
        return ovens.map(ovenMapper::toDto);
    }



    @Override
    @Transactional
    public OvenDTO saveOven(OvenDTO ovenDTO) {
        Oven ovenSaved = ovenRepository.save(ovenMapper.toEntity(ovenDTO));
        return ovenMapper.toDto(ovenSaved);
    }

    @Override
    @Transactional
    public OvenDTO updateOven(OvenDTO ovenDTO) {
        Oven oven = ovenRepository.save(ovenMapper.toEntity(ovenDTO));
        return ovenMapper.toDto(oven);
    }

    @Override
    @Transactional
    public Optional<OvenDTO> partialUpdateOven(OvenDTO ovenDTO) {

        return ovenRepository
                .findById(ovenDTO.getId())
                .map(existingCard -> {
                    ovenMapper.partialUpdate(existingCard, ovenDTO);

                    return existingCard;
                })
                .map(ovenRepository::save)
                .map(ovenMapper::toDto);
    }


    @Override
    @Transactional
    public void deleteOven(Long ovenId) {
        ovenRepository.deleteById(ovenId);
    }

    @Override
    public Optional<OvenDTO> getOvenById(Long ovenId) {
        return ovenRepository.findById(ovenId).map(ovenMapper::toDto);
    }

    @Override
    public List<OvenDTO> getOvensByLocation(String region) {
        List<Oven> ovens = ovenRepository.findByLocationContainsIgnoreCase(region);
        return ovens.stream()
                .map(ovenMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<OvenDTO> getOvensByName(String ovenName) {
        List<Oven> ovens = ovenRepository.findByNameContainsIgnoreCase(ovenName);
        return ovens.stream()
                .map(ovenMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<OvenDTO> findOvenByIp(String ip) {
        return ovenRepository.findByIpAddress(ip).map(ovenMapper::toDto);
    }

    private Specification<Oven> nameLike(String globalFilter) {
        return (root, query, cb) -> globalFilter == null ? null : cb.like(cb.lower(root.get("name")), "%" + globalFilter.toLowerCase() + "%");
    }

    private Specification<Oven> ipLike(String globalFilter) {
        return (root, query, cb) -> globalFilter == null ? null : cb.like(cb.lower(root.get("ipAddress")), "%" + globalFilter.toLowerCase() + "%");
    }

    private Specification<Oven> locationLike(String globalFilter) {
        return (root, query, cb) -> globalFilter == null ? null : cb.like(cb.lower(root.get("location")), "%" + globalFilter.toLowerCase() + "%");
    }
}
