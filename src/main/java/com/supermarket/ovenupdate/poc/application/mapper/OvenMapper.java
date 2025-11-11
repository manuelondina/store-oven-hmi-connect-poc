package com.supermarket.ovenupdate.poc.application.mapper;

import com.supermarket.ovenupdate.poc.application.dto.OvenDTO;
import com.supermarket.ovenupdate.poc.domain.Oven;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface OvenMapper extends EntityMapper<OvenDTO, Oven> {
}
