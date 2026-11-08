package com.kdt.cafe.order;

import com.kdt.cafe.category.Category;
import com.kdt.cafe.common.OrderNotCancellableException;
import com.kdt.cafe.menu.Menu;
import com.kdt.cafe.menu.MenuRepository;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

// 업무 규칙 자동 테스트 : OrderService의 실제 로직을 실행
// 리포지토리만 가짜 객체(mock)로 대신
// 현재 시각은 Clock.fixed(멈춘 시계)로 고정해 실행 시각과 상관없이 같은 결과가 나옴
public class OrderServiceTest {

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    // Spring 없이 OrderService를 직접 new로 만드는 테스트라 @MockitoBean 대신 mock()으로 직접 만들어 생성자에 넘김
    private final OrderRepository orderRepository = mock(OrderRepository.class);
    private final MenuRepository menuRepository = mock(MenuRepository.class);

    // 서울 시각 기준으로 멈춘 시계를 넣은 OrderService
    private OrderService serviceAt(LocalDateTime seoulNow) {
        Clock fixedClock = Clock.fixed(seoulNow.atZone(SEOUL).toInstant(), SEOUL);
        return new OrderService(orderRepository, menuRepository, fixedClock);
    }

    // 10:00:00에 접수한 PLACED 주문 (배송일 = 같은 날)
    private Order placedOrder(Menu menu, int quantity) {
        Order order = new Order("buyer@example.com", "서울시 실습로 10 101호", "01234",
                LocalDateTime.of(2026, 11, 3, 10, 0, 0));
        order.addItem(menu, quantity);
        return order;
    }

    @Test
    // 재주문은 현재 메뉴 이름과 가격으로 새 주문을 계산 (원본 주문의 단가, 항목 금액, 총액은 유지)
    void reorder_uses_current_price_and_keeps_original() {
        // 준비 : 5000원 메뉴 2개로 원본 주문
        Menu menu = new Menu(new Category("싱글오리진"), "Columbia Nariñó", 5000, "콜롬비아 나리뇨 원두");
        Order original = placedOrder(menu, 2);
        given(this.orderRepository.findWithItemsAndMenus(1L)).willReturn(Optional.of(original));

        // 원본 주문 이후 메뉴 이름과 가격이 바뀜
        menu.changeName("Columbia Nariñó (New)");
        menu.changePrice(7000);

        // 실행 : 2026-11-05 15:00:00에 재주문
        LocalDateTime reorderedAt = LocalDateTime.of(2026, 11, 5, 15, 0, 0);
        OrderResponse response = serviceAt(reorderedAt).reorder(1L);

        // 검증 1 : 새 주문은 현재 이름·가격, 재주문 시각 기준, PLACED / cancelledAt=null / memo=""
        assertEquals(1, response.items().size());
        assertEquals("Columbia Nariñó (New)", response.items().get(0).menuName());
        assertEquals(7000, response.items().get(0).unitPrice());
        assertEquals(2, response.items().get(0).quantity());
        assertEquals(14000, response.items().get(0).lineAmount());
        assertEquals(14000, response.totalAmount());
        assertEquals(reorderedAt, response.createdAt().toLocalDateTime());
        assertEquals(LocalDate.of(2026, 11, 6), response.shippingDate());      // 15시 접수 → 다음 날
        assertEquals(OrderStatus.PLACED, response.status());
        assertNull(response.cancelledAt());
        assertEquals("", response.memo());

        // 검증 2 : 원본 주문은 주문 당시 값 그대로
        OrderItem originalItem = original.getItems().get(0);
        assertEquals("Columbia Nariñó", originalItem.getMenuName());
        assertEquals(5000, originalItem.getUnitPrice());
        assertEquals(10000, originalItem.getLineAmount());
        assertEquals(10000, original.getTotalAmount());
        assertEquals(OrderStatus.PLACED, original.getStatus());
    }

    @Test
    // 멈춘 시계가 배송일 13:59:59이면 Service를 통한 취소 성공
    void cancel_at_135959_succeeds() {
        // 준비
        Menu menu = new Menu(new Category("싱글오리진"), "Ethiopia Sidamo", 11000, "에티오피아 시다모 원두");
        Order order = placedOrder(menu, 1);     // 배송일 2026-11-03
        given(this.orderRepository.findById(1L)).willReturn(Optional.of(order));
        LocalDateTime now = LocalDateTime.of(2026, 11, 3, 13, 59, 59);

        // 실행
        OrderResponse response = serviceAt(now).cancelOrder(1L);

        // 검증
        assertEquals(OrderStatus.CANCELLED, response.status());
        assertEquals(now, response.cancelledAt().toLocalDateTime());
    }

    @Test
    // 멈춘 시계가 배송일 14:00:00이면 Service를 통한 취소는 ORDER_NOT_CANCELLABLE
    void cancel_at_140000_fails() {
        // 준비
        Menu menu = new Menu(new Category("싱글오리진"), "Ethiopia Sidamo", 11000, "에티오피아 시다모 원두");
        Order order = placedOrder(menu, 1);     // 배송일 2026-11-03
        given(this.orderRepository.findById(1L)).willReturn(Optional.of(order));
        OrderService service = serviceAt(LocalDateTime.of(2026, 11, 3, 14, 0, 0));

        // 실행 + 검증
        assertThrows(OrderNotCancellableException.class, () -> service.cancelOrder(1L));
        assertEquals(OrderStatus.PLACED, order.getStatus());
    }
}