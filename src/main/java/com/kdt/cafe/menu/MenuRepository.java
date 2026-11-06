package com.kdt.cafe.menu;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MenuRepository extends JpaRepository<Menu, Long> {

    @EntityGraph(attributePaths = "category")
    List<Menu> findAllByOrderByIdAsc();

    List<Menu> findByCategoryIdOrderByIdAsc(Long categoryId);

    List<Menu> findByAvailableTrueOrderByIdAsc();

    boolean existsByCategoryId(Long categoryId);
}