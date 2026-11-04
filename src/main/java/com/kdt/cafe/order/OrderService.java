package com.kdt.cafe.order;

import com.kdt.cafe.menu.MenuRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final MenuRepository menuRepository;

    @Autowired
    public OrderService(OrderRepository orderRepository, MenuRepository menuRespository) {
        this.orderRepository = orderRepository;
        this.menuRepository = menuRespository;
    }

    // A11 주문 접수
    @Transactional
    public OrderResponse createOrder(OrderCreateRequest request) {

    }

    // A12 이메일별 주문

    // A13 주문 상세

    // A14 배송 묶음


    // A16 주문 취소

    // A17 접수일별 주문 통계

    // A22 주문 메모 수정

    // A23 재주문

    // A24 접수일별 고객 통계

}
