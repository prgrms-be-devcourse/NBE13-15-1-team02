package com.kdt.cafe.order;

import com.kdt.cafe.common.*;
import com.kdt.cafe.menu.Menu;
import com.kdt.cafe.menu.MenuRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Pattern;

@Service
@Transactional(readOnly = true)
public class OrderService {

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    // 명세 이메일 정규식
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(?:\\.[A-Za-z0-9-]+)+$");

    // URL 날짜 허용 범위 : 2000-01-01 ~ 2099-12-31
    private static final LocalDate MIN_DATE = LocalDate.of(2000, 1, 1);
    private static final LocalDate MAX_DATE = LocalDate.of(2099, 12, 31);

    // 400 응답 메시지 (명세 오류 표)
    private static final String VALIDATION_MESSAGE = "입력값을 확인해 주세요.";

    // 항목 합산 정렬 : 메뉴 ID → 단가 → 메뉴명 오름차순
    private static final Comparator<ItemKey> ITEM_KEY_ORDER =
            Comparator.comparing(ItemKey::menuId)
                    .thenComparingInt(ItemKey::unitPrice)
                    .thenComparing(ItemKey::menuName);

    private final OrderRepository orderRepository;
    private final MenuRepository menuRepository;

    @Autowired
    public OrderService(OrderRepository orderRepository, MenuRepository menuRepository) {
        this.orderRepository = orderRepository;
        this.menuRepository = menuRepository;
    }

    // A11 주문 접수
    @Transactional
    public OrderResponse createOrder(OrderCreateRequest request) {
        // menuId 중복 검사 -> 400 에러
        validateNoDuplicateMenu(request.items());

        // 메뉴 존재 여부 검사 -> 404 에러
        List<Menu> menus = new ArrayList<>();
        for (OrderItemRequest itemRequest : request.items()) {
            Menu menu = menuRepository.findById(itemRequest.menuId())
                    .orElseThrow(() -> new MenuNotFoundException("메뉴를 찾을 수 없습니다."));
            menus.add(menu);
        }

        // 판매 가능 여부 검사 -> 409 에러
        for (Menu menu : menus) {
            checkAvailable(menu);
        }

        // 동일한 이메일과 배송일의 기존 주문과 배송지 비교 -> 409 에러
        LocalDateTime now = now();
        checkAddress(request.email(), request.address(), request.postcode(),
                ShippingDateCalculator.shippingDateOf(now));

        Order order = new Order(request.email(), request.address(), request.postcode(), now);
        for (int i = 0; i < menus.size(); i++) {
            order.addItem(menus.get(i), request.items().get(i).quantity());
        }
        orderRepository.save(order);
        return OrderResponse.from(order);
    }

    // A12 이메일별 주문 : 취소 주문 포함, 접수 시각 내림차순 → ID 내림차순
    public List<OrderSummaryResponse> findOrdersByEmail(String email) {
        validateEmail(email);
        return orderRepository.findByEmailOrderByCreatedAtDescIdDesc(email).stream()
                .map(OrderSummaryResponse::from)
                .toList();
    }

    // A13 주문 상세
    public OrderResponse findOrder(Long orderId) {
        return OrderResponse.from(getOrder(orderId));
    }

    // A14 배송 묶음 : PLACED 주문만, 이메일순
    public List<DeliveryBatchResponse> findDeliveryBatches(LocalDate shippingDate) {
        validateDate(shippingDate);

        List<Order> orders = orderRepository
                .findDeliveryTargets(shippingDate,OrderStatus.PLACED);

        List<DeliveryBatchResponse> batches = new ArrayList<>();
        for (List<Order> group : groupByEmail(orders).values()) {
            Order first = group.get(0);
            List<Long> orderIds = group.stream().map(Order::getId).toList();   // 이미 ID 오름차순
            List<OrderItem> items = group.stream().flatMap(o -> o.getItems().stream()).toList();

            List<OrderItemResponse> itemResponses = sumItems(items).entrySet().stream()
                    .map(e -> new OrderItemResponse(
                            e.getKey().menuId(),
                            e.getKey().menuName(),
                            e.getKey().unitPrice(),
                            Math.toIntExact(e.getValue()),
                            Math.toIntExact(e.getKey().unitPrice() * e.getValue())))
                    .toList();

            batches.add(new DeliveryBatchResponse(
                    shippingDate,
                    first.getEmail(),
                    first.getAddress(),
                    first.getPostcode(),
                    orderIds,
                    orderIds.size(),
                    items.stream().mapToLong(OrderItem::getQuantity).sum(),
                    group.stream().mapToLong(Order::getTotalAmount).sum(),
                    itemResponses
            ));
        }
        return batches;
    }

    // A16 주문 취소 : 이미 취소된 주문은 그대로 200
    @Transactional
    public OrderResponse cancelOrder(Long orderId) {
        Order order = getOrder(orderId);
        order.cancel(now());
        return OrderResponse.from(order);
    }

    // A17 접수일별 주문 통계 : 주문이 없어도 0으로 채운 객체 반환
    public OrderStatisticsResponse getOrderStatistics(LocalDate date) {
        validateDate(date);
        Summary summary = Summary.of(findStatisticsTargetsOn(date));
        return new OrderStatisticsResponse(
                date,
                summary.orderCount(),
                summary.placedOrderCount(),
                summary.cancelledOrderCount(),
                summary.totalQuantity(),
                summary.totalAmount()
        );
    }

    // A18 접수일별 메뉴 통계 : PLACED 주문만, 메뉴 ID → 단가 → 메뉴명
    public List<MenuStatisticsResponse> getMenuStatistics(LocalDate date) {
        // 날짜 검사
        validateDate(date);
        // PLACED 주문만 리스트에 담기
        List<OrderItem> placedItems = new ArrayList<>();
        for (Order o : findStatisticsTargetsOn((date))) {
            if (o.getStatus() == OrderStatus.PLACED) {
                placedItems.addAll(o.getItems());
            }
        }
        // 메뉴ID, 단가, 메뉴명이 같은 항목끼리 수량을 더해서 리스트에 담기
        List<MenuStatisticsResponse> result = new ArrayList<>();
        for(Map.Entry<ItemKey, Long> e : sumItems(placedItems).entrySet()) {
            ItemKey key = e.getKey();
            Long quantity = e.getValue();
            result.add(new MenuStatisticsResponse(key.menuId(), key.menuName(), key.unitPrice(), quantity, key.unitPrice() * quantity));
        }
        return result;
    }

    // A22 주문 메모 수정 : 취소된 주문도 수정 가능
    @Transactional
    public OrderResponse updateMemo(Long orderId, MemoUpdateRequest request) {
        Order order = getOrder(orderId);
        order.changeMemo(request.memo());
        return OrderResponse.from(order);
    }

    // A23 재주문 : 원본 주문의 이메일, 주소, 우편번호, 메뉴, 수량 + 현재 메뉴 이름, 가격
    @Transactional
    public OrderResponse reorder(Long orderId) {
        // 원본 주문 꺼내기 -> 없으면 404 에러
        Order original = getOrder(orderId);     //여기서 findByid, select 쿼리 실행

        // 판매 중지 메뉴 검사 -> 하나라도 판매 중지면 409 에러
        for(OrderItem item : original.getItems()) {
            checkAvailable(item.getMenu());     //id외의 필드를 읽으면 아이템마다 select from menu where id=?가 나간다
        }

        // 배송지 비교 (배송일은 재주문 시각 기준으로 다시 계산)
        LocalDateTime now = now();
        checkAddress(original.getEmail(), original.getAddress(), original.getPostcode(),
                ShippingDateCalculator.shippingDateOf(now));

        // 새 주문서 만들기
        Order newOrder = new Order(original.getEmail(), original.getAddress(), original.getPostcode(), now);
        // 항목 담기
        for (OrderItem item : original.getItems()) {
            newOrder.addItem(item.getMenu(), item.getQuantity());   // 현재 메뉴 이름, 가격으로 복사
        }
        // 저장
        orderRepository.save(newOrder);
        return OrderResponse.from(newOrder);
    }

    // A24 접수일별 고객 통계 : 이메일 오름차순, 모두 취소한 고객도 포함
    public List<CustomerStatisticsResponse> getCustomerStatistics(LocalDate date) {
        // 날짜 검사
        validateDate(date);

        // 접수된 주문을 이메일별로 나누어 계산
        List<CustomerStatisticsResponse> result = new ArrayList<>();
        for (Map.Entry<String, List<Order>> e : groupByEmail(findStatisticsTargetsOn(date)).entrySet()) {
            String email = e.getKey();
            Summary summary = Summary.of(e.getValue());
            result.add(new CustomerStatisticsResponse(date, email, summary.orderCount(), summary.placedOrderCount(), summary.cancelledOrderCount(), summary.totalQuantity(), summary.totalAmount()));
        }

        return result;
    }

    // 현재 시각 : 서울 기준, 초 단위 (초 아래 소수점 단위 자르기)
    private LocalDateTime now() {
        return LocalDateTime.now(SEOUL).truncatedTo(ChronoUnit.SECONDS);
    }

    // orderId가 null이거나 0 이하(400 에러), DB에 없음(404 에러)
    private Order getOrder(Long orderId) {
        if (orderId == null || orderId < 1) {
            throw new ValidationErrorException(VALIDATION_MESSAGE);
        }
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("주문을 찾을 수 없습니다."));
    }

    // 주문 항목에 같은 메뉴가 중복으로 담긴 경우 400 에러
    private void validateNoDuplicateMenu(List<OrderItemRequest> items) {
        Set<Long> seen = new HashSet<>();
        for (OrderItemRequest item : items) {
            if (!seen.add(item.menuId())) {
                throw new ValidationErrorException(VALIDATION_MESSAGE);
            }
        }
    }

    // A12 이메일별 주문 조회에서 입력받은 이메일 검사 -> 400 에러
    private void validateEmail(String email) {
        if (email == null || email.isEmpty() || email.length() > 100
                || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new ValidationErrorException(VALIDATION_MESSAGE);
        }
    }

    // A14, A17, A18, A24에서 입력받은 날짜가 허용 범위 안에 있는지 검사 -> 400 에러
    private void validateDate(LocalDate date) {
        if (date == null || date.isBefore(MIN_DATE) || date.isAfter(MAX_DATE)) {
            throw new ValidationErrorException(VALIDATION_MESSAGE);
        }
    }

    // 메뉴가 판매 중인지 검사 -> 409 에러
    private void checkAvailable(Menu menu) {
        if (!menu.isAvailable()) {
            throw new MenuUnavailableException("판매 중지된 메뉴가 포함되어 있습니다.");
        }
    }

    // A11, A23 : 같은 이메일, 배송일의 PLACED 주문 중 주소나 우편번호가 다른 주문이 있는지 검사 -> 409 에러
    private void checkAddress(String email, String address, String postcode, LocalDate shippingDate) {
        boolean mismatch = orderRepository
                .findByEmailAndShippingDateAndStatus(email, shippingDate, OrderStatus.PLACED).stream()
                .anyMatch(o -> !o.getAddress().equals(address) || !o.getPostcode().equals(postcode));
        if (mismatch) {
            throw new AddressMissMatchException("같은 이메일과 배송일의 기존 주문과 배송지가 다릅니다.");
        }
    }

    // 서울 기준 접수일 : [그날 00:00, 다음 날 00:00)
    private List<Order> findStatisticsTargetsOn(LocalDate date) {
        return orderRepository.findStatisticsTargets(
                date.atStartOfDay(), date.plusDays(1).atStartOfDay());
    }

    // A14, A24 주문 목록을 이메일별로 나눠 담기
    private Map<String, List<Order>> groupByEmail(List<Order> orders) {
        Map<String, List<Order>> result = new TreeMap<>();      // 키(이메일)가 자동으로 오름차순 정렬
        for(Order order : orders) {
            String email = order.getEmail();
            if(!result.containsKey(email)) {
                result.put(email, new ArrayList<>());
            }
            result.get(email).add(order);                       // 같은 이메일의 주문 추가.
        }
        return result;
    }

    // 메뉴 ID, 주문 당시 메뉴명, 단가가 모두 같은 항목끼리 수량 합산
    private Map<ItemKey, Long> sumItems(List<OrderItem> items) {
        Map<ItemKey, Long> quantityByKey = new TreeMap<>(ITEM_KEY_ORDER);
        for (OrderItem item : items) {
            ItemKey key = new ItemKey(item.getMenu().getId(), item.getUnitPrice(), item.getMenuName());
            quantityByKey.merge(key, (long) item.getQuantity(), Long::sum);
        }
        return quantityByKey;
    }

    // 항목 합산 기준
    private record ItemKey(Long menuId, int unitPrice, String menuName) {}

    // 주문, 고객 통계 공통 계산 : 수량, 금액은 PLACED만
    private record Summary(long orderCount, long placedOrderCount, long cancelledOrderCount,
                           long totalQuantity, long totalAmount) {

        static Summary of(List<Order> orders) {
            long placedCount = 0;
            long totalQuantity = 0;
            long totalAmount = 0;
            for (Order o : orders) {
                // 주문 상태가 PLACED인 주문 수량, 금액 계산
                if(o.getStatus() == OrderStatus.PLACED) {
                    placedCount++;
                    totalAmount += o.getTotalAmount();
                    for (OrderItem item : o.getItems()) {
                        totalQuantity += item.getQuantity();
                    }
                }
            }
            return new Summary(orders.size(), placedCount, orders.size() - placedCount, totalQuantity, totalAmount);
        }
    }
}
