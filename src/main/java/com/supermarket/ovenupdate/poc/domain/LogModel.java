package com.supermarket.ovenupdate.poc.domain;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LogModel {
    
	private String country;
    private String autonomicCommunity;
    private String province;
    private int centerCode;
    private String center;

    private int ovenNumber;
    private String deviceType;
    private String version;
    private String device;
    private String ip;

    private Date date;
    private String level;
    private int thread;
    private int phase;
    private String description;
    
}