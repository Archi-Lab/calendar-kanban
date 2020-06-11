package com.example.crudwithvaadin.view;

import authentication.CurrentUser;
import com.example.crudwithvaadin.controller.SettingsViewController;
import com.example.crudwithvaadin.entity.Category;
import com.example.crudwithvaadin.entity.Task;
import com.example.crudwithvaadin.form.CategoryForm;
import com.example.crudwithvaadin.google.GoogleCalendarConnector;
import com.example.crudwithvaadin.repository.TaskRepository;
import com.google.api.services.calendar.model.Event;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import com.example.crudwithvaadin.repository.CategoryRepository;
import com.example.crudwithvaadin.repository.UserRepository;
import com.vaadin.flow.server.StreamResource;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@CssImport("./styles/style.css")
@Route("Settings")
public class SettingsViewImpl extends VerticalLayout implements SettingsView {

    private TaskRepository taskRepository;
    private Button backBtn;
    private Button createDump = new Button("Export");
    private Button deleteOldTask = new Button("Delete old tasks");
    private Button addCategory;
    private TextField distractionFactorField;
    private TextField nWeeksField;
    private CategoryRepository categoryRepository;
    private UserRepository userRepository;
    private Grid<Category> categoryGrid;
    private CategoryForm categoryForm;
    private SettingsViewController controller;


    private final Map<Tab, Component> tabsToPages = new HashMap<>();

    private Tab tab1 = new Tab("Categorys");
    private Tab tab2 = new Tab("Other settings");
    private Tab tab3 = new Tab("Size settings");
    private Tab tab4 = new Tab("Worktime");
    private Tab tab5 = new Tab("Google Calendar");
    private Tab tab6 = new Tab("Column Settings");

    private Div page1 = new Div();
    private Div page2 = new Div();
    private Div page3 = new Div();
    private Div page4 = new Div();
    private Div page5 = new Div();
    private Div page6 = new Div();
    private Tabs tabs;

    public SettingsViewImpl(CategoryRepository categoryRepository, UserRepository userRepository, TaskRepository taskRepository){
        if(CurrentUser.getRole()!=null) {
            this.add(new Label("Username: "+CurrentUser.getRole().getName()));
            this.categoryRepository = categoryRepository;
            this.userRepository=userRepository;
            this.taskRepository=taskRepository;
            this.categoryForm = new CategoryForm(this, categoryRepository);
            this.controller=new SettingsViewController(taskRepository,categoryRepository);
            buildLayout();
            initListener();
            setClassName("main-layout");
        }
    }

    private void initListener() {
        distractionFactorField.addValueChangeListener(e->{
            try {
                CurrentUser.getRole().setDistractionFactor(Integer.parseInt(e.getValue()));
                distractionFactorField.setErrorMessage(null);
                userRepository.save(CurrentUser.getRole());
            }catch (NumberFormatException exception){
                distractionFactorField.setErrorMessage("Bitte nur Zahlen");
            }
        });

        nWeeksField.addValueChangeListener(e->{
            try {
                CurrentUser.getRole().setNweeksValue(Integer.parseInt(e.getValue()));
                nWeeksField.setErrorMessage(null);
                userRepository.save(CurrentUser.getRole());
            }catch (NumberFormatException exception){
                nWeeksField.setErrorMessage("Bitte nur Zahlen");
            }
        });

        categoryGrid.addItemDoubleClickListener(e->{
            if(e.getItem() instanceof Category){
                this.categoryForm.fillForm(e.getItem());
                this.categoryForm.setVisible(true);
            }
        });

        backBtn.addClickListener(e->{
            getUI().get().navigate("Termin");
        });

        addCategory.addClickListener(e->{
            this.categoryForm.fillForm(null);
            this.categoryForm.setVisible(true);
        });

        DatePicker datePicker = new DatePicker();
        Dialog dialog = new Dialog();
        dialog.setCloseOnEsc(false);
        dialog.setCloseOnOutsideClick(false);
        Button confirmButton = new Button("Confirm", event -> {
            this.taskRepository.deleteAllByUserAndColumnAndDoneDateBefore(CurrentUser.getRole(), Task.Priority.DONE, datePicker.getValue());
            dialog.close();
        });
        Button cancelButton = new Button("Cancel", event -> {
            dialog.close();
        });
        Label dialogLabel=new Label("All tasks that are completed and older than the selected value are deleted.");
        VerticalLayout layout = new VerticalLayout(dialogLabel,datePicker,new HorizontalLayout(confirmButton,cancelButton));
        dialog.add(layout);
        this.deleteOldTask.addClickListener(e->{
            dialog.open();
        });
        Set<Component> pagesShown = Stream.of(page1)
                .collect(Collectors.toSet());
        tabs.addSelectedChangeListener(event -> {
            pagesShown.forEach(page -> page.setVisible(false));
            pagesShown.clear();
            Component selectedPage = tabsToPages.get(tabs.getSelectedTab());
            selectedPage.setVisible(true);
            pagesShown.add(selectedPage);
        });
    }

    private void buildLayout() {
        distractionFactorField = new TextField("Distraction Factor (%)");
        distractionFactorField.setValue(CurrentUser.getRole().getDistractionFactor()+"");
        distractionFactorField.setValueChangeMode(ValueChangeMode.LAZY);

        nWeeksField = new TextField("Week number");
        nWeeksField.setValue(CurrentUser.getRole().getNweeksValue()+"");
        nWeeksField.setValueChangeMode(ValueChangeMode.LAZY);

        categoryGrid=new Grid<>(Category.class,false);
        categoryGrid.setSizeFull();
        categoryGrid.setHeight("310px");
        categoryGrid.addColumn(Category::getBeschreibung)
                .setHeader("Title")
                .setSortable(false)
                .setAutoWidth(true);
        categoryGrid.addComponentColumn(this::generateColorColumn);
        categoryGrid.addComponentColumn(this::generateDeleteButton);
        categoryGrid.setItems(this.categoryRepository.findByOwner(CurrentUser.getRole()));

        backBtn=new Button();
        backBtn.setIcon(VaadinIcon.ARROW_BACKWARD.create());

        addCategory=new Button("New category");

        this.add(categoryForm);
        this.categoryForm.setVisible(false);

        this.configPage1();
        this.configPage2();
        this.configPage3();
        this.configPage4();
        this.configPage5();
        this.configPage6();


        tabsToPages.put(tab1, page1);
        tabsToPages.put(tab2, page2);
        tabsToPages.put(tab3, page3);
        tabsToPages.put(tab6, page6);
        tabsToPages.put(tab4, page4);
        tabsToPages.put(tab5, page5);
        tabs = new Tabs(tab1, tab2, tab6, tab3, tab4, tab5);
        Div pages = new Div(page1, page2, page6, page3, page4, page5);



        HorizontalLayout headlayout = new HorizontalLayout();
        headlayout.add(backBtn,tabs);
        headlayout.addClassName("centerLayout");

        this.add(headlayout,pages);
    }

    private void configPage1() {
        page1.add(addCategory);
        HorizontalLayout horizontalLayout = new HorizontalLayout(categoryGrid);
        horizontalLayout.setWidth("750px");
        page1.add(horizontalLayout);
        page1.setSizeFull();
    }

    private void configPage2() {
        Anchor download = new Anchor(new StreamResource("export.xml", () -> this.controller.createResource()), "");
        download.getElement().setAttribute("download", true);
        download.add(new Button("Export",new Icon(VaadinIcon.DOWNLOAD_ALT)));

        MemoryBuffer buffer = new MemoryBuffer();
        Upload importData = new Upload(buffer);
        importData.setDropAllowed(false);
        importData.setUploadButton(new Button("Import",new Icon(VaadinIcon.UPLOAD_ALT)));
        importData.setAcceptedFileTypes("text/xml");
        importData.addSucceededListener(e->{
            if(e.getSource().getReceiver() instanceof MemoryBuffer){
                try {
                    this.controller.importFileUploaded(((MemoryBuffer) e.getSource().getReceiver()));
                } catch (JAXBException jaxbException) {
                    jaxbException.printStackTrace();
                    new Notification("Hochladen fehgeschlagen", 2000).open();
                }
            }
        });

        HorizontalLayout horizontalLayout1 = new HorizontalLayout();
        horizontalLayout1.add(distractionFactorField);
        horizontalLayout1.add(nWeeksField);
        horizontalLayout1.add(deleteOldTask);
        horizontalLayout1.add(download);
        horizontalLayout1.add(importData);
        horizontalLayout1.addClassName("centerLayout");
        page2.add(horizontalLayout1);
        page2.setSizeFull();
        page2.setVisible(false);
    }

    private void configPage3() {
        page3.add(generateSizeLayout());
        page3.setSizeFull();
        page3.setVisible(false);
    }

    private void configPage4() {
        page4.add(new Label("Daily worktime"));
        page4.add(generateWeek());
        VerticalLayout breakLayout = new VerticalLayout();
        breakLayout.setHeight("25px");
        page4.add(breakLayout);
        page4.add(new Label("Daily blocked hour"));
        page4.add(generateBlockedWeek());
        VerticalLayout breakLayout2 = new VerticalLayout();
        breakLayout2.setHeight("25px");
        page4.add(breakLayout2);
        page4.setSizeFull();
        page4.setVisible(false);
    }

    private void configPage5() {
        Button btn = new Button("Add google calendar");
        btn.setIcon(VaadinIcon.GOOGLE_PLUS.create());
        btn.addClickListener(e->{
            if (!CurrentUser.getRole().isConnectGoogle()) {
                try {
                    GoogleCalendarConnector.connect(this.userRepository);
                    page5.add(getGoogleEvents());
                } catch (IOException ioException) {
                    //Logger.getAnonymousLogger().log(Level.ALL,ioException.getMessage());
                    ioException.printStackTrace();
                } catch (GeneralSecurityException generalSecurityException) {
                    //Logger.getAnonymousLogger().log(Level.ALL,generalSecurityException.getMessage());
                    generalSecurityException.printStackTrace();
                }
            }
        });
        page5.add(btn);
        Checkbox isConnected = new Checkbox("Connected with Google Calendar");
        isConnected.setEnabled(false);
        isConnected.setValue(CurrentUser.getRole().isConnectGoogle());
        page5.add(new VerticalLayout(isConnected));
        if (CurrentUser.getRole().isConnectGoogle()) {
            page5.add(getGoogleEvents());
        }
        page5.setSizeFull();
        page5.setVisible(false);
    }

    private void configPage6() {
        HorizontalLayout horizontalLayout2 = new HorizontalLayout(new Label("30px = +14 Tasks"),new Label("50px = 9-13 Tasks"),new Label("74px = 7-9 Tasks"),new Label("100px = 5-7 Tasks"));
        horizontalLayout2.setMargin(true);
        horizontalLayout2.setSpacing(true);
        page6.add(horizontalLayout2);
        HorizontalLayout horizontalLayout3 = new HorizontalLayout();
        horizontalLayout3.setMargin(true);
        horizontalLayout3.setSpacing(true);

        for(Task.Priority priority: Task.Priority.values()){
            TextField tmp = new TextField(priority.getBezeichnung());
            if(priority.equals(Task.Priority.NEXTNWEEK)){
                tmp = new TextField(priority.getBezeichnung().replace("$",CurrentUser.getRole().getNweeksValue()+""));
            }
            tmp.setWidth("100px");
            tmp.setValueChangeMode(ValueChangeMode.LAZY);
            tmp.setValue(""+CurrentUser.getRole().getPriorityHeightSettings().get(priority.name()));
            tmp.addValueChangeListener(e->{
                CurrentUser.getRole().getPriorityHeightSettings().put(priority.name(),Integer.parseInt(e.getValue()));
                userRepository.save(CurrentUser.getRole());
            });
            horizontalLayout3.add(tmp);
        }

        page6.add(horizontalLayout3);

        page6.setSizeFull();
        page6.setVisible(false);

    }



    private VerticalLayout getGoogleEvents() {
        VerticalLayout verticalLayout = new VerticalLayout();
        if(CurrentUser.getRole().isConnectGoogle()) {
            try {
                List<Event> eventList = GoogleCalendarConnector.connect(this.userRepository);
                eventList=eventList.stream().filter(event -> event.getTransparency()==null).collect(Collectors.toList());
                Label label = new Label("Betroffene Events:"+eventList.size());
                verticalLayout.add(label);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (GeneralSecurityException e) {
                e.printStackTrace();
            }
        }
        return verticalLayout;
    }

    private Component generateBlockedWeek() {
        HorizontalLayout horizontalLayout = new HorizontalLayout();
        Map<String,LocalTime> map = CurrentUser.getRole().getBlockedTimeSettings();

        for(DayOfWeek day: DayOfWeek.values()){
            TimePicker tmp = new TimePicker(day.name(),map.get(day.name()));
            tmp.addValueChangeListener(e->{
                CurrentUser.getRole().getBlockedTimeSettings().put(day.name(),e.getValue());
                userRepository.save(CurrentUser.getRole());
            });
            tmp.setWidth("100px");
            horizontalLayout.add(tmp);
        }
        return horizontalLayout;
    }

    private Component generateWeek() {
        HorizontalLayout horizontalLayout = new HorizontalLayout();
        Map<String,LocalTime> map = CurrentUser.getRole().getTimeSettings();

        for(DayOfWeek day: DayOfWeek.values()){
            TimePicker tmp = new TimePicker(day.name(),map.get(day.name()));
            tmp.addValueChangeListener(e->{
                CurrentUser.getRole().getBlockedTimeSettings().put(day.name(),e.getValue());
                userRepository.save(CurrentUser.getRole());
            });
            tmp.setWidth("100px");
            horizontalLayout.add(tmp);
        }
        return horizontalLayout;
    }

    private HorizontalLayout generateSizeLayout() {
        HorizontalLayout horizontalLayout = new HorizontalLayout();

        for(Task.Size size : Task.Size.values()){
            Label sizeName = new Label(size.name());
            sizeName.getStyle().set("font-weight","bold");
            switch (size){
                case S:
                    sizeName.getStyle().set("color","chartreuse");
                    break;
                case M:
                    sizeName.getStyle().set("color","darkgrey");
                    break;
                case L:
                    sizeName.getStyle().set("color","orchid");
                    break;
                case XL:
                    sizeName.getStyle().set("color","coral");
                    break;
            }
            TextField sizeValue = new TextField("Value");
            sizeValue.setWidth("100px");
            sizeValue.setTitle("Only numbers");
            ComboBox sizeUnit = new ComboBox("Unit");
            sizeUnit.setItems("Minute(s)","Hour(s)","Day(s) (8h)");
            sizeUnit.setWidth("150px");
            int value = (CurrentUser.getRole().getSizeSettings().get(size.name()));
            if(value%480==0){
                sizeUnit.setValue("Day(s) (8h)");
                sizeValue.setValue((value/480)+"");
            }else if(value%60==0){
                sizeUnit.setValue("Hour(s)");
                sizeValue.setValue((value/60)+"");
            }else{
                sizeUnit.setValue("Minute(s)");
                sizeValue.setValue((value)+"");
            }
            sizeValue.setValueChangeMode(ValueChangeMode.LAZY);
            sizeValue.addValueChangeListener(e->{
                try {
                    int eingabe = Integer.parseInt(e.getValue());
                    sizeValue.setErrorMessage(null);
                    switch ((String) sizeUnit.getValue()){
                        case "Day(s) (8h)":
                            eingabe*=480;
                            break;
                        case "Hour(s)":
                            eingabe*=60;
                            break;
                    }
                    CurrentUser.getRole().getSizeSettings().put(size.toString(),eingabe);
                    userRepository.save( CurrentUser.getRole());
                }catch (NumberFormatException exception){
                    sizeValue.setErrorMessage("Bitte nur Zahlen");
                }
            });
            sizeUnit.addValueChangeListener(e->{
                try {
                    int eingabe = Integer.parseInt(sizeValue.getValue());
                    sizeValue.setErrorMessage(null);
                    switch ((String) e.getValue()){
                        case "Day(s) (8h)":
                            eingabe*=480;
                            break;
                        case "Hour(s)":
                            eingabe*=60;
                            break;
                    }
                    CurrentUser.getRole().getSizeSettings().put(size.toString(),eingabe);
                    userRepository.save( CurrentUser.getRole());
                }catch (NumberFormatException exception){
                    sizeValue.setErrorMessage("Bitte nur Zahlen");
                }
            });
            HorizontalLayout layout = new HorizontalLayout();

            layout.add(sizeName,sizeValue,sizeUnit);
            horizontalLayout.add(layout);
        }
        return horizontalLayout;
    }


    private Button generateDeleteButton(Category category) {
        Button retButton = new Button("Delete");
        retButton.addClassName("deleteBtn");
        retButton.setIcon(VaadinIcon.TRASH.create());

        Dialog dialog = new Dialog();
        dialog.setCloseOnEsc(false);
        dialog.setCloseOnOutsideClick(false);
        Button confirmButton = new Button("Confirm", event -> {
            this.taskRepository.deleteAllByUserAndCategory(CurrentUser.getRole(),category);
            this.categoryRepository.delete(category);
            this.refreshGrid();
            dialog.close();
        });
        confirmButton.addClassName("deleteBtn");
        Button cancelButton = new Button("Cancel", event -> {
            dialog.close();
        });
        Label dialogLabel=new Label("All tasks with this category will be deleted");
        VerticalLayout layout = new VerticalLayout(dialogLabel,new HorizontalLayout(confirmButton,cancelButton));
        dialog.add(layout);
        retButton.addClickListener(e->{
            dialog.open();
        });

        return retButton;
    }

    @Override
    public void refreshGrid() {
        categoryGrid.setItems(this.categoryRepository.findByOwner(CurrentUser.getRole()));
    }

    private Icon generateColorColumn(Category category) {
        Icon icon = VaadinIcon.CIRCLE.create();
        icon.setColor(category.getColor());
        return icon;
    }

}
