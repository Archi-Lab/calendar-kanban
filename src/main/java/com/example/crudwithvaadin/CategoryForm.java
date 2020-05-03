package com.example.crudwithvaadin;

import authentication.CurrentUser;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;

import java.awt.*;


public class CategoryForm extends Div {

    private SettingsView settingsView;
    private CategoryRepository categoryRepository;

    TextField categoryName = new TextField("Bezeichnung");
    ComboBox colorBox;

    private Button save = new Button("Speichern");
    private Button abort = new Button("Abbrechen");

    private Category category = null;

    private VerticalLayout content;

    public CategoryForm(SettingsView settingsView, CategoryRepository categoryRepository) {
        this.settingsView = settingsView;
        this.categoryRepository = categoryRepository;
        buildLayout();
    }

    private void buildLayout() {
        setClassName("product-form");
        content = new VerticalLayout();
        content.setSizeUndefined();
        add(content);
        save.setWidth("100%");
        save.addClickListener(e -> {
            this.setVisible(false);
            if(category==null) {
                category=new Category();
                category.setOwner(CurrentUser.getRole());
            }
            this.category.setBeschreibung(categoryName.getValue());
            String hex="";
            if(colorBox.getValue() instanceof Color) {
                Color color = (Color) colorBox.getValue();
                int r = color.getRed();
                int g = color.getGreen();
                int b = color.getBlue();
                hex = String.format("#%02x%02x%02x", r, g, b);
            }else{
                hex = colorBox.getValue().toString();
            }
            this.category.setColor(hex);
            this.categoryRepository.save(category);
            this.settingsView.refreshGrid();
        });
        abort.addClickListener(e -> {
            this.setVisible(false);
        });
        abort.setWidth("100%");
        colorBox = new ComboBox("Farbkennung");
        colorBox.setItemLabelGenerator(e->{
            String hex="";
            if(e instanceof Color){
                int r = ((Color) e).getRed();
                int g = ((Color) e).getGreen();
                int b = ((Color) e).getBlue();
                hex = String.format("#%02x%02x%02x", r, g, b);
            }
            if(e instanceof String){
                hex=(String)e;
            }
            return hex;
        });
        colorBox.setItems(ColorFactory.getColors());
        content.add(categoryName,colorBox,save,abort);
    }

    public void fillForm(Category category) {
        this.category=category;
        if(category==null){
            this.categoryName.setValue("");
            this.colorBox.setValue("");
        }else{
            this.categoryName.setValue(category.getBeschreibung());
            this.colorBox.setValue(category.getColor());
        }
    }
}
