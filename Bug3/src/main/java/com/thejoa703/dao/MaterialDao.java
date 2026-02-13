package com.thejoa703.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.web.multipart.MultipartFile;

import com.thejoa703.dto.MaterialDto;

@Mapper
public interface MaterialDao {
	public int insertMaterial(MaterialDto dto);
	public int updateMaterial (MaterialDto dto);
	public int deleteMaterial (int materialid);
	public List<MaterialDto> MaterialList();
	public MaterialDto selectMaterial(int materialid);
	public MaterialDto selectTitle(String materialid);
	public int insert2Material(MultipartFile file, MaterialDto dto);
	public int update2Material(MultipartFile file, MaterialDto dto);
	public List<MaterialDto>  select10(HashMap<String, Object>  para);
	public int selectTotalCnt(String keyword);
	public void insertTrend(Map<String, Object> map);
}
