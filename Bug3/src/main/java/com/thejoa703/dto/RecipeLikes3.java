package com.thejoa703.dto;

import java.sql.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecipeLikes3 {

	private int appUserId;
    private int recipeId;
    private Date createdAt;
    
    public RecipeLikes3(int appUserId, int recipeId) {
        this.appUserId = appUserId;
        this.recipeId = recipeId;
    }
}
