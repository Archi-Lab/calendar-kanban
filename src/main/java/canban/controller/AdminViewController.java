package canban.controller;

import canban.entity.Category;
import canban.entity.Task;
import canban.entity.User;
import canban.form.UserForm;
import canban.repository.CategoryRepository;
import canban.repository.TaskRepository;
import canban.repository.UserRepository;
import canban.view.AdminView;
import com.vaadin.flow.component.notification.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xmlexport.JaxbConverter;
import xmlexport.TaskList;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class AdminViewController {

    private static final Logger log = LoggerFactory.getLogger(AdminViewController.class);

    private AdminView view;
    private UserRepository userRepository;
    private TaskRepository taskRepository;
    private CategoryRepository categoryRepository;

    public AdminViewController(AdminView view, UserRepository userRepository, TaskRepository taskRepository, CategoryRepository categoryRepository) {
        this.view = view;
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.categoryRepository = categoryRepository;
    }

    public void onEnter() {
        view.buildLayout();
        view.configListener();
    }

    public void userClicked(User user){
        this.view.setVisibleForm(user);
    }

    public void deleteUserClicked(User user) {
        this.taskRepository.deleteAllByUser(user);
        this.categoryRepository.deleteAllByOwner(user);
        this.userRepository.delete(user);
        this.view.refreshGrid();
    }

    public Collection<User> findAllUser() {
        return userRepository.findAll();
    }

    public InputStream createResource(User user) {
        try {
            List<Task> tasks=taskRepository.findByUser(user);
            List<Category> categories=categoryRepository.findByOwner(user);
            return new FileInputStream(JaxbConverter.export(tasks,categories));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void deleteOldTasksClicked(Task.Priority done, LocalDate value) {
        this.taskRepository.deleteAllByColumnAndDoneDateBefore(done,value);
    }

    public void importFile(TaskList taskList, User user, Boolean deleteOld) {
        if(taskList==null){
            new Notification("Hochladen fehgeschlagen", 2000).open();
            return;
        }
        if(deleteOld){
            taskRepository.deleteAllByUser(user);
            categoryRepository.deleteAllByOwner(user);
        }

        List<Category> userCategoryList = categoryRepository.findByOwner(user);
        List<Category> userCategoryListFromImport = taskList.getListCategory();
        for (Category cImport : userCategoryListFromImport){
            boolean exist = false;
            for (Category cUser: userCategoryList ){
                if(cImport.getBeschreibung().toUpperCase().equals(cUser.getBeschreibung().toUpperCase())){
                    exist=true;
                    userCategoryList.remove(cUser);
                    break;
                }
            }
            if(!exist){
                Category category = new Category();
                category.setColor(cImport.getColor());
                category.setBeschreibung(cImport.getBeschreibung());
                category.setOwner(user);
                categoryRepository.save(category);
            }
        }

        //update List with new Categories
        userCategoryList = categoryRepository.findByOwner(user);

        for(Task tmpTask: taskList.getList()){
            Task task = new Task();
            task.setUser(user);
            task.setColumn(tmpTask.getColumn());
            task.setCreationDate(tmpTask.getCreationDate());
            task.setDueDate(tmpTask.getDueDate());
            task.setSize(tmpTask.getSize());
            task.setTitle(tmpTask.getTitle());
            task.setDone(tmpTask.isDone());
            task.setDoneDate(tmpTask.getDoneDate());
            for(Category c :userCategoryList) {
                if(c.getBeschreibung().toUpperCase().equals(tmpTask.getCategory().getBeschreibung().toUpperCase()))
                    task.setCategory(c);
            }
            taskRepository.save(task);
        }
        new Notification("Hochladen erfolgreich", 2000).open();
    }
}
