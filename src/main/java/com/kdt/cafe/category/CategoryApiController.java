package com.kdt.cafe.category;

import com.kdt.cafe.menu.MenuResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryApiController {

    private final CategoryService categoryService;

    @Autowired
    public CategoryApiController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }


    // A01(카테고리 목록) : GET, 경로 없음, 목록 반환, 200
    // 객체를 바로 반환하면 기본 200으로 응답
    @GetMapping
    public List<CategoryResponse> categoryFindAll() {
        return this.categoryService.categoryFindAll();
    }


    // A02(카테고리 상세) : GET, /{categoryId}, 단건 반환, 200
    // @Min(1) : 0, 음수 categoryId는 서비스로 가기 전 400으로 거절
    @GetMapping("/{categoryId}")
    public CategoryResponse categoryFindOne(@Min(1) @PathVariable("categoryId") Long categoryId) {
        return this.categoryService.categoryFindOne(categoryId);
    }


    // A03(카테고리 등록) : POST, 경로 없음, 요청 본문 받음, 201
    // @Valid : CategoryRequest의 @NotBlank·@Size 검사 실행 (실패 시 400)
    @PostMapping
    public ResponseEntity<CategoryResponse> categoryRegister(@Valid @RequestBody CategoryRequest request) {

        CategoryResponse register = this.categoryService.categoryRegister(request);

        // 새로 생성된 카테고리의 조회 경로(Location 헤더용)
        URI location = URI.create("/api/v1/categories/" + register.id());

        // 201 Created(상태 코드) + location 헤더 + 생성된 카테고리 본문
        return ResponseEntity.created(location).body(register);

    }


    // A04(카테고리 수정) : PUT, /{categoryId}, 경로 ID와 요청 본문 둘 다 받음, 200
    // 경로 categoryId는 @Min(1), 요청 본문은 @Valid로 각각 검사
    @PutMapping("/{categoryId}")
    public CategoryResponse categoryUpdate(@Min(1) @PathVariable("categoryId") Long categoryId,
                                                           @Valid @RequestBody CategoryRequest request) {

        return this.categoryService.categoryUpdate(categoryId, request);

    }


    // A05(카테고리 삭제) : DELETE, /{categoryId}, 204 + 빈 본문
    @DeleteMapping("/{categoryId}")
    public ResponseEntity<Void> categoryDelete(@Min(1) @PathVariable("categoryId") Long categoryId) {

        // categoryId에 해당하는 카테고리 삭제
        this.categoryService.categoryDelete(categoryId);

        // 204 No Content(상태 코드), 삭제 성공, 돌려줄 본문 없음
        return ResponseEntity.noContent().build();

    }


    // A15(카테고리별 메뉴) : GET, /{categoryId}/menus, 메뉴 목록 반환, 200
    @GetMapping("/{categoryId}/menus")
    public List<MenuResponse> findMenuByCategory(@Min(1) @PathVariable("categoryId") Long categoryId) {

        return this.categoryService.findMenuByCategory(categoryId);

    }


}
