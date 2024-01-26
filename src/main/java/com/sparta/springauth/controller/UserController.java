package com.sparta.springauth.controller;

import com.sparta.springauth.dto.LoginRequestDto;
import com.sparta.springauth.dto.SignupRequestDto;
import com.sparta.springauth.service.UserService;
import com.sun.security.auth.module.Krb5LoginModule;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

//RestController 아니고 그냥 Controller
@Controller
@RequestMapping("/api")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    //그냥 컨트롤러에 이렇게 return 하면 html파일 찾으러간다고 했음!!
    @GetMapping("/user/login-page")
    public String loginPage() {
        return "login";
    }

    //회원가입페이지
    @GetMapping("/user/signup")
    public String signupPage() {
        return "signup";
    }

    //Model받아옴
    //파라미터 @modelAttribute 생략 가능함
    //회원가입이 완료되면 로그인 페이지 반환해줄거라 String 반환타입
    //로그인페이지 저기 위에있는걸로 다시 매핑되는거니까 리다이렉트 써주기
    @PostMapping("/user/signup")
    public String signup(SignupRequestDto requestDto){
        userService.signup(requestDto);
        return "redirect:/api/user/login-page";
    }

    //@ModelAttribute방식이지만 생략
    @PostMapping("/user/login")
    public String login(LoginRequestDto requestDto, HttpServletResponse res){
        try {
            userService.login(requestDto, res);
        } catch (Exception e) {
            //에러났으니까 뒤에 ?error
            return "redirect:/api/user/login-page?error";
        }
        //성공하면 메인화면으로 리다이렉트
        return "redirect:/";
    }
}