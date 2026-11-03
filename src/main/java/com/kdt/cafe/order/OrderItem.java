package com.kdt.cafe.order;

import com.kdt.cafe.menu.Menu;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "order_item")
@Getter
@NoArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    //Order 테이블의 id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id",nullable = false)
    private Order order;
    //Menu 테이블의 id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id",nullable = false)
    private Menu menu;
    @Column(name = "menu_name",length = 100,nullable = false)
    private String menuName;
    @Column(name = "unit_price",nullable = false)
    private int unitPrice;
    @Column(nullable = false)
    private int quantity;
    @Column(name = "line_amount",nullable = false)
    private int lineAmount;

    public OrderItem(Order order, Menu menu, String menuName, int unitPrice, int quantity, int lineAmount) {
        this.order = order;
        this.menu = menu;
        this.menuName = menuName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.lineAmount = lineAmount;
    }




}