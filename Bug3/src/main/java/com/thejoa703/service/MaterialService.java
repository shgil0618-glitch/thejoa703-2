package com.thejoa703.service;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.thejoa703.dto.MaterialDto;

public interface MaterialService {
	public int insertMaterial(MaterialDto dto);
	public int updateMaterial (MaterialDto dto);
	public int deleteMaterial (int materialid);
	public List<MaterialDto> MaterialList();
	public MaterialDto selectTitle(String title);
	public MaterialDto selectMaterial(int materialid);
	public int insert2Material(MultipartFile file, MaterialDto dto);
	public int update2Material(MultipartFile file, MaterialDto dto);
	public List<MaterialDto> select10(int pstartno, String keyword);
	public int selectTotalCnt(String keyword);
	public void saveTrendData(int materialId, String keyword, String jsonResponse);
}
