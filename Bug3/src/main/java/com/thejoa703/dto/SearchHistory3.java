package com.thejoa703.dto;

import java.sql.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchHistory3 {

	private int searchId;
    private int appUserId;
    private String keyword;
    private Date createdAt;
}
