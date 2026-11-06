package com.kdt.cafe.menu;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MenuApiController.class)
public class MenuApiControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private MenuService menuService;

    @Test
    void changeAvailable_without_available_returns_400() throws Exception {
        // Act
        this.mockMvc.perform(put("/api/v1/menus/1/availability")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .accept(MediaType.APPLICATION_JSON))

                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("입력값을 확인해 주세요."))
                .andExpect(jsonPath("$.fieldErrors.available").exists());


        verifyNoInteractions(this.menuService);
    }

    @Test
    void findOne_with_non_numeric_id_returns_400() throws Exception {

        this.mockMvc.perform(get("/api/v1/menus/abc")
                        .accept(MediaType.APPLICATION_JSON))

                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("입력값을 확인해 주세요."))
                .andExpect(jsonPath("$.fieldErrors.menuId").exists());

        verifyNoInteractions(this.menuService);
    }
}