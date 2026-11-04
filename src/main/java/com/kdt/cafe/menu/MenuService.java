package com.kdt.cafe.menu;

import com.kdt.cafe.category.Category;
import com.kdt.cafe.category.CategoryRepository;
import com.kdt.cafe.common.CategoryNotFoundException;
import com.kdt.cafe.common.MenuInUseException;
import com.kdt.cafe.common.MenuNotFoundException;
import com.kdt.cafe.common.ValidationErrorException;
import com.kdt.cafe.order.OrderItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/*
*   핵심규칙
* 취소주문을 포함해 한번이라도 주문에 사용한 메뉴는 삭제를 거절해야함 -> deltet
*
* A21 : 판매 여부 변경 ,PUT /api/v1/menus/{menuId}/availablity, 200 + 전체 menu 응답
* 등록 일반 수정에는 밑에 적어놓은 값을 모두 보냅니다
* 새 메뉴는 available = true, 일반 수정은 기존 available을 유지합니다
* a21은 available만 변경합니다
*
* 판매 중디 메뉴도 전체 검색 목록에 포함합니다.(findAll 말하는거같아요 이거)
* 전체 카테고리별 판매 중 검색목록은 id 오름차순,
* 카테고리가 있지만 소속 메뉴는 없는 경우 200 + ?
* 카테고리 자체가 없으면 404 카테고리 낫 파운드
* 검색 결과가 없으면 200 +?
*
* */

@Service
public class MenuService {
    //A6    전체 메뉴
    //A7    메뉴 상세
    //A8    메뉴 등록
    //A9    메뉴 일반 수정
    //A10   메뉴 삭제
    //A19   팬매중 메뉴
    //A20   메뉴명 검색
    //A21   판매 여부 변경


    private final MenuRepository menuRepository;
    private final CategoryRepository categoryRepository;
    private final OrderItemRepository orderItemRepository;

    @Autowired
    public MenuService(MenuRepository menuRepository, CategoryRepository categoryRepository, OrderItemRepository orderItemRepository) {
        this.menuRepository = menuRepository;
        this.categoryRepository = categoryRepository;
        this.orderItemRepository = orderItemRepository;
    }

    //A6전체 메뉴 조회
    @Transactional(readOnly = true)
    public List<MenuResponse> findAll(){
        List<Menu> list = this.menuRepository.findAllByOrderByIdAsc();
        List<MenuResponse> responses = new ArrayList<>();
        for(Menu menu:list){
            responses.add(MenuResponse.from(menu));
        }
        return responses;
    }

    //A7메뉴 상세(특정메뉴 조회기능?)
    @Transactional(readOnly = true)
    public MenuResponse findById(Long id){
        //메뉴가 없는경우
        //404, code:MENU_NOT_FOUND, message:메뉴를 찾을 수 없습니다
        Menu menu = this.menuRepository.findById(id)
                .orElseThrow(()->new MenuNotFoundException("메뉴를 찾을 수 없습니다."));
        return MenuResponse.from(menu);

    }

    //A8메뉴 등록
    @Transactional
    public MenuResponse create(MenuCreateRequest request){
        //카테고리가 없는경우
        //404, code:CATEGORY_NOT_FOUND,message: 카테고리를 찾으 수 없습니다
        Category found = this.categoryRepository.findById(request.categoryId())
                .orElseThrow(()->new CategoryNotFoundException("카테고리를 찾을 수 없습니다."));
        Menu menu = new Menu(found, request.name(), request.price(), request.description());
        Menu saved = this.menuRepository.save(menu);
        return MenuResponse.from(saved);
    }

    //A9메뉴 일반수정
    @Transactional
    public MenuResponse update(Long id,MenuUpdateRequest request){
        //카테고리가 없는경우
        //404, code:CATEGORY_NOT_FOUND,message: 카테고리를 찾으 수 없습니다
        Category found = this.categoryRepository.findById(request.categoryId())
                .orElseThrow(()->new CategoryNotFoundException("카테고리를 찾을 수 없습니다."));
        //메뉴가 없는경우
        //404, code:MENU_NOT_FOUND, message:메뉴를 찾을 수 없습니다
        Menu menu = this.menuRepository.findById(id)
                .orElseThrow(()->new MenuNotFoundException("메뉴를 찾을 수 없습니다"));
        menu.changeCategory(found);
        menu.changeName(request.name());
        menu.changePrice(request.price());
        menu.changeDescription(request.description());

        //트랜잭션 안에 있기때문에 더티체킹 한다고 판단해서
        //save 호출 안하였습니다
        this.menuRepository.flush();
        return MenuResponse.from(menu);
    }

    //A10메뉴 삭제
    @Transactional
    public void delete(Long id){
        //메뉴가 없는경우
        //404, code:MENU_NOT_FOUND, message:메뉴를 찾을 수 없습니다
        Menu menu = this.menuRepository.findById(id)
                .orElseThrow(()->new MenuNotFoundException("메뉴를 찾을 수 없습니다"));

        //취소 주문을 포함해 한번이라도 주문에 사용한 메뉴
        //409 Menu_In_Use로 삭제 거절
        boolean exsists = this.orderItemRepository
                .existsByMenuId(id);
        if(exsists){
            throw new MenuInUseException("주문에 사용된 메뉴는 삭제할 수 없습니다.");
        }


        this.menuRepository.delete(menu);
    }

    //A19판매중 메뉴
    @Transactional(readOnly = true)
    public List<MenuResponse> menuOnSale(){
        List<Menu> list = this.menuRepository.findAllByOrderByIdAsc();
        List<MenuResponse> responses = new ArrayList<>();

        //판매중인 상품만 리스트에 담아 반환
        for(Menu menu : list){
            if(menu.isAvailable()){
                responses.add(MenuResponse.from(menu));
            }
        }
        return responses;
    }

    //A20메뉴명 검색
    @Transactional(readOnly = true)
    public List<MenuResponse> findByName(String name){
        //메뉴명이 안들어왔을경우 null로 들어올경우
        //ValidationErrorException 처리
        if(name == null)
            throw new ValidationErrorException("입력값을 확인해 주세요");
        List<Menu> found = this.menuRepository.searchByName(name);
        List<MenuResponse> responses = new ArrayList<>();

        for(Menu menu:found){
            responses.add(MenuResponse.from(menu));
        }
        return responses;
    }

    //A21판매 여부 변경
    @Transactional
    public MenuResponse changeAvailable(Long id,MenuAvailableRequest request){
        Menu menu = this.menuRepository.findById(id)
                .orElseThrow(() -> new MenuNotFoundException("메뉴를 찾을 수 없습니다."));
        menu.changeAvailable(request.available());
        this.menuRepository.flush();
        return MenuResponse.from(menu);
    }


}
