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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.instagram_spring_boot.Mapper.FilesMapper;
import com.example.instagram_spring_boot.Mapper.PostMapper;
import com.example.instagram_spring_boot.util.JwtUtil;

@CrossOrigin(origins = "http://localhost:8081")
@RestController
public class PostController {

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private FilesMapper filesMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${file.path}")
    private String uploadFolder;

    @PostMapping("/p/{id}")
    private void uploadPost(
            @RequestPart(value = "key", required = false) Map<String, String> key,
            @RequestPart(value = "files") List<MultipartFile> files
    ) {
        try {
            System.out.println("키값: " + key);

            String token = key.get("authorToken");
            String contents = key.get("contents");

            DecodedJWT decodedJWT = jwtUtil.decodeToken(token);
            if (decodedJWT != null) {
                String username = decodedJWT.getClaim("username").asString();
                String postUUID = UUID.randomUUID().toString();
                System.out.println("추출된 유저 UUID: " + username);

                HashMap<String, String> newPost = new HashMap<>();
                newPost.put("postUUID", postUUID);
                newPost.put("username", username);
                newPost.put("contents", contents);
                System.out.println("반복!반복!반복작업!");
                postMapper.insertPost(newPost);

                for (MultipartFile file : files) {
                    String originalFilename = file.getOriginalFilename();
                    UUID fileUUID = UUID.randomUUID();
                    String saveFilename = fileUUID.toString() + "_" + originalFilename;

                    Path savePath = Paths.get(uploadFolder, saveFilename);
                    Files.write(savePath, file.getBytes());

                    System.out.println("파일이 저장된 경로: " + savePath.toString());

                    HashMap<String, String> postFiles = new HashMap<>();
                    postFiles.put("postUUID", postUUID);
                    postFiles.put("fileName", saveFilename);
                    postMapper.insertFiles(postFiles);
                }

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
            String username = key.get("username");
            String token = key.get("userToken");
            // System.out.println("토큰은:" + token);
            DecodedJWT decodedJWT = jwtUtil.decodeToken(token);

            if (decodedJWT != null) {
                String userUUID = decodedJWT.getClaim("userUUID").asString();
                List<HashMap> thumbnails = postMapper.getPostsThumbnail(username);

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
            HashMap postContents = postMapper.getPostContents(postId);
            return postContents;

        } catch (Exception e) {
            System.out.println("게시물이 조회되지않음:" + postId);
            return null;
        }
    }

    @PostMapping("/p")
    public List<HashMap> getPosts(@RequestBody HashMap<String, String> userData) {
        try {
            String pageString = userData.get("page");
            int page = Integer.parseInt(pageString);
            int offset = page * 4;
            List<HashMap> posts = postMapper.getPosts(offset);
            System.out.println("로드된 게시물:" + posts);
            return posts;
        } catch (Exception e) {
            System.out.println("게시물이 조회되지 않음: " + e.getMessage());
            return null;
        }
    }

    //포스트를 업데이트 해얗 ㅏㄴ다. 필요한 것은?
    //우선 posttable 업데이트, 그리고 filetable을 업데이트 해야한다.
    //우선 psottable.먼저 포스트의 콘텐츠를 변경시킨다.
    //포스트테이블은 이걸로 끝이다. 이제 파일테이블
    //파일테이블에서 포스트 아이디와 일치하는 값들을 전부 불러와야 할 것이다.
    //그리고 클라이언트에서 보내는, [수정할 배열] 을 메서드에서 받는다.
    //기존 파일값들을 모두 지우고 [수정할배열]을 토대로 새롭게 저장한다
    @PutMapping("/p/{postId}")
    public boolean updatePost(
            @RequestBody HashMap key) {
        try {
            System.out.println("왔다수정");
            if (key == null) {
                System.out.println("key가 null입니다.");
                return false;
            }
            String token = (String) key.get("authorToken");

            System.out.println("키값: " + key);

            DecodedJWT decodedJWT = jwtUtil.decodeToken(token);
            if (decodedJWT != null) {
                String userUUID = decodedJWT.getClaim("userUUID").asString();

                String postUUID = (String) key.get("postId");
                String contents = (String) key.get("contents");
                List<HashMap> images = (List) key.get("images");
                //[{file_name=}]...이런식으로 옴
                filesMapper.deleteFiles(postUUID);
                System.out.println("게시물아이디" + postUUID);
                postMapper.updatePost(contents, postUUID, userUUID);
                System.out.println("업뎃내역  " + contents + "  " + postUUID + "  " + userUUID);

                for (HashMap image : images) {
                    String fileName = (String) image.get("file_name");

                    HashMap<String, String> postFiles = new HashMap<>();
                    postFiles.put("postUUID", postUUID);
                    postFiles.put("fileName", fileName);
                    postMapper.insertFiles(postFiles);
                    System.out.println("수정반복작업");
                }

                System.out.println("ㅎㅇ " + images);
                return true;

            } else {
                return false;

            }

        } catch (Exception e) {
            System.err.println("에러 발생: " + e.getMessage());
            return false;
        }
    }

    @DeleteMapping("/p/{postId}")
    public boolean deletePost(@PathVariable String postId) {
        try {
            System.out.println("게시물아이디:" + postId);
            postMapper.deletePost(postId);
            return true;
        } catch (Exception e) {
            System.out.println("게시물이 조회되지않음:" + postId);
            return false;

        }
    }

}
