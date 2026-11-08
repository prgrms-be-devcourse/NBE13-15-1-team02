package com.kdt.cafe.menu;

import com.kdt.cafe.category.Category;
import com.kdt.cafe.category.CategoryRepository;
import com.kdt.cafe.common.MenuInUseException;
import com.kdt.cafe.order.OrderItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

// 선택 심화: A06 전체 메뉴 캐시 동작 테스트 (DB 조회 횟수·캐시 삭제 시점)
@ExtendWith(MockitoExtension.class)
class MenuServiceCacheTest {

    @Mock
    private MenuRepository menuRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private OrderItemRepository orderItemRepository;
    @Mock
    private MenuListCache menuListCache;

    @InjectMocks
    private MenuService menuService;

    private final Category category = new Category("싱글오리진");

    @Test
    void findAll_queries_db_only_once_while_cache_is_valid() {
        // 첫 조회는 캐시 없음(MISS), 두 번째 조회는 캐시 있음(HIT)
        Menu menu = new Menu(category, "Ethiopia Sidamo", 11000, "설명");
        List<MenuResponse> cached = List.of(MenuResponse.from(menu));
        given(menuListCache.get()).willReturn(Optional.empty(), Optional.of(cached));
        given(menuRepository.findAllByOrderByIdAsc()).willReturn(List.of(menu));

        List<MenuResponse> first = menuService.findAll();
        List<MenuResponse> second = menuService.findAll();

        // DB 조회는 첫 번째 한 번뿐
        verify(menuRepository, times(1)).findAllByOrderByIdAsc();
        verify(menuListCache).put(first);
        assertThat(second).isEqualTo(first);
    }

    @Test
    void findAll_cache_hit_does_not_touch_db() {
        given(menuListCache.get()).willReturn(Optional.of(List.of()));

        menuService.findAll();

        verifyNoInteractions(menuRepository);
    }

    @Test
    void create_evicts_cache() {
        given(categoryRepository.findById(1L)).willReturn(Optional.of(category));
        given(menuRepository.save(any(Menu.class))).willAnswer(inv -> inv.getArgument(0));

        menuService.create(new MenuRequest(1L, "Kenya AA", 12000, "설명"));

        verify(menuListCache).evictAfterCommit();
    }

    @Test
    void update_evicts_cache() {
        Menu menu = new Menu(category, "Kenya AA", 12000, "설명");
        given(menuRepository.findById(1L)).willReturn(Optional.of(menu));
        given(categoryRepository.findById(1L)).willReturn(Optional.of(category));

        menuService.update(1L, new MenuRequest(1L, "Kenya AA Washed", 13000, "설명"));

        verify(menuListCache).evictAfterCommit();
    }

    @Test
    void change_available_evicts_cache() {
        Menu menu = new Menu(category, "Kenya AA", 12000, "설명");
        given(menuRepository.findById(1L)).willReturn(Optional.of(menu));

        menuService.changeAvailable(1L, new MenuAvailableRequest(false));

        verify(menuListCache).evictAfterCommit();
    }

    @Test
    void delete_without_orders_evicts_cache() {
        Menu menu = new Menu(category, "Kenya AA", 12000, "설명");
        given(menuRepository.findById(1L)).willReturn(Optional.of(menu));
        given(orderItemRepository.existsByMenuId(1L)).willReturn(false);

        menuService.delete(1L);

        verify(menuRepository).delete(menu);
        verify(menuListCache).evictAfterCommit();
    }

    @Test
    void delete_rejected_with_orders_keeps_cache() {
        // 주문 이력이 있어 삭제가 거절되면 데이터가 그대로이므로 캐시도 유지
        Menu menu = new Menu(category, "Kenya AA", 12000, "설명");
        given(menuRepository.findById(1L)).willReturn(Optional.of(menu));
        given(orderItemRepository.existsByMenuId(1L)).willReturn(true);

        assertThatThrownBy(() -> menuService.delete(1L))
                .isInstanceOf(MenuInUseException.class);

        verify(menuListCache, never()).evictAfterCommit();
    }
}