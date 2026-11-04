package com.kdt.cafe.menu;

import com.kdt.cafe.category.Category;
import com.kdt.cafe.order.OrderItem;
import jakarta.persistence.*;
import lombok.Getter;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "menu")
@Getter
public class Menu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private int price;

    @Column(nullable = false, length = 300)
    private String description;

    @Column(nullable = false)
    private boolean available;

    // cascade/orphanRemoval 제거
    @OneToMany(mappedBy = "menu", fetch = FetchType.LAZY)
    private List<OrderItem> orderItems = new ArrayList<>();

    protected Menu() {
    }

    public Menu(Category category, String name, int price, String description) {
        this.category = category;
        this.name = name;
        this.price = price;
        this.description = description;
        this.available = true; // 새 메뉴는 판매 중
    }

    public void changeCategory(Category category) { this.category = category; }
    public void changeName(String name) { this.name = name; }
    public void changePrice(int price) { this.price = price; }
    public void changeDescription(String description) { this.description = description; }
    public void changeAvailable(boolean available) { this.available = available; }
}