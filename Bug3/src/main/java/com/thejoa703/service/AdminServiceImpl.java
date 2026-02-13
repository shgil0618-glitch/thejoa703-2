package com.thejoa703.service;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.thejoa703.dao.AdminDao;

@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private AdminDao adminDao;

	    @Override
	    public List<Map<String, Object>> findAllUsers() {
	        return adminDao.findAllUsers();
	    }

    @Override
    @Transactional
    public void updateUserStatus(Map<String, Object> params) {
        // DAO의 메서드명도 upsertUserStatus로 일치시키기
        adminDao.upsertUserStatus(params);
    }
}