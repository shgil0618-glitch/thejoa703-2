package com.thejoa703.dto;

import java.sql.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiUsageHistory3 {

	private int aiHistId;
    private int appUserId;
    private String aiAction; // 어떤 AI API 사용했는지
    private Date createdAt;
}
