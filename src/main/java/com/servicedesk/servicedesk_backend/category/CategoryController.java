
package com.servicedesk.servicedesk_backend.category;

// STEP 1: IMPORT REQUIRED CLASSES

import com.servicedesk.servicedesk_backend.category.dto.CategoryRequest;
import com.servicedesk.servicedesk_backend.category.dto.CategoryResponse;
import com.servicedesk.servicedesk_backend.category.dto.CategoryStatusRequest;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.util.List;


// STEP 2: DECLARE CATEGORY REST CONTROLLER

@RestController
@RequestMapping("/api")
public class CategoryController {

    // STEP 3: DECLARE SERVICE DEPENDENCY

    private final CategoryService categoryService;


    // STEP 4: CONSTRUCTOR INJECTION

    public CategoryController(CategoryService categoryService) {

        this.categoryService = categoryService;
    }


    // STEP 5: FETCH ACTIVE CATEGORIES

    // GET /api/categories
    // Available to any authenticated user.
    @GetMapping("/categories")
    public ResponseEntity<List<CategoryResponse>> getActiveCategories() {

        // STEP 5.1: CALL SERVICE

        List<CategoryResponse> categories =
                categoryService.getActiveCategories();


        // STEP 5.2: RETURN CATEGORY LIST

        return ResponseEntity.ok(categories);
    }


    // STEP 6: FETCH ALL CATEGORIES

    // GET /api/admin/categories
    // Available only to ADMIN.
    @GetMapping("/admin/categories")
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {

        // STEP 6.1: FETCH ALL CATEGORIES

        List<CategoryResponse> categories =
                categoryService.getAllCategories();


        // STEP 6.2: RETURN CATEGORY LIST

        return ResponseEntity.ok(categories);
    }


    // STEP 7: CREATE NEW CATEGORY

    // POST /api/admin/categories
    // Available only to ADMIN.
    @PostMapping("/admin/categories")
    public ResponseEntity<CategoryResponse> createCategory(
            @Valid @RequestBody CategoryRequest request) {

        // STEP 7.1: CALL SERVICE

        CategoryResponse category =
                categoryService.createCategory(request);


        // STEP 7.2: RETURN CREATED RESPONSE

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(category);
    }


    // STEP 8: UPDATE EXISTING CATEGORY

    // PUT /api/admin/categories/{categoryId}
    @PutMapping("/admin/categories/{categoryId}")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable Long categoryId,
            @Valid @RequestBody CategoryRequest request) {

        // STEP 8.1: CALL SERVICE

        CategoryResponse category =
                categoryService.updateCategory(
                        categoryId,
                        request
                );


        // STEP 8.2: RETURN UPDATED RESPONSE

        return ResponseEntity.ok(category);
    }


    // STEP 9: CHANGE CATEGORY STATUS

    // PATCH /api/admin/categories/{categoryId}/status
    @PatchMapping("/admin/categories/{categoryId}/status")
    public ResponseEntity<CategoryResponse> updateCategoryStatus(
            @PathVariable Long categoryId,
            @Valid @RequestBody CategoryStatusRequest request) {

        // STEP 9.1: CALL SERVICE

        CategoryResponse category =
                categoryService.updateCategoryStatus(
                        categoryId,
                        request.active()
                );


        // STEP 9.2: RETURN UPDATED CATEGORY

        return ResponseEntity.ok(category);
    }
}
