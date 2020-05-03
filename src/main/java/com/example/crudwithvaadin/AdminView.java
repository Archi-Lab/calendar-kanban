package com.example.crudwithvaadin;

import authentication.CurrentUser;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.NativeButtonRenderer;
import com.vaadin.flow.router.Route;
import xmlexport.JaxbExample;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Route("Admin")
public class AdminView extends VerticalLayout {

    Grid<User> userGrid = new Grid<>(User.class,false);
    Button back = new Button("Zurück");
    Button createNew = new Button("Nutzer anlegen");
    private Button deleteOldTask = new Button("Alte Task aus System löschen");
    private UserRepository userRepository;
    private TaskRepository taskRepository;
    private UserForm userForm;

    public AdminView(UserRepository userRepository, TaskRepository taskRepository){
        this.userRepository=userRepository;
        this.taskRepository=taskRepository;
        this.userForm=new UserForm(this,userRepository);
        if(CurrentUser.getRole()!=null&&CurrentUser.getRole().getRolle().equals(User.Rolle.ADMIN)){
            buildLayout();
        }
    }

    private void buildLayout() {
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
        back.addClickListener(e->{getUI().get().navigate("Termin");});
        createNew.addClickListener(e->{this.userForm.setVisible(true);});
        this.deleteOldTask.addClickListener(e->{
            this.taskRepository.deleteAllByColumnAndDoneDateBefore(Task.Priority.DONE, LocalDate.now().minusMonths(4));
            new Notification("Nachrichten wurden gelöscht", 2000).open();
        });
        add(back);
        this.userForm.setVisible(false);
        add(createNew);
        add(deleteOldTask);
        add(userGrid);
        add(userForm);
    }

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
}
