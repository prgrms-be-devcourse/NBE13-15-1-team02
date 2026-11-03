package com.kdt.cafe.menu;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
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
    public ResponseEntity<MenuResponse> create(@RequestBody MenuCreateRequest request){
        MenuResponse saved = this.menuService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
    //A9    메뉴 일반 수정
    //
    @PutMapping("/{menuId}")
    public
    //A10   메뉴 삭제
    @DeleteMapping("/{menuId}")

    //A19   팬매중 메뉴
    @GetMapping("/available")

    //A20   메뉴명 검색
    @GetMapping("/search")
    public List<MenuResponse> findByName(@RequestParam("keyword") String name){

    }
    //A21   판매 여부 변경
    @PutMapping("/{menuId}/availability")







}
