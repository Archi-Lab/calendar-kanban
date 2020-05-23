package com.example.crudwithvaadin.repository;

import com.example.crudwithvaadin.entity.Category;
import com.example.crudwithvaadin.entity.Task;
import com.example.crudwithvaadin.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;

@Transactional
public interface TaskRepository extends JpaRepository<Task,Long> {


    List<Task> findByUser(User user);

    List<Task> findByColumnAndUserOrderByDoneDate(Task.Priority priority, User user);
    List<Task> findByColumnAndUserAndTitleContainingIgnoreCaseOrderByDoneDate(Task.Priority priority, User user,String beschreibung);
    List<Task> findByColumnAndCategoryAndUserOrderByDoneDate(Task.Priority priority, Category ctegory, User user);
    List<Task> findByColumnAndCategoryAndUserAndTitleContainingIgnoreCaseOrderByDoneDate(Task.Priority priority, Category ctegory, User user,String beschreibung);

    void deleteAllByUser(User user);

    void deleteAllByUserAndColumnAndDoneDateBefore(User user, Task.Priority priotiry, LocalDate date);
    void deleteAllByColumnAndDoneDateBefore(Task.Priority priotiry, LocalDate date);
    void deleteAllByUserAndCategory(User user, Category category);
}
