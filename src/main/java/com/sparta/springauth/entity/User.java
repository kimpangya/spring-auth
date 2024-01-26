package com.sparta.springauth.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "users") //테이블은 클래스명이 아니라 이걸로 만들어짐
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //중복안돼 unique 이거 닉네임임
    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    //Enum타입 저장함 Enum의 이름 그대로 저장됨
    //만약 USER(Authority.USER) 이거 넣는다고 하면 이거 생긴거 그대로 저장됨
    @Enumerated(value = EnumType.STRING)
    private UserRoleEnum role;

    public User(String username, String password, String email, UserRoleEnum role) {
        this.username=username;
        this.password=password;
        this.role=role;
        this.email=email;
    }
}