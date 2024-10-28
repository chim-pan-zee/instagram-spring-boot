package com.example.instagram_spring_boot.Controller;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.instagram_spring_boot.Mapper.FilesMapper;
import com.example.instagram_spring_boot.Mapper.UserMapper;
import com.example.instagram_spring_boot.util.JwtUtil;

@CrossOrigin(origins = "http://localhost:8081")
@RestController
public class FilesController {

    @Autowired
    private FilesMapper filesMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${file.path}")
    private String uploadFolder;

    @Autowired
    private RedisTemplate<String, String> redisString;

    public void saveData(String key, String data) {
        redisString.opsForValue().set(key, data);
    }

    public String getData(String key) {
        return redisString.opsForValue().get(key);
    }

    @GetMapping("/p/file/{postId}")
    public List<HashMap> getPostFiles(@PathVariable String postId) {
        try {
            List<HashMap> posts = filesMapper.getFiles(postId);
            return posts;

        } catch (Exception e) {
            System.out.println("게시물이 조회되지않음:");
            return null;
        }
    }

    //프로필 이미지 수정
    //프로필 이미지 파일과, 프로필 정보를 저장해야 한다. 그런데 어디어디에 어떻게?
    //우선 프로젝 이미지 파일은 전달받아서 저장이 가능하다. 그렇다면 insert가 아닌,
    //업데이트 식으로 저장하는 편이 나을 것이다. 그렇다면 계정 생성 시에 아에 본인의
    //db 로우를 만들어주는 것이 좋을 것 같다.
    //프로필 정보는 이미 컬럼 생성해 뒀으니까 상관없다.
    @PutMapping("/{id}")
    private void editUser(
            @RequestPart(value = "key", required = false) Map key,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) {
        try {
            System.out.println("키값: " + key);

            String uuid = (String) key.get("authorUUID");

            DecodedJWT decodedJWT = jwtUtil.decodeToken(getData("user_" + uuid));
            if (decodedJWT != null) {
                String username = decodedJWT.getClaim("username").asString();
                String userDesc = (String) key.get("userDesc");
                String userGender = (String) key.get("userGender");
                if ((boolean) key.get("defaultImg") == false) {
                    String originalFilename = file.getOriginalFilename();
                    UUID fileUUID = UUID.randomUUID();
                    String saveFilename = fileUUID.toString() + "_" + originalFilename;

                    Path savePath = Paths.get(uploadFolder, saveFilename);
                    Files.write(savePath, file.getBytes());

                    System.out.println("파일이 저장된 경로: " + savePath.toString());

                    HashMap<String, String> setFile = new HashMap<>();
                    setFile.put("userIdx", getData("user-idx-" + username));
                    setFile.put("fileName", saveFilename);
                    setFile.put("fileDir", savePath.toString());

                    filesMapper.updateUserFile(setFile);
                }

                HashMap<String, String> setProfile = new HashMap<>();
                setProfile.put("username", username);
                setProfile.put("userDesc", userDesc);
                setProfile.put("userGender", userGender);
                userMapper.updateUserProfile(setProfile);

                System.out.println("반복!반복!반복작업!");

            } else {
                System.out.println("이 토큰은 거짓말을 하는 토큰이군2");
            }
        } catch (Exception e) {
            System.out.println("에러 발생했습니다.");
            e.printStackTrace();
        }
    }

    @GetMapping("/file/{username}")
    public String getUserProfileImage(@PathVariable String username) {
        try {
            System.out.println(filesMapper.getUserFile((String) getData("user-idx-" + username)) + " ㅎㅇ " + username);
            return filesMapper.getUserFile((String) getData("user-idx-" + username));
        } catch (Exception e) {
            return null;
        }
    }

}
