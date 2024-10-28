package com.example.instagram_spring_boot.Controller;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
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
    private RedisTemplate<String, String> redisString;

    public void saveStringDataInRedis(String key, String data) {
        redisString.opsForValue().set(key, data);
    }

    public String getData(String key) {
        return redisString.opsForValue().get(key);
    }

    @Autowired
    private RedisTemplate<String, String> redisByte;

    public void saveByteDataInRedis(String key, Long idx, boolean data) {
        redisByte.opsForValue().setBit(key, idx, data);
    }

    public Boolean getByteData(String key, Long idx) {
        System.out.println(redisString.opsForValue().getBit(key, idx));
        return redisString.opsForValue().getBit(key, idx);
    }

    @Autowired
    private RedisTemplate<String, Long> redisLong;

    public Long getLongDataInRedis(String key, Long start, Long end) {
        if (start == 0 && end == 0) {
            return redisLong.execute((RedisCallback<Long>) connection -> connection.stringCommands().bitCount(key.getBytes()));
        } else {
            return redisLong.execute((RedisCallback<Long>) connection -> connection.stringCommands().bitCount(key.getBytes(), start, end));
        }
    }

    //jwt uuid 저장
    //jwt를 세션에 저장하고 로컬스토리지에 키를 서버로 보내서 jwt를 조회(토큰그자체)
    //키는 uuid(브라우저에 전송하는 uuid(다른 곳에서 중복사용되면 안됨(구분이 되어야 함))
    //username으로 검색하는 방식 제거
    //redis expired 토큰처럼 제거
    //토큰을 두 개 두고, 리프레시
    // public Long bitcount(final String key) {
    //     return redisByteTemplate.execute(redisConnection -> redisConnection.bitCount(key.getBytes(), start, end));
    // }
    @PostMapping("/likes")
    public int increaseLike(@RequestBody HashMap<String, String> newLike) {
        try {
            System.out.println("신호 왔음" + newLike);
            String uuid = newLike.get("authorUUID");
            String postId = newLike.get("postId");
            if (uuid == null) {
                System.out.println("토큰이 없습니다...");
                return 0;
            } else {
                DecodedJWT decodedJWT = jwtUtil.decodeToken(getData("user_" + uuid));
                if (decodedJWT != null) {
                    String username = decodedJWT.getClaim("username").asString();

                    String userIdx = getData("user-idx-" + username);

                    int likeCheck = likeMapper.getLikeCheck(postId, userIdx); //rㄷdis로 수정
                    Boolean likeCheck2 = getByteData(postId, Long.parseLong(userIdx));
                    System.out.println("좋아요레디스 결과: " + likeCheck2);
                    System.out.println("5");

                    if (likeCheck > 0) {
                        System.out.println("4");

                        // deleteLike("post-like-" + postId, userIdx);
                        saveByteDataInRedis("post_like_" + postId, Long.parseLong(userIdx), false);
                        deleteLike(postId, userIdx);
                        return 2;
                    } else {
                        System.out.println("3");
                        saveByteDataInRedis("post_like_" + postId, Long.parseLong(userIdx), true);
                        System.out.println();
                        //redis
                        // Byte likeVal = 1;
                        // saveData(postId, likeVal);
                        HashMap<String, String> result = new HashMap<>();
                        result.put("postId", postId);
                        result.put("userIdx", userIdx);
                        System.out.println("1");
                        likeMapper.insertLike(result);
                        System.out.println("2");

                        return 1;
                    }
                } else {
                    System.out.println("이 토큰은 거짓말을 하는 토큰이군3");
                    return 0;
                }
            }
        } catch (Exception e) {
            System.out.println("좋아요 실패함. " + newLike);
            return 0;
        }
    }

    @GetMapping("/likes/{postId}")
    public Long getLikeCount(@PathVariable String postId) {
        try {
            // System.out.println(getLongDataInRedis("post-like-" + postId) + " 카운팅중");
            Long result = getLongDataInRedis("post_like_" + postId);
            return result;

        } catch (Exception e) {
            return 0L;
        }

    }

    @PostMapping("/likes/check")
    public boolean getLikeCheck(@RequestBody HashMap<String, String> newLike) {
        try {
            String uuid = newLike.get("authorUUID");
            String postId = newLike.get("postId");
            if (uuid != null) {

                DecodedJWT decodedJWT = jwtUtil.decodeToken(getData("user_" + uuid));
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
