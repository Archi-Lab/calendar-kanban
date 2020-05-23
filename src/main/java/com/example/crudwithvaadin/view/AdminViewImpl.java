package com.example.crudwithvaadin.view;

import authentication.CurrentUser;
import com.example.crudwithvaadin.controller.AdminViewController;
import com.example.crudwithvaadin.entity.Task;
import com.example.crudwithvaadin.entity.User;
import com.example.crudwithvaadin.form.UserForm;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.example.crudwithvaadin.repository.TaskRepository;
import com.example.crudwithvaadin.repository.UserRepository;
import xmlexport.JaxbExample;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.util.List;

@Route("Admin")
public class AdminViewImpl extends VerticalLayout implements AdminView {

    private Grid<User> userGrid = new Grid<>(User.class,false);
    private Button back = new Button("Zurück");
    private Button createNew = new Button("Nutzer anlegen");
    private Button deleteOldTask = new Button("Alte Task aus System löschen");
    private UserRepository userRepository;
    private TaskRepository taskRepository;
    private UserForm userForm;
    private AdminViewController controller = new AdminViewController(this);

    public AdminViewImpl(UserRepository userRepository, TaskRepository taskRepository){
        this.userRepository=userRepository;
        this.taskRepository=taskRepository;
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
                .setHeader("Rechte")
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

        Dialog dialog = generateConfirmDialog();
        this.deleteOldTask.addClickListener(e->{
            dialog.open();
        });

        this.userForm.setVisible(false);
        HorizontalLayout headLayout = new HorizontalLayout();
        headLayout.addClassName("centerLayout");
        headLayout.add(back,createNew,deleteOldTask);


        setClassName("main-layout");
        add(headLayout);
        add(userGrid);
        add(userForm);
    }

    @Override
    public void refreshGrid(){
        this.userGrid.setItems(userRepository.findAll());
    }

    private Button generateBtn(User user) {
        Button retButton = new Button("Löschen");
        retButton.addClickListener(e->{
            this.taskRepository.deleteAllByUser(user);
            this.userRepository.delete(user);
            this.refreshGrid();
        });
        return retButton;
    }

    private Button generateBtnExport(User user) {
        Button retButton = new Button("Export");
        retButton.addClickListener(e->{
            List<Task> list = this.taskRepository.findByUser(user);
            list.forEach(t->{t.setUser(null);});
            try {
                JaxbExample.marshal(list,new File("export.xml"));
                new Notification("Export erfolgreich", 2000).open();
            } catch (IOException ioException) {
                ioException.printStackTrace();
                new Notification("Export fehlgeschlagen", 2000).open();
            } catch (JAXBException jaxbException) {
                jaxbException.printStackTrace();
                new Notification("Export fehlgeschlagen", 2000).open();
            }

        });
        return retButton;
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
