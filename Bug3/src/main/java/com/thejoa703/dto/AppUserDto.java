package com.thejoa703.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppUserDto implements Serializable {

	private static final long serialVersionUID = 1L;

	private Integer appUserId; 
	private String email;    
	private String password;    
	private String createdAt;  
	private String ufile;      
	private String mobile;    
	private String nickname;
	private String provider;    
	private String providerId;
	private String role;
	private String postcode;
	private String address;
	private String detailAddress;
	
	public AppUserDto(String email, String provider) {
		this.email = email;
		this.provider = provider;
	} 
}
