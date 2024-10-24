package com.example.instagram_spring_boot.Controller;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.instagram_spring_boot.Mapper.LikeMapper;
import com.example.instagram_spring_boot.util.JwtUtil;

@CrossOrigin(origins = "http://localhost:8081")
@RestController
public class LikeController {

    // @Autowired
    // private CommentMapper commentMapper;
    @Autowired
    private LikeMapper likeMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public void saveData(String key, String data) {
        redisTemplate.opsForValue().set(key, data);
    }

    public String getData(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    @PostMapping("/likes")
    public int increaseLike(@RequestBody HashMap<String, String> newLike) {
        try {
            System.out.println("신호 왔음" + newLike);
            String token = newLike.get("authorToken");
            String postId = newLike.get("postId");
            if (token == null) {
                System.out.println("토큰이 없습니다...");
                return 0;
            } else {
                DecodedJWT decodedJWT = jwtUtil.decodeToken(token);
                if (decodedJWT != null) {
                    String username = decodedJWT.getClaim("username").asString();

                    String userIdx = getData("user-idx-" + username);

                    int likeCheck = likeMapper.getLikeCheck(postId, userIdx);

                    if (likeCheck > 0) {
                        // deleteLike("post-like-" + postId, userIdx);
                        deleteLike(postId, userIdx);
                        return 2;
                    } else {

                        //redis
                        // Byte likeVal = 1;
                        // saveData(postId, likeVal);
                        HashMap<String, String> result = new HashMap<>();
                        result.put("postId", postId);
                        result.put("userIdx", userIdx);

                        likeMapper.insertLike(result);

                        return 1;
                    }
                } else {
                    System.out.println("이 토큰은 거짓말을 하는 토큰이군");
                    return 0;
                }
            }
        } catch (Exception e) {
            System.out.println("좋아요 실패함. " + newLike);
            return 0;
        }
    }

    @GetMapping("/likes/{postId}")
    public int getLikeCount(@PathVariable String postId) {
        try {
            int result = likeMapper.getLikeTotal(postId);
            return result;

        } catch (Exception e) {
            return 0;
        }

    }

    @PostMapping("/likes/check")
    public boolean getLikeCheck(@RequestBody HashMap<String, String> newLike) {
        try {
            String token = newLike.get("authorToken");
            String postId = newLike.get("postId");
            if (token != null) {

                DecodedJWT decodedJWT = jwtUtil.decodeToken(token);
                if (decodedJWT != null) {
                    String username = decodedJWT.getClaim("username").asString();
                    String userIdx = getData("user-idx-" + username);
                    int likeCheck = likeMapper.getLikeCheck(postId, userIdx);
                    System.out.println("카운트시작" + username + userIdx + postId);
                    if (likeCheck == 0) {
                        return false;
                    } else {
                        return true;
                    }
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    @DeleteMapping("/likes")

    public boolean deleteLike(String postId, String userIdx) {
        {
            try {

                likeMapper.deleteLike(postId, userIdx);
                return true;
            } catch (Exception e) {
                System.out.println("좋아요 제거 중 에러 발생");
                e.printStackTrace();
                return false;
            }
        }

    }
}
