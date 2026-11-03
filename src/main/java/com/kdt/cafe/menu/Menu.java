package com.kdt.cafe.menu;

import com.kdt.cafe.category.Category;
import jakarta.persistence.*;


@Entity
@Table(name = "menu")
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

    protected Menu(){
    }

    public Menu(Category category, String name, int price, String description) {
        this.category = category;
        this.name = name;
        this.price = price;
        this.description = description;
        this.available = true;
    }

    public void update(Category category, String name, String description, int price) {
        this.category = category;
        this.name = name;
        this.price = price;
        this.description = description;
    }

    public void changeAvailable(boolean available) {
        this.available = available;
    }

    public Long getId() {
        return id;
    }

    public Category getCategory() {
        return category;
    }

    public String getName(){
        return name;
    }

    public int getPrice() {
        return price;
    }

    public String getDescription() {
        return description;
    }

    public boolean isAvailable() {
        return available;
    }
}
