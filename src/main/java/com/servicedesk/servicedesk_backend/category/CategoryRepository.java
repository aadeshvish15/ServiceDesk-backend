
package com.servicedesk.servicedesk_backend.category;

// STEP 1: IMPORT REQUIRED CLASSES

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;



// STEP 2: DECLARE CATEGORY REPOSITORY

// JpaRepository provides built-in database operations.
public interface CategoryRepository extends JpaRepository<Category, Long> {


    // STEP 3: CHECK WHETHER CATEGORY NAME ALREADY EXISTS

    boolean existsByNameIgnoreCase(String name);


    // STEP 4: CHECK DUPLICATE NAME EXCLUDING CURRENT CATEGORY

    // Useful when updating an existing category.
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);


    // STEP 5: FETCH ONLY ACTIVE CATEGORIES

    // Employees and agents should only see active categories.
    List<Category> findByActiveTrueOrderByNameAsc();


    // STEP 6: FETCH ALL CATEGORIES

    // Admin can see both active and inactive categories.
    List<Category> findAllByOrderByNameAsc();

}
