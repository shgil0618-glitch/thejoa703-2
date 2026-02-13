package com.thejoa703.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserStatusDto {

    /** 사용자 ID (BUG3.APPUSERID) */
    private Integer appUserId;

    /** 상태: ACTIVE, SUSPEND, WITHDRAW */
    private String status;

    /** 정지 사유 */
    private String suspendReason;

    /** 정지 해제일 */
    private LocalDate suspendUntil;

    /** 상태 변경 시각 */
    private LocalDateTime updatedAt;

}
