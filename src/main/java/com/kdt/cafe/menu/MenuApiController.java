package com.kdt.cafe.menu;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/menus")
public class MenuApiController {

    private final MenuService menuService;

    @Autowired
    public MenuApiController(MenuService menuService) {
        this.menuService = menuService;
    }

    // A06 200 Menu[]
    @GetMapping
    public List<MenuResponse> findAll() {
        return menuService.findAll();
    }

    // A07 200 Menu
    @GetMapping("/{menuId}")
    public MenuResponse findOne(@PathVariable("menuId") Long id) {
        return menuService.findById(id);
    }

    // A08 201 Menu
    @PostMapping
    public ResponseEntity<MenuResponse> create(@Valid @RequestBody MenuRequest request) {
        MenuResponse saved = menuService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // A09 200 Menu
    @PutMapping("/{menuId}")
    public MenuResponse update(@PathVariable("menuId") Long id,
                               @Valid @RequestBody MenuRequest request) {
        return menuService.update(id, request);
    }

    // A10 204 빈 본문
    @DeleteMapping("/{menuId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("menuId") Long id) {
        menuService.delete(id);
    }

    // A19 200 Menu[]
    @GetMapping("/available")
    public List<MenuResponse> menuAvailable() {
        return menuService.menuOnSale();
    }

    // A20 200 Menu[]
    // keyword 누락도 서비스에서 400(fieldErrors.keyword)으로 처리하기 위해 required = false
    @GetMapping("/search")
    public List<MenuResponse> findByName(
            @RequestParam(name = "keyword", required = false) String keyword) {
        return menuService.findByName(keyword);
    }

    // A21 200 전체 Menu
    @PutMapping("/{menuId}/availability")
    public MenuResponse changeAvailable(@PathVariable("menuId") Long id,
                                        @Valid @RequestBody MenuAvailableRequest request) {
        return menuService.changeAvailable(id, request);
    }
}