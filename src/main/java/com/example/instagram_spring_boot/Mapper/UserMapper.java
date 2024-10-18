package com.example.instagram_spring_boot.Mapper;

import java.util.HashMap;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {
    //회원가입

    int getCount(HashMap<String, String> map);

    void insertUser(HashMap<String, String> newUser);

    //로그인
    HashMap getUser(HashMap<String, String> map);

    HashMap getUserInfo(String userId);

    //유저 체크
    int getUserCheck(String userId, String userUUID);

}
