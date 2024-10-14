package com.example.instagram_spring_boot.Mapper;

import java.util.HashMap;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PostMapper {

    //게시물 작성
    void insertPost(HashMap<String, String> newPost);

    void insertFiles(HashMap<String, String> postFiles);

    //게시물 상세조회
    List<HashMap> getPostsThumbnail(String userId);

    HashMap getPostContents(String postUUID);

    //게시물 조회
    List<HashMap> getPosts();

    

}
