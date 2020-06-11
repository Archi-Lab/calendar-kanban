package com.example.crudwithvaadin.view;

import authentication.CurrentUser;
import com.example.crudwithvaadin.TaskList;
import com.example.crudwithvaadin.controller.AdminViewController;
import com.example.crudwithvaadin.entity.Category;
import com.example.crudwithvaadin.entity.Task;
import com.example.crudwithvaadin.entity.User;
import com.example.crudwithvaadin.form.UserForm;
import com.example.crudwithvaadin.repository.CategoryRepository;
import com.vaadin.flow.component.ItemLabelGenerator;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.Route;
import com.example.crudwithvaadin.repository.TaskRepository;
import com.example.crudwithvaadin.repository.UserRepository;
import com.vaadin.flow.server.StreamResource;
import xmlexport.JaxbConverter;

import javax.xml.bind.JAXBException;
import java.io.*;
import java.util.List;

@Route("Admin")
public class AdminViewImpl extends VerticalLayout implements AdminView {

    private Grid<User> userGrid = new Grid<>(User.class,false);
    private Button back = new Button();
    private Button createNew = new Button("Create user");
    private Button deleteOldTask = new Button("Delete old tasks");
    private Button importTask = new Button("Delete old tasks");
    private UserRepository userRepository;
    private TaskRepository taskRepository;
    private CategoryRepository categoryRepository;
    private UserForm userForm;
    private AdminViewController controller = new AdminViewController(this);

    public AdminViewImpl(UserRepository userRepository, TaskRepository taskRepository,CategoryRepository categoryRepository){
        this.userRepository=userRepository;
        this.taskRepository=taskRepository;
        this.categoryRepository=categoryRepository;
        this.userForm=new UserForm(this,userRepository);
        if(CurrentUser.getRole()!=null&&CurrentUser.getRole().getRolle().equals(User.Rolle.ADMIN)){
            controller.onEnter();
        }
    }

    @Override
    public void configListener() {

        this.userGrid.addItemDoubleClickListener(e->{
            if(e.getItem() instanceof User){
                this.controller.userClicked(e.getItem());
            }
        });

        back.addClickListener(e->{getUI().get().navigate("Termin");});

        createNew.addClickListener(e->{
            this.controller.userClicked(null);
        });
    }

    @Override
    public void buildLayout() {
        this.add(new Label("Username: "+CurrentUser.getRole().getName()));
        this.userGrid
                .addColumn(User::getName)
                .setHeader("Username")
                .setSortable(false)
                .setAutoWidth(true);
        this.userGrid
                .addColumn(User::getRolle)
                .setHeader("Rights")
                .setSortable(false)
                .setAutoWidth(true);
        this.userGrid.addComponentColumn(this::generateBtn)
                .setHeader("")
                .setAutoWidth(true)
                .setSortable(false);
        this.userGrid.addComponentColumn(this::generateBtnExport)
                .setHeader("")
                .setAutoWidth(true)
                .setSortable(false);
        this.userGrid.setWidth("100%");
        this.userGrid.setItems(userRepository.findAll());

        Dialog dialog2 = generateConfirmDialog();
        this.deleteOldTask.addClickListener(e->{
            dialog2.open();
        });

        back.setIcon(VaadinIcon.ARROW_BACKWARD.create());

        MemoryBuffer buffer = new MemoryBuffer();
        Upload importData = new Upload(buffer);
        importData.setDropAllowed(false);
        importData.setUploadButton(new Button("Import",new Icon(VaadinIcon.UPLOAD_ALT)));
        importData.setAcceptedFileTypes("text/xml");
        importData.addSucceededListener(e->{
            if(e.getSource().getReceiver() instanceof MemoryBuffer){
                try {
                    TaskList taskList = JaxbConverter.unmarshal(((MemoryBuffer) e.getSource().getReceiver()).getFileData());
                    Dialog dialog = new Dialog();
                    ComboBox<User> comboBox = new ComboBox<>();
                    comboBox.setItems(userRepository.findAll());
                    comboBox.setItemLabelGenerator(new ItemLabelGenerator<User>() {
                        @Override
                        public String apply(User user) {
                            return user.getName();
                        }
                    });
                    dialog.setCloseOnEsc(false);
                    dialog.setCloseOnOutsideClick(false);
                    Button confirmButton = new Button("Import", event -> {
                        importFile(taskList,comboBox.getValue());
                        dialog.close();
                    });
                    Button cancelButton = new Button("Cancel", event -> {
                        dialog.close();
                    });
                    Label dialogLabel=new Label("Who do you want to import the tasks for?");
                    VerticalLayout layout = new VerticalLayout(dialogLabel,comboBox,new HorizontalLayout(confirmButton,cancelButton));
                    dialog.add(layout);
                    dialog.open();
                } catch (JAXBException jaxbException) {
                    jaxbException.printStackTrace();
                    new Notification("Hochladen fehgeschlagen", 2000).open();
                }
            }
        });

        this.userForm.setVisible(false);
        HorizontalLayout headLayout = new HorizontalLayout();
        headLayout.addClassName("centerLayout");
        headLayout.add(back,createNew,deleteOldTask,importData);


        setClassName("main-layout");
        add(headLayout);
        add(userGrid);
        add(userForm);
    }

    private void importFile(TaskList taskList,User user) {
        if(taskList==null){
            new Notification("Hochladen fehgeschlagen", 2000).open();
            return;
        }
        List<Category> userCategoryList = categoryRepository.findByOwner(user);
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
                category.setOwner(user);
                categoryRepository.save(category);
            }
        };

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

    @Override
    public void refreshGrid(){
        this.userGrid.setItems(userRepository.findAll());
    }

    private Button generateBtn(User user) {
        Button retButton = new Button("Löschen");
        retButton.addClassName("deleteBtn");
        retButton.setIcon(VaadinIcon.TRASH.create());

        Dialog dialog = new Dialog();
        dialog.setCloseOnEsc(false);
        dialog.setCloseOnOutsideClick(false);
        Button confirmButton = new Button("Confirm", event -> {
            this.taskRepository.deleteAllByUser(user);
            this.userRepository.delete(user);
            this.refreshGrid();
            dialog.close();
        });
        confirmButton.addClassName("deleteBtn");
        Button cancelButton = new Button("Cancel", event -> {
            dialog.close();
        });
        Label dialogLabel=new Label("Do you want to delete the user \""+user.getName()+"\"?");
        VerticalLayout layout = new VerticalLayout(dialogLabel,new HorizontalLayout(confirmButton,cancelButton));
        dialog.add(layout);
        retButton.addClickListener(e->{
            dialog.open();
        });



        return retButton;
    }

    private Anchor generateBtnExport(User user) {
        Anchor download = new Anchor(new StreamResource("export.xml", () -> createResource(user)), "");
        download.getElement().setAttribute("download", true);
        download.add(new Button("Export",new Icon(VaadinIcon.DOWNLOAD_ALT)));
        return download;
    }
    private InputStream createResource(User user) {
        try {
            List<Task> tasks=taskRepository.findByUser(user);
            List<Category> categories=categoryRepository.findByOwner(user);
            InputStream inputStream = new FileInputStream(JaxbConverter.export(tasks,categories));
            return inputStream;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Dialog generateConfirmDialog(){
        DatePicker datePicker = new DatePicker();
        Dialog dialog = new Dialog();
        dialog.setCloseOnEsc(false);
        dialog.setCloseOnOutsideClick(false);
        Button confirmButton = new Button("Confirm", event -> {
            this.taskRepository.deleteAllByColumnAndDoneDateBefore(Task.Priority.DONE, datePicker.getValue());
            dialog.close();
            new Notification("Nachrichten wurden gelöscht", 2000).open();
        });

        Button cancelButton = new Button("Cancel", event -> {
            dialog.close();
        });

        Label dialogLabel=new Label("All tasks that are completed and older than the selected value are deleted.");
        VerticalLayout layout = new VerticalLayout(dialogLabel,datePicker,new HorizontalLayout(confirmButton,cancelButton));
        dialog.add(layout);
        return dialog;
    }

    @Override
    public void setVisibleForm(User user){
        this.userForm.fillLayout(user);
        this.userForm.setVisible(true);
    }
}
