package com.kdt.cafe.order;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//A11
//A12
//A13
//A14
//A16
//A17
//A18
//A22
//A23
//A24
@RestController
@RequestMapping("/api/v1/orders")
public class OrderApiController {
    private final OrderService orderService;

    @Autowired
    public OrderApiController(OrderService orderService) {
        this.orderService = orderService;
    }

    //A11   주문접수 201 · Order
    //POST /api/v1/orders
    @PostMapping()
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody OrderCreateRequest request){
        OrderResponse order = this.orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    //A12   이메일별 주문 200 · OrderSummary[]
    //GET /api/v1/orders?email={email}
    @GetMapping()
    public List<OrderSummaryResponse> findByEmail(@RequestParam("email") String email){
        return this.orderService.findOrdersByEmail(email);
    }

    //A13   주문 상세 200 · Order
    //GET /api/v1/orders/{orderId}
    @GetMapping("/{orderId}")
    public OrderResponse findOrder(@PathVariable("orderId") Long id){
        return this.orderService.findOrder(id);
    }

    //A14   배송 묶음 200 · DeliveryBatch[]
    //GET /api/v1/delivery-batches?shippingDate={shippingDate}
    @GetMapping()
    public List<DeliveryBatchResponse> findDeliveryBatches()


    //A16   주문 취소 200 · 전체 Order
    //POST /api/v1/orders/{orderId}/cancel


    //A17   접수일별 주문 통계 200 · OrderStatistics
    //GET /api/v1/statistics/orders?date={date}


    //A18   접수일별 메뉴 통계 200 · MenuStatistics[]
    //GET /api/v1/statistics/menus?date={date}


    //A22   주문 메모 수정 200 · 전체 Order
    //PUT /api/v1/orders/{orderId}/memo

    //A23   재주문 201 · 새 Order
    //POST /api/v1/orders/{orderId}/reorder


    //A24   접수일별 고객 통계 200 · CustomerStatistics[]
    //GET /api/v1/statistics/customers?date={date}







}
