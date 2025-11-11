package com.mercadona.poc.application.mapper;

import com.mercadona.poc.application.dto.OvenDTO;
import com.mercadona.poc.domain.Oven;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface OvenMapper extends EntityMapper<OvenDTO, Oven> {
}
