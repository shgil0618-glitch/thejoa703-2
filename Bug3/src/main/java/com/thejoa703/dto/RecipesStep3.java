package com.thejoa703.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecipesStep3 {

	  	private int stepMapId;
	    private int recipeId;
	    private String stepDesc;
	    private String stepImage;
		public String getDescription() {
			// TODO Auto-generated method stub
			return null;
		}
}
