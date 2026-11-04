package com.kdt.cafe.order;

import com.kdt.cafe.common.AddressMissMatchException;
import com.kdt.cafe.common.MenuNotFoundException;
import com.kdt.cafe.common.MenuUnavailableException;
import com.kdt.cafe.common.OrderNotFoundException;
import com.kdt.cafe.common.ValidationErrorException;
import com.kdt.cafe.menu.Menu;
import com.kdt.cafe.menu.MenuRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
        // 400 : menuId 중복
        validateNoDuplicateMenu(request.items());

        // 404 : 메뉴 존재 여부
        List<Menu> menus = new ArrayList<>();
        for (OrderItemRequest itemRequest : request.items()) {
            Menu menu = menuRepository.findById(itemRequest.menuId())
                    .orElseThrow(() -> new MenuNotFoundException("메뉴를 찾을 수 없습니다."));
            menus.add(menu);
        }

        // 409 : 판매 중지 메뉴
        menus.forEach(this::checkAvailable);

        // 409 : 같은 이메일, 배송일의 기존 주문과 배송지 비교
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
                .findByShippingDateAndStatusOrderByIdAsc(shippingDate, OrderStatus.PLACED);

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
        Summary summary = Summary.of(findOrdersReceivedOn(date));
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
        validateDate(date);
        List<OrderItem> placedItems = findOrdersReceivedOn(date).stream()
                .filter(o -> o.getStatus() == OrderStatus.PLACED)
                .flatMap(o -> o.getItems().stream())
                .toList();

        return sumItems(placedItems).entrySet().stream()
                .map(e -> new MenuStatisticsResponse(
                        e.getKey().menuId(),
                        e.getKey().menuName(),
                        e.getKey().unitPrice(),
                        e.getValue(),
                        e.getKey().unitPrice() * e.getValue()))
                .toList();
    }

    // A22 주문 메모 수정 : 취소된 주문도 수정 가능
    @Transactional
    public OrderResponse updateMemo(Long orderId, MemoUpdateRequest request) {
        Order order = getOrder(orderId);
        order.changeMemo(request.memo());
        return OrderResponse.from(order);
    }

    // A23 재주문 : 원본의 이메일, 주소, 우편번호, 메뉴, 수량 + 현재 메뉴 이름, 가격
    @Transactional
    public OrderResponse reorder(Long orderId) {
        // 404 : 원본 주문
        Order original = getOrder(orderId);

        // 409 : 판매 중지 메뉴
        original.getItems().forEach(item -> checkAvailable(item.getMenu()));

        // 409 : 배송지 비교 (배송일은 재주문 시각 기준으로 다시 계산)
        LocalDateTime now = now();
        checkAddress(original.getEmail(), original.getAddress(), original.getPostcode(),
                ShippingDateCalculator.shippingDateOf(now));

        Order newOrder = new Order(original.getEmail(), original.getAddress(), original.getPostcode(), now);
        for (OrderItem item : original.getItems()) {
            newOrder.addItem(item.getMenu(), item.getQuantity());   // 현재 메뉴 이름, 가격으로 복사
        }
        orderRepository.save(newOrder);
        return OrderResponse.from(newOrder);
    }

    // A24 접수일별 고객 통계 : 이메일 오름차순, 모두 취소한 고객도 포함
    public List<CustomerStatisticsResponse> getCustomerStatistics(LocalDate date) {
        validateDate(date);
        return groupByEmail(findOrdersReceivedOn(date)).entrySet().stream()
                .map(e -> {
                    Summary summary = Summary.of(e.getValue());
                    return new CustomerStatisticsResponse(
                            date,
                            e.getKey(),
                            summary.orderCount(),
                            summary.placedOrderCount(),
                            summary.cancelledOrderCount(),
                            summary.totalQuantity(),
                            summary.totalAmount()
                    );
                })
                .toList();
    }

    // 현재 시각 : 서울 기준, 초 단위
    private LocalDateTime now() {
        return LocalDateTime.now(SEOUL).truncatedTo(ChronoUnit.SECONDS);
    }

    // 400 : orderId 형식 → 404 : 주문 없음
    private Order getOrder(Long orderId) {
        if (orderId == null || orderId < 1) {
            throw new ValidationErrorException(VALIDATION_MESSAGE);
        }
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("주문을 찾을 수 없습니다."));
    }

    private void validateNoDuplicateMenu(List<OrderItemRequest> items) {
        Set<Long> seen = new HashSet<>();
        for (OrderItemRequest item : items) {
            if (!seen.add(item.menuId())) {
                throw new ValidationErrorException(VALIDATION_MESSAGE);
            }
        }
    }

    private void validateEmail(String email) {
        if (email == null || email.isEmpty() || email.length() > 100
                || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new ValidationErrorException(VALIDATION_MESSAGE);
        }
    }

    private void validateDate(LocalDate date) {
        if (date == null || date.isBefore(MIN_DATE) || date.isAfter(MAX_DATE)) {
            throw new ValidationErrorException(VALIDATION_MESSAGE);
        }
    }

    private void checkAvailable(Menu menu) {
        if (!menu.isAvailable()) {
            throw new MenuUnavailableException("판매 중지된 메뉴가 포함되어 있습니다.");
        }
    }

    // 같은 이메일, 배송일의 PLACED 주문 중 주소나 우편번호가 다른 주문이 있으면 409
    private void checkAddress(String email, String address, String postcode, LocalDate shippingDate) {
        boolean mismatch = orderRepository
                .findByEmailAndShippingDateAndStatus(email, shippingDate, OrderStatus.PLACED).stream()
                .anyMatch(o -> !o.getAddress().equals(address) || !o.getPostcode().equals(postcode));
        if (mismatch) {
            throw new AddressMissMatchException("같은 이메일과 배송일의 기존 주문과 배송지가 다릅니다.");
        }
    }

    // 서울 기준 접수일 : [그날 00:00, 다음 날 00:00)
    private List<Order> findOrdersReceivedOn(LocalDate date) {
        return orderRepository.findByCreatedAtGreaterThanEqualAndCreatedAtLessThanOrderByIdAsc(
                date.atStartOfDay(), date.plusDays(1).atStartOfDay());
    }

    // 이메일 오름차순으로 묶기 (각 묶음 안은 원래 순서 유지)
    private Map<String, List<Order>> groupByEmail(List<Order> orders) {
        return orders.stream()
                .collect(Collectors.groupingBy(Order::getEmail, TreeMap::new, Collectors.toList()));
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
            List<Order> placed = orders.stream()
                    .filter(o -> o.getStatus() == OrderStatus.PLACED)
                    .toList();
            long totalQuantity = placed.stream()
                    .flatMap(o -> o.getItems().stream())
                    .mapToLong(OrderItem::getQuantity)
                    .sum();
            long totalAmount = placed.stream().mapToLong(Order::getTotalAmount).sum();
            return new Summary(orders.size(), placed.size(), orders.size() - placed.size(),
                    totalQuantity, totalAmount);
        }
    }
}
