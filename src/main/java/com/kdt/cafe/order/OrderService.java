package com.kdt.cafe.order;

import com.kdt.cafe.menu.MenuRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final MenuRepository menuRepository;

    @Autowired
    public OrderService(OrderRepository orderRepository, MenuRepository menuRespository) {
        this.orderRepository = orderRepository;
        this.menuRepository = menuRespository;
    }
}
