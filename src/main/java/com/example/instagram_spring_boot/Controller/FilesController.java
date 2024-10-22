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
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.instagram_spring_boot.Mapper.FilesMapper;
import com.example.instagram_spring_boot.util.JwtUtil;

@CrossOrigin(origins = "http://localhost:8081")
@RestController
public class FilesController {

    @Autowired
    private FilesMapper filesMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${file.path}")
    private String uploadFolder;

    // @PostMapping("/file/{id}")
    // public List<HashMap> getPostInfo(@RequestPart(value = "key", required = false) Map<String, String> key) {
    //     try {
    //         String userId = key.get("userId");
    //         String token = key.get("userToken");
    //         System.out.println("토큰은:" + token);
    //         DecodedJWT decodedJWT = jwtUtil.decodeToken(token);
    //         if (decodedJWT != null) {
    //             String userUUID = decodedJWT.getClaim("userUUID").asString();
    //             List<HashMap> thumbnails = postMapper.getPostsThumbnail(userId);
    //             List<HashMap> imagesWithPath = new ArrayList<>();
    //             for (HashMap thumbnail : thumbnails) {
    //                 String fileName = (String) thumbnail.get("file_name");
    //                 Timestamp createdAtTimestamp = (Timestamp) thumbnail.get("created_at");
    //                 String createdAt = createdAtTimestamp != null ? createdAtTimestamp.toString() : null;
    //                 String imagePath = "upload/" + fileName;
    //                 HashMap<String, String> imageMap = new HashMap<>();
    //                 imageMap.put("post_uuid", (String) thumbnail.get("post_uuid"));
    //                 imageMap.put("image_path", imagePath);
    //                 imageMap.put("created_at", createdAt);
    //                 imagesWithPath.add(imageMap);
    //             }
    //             return imagesWithPath;
    //         } else {
    //             System.out.println("이 토큰은 거짓말을 하는 토큰이군");
    //             return null;
    //         }
    //     } catch (Exception e) {
    //         e.printStackTrace();
    //         System.out.println("크아악!토큰이없어");
    //         return null;
    //     }
    // }
    // @GetMapping("/p/file/{postId}")
    // public HashMap getPostContents(@PathVariable String postId) {
    //     try {
    //         System.out.println("뭔데이거" + postId);
    //         List<HashMap postContents = filesMapper.getFiles(postId);
    //         System.out.println("작성시각은" + postContents.get("created_at"));
    //         System.out.println("뭐야이거" + postContents);
    //         return postContents;
    //     } catch (Exception e) {
    //         System.out.println("게시물이 조회되지않음:" + postId);
    //         return null;
    //     }
    // }
    @GetMapping("/p/file/{postId}")
    public List<HashMap> getFiles(@PathVariable String postId) {
        try {
            List<HashMap> posts = filesMapper.getFiles(postId);
            return posts;

        } catch (Exception e) {
            System.out.println("게시물이 조회되지않음:");
            return null;
        }
    }

    //프로필 이미지 수정
    @PutMapping("/{id}")
    private void editUser(
            @RequestPart(value = "key", required = false) Map<String, String> key,
            @RequestPart(value = "files") List<MultipartFile> files
    ) {
        try {
            System.out.println("키값: " + key);

            String token = key.get("authorToken");

            DecodedJWT decodedJWT = jwtUtil.decodeToken(token);
            if (decodedJWT != null) {
                String username = decodedJWT.getClaim("username").asString();
                String userDesc = key.get("userDesc");
                String userGender = key.get("userGender");

                HashMap<String, String> setProfile = new HashMap<>();
                setProfile.put("username", username);
                setProfile.put("userDesc", userDesc);
                setProfile.put("userGender", userGender);
                System.out.println("반복!반복!반복작업!");

                for (MultipartFile file : files) {
                    String originalFilename = file.getOriginalFilename();
                    UUID fileUUID = UUID.randomUUID();
                    String saveFilename = fileUUID.toString() + "_" + originalFilename;

                    Path savePath = Paths.get(uploadFolder, saveFilename);
                    Files.write(savePath, file.getBytes());

                    System.out.println("파일이 저장된 경로: " + savePath.toString());

                    HashMap<String, String> postFiles = new HashMap<>();
                    postFiles.put("username", username);
                    postFiles.put("fileName", saveFilename);
                }

            } else {
                System.out.println("이 토큰은 거짓말을 하는 토큰이군");
            }
        } catch (Exception e) {
            System.out.println("에러 발생했습니다.");
            e.printStackTrace();
        }
    }

}
