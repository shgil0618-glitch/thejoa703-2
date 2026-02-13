package com.thejoa703.service;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.thejoa703.dao.MaterialDao;
import com.thejoa703.dto.MaterialDto;


@Service
public class MaterialServiceImpl implements MaterialService {
	 @Autowired MaterialDao dao;
	 @Autowired PasswordEncoder pwencoder;

	@Override public int insertMaterial(MaterialDto dto) {	
		try {
			return dao.insertMaterial(dto);
		}catch(DataAccessException e) {e.printStackTrace();
		return 0;
		}
	}

	@Override
	public int updateMaterial(MaterialDto dto) {
		try {
			return dao.updateMaterial(dto);
		}catch(DataAccessException e) {
			e.printStackTrace();
			return 0;
			}
	}
	@Override
	public int deleteMaterial(int materialid) {
		try{
			return dao.deleteMaterial(materialid);
		}catch (DataAccessException e) {
			e.printStackTrace();
		return 0;
		}
	}
	@Override
	public List<MaterialDto> MaterialList() {
		try{
			return dao.MaterialList();
		}catch (DataAccessException e) {
			e.printStackTrace();	
			return null;
		}
	}
	@Override
	public MaterialDto selectMaterial(int materialid) {
		try {
			return dao.selectMaterial(materialid);
		}catch(DataAccessException e) {
			e.printStackTrace();
			return null;
		}
	}
	public MaterialDto findIntegratedMaterial(String inputName) {
	    MaterialDto dto = dao.selectTitle(inputName);
	    if (dto != null) return dto;
	    List<MaterialDto> allMaterials = dao.MaterialList();
	    for (MaterialDto m : allMaterials) {
	        // 예: 입력값이 "유기농 사과"이고 DB에 "사과"가 있다면 매칭
	        if (inputName.contains(m.getTitle())) {
	            return m; 
	        }
	    }
	    return null;
	}
	
	
	@Override
	public MaterialDto selectTitle(String title) {
	try {
		return dao.selectTitle(title);
	}catch(DataAccessException e) {
		e.printStackTrace();
		return null;
	}
}

	@Override
	public int insert2Material(MultipartFile file, MaterialDto dto) {

	    // 업로드 폴더
	    String uploadPath = "C:/upload/";
	    File folder = new File(uploadPath);
	    if (!folder.exists()) folder.mkdirs();

	    // 파일 업로드 안 했으면 기본 이미지
	    if (file == null || file.isEmpty()) {
	        if (dto.getImageurl() == null || dto.getImageurl().trim().isEmpty()) {
	            dto.setImageurl("default.png");
	        }
	        return dao.insertMaterial(dto);
	    }

	    // ✅ 저장할 파일명 만들기 (UUID_원본파일명.확장자)
	    String original = file.getOriginalFilename();
	    if (original == null) original = "upload.png";

	    // 확장자 추출
	    String ext = "";
	    int dot = original.lastIndexOf('.');
	    if (dot >= 0) ext = original.substring(dot); // .png 같은 형태

	    // 파일명(확장자 제외)만 추출
	    String base = (dot >= 0) ? original.substring(0, dot) : original;

	    // 파일명에 위험한 문자 제거(공백/특수문자 등)
	    base = base.replaceAll("[\\\\/:*?\"<>|\\s]+", "_");

	    // 저장 파일명
	    String savedName = java.util.UUID.randomUUID().toString() + "_" + base + ext;

	    try {
	        file.transferTo(new File(uploadPath + savedName));
	        dto.setImageurl(savedName); // ✅ DB에도 저장 파일명으로 넣기
	    } catch (IOException e) {
	        e.printStackTrace();
	        dto.setImageurl("default.png");
	    }

	    return dao.insertMaterial(dto);
	}
	
	@Override
	public int update2Material(MultipartFile file, MaterialDto dto) {

	    String uploadPath = "C:/upload/";
	    File folder = new File(uploadPath);
	    if (!folder.exists()) folder.mkdirs();

	    // ✅ 새 파일 업로드가 없으면 기존 이미지 유지
	    if (file == null || file.isEmpty()) {
	        MaterialDto existing = dao.selectMaterial(dto.getMaterialid());
	        if (existing != null && existing.getImageurl() != null && !existing.getImageurl().isEmpty()) {
	            dto.setImageurl(existing.getImageurl());
	        } else {
	            dto.setImageurl("default.png");
	        }
	        return dao.updateMaterial(dto);
	    }

	    // ✅ 새 파일 업로드가 있으면 새 파일 저장 + DB 업데이트
	    String original = file.getOriginalFilename();
	    if (original == null) original = "upload.png";

	    String ext = "";
	    int dot = original.lastIndexOf('.');
	    if (dot >= 0) ext = original.substring(dot);

	    String base = (dot >= 0) ? original.substring(0, dot) : original;
	    base = base.replaceAll("[\\\\/:*?\"<>|\\s]+", "_");

	    String savedName = java.util.UUID.randomUUID().toString() + "_" + base + ext;

	    try {
	        file.transferTo(new File(uploadPath + savedName));
	        dto.setImageurl(savedName); // ✅ DB에도 저장 파일명으로 넣기
	    } catch (IOException e) {
	        e.printStackTrace();
	        // 업로드 실패하면 기존 이미지 유지
	        MaterialDto existing = dao.selectMaterial(dto.getMaterialid());
	        if (existing != null) dto.setImageurl(existing.getImageurl());
	    }

	    return dao.updateMaterial(dto);
	}

	/*
	 * @Override public List<MaterialDto> select10(int pstartno) { HashMap<String,
	 * Object> para = new HashMap(); int start=(pstartno-1)*10 + 1;
	 * para.put("start", start); para.put("end", start + 10 -1); return
	 * dao.select10(para); }
	 */
	@Override
	public List<MaterialDto> select10(int pstartno, String keyword) {
	    HashMap<String, Object> para = new HashMap<>();
	    para.put("start", (pstartno - 1) * 10 + 1);
	    para.put("end", pstartno * 10);
	    para.put("keyword", keyword); // MyBatis XML에서 #{keyword}로 쓰기 위해 담음
	    
	    return dao.select10(para);
	}
	
	/*
	 * @Override public int selectTotalCnt() {return dao.selectTotalCnt();}
	 */
	@Override
	public int selectTotalCnt(String keyword) {
	    return dao.selectTotalCnt(keyword); // DAO에도 keyword 전달
	}
	@Autowired
    private MaterialDao materialDao;
	 
	@Override
	public void saveTrendData(int materialId, String keyword, String jsonResponse) {
	    // 1. 변수명이 중복되지 않게 map이라는 이름을 사용
	    // 2. HashMap<> 앞에 타입을 명시 (Java 11 기준)
	    Map<String, Object> map = new java.util.HashMap<String, Object>(); 
	    
	    map.put("materialId", materialId);
	    map.put("keyword", keyword);
	    map.put("periodData", jsonResponse);
	    
	    // 3. 필드명이 materialDao인지 확인 (에러창에 materialMapper를 못찾는다고 나옴)
	    materialDao.insertTrend(map);
	    }
		
	}

