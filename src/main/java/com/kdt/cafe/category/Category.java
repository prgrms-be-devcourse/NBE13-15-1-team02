package com.kdt.cafe.category;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
* 카테고리 엔티티
* menu 엔티티가 이 엔티티 참조
* */
@Entity
@Table(name = "category")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)  // 기본 생성자를 protected로 생성
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // 대소문자 구분은 DDL에서, JPA는 관리하지 않음 ( 이유 : utf8mb4_bin 설정 해놓음)
    @Column(name = "name", nullable = false, unique = true, length = 30)
    private String name;

    // 카테고리 생성자
    public Category(String name) {
        this.name = name;
    }

    // 이름 변경
    public void updateName(String name) {
        this.name = name;
    }

}
