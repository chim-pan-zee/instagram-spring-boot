package com.example.instagram_spring_boot.Controller;

import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.instagram_spring_boot.Mapper.CommentMapper;
import com.example.instagram_spring_boot.util.JwtUtil;

@CrossOrigin(origins = "http://localhost:8081")
@RestController
public class CommentController {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/comm")
    public Boolean uploadComment(@RequestBody HashMap<String, String> newComment) {
        try {
            System.out.println("신호 왔음" + newComment);
            String token = newComment.get("authorToken");
            String postId = newComment.get("postId");
            String contents = newComment.get("contents");
            if (token == null || token.isEmpty()) {
                System.out.println("토큰이 없습니다...");
                return false;
            }

            DecodedJWT decodedJWT = jwtUtil.decodeToken(token);
            if (decodedJWT != null) {
                String userUUID = decodedJWT.getClaim("userUUID").asString();

                HashMap<String, String> result = new HashMap<>();
                result.put("postId", postId);
                result.put("userUUID", userUUID);
                result.put("contents", contents);

                commentMapper.insertComment(result);

                return true;
            } else {
                System.out.println("이 토큰은 거짓말을 하는 토큰이군");
                return false;
            }

        } catch (Exception e) {
            System.out.println("회원가입 실패함. " + newComment);
            return false;
        }
    }

    @GetMapping("/comm/{postId}")
    public List<HashMap> userSignUp(@PathVariable String postId) {
        try {
            List<HashMap> comments = commentMapper.getComments(postId);
            System.out.println("글id:=" + postId);
            System.out.println("댓글=" + comments);

            return comments;

        } catch (Exception e) {
            System.out.println("에러 발생.");
            return null;

        }
    }

}
