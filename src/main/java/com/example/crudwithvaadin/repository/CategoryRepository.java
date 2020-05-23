package com.example.crudwithvaadin.repository;

import com.example.crudwithvaadin.entity.Category;
import com.example.crudwithvaadin.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByOwner(User owner);

}