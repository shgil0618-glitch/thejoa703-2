package com.thejoa703.dto;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class AppUserAuthDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private String email;        
    private String password;    
    private List<AuthDto> authList;
}
