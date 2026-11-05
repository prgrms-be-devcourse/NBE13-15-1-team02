package com.kdt.cafe.category;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryApiController.class)
public class CategoryApiControllerTest {

    // 실제 서버 없이 HTTP 요청을 보내고 응답을 받아보는 테스트용 클라이언트
    @Autowired
    private MockMvc mockMvc;

    // 진짜 CategoryService 대신 넣는 가짜 객체
    @MockitoBean
    private CategoryService categoryService;

    // 요구사항: 카테고리 등록(A03)은 201과 생성된 카테고리의 id·name을 반환한다
    @Test
    void registerCategory() throws Exception {

        // 준비: 초기 데이터(싱글오리진 1, 블렌드 2)에 없는 새 카테고리를 등록하면 id=3으로 반환되도록 설정
        String requestBody = """
                {"name":"디카페인"}
                """;                // 요청 본문으로 보낼 JSON 문자열
        // 어떤 CategoryRequest로 등록이 호출되든 id=3, name = "디카페인"을 돌려주도록 설정
        given(categoryService.categoryRegister(any(CategoryRequest.class)))
                .willReturn(new CategoryResponse(3L, "디카페인"));

        // 실행: 등록 요청
        // POST /api/v1/categories 요청을 만들어서 본문 형식이 JSON 이라고 알리고 준비한 JSON을 본문에 담아 실행
        // 위 결과를 result에 저장
        ResultActions result = mockMvc.perform(post("/api/v1/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));

        // 검증: 201, 응답 필드 값
        result.andDo(print())                                                           // 요청 응답 내용을 콘솔에 출력(확인용)
                .andExpect(status().isCreated())                                        // 상태 코드가 201 Created인지
                .andExpect(jsonPath("$.id").value(3))              // 응답 JSON의 id가 3인지 
                .andExpect(jsonPath("$.name").value("디카페인"));   // 응답 JSON의 name이 "디카페인"인지

    }


    // 요구사항: 카테고리 삭제(A05)는 204와 빈 본문을 반환한다
    @Test
    void deleteCategory() throws Exception {

        // 준비: 삭제할 카테고리 ID
        Long categoryId = 3L;

        // 실행: 삭제 요청
        // DELETE /api/v1/categories/3 요청 실행, 결과를 result에 저장
        ResultActions result = mockMvc.perform(delete("/api/v1/categories/{categoryId}", categoryId));

        // 검증: 204, 빈 본문
        result.andDo(print())                                       // 요청 응답 내용을 콘솔에 출력(확인용)
                .andExpect(status().isNoContent())                  // 상태 코드가 204 No Content인지
                .andExpect(content().string(""));     // 응답 본문이 비어있는지

        verify(categoryService).categoryDelete(categoryId);         // Controller가 Service의 삭제 메서드를 ID 3으로 호출했는지

    }


}
