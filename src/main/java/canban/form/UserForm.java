package canban.form;

import canban.entity.User;
import canban.view.AdminView;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import canban.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserForm extends Div {

    private static final Logger log = LoggerFactory.getLogger(UserForm.class);

    private AdminView adminView;
    private UserRepository userRepository;

    private TextField username = new TextField("Username");
    private PasswordField passwordField = new PasswordField("Password");
    private PasswordField repeatPasswordField = new PasswordField("Repeat password");

    private Button save = new Button("Save");
    private Button abort = new Button("Abort");
    private ComboBox box = new ComboBox("Right");
    private User user;

    public UserForm(AdminView adminView, UserRepository userRepository) {
        this.adminView = adminView;
        this.userRepository = userRepository;
        buildLayout();
    }

    private void buildLayout() {
        setClassName("product-form");
        VerticalLayout content = new VerticalLayout();
        content.setSizeUndefined();
        add(content);
        save.setWidth("100%");
        save.addClickListener(e->{
            if (!passwordField.getValue().equals(repeatPasswordField.getValue())){
                new Notification("Passwörter stimmen nicht überein", 2000).open();
                return;
            }
            createUser();
        });
        abort.addClickListener(e-> this.setVisible(false));
        abort.setWidth("100%");
        box.setItems(User.Rolle.values());
        username.setRequired(true);
        passwordField.setRequired(true);
        repeatPasswordField.setRequired(true);
        content.add(username,passwordField,repeatPasswordField,box,save,abort);
    }

    private void createUser() {
        if((user == null && (passwordField.getValue().trim().length()==0||repeatPasswordField.getValue().trim().length()==0))||username.getValue().trim().length()==0){
            new Notification("Werte nicht komplett befüllt", 2000).open();
            return;
        }
        if(user ==null) {
            user = new User();
        }
        user.setName(this.username.getValue());
        if(passwordField.getValue().length()>0) {
            user.setPassword(passwordField.getValue());
        }
        user.setRolle((User.Rolle) box.getValue());
        this.userRepository.save(user);
        this.setVisible(false);
        this.adminView.refreshGrid();
    }

    public void fillLayout(User user){
        passwordField.setValue("");
        repeatPasswordField.setValue("");
        if(user==null){
            this.user=null;
            username.setValue("");
            box.setValue(User.Rolle.NUTZER);
        }else{
            this.user=user;
            username.setValue(user.getName());
            box.setValue(user.getRolle());
        }
    }
}
