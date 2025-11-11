package com.supermarket.ovenupdate.poc.infraestructure.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginDTO {

	private String user;
	private String password;
	private String multiUsetoken ;
	private String singleToken; 
	private String cookie ;
	private String ovenHost;


}
