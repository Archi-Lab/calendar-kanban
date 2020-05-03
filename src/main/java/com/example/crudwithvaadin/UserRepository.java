package com.example.crudwithvaadin;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User,Long> {

    User findByUsernameEqualsAndPasswordEquals(String username, String password);

}
