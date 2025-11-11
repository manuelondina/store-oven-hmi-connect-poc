package com.supermarket.ovenupdate.poc.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StartJobRequest {
    private String[] ovenList;
    private String pathFolder;
}
