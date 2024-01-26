package com.sparta.springauth.jwt;

import com.sparta.springauth.entity.UserRoleEnum;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.Key;
import java.util.Base64;
import java.util.Date;

//특정한 매개변수, 파라미터에 대한 기능이 모여있는 클래스
//다른 클래스에 의존하지 X 기능만 하는 모듈 클래스
@Component
public class JwtUtil {
    //1.토큰을 그냥 response헤더에 담아서 보낼 수 O
    //이러면 코드가 줄어듦 그냥 헤더에 담으면 되니까
    //헤더에 키 값 필요함
    //2.쿠키객체를 만들어서 토큰을 담은 후 , 쿠키를 Response객체에 담을 수 O
    //이러면 쿠키자체에 만료기한, 다른 옵션 달 수 O 장점 자동으로 쿠키도 저장되고,,,
    //이때도 키 값 필요한데 Name-Value그거였음

    //JWT 데이터 만들어두기

    // Header KEY 값 = 2번방법에선 Name
    public static final String AUTHORIZATION_HEADER = "Authorization";
    // 사용자 권한 값의 KEY 권한을 가져오기 위한 키
    public static final String AUTHORIZATION_KEY = "auth";
    // Token 식별자 만들 토큰 앞에 붙여줄거임 규칙이라서 지켜주는게 좋음
    // Bearer이 붙어있으면 토큰이다~ 라고 생각하면 됨 뒤에 한칸 띄어주기 주의
    public static final String BEARER_PREFIX = "Bearer ";
    // 토큰 만료시간
    //기준은 ms단위임
    private final long TOKEN_TIME = 60 * 60 * 1000L; // 60분

    //application.properties에서 jwt.secret.key=7Iqk7YyM66Wd어쩌구
    // 값을 가져와서 secretKey에 값을 넣어주게 됨
    @Value("${jwt.secret.key}") // Base64 Encode 한 SecretKey
    private String secretKey;
    private Key key;
    private final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

    // 로그 설정
    //애플리케이션 동작중 프로그램 동작 정보를 시간순으로 기록해둔거
    //importLogger친구들 해오면 알아서 로그정보 가져오기가능
    public static final Logger logger = LoggerFactory.getLogger("JWT 관련 로그");

    //@PostConstruct 딱 한번만 요청할 값을 사용할 때 마다 새로 요청을 호출하는 실수를 방지하기 위해 사용함
    @PostConstruct
    public void init() {
        byte[] bytes = Base64.getDecoder().decode(secretKey);
        key = Keys.hmacShaKeyFor(bytes);
    }



    //JWT 생성
// 토큰 생성
    public String createToken(String username, UserRoleEnum role) {
        Date date = new Date();

        //무조건 이걸 다 넣어야하는거 X 선택하면 됨
        return BEARER_PREFIX +
                Jwts.builder() //builder()사용해서 뒤에 오는 값들 다 붙여이어줄거임
                        .setSubject(username) // 사용자 식별자값(ID)
                        .claim(AUTHORIZATION_KEY, role) // 사용자 권한
                        .setExpiration(new Date(date.getTime() + TOKEN_TIME)) // 만료 시간
                        .setIssuedAt(date) // 발급일
                        .signWith(key, signatureAlgorithm) // 키, 암호화 알고리즘 HS256
                        .compact();
    }

    //생성된 JWT를 Cookie에 저장
    public void addJwtToCookie(String token, HttpServletResponse res) {
        try {
            //쿠키에는 공백 안됨
            token = URLEncoder.encode(token, "utf-8").replaceAll("\\+", "%20"); // Cookie Value 에는 공백이 불가능해서 encoding 진행

            Cookie cookie = new Cookie(AUTHORIZATION_HEADER, token); // Name-Value=인코딩된 토큰값
            cookie.setPath("/");

            // Response 객체에 Cookie 추가
            res.addCookie(cookie);
        } catch (UnsupportedEncodingException e) {
            logger.error(e.getMessage());
        }
    }

    //쿠키에 들어있던 JWT 토큰을 잘라 Substring
    //아까 토큰에 Bearer붙였었음 그래서 그거 떼내자
    public String substringToken(String tokenValue) {
        //공백, null이 아니어야함 && Bearer로 시작해야됨 그래야 토큰
        if (StringUtils.hasText(tokenValue) && tokenValue.startsWith(BEARER_PREFIX)) {
            //공백까지 Bearer 7자 잘라내기
            return tokenValue.substring(7);
        }
        logger.error("Not Found Token");
        throw new NullPointerException("Not Found Token");
    }

    //JWT 검증 토큰 검증
    public boolean validateToken(String token) {
        try {
            //토큰이 변조되었는지 확인해줌
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (SecurityException | MalformedJwtException | SignatureException e) {
            logger.error("Invalid JWT signature, 유효하지 않는 JWT 서명 입니다.");
        } catch (ExpiredJwtException e) {
            logger.error("Expired JWT token, 만료된 JWT token 입니다.");
        } catch (UnsupportedJwtException e) {
            logger.error("Unsupported JWT token, 지원되지 않는 JWT 토큰 입니다.");
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims is empty, 잘못된 JWT 토큰 입니다.");
        }
        return false;
    }
    //검증 완료 되면 JWT에서 사용자 정보 가져오기
    //getBody()하면 Claims 가져옴
    //Claims = 안에 데이터 많이들어있음 여기 안에 사용자 정보 들어있음 jwt = 클레임 기반
    //토큰가져오는 코드랑 비슷함
    public Claims getUserInfoFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }

}

