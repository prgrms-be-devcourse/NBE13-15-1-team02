package com.kdt.cafe.order;

import com.kdt.cafe.menu.Menu;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

        @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
        @OrderBy("id ASC")
        private List<OrderItem> items = new ArrayList<>();

        protected Order() {}

        // 주문 생성 : A11 주문 접수, A23 재주문
        // now = Service가 Clock으로 구해서 넘겨준 접수 시각
        public Order (String email, String address, String postcode, LocalDateTime now) {
                this.email = email;
                this.address = address;
                this.postcode = postcode;
                this.createdAt = now;
                this.shippingDate = ShippingDateCalculator.shippingDateOf(now); // 접수 시간과 같은 시각으로 배송일 계산
                this.totalAmount = 0;
                this.status = OrderStatus.PLACED;
                this.cancelledAt = null;
                this.memo = "";
        }

        // 주문 항목 추가 : 메뉴의 현재 이름과 가격을 복사하여 총액에 더함
        public void addItem(Menu menu, int quantity) {
                OrderItem item = new OrderItem(this, menu, quantity);
                items.add(item);
                totalAmount += item.getLineAmount();
        }

        // A16 주문 취소
        public void cancel(LocalDateTime now) {
                // 이미 취소된 주문일 경운 최초 cancelledAt 유지
                if (status == OrderStatus.CANCELLED) {
                        return;
                }
                // 취소 가능 여부 판단 - 예외 던지기
                //if (!ShippingDateCalculator.isCancellable(shippingDate, now)) {
                //
                //}
                this.status = OrderStatus.CANCELLED;
                this.cancelledAt = now;
        }

        // A22 메모 수정 : 취소된 주문도 메모 수정 가능
        public void changeMemo(String memo) {
                this.memo = memo;
        }

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

        public List<OrderItem> getItems() {
                return items;
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