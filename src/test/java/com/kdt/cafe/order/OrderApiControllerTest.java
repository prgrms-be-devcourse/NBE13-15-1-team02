package com.kdt.cafe.order;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.kdt.cafe.common.ApiExceptionAdvice;
import com.kdt.cafe.common.OrderNotCancellableException;
import com.kdt.cafe.common.OrderNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

// OrderApiController만 테스트. 실제 DB나 8080 서버는 사용하지 않음.
@WebMvcTest(OrderApiController.class)

@Import(ApiExceptionAdvice.class)
public class OrderApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // 실제 OrderService 대신 테스트에서 동작을 지정하는 mock 객체를 사용한다.
    @MockitoBean
    private OrderService orderService;

    @Test
        // A13 주문 상세 : 없는 주문을 조회하면 404 ORDER_NOT_FOUND를 반환하는지 확인
    void order_not_exist_is_404() throws Exception {
        // 준비 : 999번 주문을 찾으면 "없음" 예외를 던지도록 지정
        given(this.orderService.findOrder(999L))
                .willThrow(new OrderNotFoundException("주문을 찾을 수 없습니다."));

        // 실행과 응답 검증
        this.mockMvc.perform(get("/api/v1/orders/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ORDER_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("주문을 찾을 수 없습니다."));
    }

    @Test
        // A16 주문 취소 : 취소 마감이 지난 주문을 취소하면 409 ORDER_NOT_CANCELLABLE을 반환하는지 확인
    void cancel_after_deadline_is_409() throws Exception {
        // 준비 : 1번 주문을 취소하면 "취소 불가" 예외를 던지도록 지정
        given(this.orderService.cancelOrder(1L))
                .willThrow(new OrderNotCancellableException("배송 대상일 14시 전의 접수 상태 주문만 취소할 수 있습니다."));

        // 실행과 응답 검증
        this.mockMvc.perform(post("/api/v1/orders/1/cancel"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ORDER_NOT_CANCELLABLE"))
                .andExpect(jsonPath("$.message").value("배송 대상일 14시 전의 접수 상태 주문만 취소할 수 있습니다."));
    }
}