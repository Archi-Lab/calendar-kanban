package com.example.crudwithvaadin;

import com.example.crudwithvaadin.entity.Category;
import com.example.crudwithvaadin.entity.Task;
import com.example.crudwithvaadin.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import com.example.crudwithvaadin.repository.CategoryRepository;
import com.example.crudwithvaadin.repository.TaskRepository;
import com.example.crudwithvaadin.repository.UserRepository;

import java.time.LocalDate;

@SpringBootApplication
public class CrudWithVaadinApplication {

    private static final Logger log = LoggerFactory.getLogger(CrudWithVaadinApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(CrudWithVaadinApplication.class);
    }

    @Bean
    public CommandLineRunner loadData(TaskRepository repository, CategoryRepository categoryRepository, UserRepository userRepository) {
        return (args) -> {

            User user = userRepository.findByUsernameEqualsAndPasswordEquals("test","test");
            if(user==null){
                log.info("INIT USER");
                log.info("-------------------------------");

                user = new User("test","test", User.Rolle.NUTZER);
                userRepository.save(user); Category bachelor = new Category("Bachelor");
                bachelor.setColor("#123456");
                bachelor.setOwner(user);
                Category freizeit = new Category("Freizeit");
                freizeit.setColor("#00ffaa");
                freizeit.setOwner(user);

                categoryRepository.save(bachelor);
                categoryRepository.save(freizeit);
                Task task1 = new Task();
                task1.setTitle("B100 erledigen");
                task1.setCreationDate(LocalDate.now());
                task1.setDueDate(LocalDate.now().plusDays(3));
                task1.setColumn(Task.Priority.TODAY);
                task1.setCategory(bachelor);
                task1.setSize(Task.Size.L);
                task1.setUser(user);

                Task task2 = new Task();
                task2.setTitle("A101 erledigen");
                task2.setCreationDate(LocalDate.now());
                task2.setDueDate(LocalDate.now().plusDays(5));
                task2.setColumn(Task.Priority.TODAY);
                task2.setCategory(freizeit);
                task2.setSize(Task.Size.M);
                task2.setUser(user);

                repository.save(task1);
                repository.save(task2);
            }
            User user2 = userRepository.findByUsernameEqualsAndPasswordEquals("admin","admin");
            if (user2==null){
                log.info("INIT ADMIN");
                log.info("-------------------------------");
                user2 = new User("admin","admin", User.Rolle.ADMIN);
                userRepository.save(user2);
            }








            // fetch all customers
            log.info("Termine found with findAll():");
            log.info("-------------------------------");
            for (Task task : repository.findAll()) {
                log.info(task.toString());
            }
            log.info("Gestartet");
        };
    }

}