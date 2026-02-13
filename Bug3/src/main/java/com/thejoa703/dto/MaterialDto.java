package com.thejoa703.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaterialDto {
	   	private int materialid;
	    private String title;
	    private String imageurl;
	    private String season;
	    private String temperature;
	    private String calories100g;
	    private String efficacy;
	    private String buyguide;
	    private String trimguide;
	    private String storeguide;
	    private String category;
	    private LocalDateTime created_at;
	    private LocalDateTime updated_at; 
	    private String allergy;
}
