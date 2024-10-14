package com.example.instagram_spring_boot.Mapper;

import java.util.HashMap;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FilesMapper {

    //게시물 파일 조회
    List<HashMap> getFiles(String connectedUUID);

}
