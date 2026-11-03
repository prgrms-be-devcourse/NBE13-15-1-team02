package com.kdt.cafe.menu;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MenuApiController {
    private final MenuService menuService;

    @Autowired
    public MenuApiController(MenuService menuService) {
        this.menuService = menuService;
    }










}
