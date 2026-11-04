package com.kdt.cafe.order;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order,Long> {

    // A12 이메일별 주문 목록 : 취소 주문 포함, 접수 시각 내림차순, 같으면 ID 내림차순
    List<Order> findByEmailOrderByCreatedAtDescIdDesc(String email);

    // A14 배송 묶음 : 해당 배송일의 취소되지 않은 주문, 이메일순
    List<Order> findByShippingDateAndStatusOrderByIdAsc(LocalDate shippingDate, OrderStatus status);

    // A11, A23 배송지 비교
    List<Order> findByEmailAndShippingDateAndStatus(String email, LocalDate shippingDate, OrderStatus status);

    // A17, A18, A24 통계 : 접수 시각이 [start, end) 범위인 주문
    List<Order> findByCreatedAtGreaterThanEqualAndCreatedAtLessThanOrderByIdAsc(LocalDateTime start, LocalDateTime end);
}
