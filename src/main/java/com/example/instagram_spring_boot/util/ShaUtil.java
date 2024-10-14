package com.example.instagram_spring_boot.util;

import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Service;

import com.google.common.hash.Hashing;

@Service 
public class ShaUtil {

    public String sha256Encode(String plainText) {

        return Hashing.sha256()
                .hashString(plainText, StandardCharsets.UTF_8)
                .toString();
    }
}
