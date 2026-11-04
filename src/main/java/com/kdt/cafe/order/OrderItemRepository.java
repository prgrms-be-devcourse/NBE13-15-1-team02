package com.kdt.cafe.order;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem,Long> {

    public boolean existsByMenuId(Long menuId);

    public List<OrderItem> findByOrderIdOrderByIdAsc(Long orderId);

}
