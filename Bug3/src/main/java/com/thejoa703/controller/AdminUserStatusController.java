package com.thejoa703.controller;

import java.util.Map;
import java.util.HashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.thejoa703.service.AdminService;
import com.thejoa703.service.UserStatusService;

@Controller
@RequestMapping("/admin/user_status")
public class AdminUserStatusController {

    @Autowired
    private UserStatusService userStatusService; // 메일 발송 및 목록 조회용

    @Autowired
    private AdminService adminService; // 실제 DB 상태 변경용

    @GetMapping
    public String userStatusPage(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "1") int page, 
            Model model) {
        Map<String, Object> data = userStatusService.getUserList(keyword, page);
        model.addAllAttributes(data);
        model.addAttribute("keyword", keyword); 
        return "admin/user_status";
    }

    @GetMapping("/table")
    public String userStatusTable(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "1") int page, 
            Model model) {
        Map<String, Object> data = userStatusService.getUserList(keyword, page);
        model.addAllAttributes(data);
        model.addAttribute("keyword", keyword);
        return "admin/user_status :: #userTableArea";
    }

    @PostMapping("/activate")
    @ResponseBody
    public String activateUser(@RequestParam int appUserId) {
        // 1. DB 상태 변경
        Map<String, Object> params = new HashMap<>();
        params.put("appUserId", appUserId);
        params.put("status", "ACTIVE");
        adminService.updateUserStatus(params);

        // 2. 기존 메일 발송 기능 호출 (기존에 쓰시던 로직 유지)
        userStatusService.activateUser(appUserId);
        
        return "success";
    }

    @PostMapping("/suspend")
    @ResponseBody
    public String suspendUser(
            @RequestParam int appUserId,
            @RequestParam String reason,
            @RequestParam String untilDate) {
        
        // 1. DB 상태 변경 (사유와 기한 포함)
        Map<String, Object> params = new HashMap<>();
        params.put("appUserId", appUserId);
        params.put("status", "SUSPEND");
        params.put("reason", reason);
        params.put("untilDate", untilDate);
        adminService.updateUserStatus(params);

        // 2. 기존 메일 발송 기능 호출 (사유와 기한을 담아 메일 발송)
        userStatusService.suspendUser(appUserId, reason, untilDate);
        
        return "success";
    }
}