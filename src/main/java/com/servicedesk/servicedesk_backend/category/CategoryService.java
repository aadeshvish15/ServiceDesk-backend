
package com.servicedesk.servicedesk_backend.category;

// STEP 1: IMPORT REQUIRED CLASSES

import com.servicedesk.servicedesk_backend.category.dto.CategoryRequest;
import com.servicedesk.servicedesk_backend.category.dto.CategoryResponse;

import com.servicedesk.servicedesk_backend.category.exception.CategoryAlreadyExistsException;
import com.servicedesk.servicedesk_backend.category.exception.ResourceNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


// STEP 2: DECLARE CATEGORY SERVICE

// Contains business logic for category management.
@Service
public class CategoryService {

    // STEP 3: DECLARE REPOSITORY DEPENDENCY

    private final CategoryRepository categoryRepository;


    // STEP 4: CONSTRUCTOR INJECTION

    public CategoryService(CategoryRepository categoryRepository) {

        this.categoryRepository = categoryRepository;
    }


    // STEP 5: FETCH ACTIVE CATEGORIES

    @Transactional(readOnly = true)
    public List<CategoryResponse> getActiveCategories() {

        // STEP 5.1: FETCH ACTIVE CATEGORIES FROM MYSQL

        List<Category> categories =
                categoryRepository.findByActiveTrueOrderByNameAsc();


        // STEP 5.2: CONVERT ENTITIES INTO DTOs

        return categories.stream()
                .map(this::convertToResponse)
                .toList();
    }


    // STEP 6: FETCH ALL CATEGORIES FOR ADMIN

    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {

        // STEP 6.1: FETCH ALL CATEGORIES

        List<Category> categories =
                categoryRepository.findAllByOrderByNameAsc();


        // STEP 6.2: CONVERT ENTITIES INTO DTOs

        return categories.stream()
                .map(this::convertToResponse)
                .toList();
    }


    // STEP 7: CREATE NEW CATEGORY

    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {

        // STEP 7.1: NORMALIZE CATEGORY NAME

        // Remove accidental spaces without changing display capitalization.
        String name = request.name().trim();


        // STEP 7.2: CHECK FOR DUPLICATE CATEGORY

        if (categoryRepository.existsByNameIgnoreCase(name)) {

            throw new CategoryAlreadyExistsException(
                    "Category with this name already exists"
            );
        }


        // STEP 7.3: NORMALIZE DESCRIPTION

        String description = normalizeDescription(request.description());


        // STEP 7.4: CREATE CATEGORY ENTITY

        Category category = new Category(
                name,
                description
        );


        // STEP 7.5: SAVE CATEGORY IN MYSQL

        Category savedCategory = categoryRepository.save(category);


        // STEP 7.6: RETURN CATEGORY RESPONSE

        return convertToResponse(savedCategory);
    }


    // STEP 8: UPDATE EXISTING CATEGORY

    @Transactional
    public CategoryResponse updateCategory(
            Long categoryId,
            CategoryRequest request) {

        // STEP 8.1: FIND CATEGORY BY ID

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Category not found with ID: " + categoryId
                        )
                );


        // STEP 8.2: NORMALIZE UPDATED CATEGORY NAME

        String name = request.name().trim();


        // STEP 8.3: CHECK DUPLICATE NAME

        // Exclude the current category from the duplicate check.
        boolean duplicateExists =
                categoryRepository.existsByNameIgnoreCaseAndIdNot(
                        name,
                        categoryId
                );

        if (duplicateExists) {

            throw new CategoryAlreadyExistsException(
                    "Another category already uses this name"
            );
        }


        // STEP 8.4: UPDATE CATEGORY DETAILS

        String description = normalizeDescription(request.description());

        category.updateDetails(name, description);


        // STEP 8.5: SAVE UPDATED CATEGORY

        Category updatedCategory = categoryRepository.save(category);


        // STEP 8.6: RETURN UPDATED CATEGORY

        return convertToResponse(updatedCategory);
    }


    // STEP 9: ACTIVATE OR DEACTIVATE CATEGORY

    @Transactional
    public CategoryResponse updateCategoryStatus(
            Long categoryId,
            boolean active) {

        // STEP 9.1: FIND CATEGORY

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Category not found with ID: " + categoryId
                        )
                );


        // STEP 9.2: UPDATE ACTIVE STATUS

        category.updateStatus(active);


        // STEP 9.3: SAVE UPDATED CATEGORY

        Category updatedCategory = categoryRepository.save(category);


        // STEP 9.4: RETURN UPDATED CATEGORY

        return convertToResponse(updatedCategory);
    }


    // STEP 10: NORMALIZE OPTIONAL DESCRIPTION

    private String normalizeDescription(String description) {

        if (description == null || description.isBlank()) {
            return null;
        }

        return description.trim();
    }


    // STEP 11: CONVERT CATEGORY ENTITY INTO RESPONSE DTO

    private CategoryResponse convertToResponse(Category category) {

        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.isActive(),
                category.getCreatedAt()
        );
    }
}
