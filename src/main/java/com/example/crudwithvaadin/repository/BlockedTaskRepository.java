package com.example.crudwithvaadin.repository;

import com.example.crudwithvaadin.entity.BlockedTask;
import com.example.crudwithvaadin.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;

@Transactional
public interface BlockedTaskRepository extends JpaRepository<BlockedTask, Long> {

    List<BlockedTask> findByUser(User user);

    List<BlockedTask> findByUserAndDate(User user,LocalDate date);
    List<BlockedTask> findByUserAndDateBetween(User user,LocalDate start,LocalDate end);

    void deleteAllByUserAndDateBefore(User user, LocalDate localDate);

}
