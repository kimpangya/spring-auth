package com.sparta.springauth.service;

import com.sparta.springauth.dto.LoginRequestDto;
import com.sparta.springauth.dto.SignupRequestDto;
import com.sparta.springauth.entity.User;
import com.sparta.springauth.entity.UserRoleEnum;
import com.sparta.springauth.jwt.JwtUtil;
import com.sparta.springauth.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor //final, @NotNull만 생성자로 만들어줌 = 생성자 주입에 사용
//이거 안쓰면 @Autowired하고(생략가능) 생성자 만들어줘야함
//필드로 있는 생성자 모두 만들어줌 = @AllArgsConstructor
//파라미터X 기본생성자는 @NoArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;


    // ADMIN_TOKEN
    //일반 사용자 vs 관리자 구분하기위해서 씀
    //현업에서는 보통 관리자 권한을 줄 때 이렇게 안쓰고 따로 페이지 만들거나 함...
    private final String ADMIN_TOKEN = "AAABnvxRVklrnYxKZ0aHgTBcXukeZygoC";

    public void signup(SignupRequestDto requestDto) {
        String username = requestDto.getUsername();
        String password = passwordEncoder.encode(requestDto.getPassword());

        // 회원 중복 확인
        Optional<User> checkUsername = userRepository.findByUsername(username);
        // Optional에 있는 메소드임 isPresent() DB에 존재하는지 확인해줌
        if (checkUsername.isPresent()) {
            throw new IllegalArgumentException("중복된 사용자가 존재합니다.");
        }

        // email 중복확인
        String email = requestDto.getEmail();
        Optional<User> checkEmail = userRepository.findByEmail(email);
        if (checkEmail.isPresent()) {
            throw new IllegalArgumentException("중복된 Email 입니다.");
        }

        // 사용자 ROLE 확인
        UserRoleEnum role = UserRoleEnum.USER;
        //만약 관리자라면 여기서 관리자로 바뀜
        if (requestDto.isAdmin()) {
            if (!ADMIN_TOKEN.equals(requestDto.getAdminToken())) {
                throw new IllegalArgumentException("관리자 암호가 틀려 등록이 불가능합니다.");
            }
            role = UserRoleEnum.ADMIN;
        }

        // 사용자 등록
        User user = new User(username, password, email, role);
        userRepository.save(user);
    }

    public void login(LoginRequestDto requestDto, HttpServletResponse res) {
        String username=requestDto.getUsername();
        String password=requestDto.getPassword();

        //사용자 확인
        //원래 findBy하고나면 Optional이 리턴되는데 그걸 User객체로 바로 받고싶은거임
        // 그래서 orElseThorw해준다 = 값이 잘 나오면 그냥 User객체 주고,
        //값이 없으면 안에 있는 에러익셉션을 발생시킴
        User user= userRepository.findByUsername(username).orElseThrow(
                () -> new IllegalArgumentException("등록된 사용자가 없습니다")
        );

        //비밀번호 확인
        //passwordEncoder.matches(평문, 암호화된 비밀번호)
        //평문= 받아온비번, 암호화된비번 = 찾아온 user객체의 비번
        //user객체에는 이미 암호화된 비번 저장되는거잖음
        if(!passwordEncoder.matches(password, user.getPassword())){
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        //인증 완료됨. JWT 생성 => 쿠키에 저장 => Response객체에 추가
        String token = jwtUtil.createToken(user.getUsername(), user.getRole());
        //http서블릿에서 받아온것도 넣어줌
        jwtUtil.addJwtToCookie(token, res);
    }
}