package com.mercadona.poc.application.mapper;

import com.mercadona.poc.application.dto.OvenDTO;
import com.mercadona.poc.domain.Oven;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-11T13:24:03+0000",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.17 (Eclipse Adoptium)"
)
@Component
public class OvenMapperImpl implements OvenMapper {

    @Override
    public Oven toEntity(OvenDTO dto) {
        if ( dto == null ) {
            return null;
        }

        Oven oven = new Oven();

        oven.setId( dto.getId() );
        oven.setName( dto.getName() );
        oven.setIpAddress( dto.getIpAddress() );
        oven.setLocation( dto.getLocation() );

        return oven;
    }

    @Override
    public OvenDTO toDto(Oven entity) {
        if ( entity == null ) {
            return null;
        }

        OvenDTO ovenDTO = new OvenDTO();

        ovenDTO.setId( entity.getId() );
        ovenDTO.setName( entity.getName() );
        ovenDTO.setIpAddress( entity.getIpAddress() );
        ovenDTO.setLocation( entity.getLocation() );

        return ovenDTO;
    }

    @Override
    public List<Oven> toEntity(List<OvenDTO> dtoList) {
        if ( dtoList == null ) {
            return null;
        }

        List<Oven> list = new ArrayList<Oven>( dtoList.size() );
        for ( OvenDTO ovenDTO : dtoList ) {
            list.add( toEntity( ovenDTO ) );
        }

        return list;
    }

    @Override
    public List<OvenDTO> toDto(List<Oven> entityList) {
        if ( entityList == null ) {
            return null;
        }

        List<OvenDTO> list = new ArrayList<OvenDTO>( entityList.size() );
        for ( Oven oven : entityList ) {
            list.add( toDto( oven ) );
        }

        return list;
    }

    @Override
    public void partialUpdate(Oven entity, OvenDTO dto) {
        if ( dto == null ) {
            return;
        }

        if ( dto.getId() != null ) {
            entity.setId( dto.getId() );
        }
        if ( dto.getName() != null ) {
            entity.setName( dto.getName() );
        }
        if ( dto.getIpAddress() != null ) {
            entity.setIpAddress( dto.getIpAddress() );
        }
        if ( dto.getLocation() != null ) {
            entity.setLocation( dto.getLocation() );
        }
    }
}
