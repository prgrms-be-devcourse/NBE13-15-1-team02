package com.kdt.cafe.menu;

import com.kdt.cafe.category.Category;
import com.kdt.cafe.category.CategoryRepository;
import com.kdt.cafe.common.CategoryNotFoundException;
import com.kdt.cafe.common.MenuInUseException;
import com.kdt.cafe.common.MenuNotFoundException;
import com.kdt.cafe.common.ValidationErrorException;
import com.kdt.cafe.order.OrderItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class MenuService {

    private final MenuRepository menuRepository;
    private final CategoryRepository categoryRepository;
    private final OrderItemRepository orderItemRepository;
    private final MenuListCache menuListCache;

    @Autowired
    public MenuService(MenuRepository menuRepository,
                       CategoryRepository categoryRepository,
                       OrderItemRepository orderItemRepository,
                       MenuListCache menuListCache) {
        this.menuRepository = menuRepository;
        this.categoryRepository = categoryRepository;
        this.orderItemRepository = orderItemRepository;
        this.menuListCache = menuListCache;
    }

    // ID는 1 이상의 정수여야 한다 (아니면 400)
    private void validateId(Long id, String field) {
        if (id == null || id < 1) {
            throw new ValidationErrorException("입력값을 확인해 주세요.", Map.of(field, "id 형식을 확인해 주세요."));
        }
    }

    // A06 전체 메뉴 (Redis 캐시 사용)
    // 캐시가 있으면 DB를 조회하지 않고 바로 반환, 없으면 DB 조회 후 캐시에 저장
    // 캐시 HIT 때는 DB 연결도 쓰지 않도록 @Transactional 을 붙이지 않음
    public List<MenuResponse> findAll() {
        return menuListCache.get().orElseGet(() -> {
            List<MenuResponse> menus = menuRepository.findAllByOrderByIdAsc().stream()
                    .map(MenuResponse::from)
                    .toList();
            menuListCache.put(menus);
            return menus;
        });
    }

    // A07 메뉴 상세
    @Transactional(readOnly = true)
    public MenuResponse findById(Long id) {
        validateId(id, "menuId");
        Menu menu = menuRepository.findById(id)
                .orElseThrow(() -> new MenuNotFoundException("메뉴를 찾을 수 없습니다."));
        return MenuResponse.from(menu);
    }

    // A08 메뉴 등록
    @Transactional
    public MenuResponse create(MenuRequest request) {
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new CategoryNotFoundException("카테고리를 찾을 수 없습니다."));
        Menu menu = new Menu(category, request.name(), request.price(), request.description());
        MenuResponse saved = MenuResponse.from(menuRepository.save(menu));
        menuListCache.evictAfterCommit();
        return saved;
    }

    // A09 메뉴 일반 수정
    @Transactional
    public MenuResponse update(Long id, MenuRequest request) {
        validateId(id, "menuId");
        Menu menu = menuRepository.findById(id)
                .orElseThrow(() -> new MenuNotFoundException("메뉴를 찾을 수 없습니다."));
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new CategoryNotFoundException("카테고리를 찾을 수 없습니다."));

        menu.changeCategory(category);
        menu.changeName(request.name());
        menu.changePrice(request.price());
        menu.changeDescription(request.description());

        menuRepository.flush();
        menuListCache.evictAfterCommit();
        return MenuResponse.from(menu);
    }

    // A10 메뉴 삭제
    @Transactional
    public void delete(Long id) {
        validateId(id, "menuId");
        Menu menu = menuRepository.findById(id)
                .orElseThrow(() -> new MenuNotFoundException("메뉴를 찾을 수 없습니다."));

        // 취소 주문 포함, 한 번이라도 주문에 쓰였으면 409
        if (orderItemRepository.existsByMenuId(id)) {
            throw new MenuInUseException("주문에 사용된 메뉴는 삭제할 수 없습니다.");
        }
        menuRepository.delete(menu);
        menuListCache.evictAfterCommit();
    }

    // A15 카테고리별 메뉴
    @Transactional(readOnly = true)
    public List<MenuResponse> findByCategory(Long categoryId) {
        validateId(categoryId, "categoryId");
        // 카테고리가 없으면 404, 있는데 메뉴가 없으면 200 + []
        if (!categoryRepository.existsById(categoryId)) {
            throw new CategoryNotFoundException("카테고리를 찾을 수 없습니다.");
        }
        return menuRepository.findByCategoryIdOrderByIdAsc(categoryId).stream()
                .map(MenuResponse::from)
                .toList();
    }

    // A19 판매 중 메뉴
    @Transactional(readOnly = true)
    public List<MenuResponse> menuOnSale() {
        return menuRepository.findByAvailableTrueOrderByIdAsc().stream()
                .map(MenuResponse::from)
                .toList();
    }

    // A20 메뉴명 검색
    @Transactional(readOnly = true)
    public List<MenuResponse> findByName(String name) {
        String keyword = (name == null) ? "" : name.trim();
        if (keyword.isEmpty() || keyword.length() > 100) {
            throw new ValidationErrorException("입력값을 확인해 주세요.", Map.of("keyword", "메뉴 형식을 확인해 주세요."));
        }
        return menuRepository.findAllByOrderByIdAsc().stream()
                .filter(menu -> menu.getName().contains(keyword))
                .map(MenuResponse::from)
                .toList();
    }

    // A21 판매 여부 변경
    @Transactional
    public MenuResponse changeAvailable(Long id, MenuAvailableRequest request) {
        validateId(id, "menuId");
        Menu menu = menuRepository.findById(id)
                .orElseThrow(() -> new MenuNotFoundException("메뉴를 찾을 수 없습니다."));
        menu.changeAvailable(request.available());
        menuRepository.flush();
        menuListCache.evictAfterCommit();
        return MenuResponse.from(menu);
    }

    @Transactional(readOnly = true)
    public MenuPageResponse findPage(int page, int size) {
        //범위검증
        //page 기본값과 허용범위 : 기본값 0, 허용범위 0 이상의 정수
        //size 기본값과 허용범위 : 기본 10, 허용범위 1~50의 정수
        if (page < 0)
            throw new ValidationErrorException("입력값을 확인해 주세요.", Map.of("page", "page는 0 이상이어야합니다."));
        if (size > 50 || size < 1)
            throw new ValidationErrorException("입력값을 확인해 주세요.", Map.of("size", "size는 1이상 50 이하여야합니다"));
        Pageable pageable = PageRequest.of(page, size, Sort.by("id"));
        Page<Menu> menus = this.menuRepository.findAll(pageable);
        return MenuPageResponse.from(menus);
    }
}
