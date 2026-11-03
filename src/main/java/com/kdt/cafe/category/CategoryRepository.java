package com.kdt.cafe.category;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
* 카테고리 레포지토리 (데이터 접근 계층)
* JpaRepository 상속으로 기본 CRUD 제공
* */
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // id 오름차순으로 카테고리 목록 전체 조회
    List<Category> findAllByOrderByIdAsc();

    // 등록 시 중복검사 (A03)
    // 같은 이름이 하나라도 있다면 -> 409 CATEGORY_NAME_DUPLICATED
    boolean existsByName(String name);

    // 수정 시 중복검사 (A04)
    // 카테고리 이름 변경 시 자기 자신을 제외하고 그 이름을 다른 카테고리가 쓰고 있는지 검사
    boolean existsByNameAndIdNot(String name, Long id);

}
