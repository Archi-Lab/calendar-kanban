package com.example.crudwithvaadin.repository;

import com.example.crudwithvaadin.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User,Long> {

    User findByUsernameEqualsAndPasswordEquals(String username, String password);

}
