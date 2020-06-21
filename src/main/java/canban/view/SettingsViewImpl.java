package canban.view;

import authentication.CurrentUser;
import canban.controller.SettingsViewController;
import canban.entity.Category;
import canban.entity.Task;
import canban.form.CategoryForm;
import canban.repository.TaskRepository;
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
import canban.repository.CategoryRepository;
import canban.repository.UserRepository;
import com.vaadin.flow.server.StreamResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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


    private static final Logger log = LoggerFactory.getLogger(SettingsViewImpl.class);

    private Button backBtn;
    private Button deleteOldTask = new Button("Delete old tasks");
    private Button addCategory;
    private TextField distractionFactorField;
    private TextField nWeeksField;
    private Grid<Category> categoryGrid;
    private CategoryForm categoryForm;
    private SettingsViewController controller;

    private final Map<Tab, Component> tabsToPages = new HashMap<>();

    private Tab categoryTab = new Tab("Categorys");
    private Tab otherSettingsTab = new Tab("Other settings");
    private Tab sizeSettinsTab = new Tab("Size settings");
    private Tab worktimeTab = new Tab("Worktime");
    private Tab googleTab = new Tab("Google Calendar");
    private Tab columnSettingsTab = new Tab("Column Settings");

    private Div categoryPage = new Div();
    private Div otherSettingsPage = new Div();
    private Div sizeSettingsPage = new Div();
    private Div worktimePage = new Div();
    private Div googlePage = new Div();
    private Div columnSettinsPage = new Div();
    private Tabs tabs;

    public SettingsViewImpl(CategoryRepository categoryRepository, UserRepository userRepository, TaskRepository taskRepository){
        if(CurrentUser.getUser()!=null) {
            this.add(new Label("Username: "+CurrentUser.getUser().getName()));
            this.categoryForm = new CategoryForm(this, categoryRepository);
            this.controller=new SettingsViewController(taskRepository,categoryRepository,userRepository,this);
            this.controller.onEnter();
            setClassName("main-layout");
        }
    }
    @Override
    public void initListener() {
        distractionFactorField.addValueChangeListener(e->{
            try {
                this.controller.setUserDistractionFactor(Integer.parseInt(e.getValue()));
                distractionFactorField.setErrorMessage(null);
            }catch (NumberFormatException exception){
                log.error(exception.getMessage());
                distractionFactorField.setErrorMessage("Bitte nur Zahlen");
            }
        });

        nWeeksField.addValueChangeListener(e->{
            try {
                this.controller.setUserNWeeks(Integer.parseInt(e.getValue()));
                nWeeksField.setErrorMessage(null);
            }catch (NumberFormatException exception){
                log.error(exception.getMessage());
                nWeeksField.setErrorMessage("Bitte nur Zahlen");
            }
        });

        categoryGrid.addItemDoubleClickListener(e->{
            if(e.getItem() != null){
                this.categoryForm.fillForm(e.getItem());
                this.categoryForm.setVisible(true);
            }
        });

        backBtn.addClickListener(e-> getUI().get().navigate("Termin"));

        addCategory.addClickListener(e->{
            this.categoryForm.fillForm(null);
            this.categoryForm.setVisible(true);
        });

        DatePicker datePicker = new DatePicker();
        Dialog deleteOldTaskDialog = new Dialog();
        deleteOldTaskDialog.setCloseOnEsc(false);
        deleteOldTaskDialog.setCloseOnOutsideClick(false);
        Button confirmButton = new Button("Confirm", event -> {
            this.controller.deletOldTasks(datePicker.getValue());
            deleteOldTaskDialog.close();
        });
        Button cancelButton = new Button("Cancel", event -> deleteOldTaskDialog.close());
        Label dialogLabel=new Label("All tasks that are completed and older than the selected value are deleted.");
        VerticalLayout layout = new VerticalLayout(dialogLabel,datePicker,new HorizontalLayout(confirmButton,cancelButton));
        deleteOldTaskDialog.add(layout);

        this.deleteOldTask.addClickListener(e-> deleteOldTaskDialog.open());

        Set<Component> pagesShown = Stream.of(categoryPage)
                .collect(Collectors.toSet());
        tabs.addSelectedChangeListener(event -> {
            pagesShown.forEach(page -> page.setVisible(false));
            pagesShown.clear();
            Component selectedPage = tabsToPages.get(tabs.getSelectedTab());
            selectedPage.setVisible(true);
            pagesShown.add(selectedPage);
        });
    }

    @Override
    public void buildLayout() {
        distractionFactorField = new TextField("Distraction Factor (%)");
        distractionFactorField.setValue(CurrentUser.getUser().getDistractionFactor()+"");
        distractionFactorField.setValueChangeMode(ValueChangeMode.LAZY);

        nWeeksField = new TextField("Week number");
        nWeeksField.setValue(CurrentUser.getUser().getNweeksValue()+"");
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
        categoryGrid.setItems(this.controller.getCategoryByUser());

        backBtn=new Button();
        backBtn.setIcon(VaadinIcon.ARROW_BACKWARD.create());

        addCategory=new Button("New category");

        this.add(categoryForm);
        this.categoryForm.setVisible(false);

        this.configCategoryPage();
        this.configOtherSettingsPage();
        this.configSizeSettingsPage();
        this.configWorktimePage();
        this.configGooglePage();
        this.configColumnSettingsPage();

        tabsToPages.put(categoryTab, categoryPage);
        tabsToPages.put(otherSettingsTab, otherSettingsPage);
        tabsToPages.put(sizeSettinsTab, sizeSettingsPage);
        tabsToPages.put(columnSettingsTab, columnSettinsPage);
        tabsToPages.put(worktimeTab, worktimePage);
        tabsToPages.put(googleTab, googlePage);
        tabs = new Tabs(categoryTab, otherSettingsTab, columnSettingsTab, sizeSettinsTab, worktimeTab, googleTab);
        Div pages = new Div(categoryPage, otherSettingsPage, columnSettinsPage, sizeSettingsPage, worktimePage, googlePage);

        HorizontalLayout headlayout = new HorizontalLayout();
        headlayout.add(backBtn,tabs);
        headlayout.addClassName("centerLayout");

        this.add(headlayout,pages);
    }

    private void configCategoryPage() {
        categoryPage.add(addCategory);
        HorizontalLayout horizontalLayout = new HorizontalLayout(categoryGrid);
        horizontalLayout.setWidth("750px");
        categoryPage.add(horizontalLayout);
        categoryPage.setSizeFull();
    }

    private void configOtherSettingsPage() {
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
                    Dialog dialog = new Dialog();
                    dialog.setCloseOnEsc(false);
                    dialog.setCloseOnOutsideClick(false);
                    Button confirmButton = new Button("Yes", event -> {
                        try {
                            this.controller.importFileUploaded(((MemoryBuffer) e.getSource().getReceiver()),true);
                        } catch (JAXBException jaxbException) {
                            log.error(jaxbException.getMessage());
                            jaxbException.printStackTrace();
                            new Notification("Hochladen fehgeschlagen", 2000).open();
                        }
                        dialog.close();
                    });
                    Button cancelButton = new Button("No", event -> {
                        try {
                            this.controller.importFileUploaded(((MemoryBuffer) e.getSource().getReceiver()),false);
                        } catch (JAXBException jaxbException) {
                            log.error(jaxbException.getMessage());
                            jaxbException.printStackTrace();
                            new Notification("Hochladen fehgeschlagen", 2000).open();
                        }
                        dialog.close();
                    });
                    Label dialogLabel=new Label("Do you want to delete other Tasks?");
                    VerticalLayout layout = new VerticalLayout(dialogLabel,new HorizontalLayout(confirmButton,cancelButton));
                    dialog.add(layout);
                    dialog.open();
            }
        });

        HorizontalLayout horizontalLayout1 = new HorizontalLayout();
        horizontalLayout1.add(distractionFactorField);
        horizontalLayout1.add(nWeeksField);
        horizontalLayout1.add(deleteOldTask);
        horizontalLayout1.add(download);
        horizontalLayout1.add(importData);
        horizontalLayout1.addClassName("centerLayout");
        otherSettingsPage.add(horizontalLayout1);
        otherSettingsPage.setSizeFull();
        otherSettingsPage.setVisible(false);
    }

    private void configSizeSettingsPage() {
        sizeSettingsPage.add(generateSizeLayout());
        sizeSettingsPage.setSizeFull();
        sizeSettingsPage.setVisible(false);
    }

    private void configWorktimePage() {
        worktimePage.add(new Label("Daily worktime"));
        worktimePage.add(generateWeek());
        VerticalLayout breakLayout = new VerticalLayout();
        breakLayout.setHeight("25px");
        worktimePage.add(breakLayout);
        worktimePage.add(new Label("Daily blocked hour"));
        worktimePage.add(generateBlockedWeek());
        VerticalLayout breakLayout2 = new VerticalLayout();
        breakLayout2.setHeight("25px");
        worktimePage.add(breakLayout2);
        worktimePage.setSizeFull();
        worktimePage.setVisible(false);
    }

    private void configGooglePage() {
        Button btn = new Button("Add google calendar");
        btn.setIcon(VaadinIcon.GOOGLE_PLUS.create());
        btn.addClickListener(e->{
            if (!CurrentUser.getUser().isConnectGoogle()) {
                try {
                    this.controller.googleCalendarConnect();
                    googlePage.add(getGoogleEvents());
                } catch (IOException | GeneralSecurityException ioException) {
                    //Logger.getAnonymousLogger().log(Level.ALL,ioException.getMessage());
                    log.error(ioException.getMessage());
                    ioException.printStackTrace();
                }
            }
        });
        googlePage.add(btn);
        Checkbox isConnected = new Checkbox("Connected with Google Calendar");
        isConnected.setEnabled(false);
        isConnected.setValue(CurrentUser.getUser().isConnectGoogle());
        googlePage.add(new VerticalLayout(isConnected));
        if (CurrentUser.getUser().isConnectGoogle()) {
            googlePage.add(getGoogleEvents());
        }
        googlePage.setSizeFull();
        googlePage.setVisible(false);
    }

    private void configColumnSettingsPage() {
        HorizontalLayout horizontalLayout2 = new HorizontalLayout(new Label("30px = +14 Tasks"),new Label("50px = 9-13 Tasks"),new Label("74px = 7-9 Tasks"),new Label("100px = 5-7 Tasks"));
        horizontalLayout2.setMargin(true);
        horizontalLayout2.setSpacing(true);
        columnSettinsPage.add(horizontalLayout2);
        HorizontalLayout horizontalLayout3 = new HorizontalLayout();
        horizontalLayout3.setMargin(true);
        horizontalLayout3.setSpacing(true);

        for(Task.Priority priority: Task.Priority.values()){
            TextField tmp = new TextField(priority.getBezeichnung());
            if(priority.equals(Task.Priority.NEXTNWEEK)){
                tmp = new TextField(priority.getBezeichnung().replace("$",CurrentUser.getUser().getNweeksValue()+""));
            }
            tmp.setWidth("100px");
            tmp.setValueChangeMode(ValueChangeMode.LAZY);
            tmp.setValue(""+CurrentUser.getUser().getPriorityHeightSettings().get(priority.name()));
            tmp.addValueChangeListener(e-> this.controller.setPriorityHeightSettings(priority.name(),Integer.parseInt(e.getValue())));
            horizontalLayout3.add(tmp);
        }

        columnSettinsPage.add(horizontalLayout3);

        columnSettinsPage.setSizeFull();
        columnSettinsPage.setVisible(false);

    }

    private VerticalLayout getGoogleEvents() {
        VerticalLayout verticalLayout = new VerticalLayout();
        if(CurrentUser.getUser().isConnectGoogle()) {
            try {
                Collection<Event> eventList = this.controller.googleCalendarConnect();
                eventList=eventList.stream().filter(event -> event.getTransparency()==null).collect(Collectors.toList());
                Label label = new Label("Betroffene Events:"+eventList.size());
                verticalLayout.add(label);
            } catch (IOException | GeneralSecurityException e) {
                log.error(e.getMessage());
                e.printStackTrace();
            }
        }
        return verticalLayout;
    }

    private Component generateBlockedWeek() {
        HorizontalLayout horizontalLayout = new HorizontalLayout();
        Map<String,LocalTime> map = CurrentUser.getUser().getBlockedTimeSettings();

        for(DayOfWeek day: DayOfWeek.values()){
            TimePicker tmp = new TimePicker(day.name(),map.get(day.name()));
            tmp.addValueChangeListener(e-> this.controller.setBlockedTimeSettings(day.name(),e.getValue()));
            tmp.setWidth("100px");
            horizontalLayout.add(tmp);
        }
        return horizontalLayout;
    }

    private Component generateWeek() {
        HorizontalLayout horizontalLayout = new HorizontalLayout();
        Map<String,LocalTime> map = CurrentUser.getUser().getTimeSettings();

        for(DayOfWeek day: DayOfWeek.values()){
            TimePicker tmp = new TimePicker(day.name(),map.get(day.name()));
            tmp.addValueChangeListener(e-> this.controller.setTimeSettings(day.name(),e.getValue()));
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
            ComboBox<String> sizeUnit = new ComboBox<>("Unit");
            sizeUnit.setItems("Minute(s)","Hour(s)","Day(s) (8h)");
            sizeUnit.setWidth("150px");
            int value = (CurrentUser.getUser().getSizeSettings().get(size.name()));
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
                    this.controller.setSizeSettings(size.toString(),sizeValue.getValue(),sizeUnit.getValue());
                    sizeValue.setErrorMessage(null);
                }catch (NumberFormatException exception){
                    log.error(exception.getMessage());
                    sizeValue.setErrorMessage("Bitte nur Zahlen");
                }
            });
            sizeUnit.addValueChangeListener(e->{
                try {
                    this.controller.setSizeSettings(size.toString(),sizeValue.getValue(),sizeUnit.getValue());
                    sizeValue.setErrorMessage(null);
                }catch (NumberFormatException exception){
                    log.error(exception.getMessage());
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
            this.controller.deletCategory(category);
            dialog.close();
        });
        confirmButton.addClassName("deleteBtn");
        Button cancelButton = new Button("Cancel", event -> dialog.close());
        Label dialogLabel=new Label("All tasks with this category will be deleted");
        VerticalLayout layout = new VerticalLayout(dialogLabel,new HorizontalLayout(confirmButton,cancelButton));
        dialog.add(layout);
        retButton.addClickListener(e-> dialog.open());

        return retButton;
    }

    @Override
    public void refreshGrid() {
        categoryGrid.setItems(this.controller.findByOwner());
    }

    private Icon generateColorColumn(Category category) {
        Icon icon = VaadinIcon.CIRCLE.create();
        icon.setColor(category.getColor());
        return icon;
    }

}
