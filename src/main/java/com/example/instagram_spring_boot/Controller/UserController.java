package com.example.instagram_spring_boot.Controller;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.example.instagram_spring_boot.Mapper.UserMapper;
import com.example.instagram_spring_boot.util.ShaUtil;
import com.example.instagram_spring_boot.util.JwtUtil;
// import com.example.instagram_spring_boot.util.JwtAuthenticationFilter;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.instagram_spring_boot.Mapper.FilesMapper;

@CrossOrigin(origins = "http://localhost:8081")
@RestController
public class UserController {

    private static final String EMAIL_REGEX
            = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
    private static final String PHONE_REGEX
            = "^(01[0-9]{1}-?[0-9]{4}-?[0-9]{4}|01[0-9]{8})$";
    private static final String ID_REGEX
            = "^[A-Za-z0-9]{3,30}$";
    private static final String NAME_REGEX
            = "^[가-힣]{2,4}|[a-zA-Z]{2,10}\\s[a-zA-Z]{2,10}$";
    private static final String PASSWORD_REGEX = "^[a-zA-Z0-9`~!@#$%^&*()\\-_=+\\|\\[\\]{};:',.<>/?]{3,24}$";

    public static boolean isValidText(String text, String type) {
        switch (type) {
            case "email":
                type = EMAIL_REGEX;
                break;
            case "phone":
                type = PHONE_REGEX;
                break;
            case "username":
                type = ID_REGEX;
                break;
            case "name":
                type = NAME_REGEX;
                break;
            case "password":
                type = PASSWORD_REGEX;
                break;
            default:
                break;
        }

        Pattern pattern = Pattern.compile(type);
        Matcher matcher = pattern.matcher(text);
        return matcher.matches();
    }

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private FilesMapper filesMapper;

    @Autowired
    private ShaUtil shaUtil;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RedisTemplate<String, String> redisString;

    public void saveData(String key, String data) {
        redisString.opsForValue().set(key, data);
    }

    public String getData(String key) {
        return redisString.opsForValue().get(key);
    }

    // @Autowired
    // private JwtAuthenticationFilter jwtAuthenticationFilter;
    //조회
    @GetMapping("/check/{inputType}/{inputVal}")
    public Boolean dupeCheck(@PathVariable String inputType, @PathVariable String inputVal) {
        try {
            if (inputType.equals("email") || inputType.equals("phone") || inputType.equals("username")) {
                System.out.println(inputType);
                System.out.println(inputVal);

                HashMap<String, String> inputMap = new HashMap<String, String>();
                inputMap.put("type", inputType);
                inputMap.put("val", inputVal);
                System.out.println(inputMap);
                int count = userMapper.getCount(inputMap);

                if (count > 0) {
                    return false;
                } else {
                    return true;
                }
            } else {
                return false;
            }

        } catch (Exception e) {
            System.out.println("체크 실패: " + e.getMessage());
            return false;
        }
    }

    @GetMapping("/{username}")

    public HashMap getInfo(@PathVariable String username) {
        try {
            HashMap resultInfo = userMapper.getUserInfo(username);

            return resultInfo;
        } catch (Exception e) {
            System.out.println("에러 발생했습니다.");
            e.printStackTrace();
            return null;
        }
    }

    @PostMapping("/signup")
    public Boolean userSignUp(@RequestBody HashMap<String, String> newUser) {
        try {
            System.out.println("신호 왔음");
            boolean isNotDupe = false;
            String userAddress = newUser.get("userAddress");
            String userId = newUser.get("userId");
            String userName = newUser.get("userName");
            String userPassword = newUser.get("userPassword");
            HashMap<String, String> inputMap = new HashMap<String, String>();
            int count = 0;
            System.out.println("검증 시작" + newUser);
            if (isValidText(userAddress, "user_email") == true) {
                System.out.println("이메일 검증 시작");
                inputMap.put("type", "email");
                inputMap.put("val", userAddress);
                count = userMapper.getCount(inputMap);

                newUser.put("userEmail", userAddress);
                newUser.put("userPhone", "noPhone");
                System.out.println("이메일 검증");

            } else if (isValidText(userAddress, "phone") == true) {
                inputMap.put("type", "phone");
                inputMap.put("val", userAddress);
                count = userMapper.getCount(inputMap);

                newUser.put("userPhone", userAddress);
                newUser.put("userEmail", "noEmail");
                System.out.println("폰 검증");

            }

            if (count > 0) {
                return false;

            } else {
                System.out.println("중복아님");

                if ((isValidText(userId, "username") == true)) {
                    inputMap.put("type", "username");
                    inputMap.put("val", userAddress);
                    count = userMapper.getCount(inputMap);

                    System.out.println("id검증");

                    if (count > 0) {
                        return false;

                    } else {
                        System.out.println("id검증완료");

                        if (isValidText(userName, "name") == true && isValidText(userPassword, "password") == true) {
                            String hashPassword = shaUtil.sha256Encode(userPassword);
                            newUser.put("userPassword", hashPassword);
                            System.out.println("비번검증완료" + newUser);

                            userMapper.insertUser(newUser);
                            saveData("user-idx-" + userId, userMapper.getUserIdx(userId));

                            filesMapper.insertUserFile(getData("user-idx-" + userId));
                            System.out.println("실행완료");
                            return true;

                        } else {
                            return false;
                        }
                    }
                } else {
                    return false;
                }
            }

        } catch (Exception e) {
            System.out.println("회원가입 실패함. " + newUser);
            return false;
        }
    }

    //로그인
    @PostMapping("/signin")
    public ResponseEntity<HashMap<String, Object>> signInUser(@RequestBody HashMap<String, String> signInData, HttpServletResponse response) {
        try {
            String userNameType = signInData.get("userNameType");
            String userName = signInData.get("userName");
            HashMap<String, String> inputMap = new HashMap<>();

            int count = 0;
            System.out.println("연결됨");
            if (isValidText(userName, userNameType)) {
                System.out.println("양식 맞음");
                inputMap.put("type", userNameType);
                inputMap.put("val", userName);
                count = userMapper.getCount(inputMap);
                System.out.println("1단계통과");

                if (count > 0) {
                    System.out.println("2단계");

                    String userPassword = signInData.get("userPassword");
                    HashMap<String, Object> user = userMapper.getUser(inputMap);
                    String hashPassword = shaUtil.sha256Encode(userPassword);
                    System.out.println("암호화");

                    if (hashPassword.equals(user.get("password"))) {
                        String username = (String) user.get("username");
                        String name = (String) user.get("name");
                        System.out.println("실행이쿠죠");

                        String token = jwtUtil.createToken(username);
                        System.out.println(token);
                        response.setHeader("Authorization", token);

                        HashMap<String, Object> result = new HashMap<>();
                        result.put("username", username);
                        result.put("name", name);
                        result.put("user_token", token);
                        System.out.println("현재토큰: " + token);

                        return ResponseEntity.ok(result);
                    } else {
                        return ResponseEntity.status(401).body(null);
                    }
                } else {
                    return ResponseEntity.status(404).body(null);
                }
            } else {
                return ResponseEntity.status(400).body(null);
            }
        } catch (Exception e) {
            System.out.println("로그인 실패애애앳!! " + signInData);
            return ResponseEntity.status(500).body(null);
        }
    }

    @GetMapping("/{userId}/{userToken}")
    public boolean getUserUUIDCheck(@PathVariable String userId, @PathVariable String userToken) {
        try {
            if (userToken != null) {

                DecodedJWT decodedJWT = jwtUtil.decodeToken(userToken);
                if (decodedJWT != null) {
                    String userUUID = decodedJWT.getClaim("userUUID").asString();
                    int rs = userMapper.getUserCheck(userId, userUUID);
                    if (rs > 0) {
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } catch (Exception e) {
            System.out.println("에러 발생했습니다.");
            e.printStackTrace();
            return false;
        }
    }

}
