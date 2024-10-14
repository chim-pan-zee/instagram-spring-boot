package com.example.instagram_spring_boot.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class CommentDTO {

    private String USER_UUID;
    private String USER_PHONE;
    private String USER_EMAIL;
    private String USER_ID;
    private String USER_NAME;
    private String USER_PASSWORD;
    private String USER_GENDER;
    private String USER_DESCRIPTION;
    private String USER_PROFILE_IMG;
    private String USER_ACCOUNT_AGE;
}
