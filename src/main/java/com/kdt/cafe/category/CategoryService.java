package com.kdt.cafe.category;

import com.kdt.cafe.common.CategoryInUseException;
import com.kdt.cafe.common.CategoryNameDuplicatedException;
import com.kdt.cafe.common.CategoryNotFoundException;
import com.kdt.cafe.menu.Menu;
import com.kdt.cafe.menu.MenuRepository;
import com.kdt.cafe.menu.MenuResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final MenuRepository menuRepository;    // 삭제 시 소속 메뉴 확인용, 카테고리별 메뉴 조회용

    @Autowired
    public CategoryService(CategoryRepository categoryRepository, MenuRepository menuRepository) {
        this.categoryRepository = categoryRepository;
        this.menuRepository = menuRepository;
    }

    // 전체 목록 조회
    @Transactional(readOnly = true)
    public List<CategoryResponse> categoryFindAll() {

        // ID 오름차순 조회(없으면 빈 리스트)
        List<Category> find = this.categoryRepository.findAllByOrderByIdAsc();

        // 응답 DTO 리스트로 변환
        return find.stream().map(CategoryResponse::toResponse).toList();

    }


    // id로 단건 조회
    @Transactional(readOnly = true)
    public CategoryResponse categoryFindOne(Long categoryId) {

        // ID로 카테고리 조회 -> 없으면 404, CATEGORY_NOT_FOUND
        Category result = this.categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException("카테고리를 찾을 수 없습니다."));

        // 응답 DTO로 변환
        return CategoryResponse.toResponse(result);

    }


    // 카테고리 등록
    @Transactional
    public CategoryResponse categoryRegister(CategoryRequest request) {

        // 이름 trim
        String trimmedName = request.trimmedName();

        // 같은 이름의 카테고리가 이미 있으면 -> 409, CATEGORY_NAME_DUPLICATED
        if( this.categoryRepository.existsByName(trimmedName) ) {
            throw new CategoryNameDuplicatedException("같은 이름의 카테고리가 이미 있습니다.");
        }

        // 카테고리 등록
        Category register = this.categoryRepository.save(new Category(trimmedName));

        // 등록된 카테고리 응답 DTO로 변환
        return CategoryResponse.toResponse(register);

    }


    // 카테고리 수정
    @Transactional
    public CategoryResponse categoryUpdate(Long categoryId, CategoryRequest request) {

        // 수정할 카테고리가 존재하지 않으면 -> 404, CATEGORY_NOT_FOUND
        Category find = this.categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException("카테고리를 찾을 수 없습니다."));

        // 이름 trim
        String trimmedName = request.trimmedName();

        // 다른 카테고리가 같은 이름을 쓰면 -> 409, CATEGORY_NAME_DUPLICATED
        if ( this.categoryRepository.existsByNameAndIdNot(trimmedName, categoryId) ) {
            throw new CategoryNameDuplicatedException("같은 이름의 카테고리가 이미 있습니다.");
        }

        // 이름 변경
        find.updateName(trimmedName);

        // 변경된 카테고리 응답 DTO로 변환
        return CategoryResponse.toResponse(find);

    }


    // 카테고리 삭제
    @Transactional
    public void categoryDelete(Long categoryId) {

        // 삭제할 카테고리가 존재하지 않으면 -> 404, CATEGORY_NOT_FOUND
        Category find = this.categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException("카테고리를 찾을 수 없습니다."));

        // 해당 카테고리에 소속된 메뉴가 있으면(판매 중지 포함) -> 409, CATEGORY_IN_USE
        if (this.menuRepository.existsByCategoryId(categoryId)) {
            throw new CategoryInUseException("메뉴가 등록된 카테고리는 삭제할 수 없습니다.");
        }

        // 카테고리 삭제
        this.categoryRepository.delete(find);

    }


    // 카테고리별 메뉴 조회
    @Transactional(readOnly = true)
    public List<MenuResponse> findMenuByCategory(Long categoryId) {

        // 카테고리가 존재하지 않으면 -> 404, CATEGORY_NOT_FOUND
        if ( !this.categoryRepository.existsById(categoryId) ) {
            throw new CategoryNotFoundException("카테고리를 찾을 수 없습니다.");
        }

        // 해당 카테고리에 속한 메뉴를 ID 오름차순 조회(없으면 빈 리스트)
        List<Menu> find = this.menuRepository.findByCategoryIdOrderByIdAsc(categoryId);

        // 응답 DTO 리스트로 변환
        return find.stream().map(MenuResponse::from).toList();

    }

}
