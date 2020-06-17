package canban.view;

import authentication.CurrentUser;
import canban.entity.Task;
import canban.entity.User;
import canban.form.UserForm;
import canban.repository.TaskRepository;
import xmlexport.TaskList;
import canban.controller.AdminViewController;
import canban.repository.CategoryRepository;
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
import canban.repository.UserRepository;
import com.vaadin.flow.server.StreamResource;
import xmlexport.JaxbConverter;

import javax.xml.bind.JAXBException;

@Route("Admin")
public class AdminViewImpl extends VerticalLayout implements AdminView {

    private Grid<User> userGrid = new Grid<>(User.class,false);
    private Button back = new Button();
    private Button createNew = new Button("Create user");
    private Button deleteOldTask = new Button("Delete old tasks");
    private Button importTask = new Button("Delete old tasks");
    private UserForm userForm;
    private AdminViewController controller;

    public AdminViewImpl(UserRepository userRepository, TaskRepository taskRepository,CategoryRepository categoryRepository){
        if(CurrentUser.getRole()!=null&&CurrentUser.getRole().getRolle().equals(User.Rolle.ADMIN)){
            this.userForm=new UserForm(this,userRepository);
            controller = new AdminViewController(this,userRepository,taskRepository,categoryRepository);
            controller.onEnter();
        }
    }

    @Override
    public void configListener() {
        this.userGrid.addItemDoubleClickListener(e->{
            if(e.getItem() != null){
                this.controller.userClicked(e.getItem());
            }
        });
        back.addClickListener(e-> getUI().get().navigate("Termin"));
        createNew.addClickListener(e-> this.controller.userClicked(null));
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
        this.userGrid.setItems(this.controller.findAllUser());

        Dialog dialog2 = generateConfirmDialog();
        this.deleteOldTask.addClickListener(e-> dialog2.open());

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
                    comboBox.setItems(this.controller.findAllUser());
                    comboBox.setItemLabelGenerator((ItemLabelGenerator<User>) User::getName);
                    dialog.setCloseOnEsc(false);
                    dialog.setCloseOnOutsideClick(false);
                    Button confirmButton = new Button("Import", event -> {
                        this.controller.importFile(taskList,comboBox.getValue());
                        dialog.close();
                    });
                    Button cancelButton = new Button("Cancel", event -> dialog.close());
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

    @Override
    public void refreshGrid(){
        this.userGrid.setItems(this.controller.findAllUser());
    }

    private Button generateBtn(User user) {
        Button retButton = new Button("Löschen");
        retButton.addClassName("deleteBtn");
        retButton.setIcon(VaadinIcon.TRASH.create());

        Dialog dialog = new Dialog();
        dialog.setCloseOnEsc(false);
        dialog.setCloseOnOutsideClick(false);
        Button confirmButton = new Button("Confirm", event -> {
            this.controller.deleteUserClicked(user);
            dialog.close();
        });
        confirmButton.addClassName("deleteBtn");
        Button cancelButton = new Button("Cancel", event -> dialog.close());
        Label dialogLabel=new Label("Do you want to delete the user \""+user.getName()+"\"?");
        VerticalLayout layout = new VerticalLayout(dialogLabel,new HorizontalLayout(confirmButton,cancelButton));
        dialog.add(layout);
        retButton.addClickListener(e-> dialog.open());

        return retButton;
    }

    private Anchor generateBtnExport(User user) {
        Anchor download = new Anchor(new StreamResource("export.xml", () -> this.controller.createResource(user)), "");
        download.getElement().setAttribute("download", true);
        download.add(new Button("Export",new Icon(VaadinIcon.DOWNLOAD_ALT)));
        return download;
    }


    private Dialog generateConfirmDialog(){
        DatePicker datePicker = new DatePicker();
        Dialog dialog = new Dialog();
        dialog.setCloseOnEsc(false);
        dialog.setCloseOnOutsideClick(false);
        Button confirmButton = new Button("Confirm", event -> {
            this.controller.deleteTaskOldTaskClicked(Task.Priority.DONE, datePicker.getValue());
            dialog.close();
            new Notification("Nachrichten wurden gelöscht", 2000).open();
        });

        Button cancelButton = new Button("Cancel", event -> dialog.close());

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
