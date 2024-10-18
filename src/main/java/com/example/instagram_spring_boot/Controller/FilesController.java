package com.example.instagram_spring_boot.Controller;

import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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
    @PatchMapping("/{id}")
    public boolean updateProfileImg(@RequestBody HashMap<String, String> profile) {
        try {
            String token = profile.get("authorToken");
            if (token != null) {

                DecodedJWT decodedJWT = jwtUtil.decodeToken(token);
                if (decodedJWT != null) {
                    String userUUID = decodedJWT.getClaim("userUUID").asString();
                    String fileName = profile.get("fileName");
                }
            }
            return false;
        } catch (Exception e) {
            // TODO: handle exception
            return false;
        }
    }

}
