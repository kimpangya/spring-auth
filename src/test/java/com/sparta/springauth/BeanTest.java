package com.sparta.springauth;

import com.sparta.springauth.food.Food;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

//이거 걸어야 Bean 받아오는 거 주입 받을수있음
@SpringBootTest
public class BeanTest {
    @Autowired
    @Qualifier("piz")
    Food food;

    @Test
    @DisplayName("테스트")
    void test1() {
        food.eat();
    }
}
