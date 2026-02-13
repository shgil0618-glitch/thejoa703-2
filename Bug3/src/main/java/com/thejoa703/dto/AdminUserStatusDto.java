package com.thejoa703.dto;

import java.time.LocalDate;

import lombok.Data;

@Data
public class AdminUserStatusDto {
    private int appUserId;
    private String email;
    private String nickname;
    private String provider;
    private String status;
    private String suspendReason;
    private LocalDate suspendUntil;
}


