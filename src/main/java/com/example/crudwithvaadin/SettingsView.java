package com.example.crudwithvaadin;

import authentication.CurrentUser;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;

import java.time.LocalTime;
import java.util.Map;

@CssImport("./styles/style.css")
@Route("Settings")
public class SettingsView extends VerticalLayout {

    private Button saveBtn;
    private Button backBtn;
    private Button addCategory;
    private TextField distractionFactorField;
    private CategoryRepository categoryRepository;
    private UserRepository userRepository;
    private Grid<Category> categoryGrid;
    private CategoryForm categoryForm;

    public SettingsView(CategoryRepository categoryRepository,UserRepository userRepository){
        if(CurrentUser.getRole()!=null) {
            this.categoryRepository = categoryRepository;
            this.userRepository=userRepository;
            this.categoryForm = new CategoryForm(this, categoryRepository);
            buildLayout();
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
        categoryGrid=new Grid<>(Category.class,false);
        categoryGrid.setWidth("100%");
        categoryGrid.addColumn(Category::getBeschreibung)
                .setHeader("Bezeichnung")
                .setSortable(false)
                .setAutoWidth(true);
        categoryGrid.addComponentColumn(this::generateColorColumn);
        categoryGrid.addComponentColumn(this::generateEditButton);
        categoryGrid.addComponentColumn(this::generateDeleteButton);
        categoryGrid.setItems(this.categoryRepository.findByOwner(CurrentUser.getRole()));

        saveBtn=new Button();
        saveBtn.setIcon(VaadinIcon.DISC.create());
        backBtn=new Button();
        backBtn.setIcon(VaadinIcon.ARROW_BACKWARD.create());
        backBtn.addClickListener(e->{
            getUI().get().navigate("Termin");
        });

        addCategory=new Button("Neue Kategorie");
        addCategory.addClickListener(e->{
            this.categoryForm.fillForm(null);
            this.categoryForm.setVisible(true);
        });

        HorizontalLayout headlayout = new HorizontalLayout();
        headlayout.add(saveBtn,backBtn,addCategory,distractionFactorField);

        this.add(headlayout);
        HorizontalLayout mainLayout= new HorizontalLayout();
        mainLayout.add(categoryGrid);
        mainLayout.add(generateSizeLayout());
        mainLayout.setSizeFull();
        this.add(mainLayout);
        this.add(generateWeek());
        this.add(categoryForm);
        this.categoryForm.setVisible(false);
    }

    private Component generateWeek() {
        HorizontalLayout horizontalLayout = new HorizontalLayout();
        Map<String,LocalTime> map = CurrentUser.getRole().getTimeSettings();

        TimePicker monday = new TimePicker("Monday",map.get("Monday"));
        monday.addValueChangeListener(e->{
            CurrentUser.getRole().getTimeSettings().put("Monday",e.getValue());
            userRepository.save(CurrentUser.getRole());
        });
        monday.setWidth("100px");

        TimePicker tuesday = new TimePicker("Tuesday",map.get("Tuesday"));
        tuesday.addValueChangeListener(e->{
            CurrentUser.getRole().getTimeSettings().put("Tuesday",e.getValue());
            userRepository.save(CurrentUser.getRole());
        });
        tuesday.setWidth("100px");

        TimePicker wednesday = new TimePicker("Wednesday",map.get("Wednesday"));
        wednesday.addValueChangeListener(e->{
            CurrentUser.getRole().getTimeSettings().put("Wednesday",e.getValue());
            userRepository.save(CurrentUser.getRole());
        });
        wednesday.setWidth("100px");

        TimePicker thursday = new TimePicker("Thursday",map.get("Thursday"));
        thursday.addValueChangeListener(e->{
            CurrentUser.getRole().getTimeSettings().put("Thursday",e.getValue());
            userRepository.save(CurrentUser.getRole());
        });
        thursday.setWidth("100px");

        TimePicker friday = new TimePicker("Friday",map.get("Friday"));
        friday.addValueChangeListener(e->{
            CurrentUser.getRole().getTimeSettings().put("Friday",e.getValue());
            userRepository.save(CurrentUser.getRole());
        });
        friday.setWidth("100px");

        TimePicker saturday = new TimePicker("Saturday",map.get("Saturday"));
        saturday.addValueChangeListener(e->{
            CurrentUser.getRole().getTimeSettings().put("Saturday",e.getValue());
            userRepository.save(CurrentUser.getRole());
        });
        saturday.setWidth("100px");

        TimePicker sunday = new TimePicker("Sunday", map.get("Sunday"));
        sunday.addValueChangeListener(e->{
            CurrentUser.getRole().getTimeSettings().put("Sunday",e.getValue());
            userRepository.save(CurrentUser.getRole());
        });
        sunday.setWidth("100px");

        horizontalLayout.add(monday,tuesday,wednesday,thursday,friday,saturday,sunday);
        return horizontalLayout;
    }

    private VerticalLayout generateSizeLayout() {
        VerticalLayout verticalLayout = new VerticalLayout();

        for(Task.Size size : Task.Size.values()){
            Label sizeName = new Label(size.name());
            TextField sizeValue = new TextField("Wert");
            sizeValue.setTitle("Bitte nur Zahlen");
            ComboBox sizeUnit = new ComboBox("Einheit");
            sizeUnit.setItems("Minute(n)","Stunde(n)","Tag(e) (8h)");
            int value = (CurrentUser.getRole().getSizeSettings().get(size.name()));
            if(value%480==0){
                sizeUnit.setValue("Tag(e) (8h)");
                sizeValue.setValue((value/480)+"");
            }else if(value%60==0){
                sizeUnit.setValue("Stunde(n)");
                sizeValue.setValue((value/60)+"");
            }else{
                sizeUnit.setValue("Minute(n)");
                sizeValue.setValue((value)+"");
            }
            sizeValue.setValueChangeMode(ValueChangeMode.LAZY);
            sizeValue.addValueChangeListener(e->{
                try {
                    int eingabe = Integer.parseInt(e.getValue());
                    sizeValue.setErrorMessage(null);
                    switch ((String) sizeUnit.getValue()){
                        case "Tag(e) (8h)":
                            eingabe*=480;
                            break;
                        case "Stunde(n)":
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
            verticalLayout.add(layout);
        }
        return verticalLayout;
    }

    private Button generateEditButton(Category category) {
        Button button = new Button("Bearbeiten");
        button.addClickListener(e-> {
            this.categoryForm.fillForm(category);
            this.categoryForm.setVisible(true);
        });
        return button;
    }

    private Button generateDeleteButton(Category category) {
        Button retButton = new Button("Löschen");
        retButton.addClickListener(e->{
            this.categoryRepository.delete(category);
            this.refreshGrid();
        });
        return retButton;
    }

    public void refreshGrid() {
        categoryGrid.setItems(this.categoryRepository.findByOwner(CurrentUser.getRole()));
    }

    private Icon generateColorColumn(Category category) {
        Icon icon = VaadinIcon.CIRCLE.create();
        icon.setColor(category.getColor());
        return icon;
    }


}
