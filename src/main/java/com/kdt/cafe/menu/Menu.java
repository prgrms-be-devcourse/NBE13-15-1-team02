package com.kdt.cafe.menu;

import com.kdt.cafe.category.Category;
import com.kdt.cafe.order.OrderItem;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "menu")
//Getter로 잠깐 수정하였습니다
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

    //새 메뉴는 available=true 라고 되어있어서 수정하였습니다.
    @Column(nullable = true)
    private boolean available;

    //OneToMany 추가
    //2026.11.3
    @OneToMany(mappedBy = "menu",fetch = FetchType.LAZY,
    cascade = CascadeType.ALL,orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    protected Menu(){
    }

    public Menu(Category category, String name, int price, String description) {
        this.category = category;
        this.name = name;
        this.price = price;
        this.description = description;
        this.available = true;
    }

    public boolean isAvailable() {
        return available;
    }

    public void changeCategory(Category category) {
        this.category = category;
    }

    public void changeName(String name) {
        this.name = name;
    }

    public void changePrice(int price) {
        this.price = price;
    }

    public void changeDescription(String description) {
        this.description = description;
    }

    public void changeAvailable(boolean available) {
        this.available = available;
    }

    //update 로직이 있으면 안될거같아서 주석처리 하였습니다.
    //
    //    public void update(Category category, String name, String description, int price) {
//        this.category = category;
//        this.name = name;
//        this.price = price;
//        this.description = description;
//    }
}
