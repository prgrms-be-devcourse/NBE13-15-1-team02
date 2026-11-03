package com.kdt.cafe.menu;

import com.kdt.cafe.category.Category;
import com.kdt.cafe.category.CategoryRepository;
import com.kdt.cafe.common.CategoryNotFoundException;
import com.kdt.cafe.common.MenuNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class MenuService {
    private final MenuRepository menuRepository;
    private final CategoryRepository categoryRepository;

    @Autowired
    public MenuService(MenuRepository menuRepository, CategoryRepository categoryRepository) {
        this.menuRepository = menuRepository;
        this.categoryRepository = categoryRepository;
    }

    //A6전체 메뉴 조회
    @Transactional(readOnly = true)
    public List<MenuResponse> findAll(){
        List<Menu> list = this.menuRepository.findAll();
        List<MenuResponse> responses = new ArrayList<>();
        for(Menu menu:list){
            responses.add(MenuResponse.from(menu));
        }
        return responses;
    }
    //A7메뉴 상세(특정메뉴 조회기능?)
    //id로? name으로..? 훔...
    //메뉴가 없는경우
    //404, code:MENU_NOT_FOUND, message:메뉴를 찾을 수 없습니다
    @Transactional(readOnly = true)
    public MenuResponse findById(Long id){

        Menu menu = this.menuRepository.findById(id)
                .orElseThrow(()->new MenuNotFoundException("메뉴를 찾을 수 없습니다."));
        return MenuResponse.from(menu);

    }
    //A8메뉴 등록
    //등록 일반 수정에는 categoryId,name,price,description
    //카테고리가 없는경우
    //404, code:CATEGORY_NOT_FOUND,message: 카테고리를 찾으 수 없습니다
    @Transactional
    public MenuResponse create(Long id,MenuCreateRequest request){
        Category found = this.categoryRepository.findById(request.categoryId())
                .orElseThrow(()->new CategoryNotFoundException("카테고리를 찾을 수 없습니다."));
        Menu menu = new Menu(found, request.name(), request.price(), request.description());
        Menu saved = this.menuRepository.save(menu);
        return MenuResponse.from(saved);
    }

    //A9메뉴 일반수정
    //등록 일반 수정에는 categoryId,name,price,description
    //그래도 유지보수할때 혹시 몰라서 updateRequest dto 새로 생성했습니다!
    //메뉴가 없는경우
    //404, code:MENU_NOT_FOUND, message:메뉴를 찾을 수 없습니다
    //카테고리가 없는경우
    //404, code:CATEGORY_NOT_FOUND,message: 카테고리를 찾으 수 없습니다
    @Transactional
    public MenuResponse update(Long id,MenuUpdateRequest request){
        Category found = this.categoryRepository.findById(request.categoryId())
                .orElseThrow(()->new CategoryNotFoundException("카테고리를 찾을 수 없습니다."));
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
    //메뉴가 없는경우
    //404, code:MENU_NOT_FOUND, message:메뉴를 찾을 수 없습니다
    @Transactional
    public void delete(Long id){
        Menu menu = this.menuRepository.findById(id)
                .orElseThrow(()->new MenuNotFoundException("메뉴를 찾을 수 없습니다"));
        this.menuRepository.delete(menu);
    }

}
