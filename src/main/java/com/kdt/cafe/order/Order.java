package com.kdt.cafe.order;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "`order`")
public class Order {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(nullable = false, length = 100)
        private String email;

        @Column(nullable = false, length = 200)
        private String address;

        @Column(nullable = false, length = 5)
        private String postcode;

        @Column(nullable = false)
        private LocalDateTime createdAt;

        @Column(nullable = false)
        private LocalDate shippingDate;

        @Column(nullable = false)
        private long totalAmount;

        @Enumerated(EnumType.STRING)
        @Column(nullable = false)
        private OrderStatus status;

        @Column(nullable = true)
        private LocalDateTime cancelledAt;

        @Column(nullable = false, length = 200)
        private String memo;

        protected Order() {
        };

        public Long getId() {
            return id;
        }

        public String getEmail() {
            return email;
        }

        public String getAddress() {
            return address;
        }

        public String getPostcode() {
            return postcode;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        public LocalDate getShippingDate() {
            return shippingDate;
        }

        public long getTotalAmount() {
            return totalAmount;
        }

        public OrderStatus getStatus() {
            return status;
        }

        public LocalDateTime getCancelledAt() {
            return cancelledAt;
        }

        public String getMemo() {
            return memo;
        }
}