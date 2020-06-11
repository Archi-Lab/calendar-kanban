package com.example.crudwithvaadin.controller;

import authentication.CurrentUser;
import com.example.crudwithvaadin.TaskList;
import com.example.crudwithvaadin.entity.Category;
import com.example.crudwithvaadin.entity.Task;
import com.example.crudwithvaadin.repository.CategoryRepository;
import com.example.crudwithvaadin.repository.TaskRepository;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import xmlexport.JaxbConverter;

import javax.xml.bind.JAXBException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

public class SettingsViewController {

    private TaskRepository taskRepository;
    private CategoryRepository categoryRepository;


    public SettingsViewController(TaskRepository taskRepository, CategoryRepository categoryRepository) {
        this.taskRepository = taskRepository;
        this.categoryRepository = categoryRepository;
    }

    public void importFileUploaded(MemoryBuffer receiver) throws JAXBException {
        TaskList taskList = JaxbConverter.unmarshal(receiver.getFileData());
        importFile(taskList);
    }

    private void importFile(TaskList taskList) {
        if(taskList==null){
            new Notification("Hochladen fehgeschlagen", 2000).open();
            return;
        }
        List<Category> userCategoryList = categoryRepository.findByOwner(CurrentUser.getRole());
        List<Category> userCategoryListFromImport = taskList.getListCategory();
        for (Category cImport : userCategoryListFromImport){
            boolean exist = false;
            for (Category cUser: userCategoryList ){
                if(cImport.getBeschreibung().toUpperCase().equals(cUser.getBeschreibung().toUpperCase())){
                    exist=true;
                    break;
                }
            }
            if(!exist){
                Category category = new Category();
                category.setColor(cImport.getColor());
                category.setBeschreibung(cImport.getBeschreibung());
                category.setOwner(CurrentUser.getRole());
                categoryRepository.save(category);
            }
        };

        //update List with new Categories
        userCategoryList = categoryRepository.findByOwner(CurrentUser.getRole());
        for(Task tmpTask: taskList.getList()){
            Task task = tmpTask.copy();
            task.setUser(CurrentUser.getRole());
            for(Category c :userCategoryList) {
                if(c.getBeschreibung().toUpperCase().equals(tmpTask.getCategory().getBeschreibung().toUpperCase()))
                    task.setCategory(c);
            }
            taskRepository.save(task);
        }
        new Notification("Hochladen erfolgreich", 2000).open();
    }

    public InputStream createResource() {
        try {
            List<Task> tasks=taskRepository.findByUser(CurrentUser.getRole());
            List<Category> categories=categoryRepository.findByOwner(CurrentUser.getRole());
            InputStream inputStream = new FileInputStream(JaxbConverter.export(tasks,categories));
            return inputStream;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
