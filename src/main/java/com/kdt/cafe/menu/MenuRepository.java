package com.kdt.cafe.menu;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MenuRepository extends JpaRepository<Menu, Long> {

    List<Menu> findAllByOrderByIdAsc();

    List<Menu> findByCategoryIdOrderByIdAsc(Long categoryId);

    List<Menu> findByAvailableTrueOrderByIdAsc();

    @Query(value = "select m from Menu m where locate(:keyword, m.name) > 0 order by m.id asc")
    List<Menu> searchByName(@Param("keyword") String keyword);

    boolean existsByCategoryId(Long categoryId);
}
