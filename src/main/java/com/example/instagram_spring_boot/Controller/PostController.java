package com.example.instagram_spring_boot.Controller;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.instagram_spring_boot.Mapper.PostMapper;
import com.example.instagram_spring_boot.Mapper.UserMapper;
import com.example.instagram_spring_boot.util.JwtUtil;
import org.springframework.web.bind.annotation.RequestParam;

@CrossOrigin(origins = "http://localhost:8081")
@RestController
public class PostController {

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${file.path}")
    private String uploadFolder;

    @PostMapping("/post")
    private void uploadPost(@RequestPart(value = "key", required = false) Map<String, String> key,
            @RequestPart(value = "file", required = true) MultipartFile file) {
        try {
            System.out.println("파일값: " + file);
            System.out.println("키값: " + key);

            String token = key.get("authorToken");
            String contents = key.get("contents");

            DecodedJWT decodedJWT = jwtUtil.decodeToken(token);
            if (decodedJWT != null) {
                String userUUID = decodedJWT.getClaim("userUUID").asString();
                UUID postUUID = UUID.randomUUID();
                String strPostUUID = postUUID.toString();
                System.out.println("추출된 유저 UUID: " + userUUID);

                String originalFilename = file.getOriginalFilename();
                UUID fileUUID = UUID.randomUUID();
                String saveFilename = fileUUID.toString() + "_" + originalFilename;

                Path savePath = Paths.get(uploadFolder, saveFilename);

                Files.write(savePath, file.getBytes());

                System.out.println("파일이 저장된 경로: " + savePath.toString());

                HashMap<String, String> newPost = new HashMap<String, String>();
                newPost.put("postUUID", strPostUUID);
                newPost.put("userUUID", userUUID);
                newPost.put("contents", contents);
                // newPost.put("fileName", originalFilename);
                // newPost.put("filePath", savePath.toString());

                postMapper.insertPost(newPost);

                HashMap<String, String> postFiles = new HashMap<String, String>();
                postFiles.put("postUUID", strPostUUID);
                postFiles.put("fileName", saveFilename);
                postMapper.insertFiles(postFiles);

            } else {
                System.out.println("이 토큰은 거짓말을 하는 토큰이군");
            }
        } catch (Exception e) {
            System.out.println("에러 발생했습니다.");
            e.printStackTrace();
        }
    }
    //해당 코드를 통해선 게시물만 불러온다. 게시물의 id를 통해 파일들을 가져올 수 있다. 그 파일 중 인덱스 값이 가장 낮은 것만 가져온다.
    //해당 과정을 모두 쿼리문에서 해결하고 그것들을 해시맵에 담아 가져온다.
    //그렇게 한다면 게시물과, 해당하는 파일 1개만 전송되어 올 것이다.
    //근데 생각해보니까 그냥 게시물 썸네일만 가져와도 되는 거 아님?

    //수정한다. user id를 통해 게시물 id 전체 조회, 그 후 6개의 게시물아이디&연관된 가장인덱스값이 낮은 파일네임
    //이것을 게시물 수(너무 많으면 동시에 가져올 때 상당한 시간이 소요되므로, 6개씩 끊어서 가져오기)
    //만큼 가져온다. 그리고 가져온 게시물을 오브젝트화 시켜 해시맵으로 한번 더 담아 전체 전송한다.
    @PostMapping("/{id}")
    public List<HashMap> getPostInfo(@RequestPart(value = "key", required = false) Map<String, String> key) {
        try {
            String userId = key.get("userId");
            String token = key.get("userToken");
            System.out.println("토큰은:" + token);
            DecodedJWT decodedJWT = jwtUtil.decodeToken(token);

            if (decodedJWT != null) {
                String userUUID = decodedJWT.getClaim("userUUID").asString();
                List<HashMap> thumbnails = postMapper.getPostsThumbnail(userId);

                List<HashMap> imagesWithPath = new ArrayList<>();
                for (HashMap thumbnail : thumbnails) {
                    String fileName = (String) thumbnail.get("file_name");

                    Timestamp createdAtTimestamp = (Timestamp) thumbnail.get("created_at");

                    String createdAt = createdAtTimestamp != null ? createdAtTimestamp.toString() : null;

                    String imagePath = "upload/" + fileName;

                    HashMap<String, String> imageMap = new HashMap<>();
                    imageMap.put("post_uuid", (String) thumbnail.get("post_uuid"));
                    imageMap.put("image_path", imagePath);
                    imageMap.put("created_at", createdAt);
                    imagesWithPath.add(imageMap);
                }

                return imagesWithPath;

            } else {
                System.out.println("이 토큰은 거짓말을 하는 토큰이군");
                return null;
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("크아악!토큰이없어");
            return null;
        }
    }

    @GetMapping("/p/{postId}")
    public HashMap getPostContents(@PathVariable String postId) {
        try {
            System.out.println("뭔데이거" + postId);
            HashMap postContents = postMapper.getPostContents(postId);
            System.out.println("작성시각은" + postContents.get("created_at"));
            System.out.println("뭐야이거" + postContents);
            return postContents;

        } catch (Exception e) {
            System.out.println("게시물이 조회되지않음:" + postId);
            return null;
        }
    }

    @GetMapping("/post")
    public List<HashMap> getPosts() {
        try {
            List<HashMap> posts = postMapper.getPosts();

            System.out.println("뭐야이거" + posts);
            return posts;

        } catch (Exception e) {
            System.out.println("게시물이 조회되지않음:");
            return null;
        }
    }

}
