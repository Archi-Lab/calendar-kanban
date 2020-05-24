package com.example.crudwithvaadin.view;

import authentication.CurrentUser;
import com.example.crudwithvaadin.component.ColumnGrid;
import com.example.crudwithvaadin.entity.BlockedTask;
import com.example.crudwithvaadin.entity.Category;
import com.example.crudwithvaadin.entity.Task;
import com.example.crudwithvaadin.entity.User;
import com.example.crudwithvaadin.form.BlockedTaskForm;
import com.example.crudwithvaadin.form.CategoryForm;
import com.example.crudwithvaadin.repository.BlockedTaskRepository;
import com.example.crudwithvaadin.repository.TaskRepository;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.data.binder.RequiredFieldConfiguratorUtil;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import com.example.crudwithvaadin.repository.CategoryRepository;
import com.example.crudwithvaadin.repository.UserRepository;
import jdk.nashorn.internal.ir.Block;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@CssImport("./styles/style.css")
@Route("Settings")
public class SettingsView extends VerticalLayout {

    private TaskRepository taskRepository;
    private Button backBtn;
    private Button createDump = new Button("Export");
    private Button deleteOldTask = new Button("Delete old tasks");
    private Button addCategory;
    private Button addBlockedTask;
    private TextField distractionFactorField;
    private TextField nWeeksField;
    private CategoryRepository categoryRepository;
    private UserRepository userRepository;
    private BlockedTaskRepository blockedTaskRepository;
    private Grid<Category> categoryGrid;
    private Grid<BlockedTask> blockedTaskGrid;
    private CategoryForm categoryForm;
    private BlockedTaskForm blockedTaskForm;

    public SettingsView(CategoryRepository categoryRepository, UserRepository userRepository, BlockedTaskRepository blockedTaskRepository, TaskRepository taskRepository){
        if(CurrentUser.getRole()!=null) {
            this.add(new Label("Username: "+CurrentUser.getRole().getName()));
            this.categoryRepository = categoryRepository;
            this.userRepository=userRepository;
            this.blockedTaskRepository=blockedTaskRepository;
            this.taskRepository=taskRepository;
            this.categoryForm = new CategoryForm(this, categoryRepository);
            this.blockedTaskForm = new BlockedTaskForm(this,blockedTaskRepository);
            this.blockedTaskRepository.deleteAllByUserAndDateBefore(CurrentUser.getRole(), LocalDate.now());
            buildLayout();
            setClassName("main-layout");
        }
    }

    private void buildLayout() {
        distractionFactorField = new TextField("Distraction Factor (%)");
        distractionFactorField.setValue(CurrentUser.getRole().getDistractionFactor()+"");
        distractionFactorField.setValueChangeMode(ValueChangeMode.LAZY);
        distractionFactorField.addValueChangeListener(e->{
        try {
            CurrentUser.getRole().setDistractionFactor(Integer.parseInt(e.getValue()));
            distractionFactorField.setErrorMessage(null);
            userRepository.save(CurrentUser.getRole());
        }catch (NumberFormatException exception){
            distractionFactorField.setErrorMessage("Bitte nur Zahlen");
        }
        });
        nWeeksField = new TextField("Week number");
        nWeeksField.setValue(CurrentUser.getRole().getNweeksValue()+"");
        nWeeksField.setValueChangeMode(ValueChangeMode.LAZY);
        nWeeksField.addValueChangeListener(e->{
            try {
                CurrentUser.getRole().setNweeksValue(Integer.parseInt(e.getValue()));
                nWeeksField.setErrorMessage(null);
                userRepository.save(CurrentUser.getRole());
            }catch (NumberFormatException exception){
                nWeeksField.setErrorMessage("Bitte nur Zahlen");
            }
        });

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
        categoryGrid.addItemDoubleClickListener(e->{
           if(e.getItem() instanceof Category){
               this.categoryForm.fillForm(e.getItem());
               this.categoryForm.setVisible(true);
           }
        });

        blockedTaskGrid=new Grid<>(BlockedTask.class,false);
        blockedTaskGrid.setSizeFull();
        blockedTaskGrid.setHeight("310px");
        blockedTaskGrid.addColumn(BlockedTask::getBeschreibung)
                .setHeader("Title")
                .setSortable(false)
                .setAutoWidth(true);
        blockedTaskGrid.addColumn(BlockedTask::getDate)
                .setHeader("Date")
                .setSortable(false)
                .setAutoWidth(true);
        blockedTaskGrid.addColumn(BlockedTask::getStartTime)
                .setHeader("Start")
                .setSortable(false)
                .setAutoWidth(true);
        blockedTaskGrid.addColumn(BlockedTask::getEndTime)
                .setHeader("End")
                .setSortable(false)
                .setAutoWidth(true);
        blockedTaskGrid.setItems(this.blockedTaskRepository.findByUser(CurrentUser.getRole()));
        blockedTaskGrid.addItemDoubleClickListener(e->{
            if(e.getItem() instanceof BlockedTask){
                this.blockedTaskForm.fillForm(e.getItem());
                this.blockedTaskForm.setVisible(true);
            }
        });


        backBtn=new Button();
        backBtn.setIcon(VaadinIcon.ARROW_BACKWARD.create());
        backBtn.addClickListener(e->{
            getUI().get().navigate("Termin");
        });

        addCategory=new Button("New category");
        addCategory.addClickListener(e->{
            this.categoryForm.fillForm(null);
            this.categoryForm.setVisible(true);
        });
        addBlockedTask=new Button("New blocked task");
        addBlockedTask.addClickListener(e->{
            this.blockedTaskForm.fillForm(null);
            this.blockedTaskForm.setVisible(true);
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

        this.add(categoryForm);
        this.add(blockedTaskForm);
        this.categoryForm.setVisible(false);
        this.blockedTaskForm.setVisible(false);
        Tab tab1 = new Tab("Categorys");
        Div page1 = new Div();
        //page1.setText("Page#1");
        page1.add(addCategory);
        HorizontalLayout horizontalLayout = new HorizontalLayout(categoryGrid);
        horizontalLayout.setWidth("750px");
        page1.add(horizontalLayout);
        page1.setSizeFull();

        Tab tab2 = new Tab("Other settings");
        Div page2 = new Div();
        HorizontalLayout horizontalLayout1 = new HorizontalLayout();
        horizontalLayout1.add(distractionFactorField);
        horizontalLayout1.add(nWeeksField);
        horizontalLayout1.add(deleteOldTask);
        horizontalLayout1.add(createDump);
        horizontalLayout1.addClassName("centerLayout");
        page2.add(horizontalLayout1);
        page2.setSizeFull();
        page2.setVisible(false);

        Tab tab3 = new Tab("Size settings");
        Div page3 = new Div();
        page3.add(generateSizeLayout());
        page3.setSizeFull();
        page3.setVisible(false);

        Tab tab4 = new Tab("Worktime");
        Div page4 = new Div();
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
        page4.add(addBlockedTask);
        page4.add(blockedTaskGrid);
        page4.setSizeFull();
        page4.setVisible(false);

        Map<Tab, Component> tabsToPages = new HashMap<>();
        tabsToPages.put(tab1, page1);
        tabsToPages.put(tab2, page2);
        tabsToPages.put(tab3, page3);
        tabsToPages.put(tab4, page4);
        Tabs tabs = new Tabs(tab1, tab2, tab3, tab4);
        Div pages = new Div(page1, page2, page3, page4);
        Set<Component> pagesShown = Stream.of(page1)
                .collect(Collectors.toSet());

        tabs.addSelectedChangeListener(event -> {
            pagesShown.forEach(page -> page.setVisible(false));
            pagesShown.clear();
            Component selectedPage = tabsToPages.get(tabs.getSelectedTab());
            selectedPage.setVisible(true);
            pagesShown.add(selectedPage);
        });
        HorizontalLayout headlayout = new HorizontalLayout();
        headlayout.add(backBtn,tabs);
        headlayout.addClassName("centerLayout");

        this.add(headlayout,pages);
    }

    private Component generateBlockedWeek() {
        HorizontalLayout horizontalLayout = new HorizontalLayout();
        Map<String,LocalTime> map = CurrentUser.getRole().getBlockedTimeSettings();

        TimePicker monday = new TimePicker("MONDAY",map.get("MONDAY"));
        monday.addValueChangeListener(e->{
            CurrentUser.getRole().getBlockedTimeSettings().put("MONDAY",e.getValue());
            userRepository.save(CurrentUser.getRole());
        });
        monday.setWidth("100px");

        TimePicker tuesday = new TimePicker("TUESDAY",map.get("TUESDAY"));
        tuesday.addValueChangeListener(e->{
            CurrentUser.getRole().getBlockedTimeSettings().put("TUESDAY",e.getValue());
            userRepository.save(CurrentUser.getRole());
        });
        tuesday.setWidth("100px");

        TimePicker wednesday = new TimePicker("WEDNESDAY",map.get("WEDNESDAY"));
        wednesday.addValueChangeListener(e->{
            CurrentUser.getRole().getBlockedTimeSettings().put("WEDNESDAY",e.getValue());
            userRepository.save(CurrentUser.getRole());
        });
        wednesday.setWidth("100px");

        TimePicker thursday = new TimePicker("THURSDAY",map.get("THURSDAY"));
        thursday.addValueChangeListener(e->{
            CurrentUser.getRole().getBlockedTimeSettings().put("THURSDAY",e.getValue());
            userRepository.save(CurrentUser.getRole());
        });
        thursday.setWidth("100px");

        TimePicker friday = new TimePicker("FRIDAY",map.get("FRIDAY"));
        friday.addValueChangeListener(e->{
            CurrentUser.getRole().getBlockedTimeSettings().put("FRIDAY",e.getValue());
            userRepository.save(CurrentUser.getRole());
        });
        friday.setWidth("100px");

        TimePicker saturday = new TimePicker("SATURDAY",map.get("SATURDAY"));
        saturday.addValueChangeListener(e->{
            CurrentUser.getRole().getBlockedTimeSettings().put("SATURDAY",e.getValue());
            userRepository.save(CurrentUser.getRole());
        });
        saturday.setWidth("100px");

        TimePicker sunday = new TimePicker("SUNDAY", map.get("SUNDAY"));
        sunday.addValueChangeListener(e->{
            CurrentUser.getRole().getBlockedTimeSettings().put("SUNDAY",e.getValue());
            userRepository.save(CurrentUser.getRole());
        });
        sunday.setWidth("100px");

        horizontalLayout.add(monday,tuesday,wednesday,thursday,friday,saturday,sunday);
        return horizontalLayout;
    }

    private Component generateWeek() {
        HorizontalLayout horizontalLayout = new HorizontalLayout();
        Map<String,LocalTime> map = CurrentUser.getRole().getTimeSettings();

        TimePicker monday = new TimePicker("MONDAY",map.get("MONDAY"));
        monday.addValueChangeListener(e->{
            CurrentUser.getRole().getTimeSettings().put("MONDAY",e.getValue());
            userRepository.save(CurrentUser.getRole());
        });
        monday.setWidth("100px");

        TimePicker tuesday = new TimePicker("TUESDAY",map.get("TUESDAY"));
        tuesday.addValueChangeListener(e->{
            CurrentUser.getRole().getTimeSettings().put("TUESDAY",e.getValue());
            userRepository.save(CurrentUser.getRole());
        });
        tuesday.setWidth("100px");

        TimePicker wednesday = new TimePicker("WEDNESDAY",map.get("WEDNESDAY"));
        wednesday.addValueChangeListener(e->{
            CurrentUser.getRole().getTimeSettings().put("WEDNESDAY",e.getValue());
            userRepository.save(CurrentUser.getRole());
        });
        wednesday.setWidth("100px");

        TimePicker thursday = new TimePicker("THURSDAY",map.get("THURSDAY"));
        thursday.addValueChangeListener(e->{
            CurrentUser.getRole().getTimeSettings().put("THURSDAY",e.getValue());
            userRepository.save(CurrentUser.getRole());
        });
        thursday.setWidth("100px");

        TimePicker friday = new TimePicker("FRIDAY",map.get("FRIDAY"));
        friday.addValueChangeListener(e->{
            CurrentUser.getRole().getTimeSettings().put("FRIDAY",e.getValue());
            userRepository.save(CurrentUser.getRole());
        });
        friday.setWidth("100px");

        TimePicker saturday = new TimePicker("SATURDAY",map.get("SATURDAY"));
        saturday.addValueChangeListener(e->{
            CurrentUser.getRole().getTimeSettings().put("SATURDAY",e.getValue());
            userRepository.save(CurrentUser.getRole());
        });
        saturday.setWidth("100px");

        TimePicker sunday = new TimePicker("SUNDAY", map.get("SUNDAY"));
        sunday.addValueChangeListener(e->{
            CurrentUser.getRole().getTimeSettings().put("SUNDAY",e.getValue());
            userRepository.save(CurrentUser.getRole());
        });
        sunday.setWidth("100px");

        horizontalLayout.add(monday,tuesday,wednesday,thursday,friday,saturday,sunday);
        return horizontalLayout;
    }

    private HorizontalLayout generateSizeLayout() {
        HorizontalLayout horizontalLayout = new HorizontalLayout();

        for(Task.Size size : Task.Size.values()){
            Label sizeName = new Label(size.name());
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
                    User user = userRepository.save( CurrentUser.getRole());
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
                    User user = userRepository.save( CurrentUser.getRole());
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

    public void refreshGrid() {
        categoryGrid.setItems(this.categoryRepository.findByOwner(CurrentUser.getRole()));
        this.blockedTaskRepository.deleteAllByUserAndDateBefore(CurrentUser.getRole(), LocalDate.now());
        blockedTaskGrid.setItems(this.blockedTaskRepository.findByUser(CurrentUser.getRole()));
    }

    private Icon generateColorColumn(Category category) {
        Icon icon = VaadinIcon.CIRCLE.create();
        icon.setColor(category.getColor());
        return icon;
    }


}
