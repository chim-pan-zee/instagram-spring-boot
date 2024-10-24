package com.example.instagram_spring_boot.Mapper;

import java.util.HashMap;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FilesMapper {

    //게시물 파일 조회
    List<HashMap> getFiles(String postId);

    void deleteFiles(String postId);

    String getUserFile (String userIdx);

    //사용자 프로필 업데이트
    void updateUserFile(HashMap<String, String> setFile);

    void insertUserFile(String userIdx);

}
