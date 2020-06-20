package canban.form;

import authentication.CurrentUser;
import canban.component.ColorFactory;
import canban.view.SettingsView;
import canban.entity.Category;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import canban.repository.CategoryRepository;

import java.awt.*;


public class CategoryForm extends Div {

    private SettingsView view;
    private CategoryRepository categoryRepository;

    private TextField categoryName = new TextField("Title");
    private ComboBox colorBox;
    private Icon icon = VaadinIcon.CIRCLE.create();

    private Button save = new Button("Save");
    private Button abort = new Button("Abort");

    private Category category = null;

    public CategoryForm(SettingsView view, CategoryRepository categoryRepository) {
        this.view = view;
        this.categoryRepository = categoryRepository;
        buildLayout();
    }

    private void buildLayout() {
        setClassName("product-form");
        VerticalLayout content = new VerticalLayout();
        content.setSizeUndefined();
        add(content);
        save.setWidth("100%");
        save.addClickListener(e -> {
            if (categoryName.getValue().trim().length()==0||!(colorBox.getValue() instanceof Color)){
                new Notification("Werte nicht komplett befüllt", 2000).open();
                return;
            }
            this.setVisible(false);
            if(category==null) {
                category=new Category();
                category.setOwner(CurrentUser.getUser());
            }
            this.category.setBeschreibung(categoryName.getValue());
            String hex;
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
            this.view.refreshGrid();
        });
        abort.addClickListener(e -> this.setVisible(false));
        abort.setWidth("100%");
        colorBox = new ComboBox<>("Color");
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
        colorBox.addValueChangeListener(e->{
            if(e.getValue() instanceof Color){
                int r = ((Color) e.getValue()).getRed();
                int g = ((Color) e.getValue()).getGreen();
                int b = ((Color) e.getValue()).getBlue();
                String hex = String.format("#%02x%02x%02x", r, g, b);
                icon.setColor(hex);
            }
        });
        categoryName.setRequired(true);
        colorBox.setRequired(true);
        content.add(categoryName,colorBox,icon,save,abort);
    }

    public void fillForm(Category category) {
        this.category=category;
        if(category==null){
            this.categoryName.setValue("");
            this.colorBox.setValue("");
            icon.setColor("#000000");
        }else{
            this.categoryName.setValue(category.getBeschreibung());
            this.colorBox.setValue(category.getColor());
            icon.setColor(this.category.getColor());
        }
    }

}
