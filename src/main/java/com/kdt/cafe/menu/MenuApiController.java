package com.kdt.cafe.menu;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//A6    전체 메뉴
//A7    메뉴 상세
//A8    메뉴 등록
//A9    메뉴 일반 수정
//A10   메뉴 삭제
//A19   팬매중 메뉴
//A20   메뉴명 검색
//A21   판매 여부 변경

@RestController
@RequestMapping("/api/v1/menus")
public class MenuApiController {
    private final MenuService menuService;

    @Autowired
    public MenuApiController(MenuService menuService) {
        this.menuService = menuService;
    }

    //A6    전체 메뉴
    //200 Menu[]반환
    @GetMapping
    public List<MenuResponse> findAll(){
        return this.menuService.findAll();
    }
    //A7    메뉴 상세
    //200 Menu반환
    @GetMapping("/{menuId}")
    public MenuResponse findOne(@PathVariable("menuId") Long id){
        return this.menuService.findById(id);
    }

    //A8    메뉴 등록
    //201 Menu반환
    @PostMapping()
    public ResponseEntity<MenuResponse> create(@Valid @RequestBody MenuCreateRequest request){
        MenuResponse saved = this.menuService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
    //A9    메뉴 일반 수정
    //200 Menu반환
    @PutMapping("/{menuId}")
    public MenuResponse update(@PathVariable("menuId") Long id,
                               @Valid @RequestBody MenuUpdateRequest request){
        return this.menuService.update(id,request);
    }
    //A10   메뉴 삭제
    //204 빈 본문 반환
    @DeleteMapping("/{menuId}")
    @ResponseStatus(HttpStatus.NO_CONTENT) // 빈 본문으로 응답
    public void delete(@PathVariable("menuId") Long id){
        this.menuService.delete(id);
    }

    //A19   팬매중 메뉴
    //200 Menu[] 반환
    @GetMapping("/available")
    public List<MenuResponse> menuAvailable(){
        return this.menuService.menuOnSale();
    }

    //A20   메뉴명 검색
    //200 Menu[]반환
    @GetMapping("/search")
    public List<MenuResponse> findByName(@RequestParam("keyword") String name){
        return this.menuService.findByName(name);
    }
    //A21   판매 여부 변경
    //200 전체 Menu 반환
    @PutMapping("/{menuId}/availability")
    public MenuResponse changeAvailable(@PathVariable("menuId")Long id,
                                        @Valid @RequestBody MenuAvailableRequest request){
        return this.menuService.changeAvailable(id,request);
    }







}
