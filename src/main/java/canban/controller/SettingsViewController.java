package canban.controller;

import authentication.CurrentUser;
import canban.entity.Category;
import canban.entity.Task;
import canban.entity.User;
import canban.google.GoogleCalendarConnector;
import canban.repository.CategoryRepository;
import canban.repository.TaskRepository;
import canban.repository.UserRepository;
import canban.view.SettingsView;
import com.google.api.services.calendar.model.Event;
import xmlexport.TaskList;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import xmlexport.JaxbConverter;

import javax.xml.bind.JAXBException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;

public class SettingsViewController {

    private TaskRepository taskRepository;
    private CategoryRepository categoryRepository;
    private UserRepository userRepository;
    private SettingsView view;


    public SettingsViewController(TaskRepository taskRepository, CategoryRepository categoryRepository,UserRepository userRepository, SettingsView view) {
        this.taskRepository = taskRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository=userRepository;
        this.view=view;
    }

    public void importFileUploaded(MemoryBuffer receiver, boolean deleteOld) throws JAXBException {
        TaskList taskList = JaxbConverter.unmarshal(receiver.getFileData());
        importFile(taskList,deleteOld);
    }

    private void importFile(TaskList taskList, boolean deleteOld) {
        if(taskList==null){
            new Notification("Hochladen fehgeschlagen", 2000).open();
            return;
        }
        if(deleteOld){
            taskRepository.deleteAllByUser(CurrentUser.getRole());
            categoryRepository.deleteAllByOwner(CurrentUser.getRole());
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
        }

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
            return new FileInputStream(JaxbConverter.export(tasks,categories));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void setUserDistractionFactor(int e) {
        CurrentUser.getRole().setDistractionFactor(e);
        userRepository.save(CurrentUser.getRole());
    }

    public void setUserNWeeks(int parseInt) {
        CurrentUser.getRole().setNweeksValue(parseInt);
        userRepository.save(CurrentUser.getRole());
    }

    public void deletTasks(User role, Task.Priority done, LocalDate value) {
        this.taskRepository.deleteAllByUserAndColumnAndDoneDateBefore(role, done, value);
    }

    public void onEnter() {
        this.view.buildLayout();
        this.view.initListener();
    }

    public List<Category> getCategoryByUser() {
        return this.categoryRepository.findByOwner(CurrentUser.getRole());
    }

    public void setPriorityHeightSettings(String name, int parseInt) {
        CurrentUser.getRole().getPriorityHeightSettings().put(name,parseInt);
        userRepository.save(CurrentUser.getRole());
    }

    public Collection<Event> googleCalendarConnect() throws IOException, GeneralSecurityException {
        return GoogleCalendarConnector.connect(this.userRepository);
    }

    public void setBlockedTimeSettings(String name, LocalTime value) {
        CurrentUser.getRole().getBlockedTimeSettings().put(name, value);
        userRepository.save(CurrentUser.getRole());
    }

    public void setTimeSettings(String name, LocalTime value) {
        CurrentUser.getRole().getTimeSettings().put(name, value);
        userRepository.save(CurrentUser.getRole());
    }

    public void setSizeSettings(String toString, int eingabe) {
        CurrentUser.getRole().getSizeSettings().put(toString,eingabe);
        userRepository.save( CurrentUser.getRole());
    }

    public Collection<Category> findByOwner() {
        return this.categoryRepository.findByOwner(CurrentUser.getRole());
    }

    public void deletCategory(Category category) {
        this.taskRepository.deleteAllByUserAndCategory(CurrentUser.getRole(),category);
        this.categoryRepository.delete(category);
        this.view.refreshGrid();
    }

    public void setSizeSettings(String toString, String value, Object value1) throws NumberFormatException {
            int eingabe = Integer.parseInt(value);
            switch ((String) value1){
                case "Day(s) (8h)":
                    eingabe*=480;
                    break;
                case "Hour(s)":
                    eingabe*=60;
                    break;
            }
            CurrentUser.getRole().getSizeSettings().put(toString,eingabe);
            userRepository.save( CurrentUser.getRole());
    }
}
