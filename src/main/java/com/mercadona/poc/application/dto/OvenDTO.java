package com.mercadona.poc.application.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Getter
@Setter
@ToString
public class OvenDTO implements Serializable {

    private Long id;

    @NotBlank(message = "El nombre no puede ser nulo o vacío")
    private String name;

    @NotBlank(message = "La IP no puede ser nula o vacía")
    private String ipAddress;

    private String location;
}
