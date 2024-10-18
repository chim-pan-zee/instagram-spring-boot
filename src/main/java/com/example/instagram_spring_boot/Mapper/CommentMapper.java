package com.example.instagram_spring_boot.Mapper;

import java.util.HashMap;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CommentMapper {

    //댓글 작성
    void insertComment(HashMap<String, String> newComment);

    //댓글 조회
    List<HashMap> getComments(String postUUID);

    int getCount(String postId);
}
