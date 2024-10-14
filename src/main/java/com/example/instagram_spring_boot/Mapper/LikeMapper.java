package com.example.instagram_spring_boot.Mapper;

import java.util.HashMap;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LikeMapper {

    //좋아요 조회
    int getLikeTotal(String postId);

    int getLikeCheck(String postId, String userUUID);

    //좋아요 삽입
    void insertLike(HashMap<String, String> result);

    //좋아요 삭제
    void deleteLike(String postId, String userUUID);

}
