package com.example.crudwithvaadin;

import authentication.AccessControl;
import authentication.AccessControlFactory;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

/**
 * UI content when the user is not logged in yet.
 */
@Route("Login")
public class LoginScreen extends FlexLayout {

    private final UserRepository userRepository;
    private AccessControl accessControl;

    public LoginScreen(UserRepository userRepository) {
        this.userRepository=userRepository;
        accessControl = AccessControlFactory.getInstance().createAccessControl(userRepository);
        buildUI();
    }

    private void buildUI() {
        setSizeFull();
        setClassName("login-screen");

        //Deutsches Formular
        LoginI18n.Form form = new LoginI18n.Form();
        form.setPassword("Passwort");
        form.setUsername("Nutzername");
        form.setForgotPassword("Passwort vergessen");
        form.setSubmit("Login");

        LoginI18n.ErrorMessage errorMessage = new LoginI18n.ErrorMessage();
        errorMessage.setMessage("Benutzername oder Passwort nicht korrekt.");
        errorMessage.setTitle("Login Fehlgeschlagen");

        LoginI18n loginI18n = new LoginI18n();
        loginI18n.setForm(form);
        loginI18n.setErrorMessage(errorMessage);

        // login form, centered in the available part of the screen
        LoginForm loginForm = new LoginForm(loginI18n);
        loginForm.addLoginListener(this::login);
        loginForm.addForgotPasswordListener(
                event -> Notification.show("Bitte melden Sie sich beim Admin.")
        );


        // layout to center login form when there is sufficient screen space
        FlexLayout centeringLayout = new FlexLayout();
        centeringLayout.setSizeFull();
        centeringLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        centeringLayout.setAlignItems(Alignment.CENTER);
        centeringLayout.add(loginForm);


        // information text about logging in
        Component loginInformation = buildLoginInformation();

        add(loginInformation);
        add(centeringLayout);
    }

    private Component buildLoginInformation() {
        VerticalLayout loginInformation = new VerticalLayout();
        loginInformation.setClassName("login-information");

        H1 loginInfoHeader = new H1("Willkommen bei dem Kanban-Kalender");
        loginInfoHeader.setWidth("100%");
        loginInfoHeader.setClassName("welcome-h1");
        Span loginInfoText = new Span(
                "Ein Praxisprojekt von John-Bryan Spieker");
        loginInfoText.setWidth("100%");
        loginInformation.add(loginInfoHeader);
        loginInformation.add(loginInfoText);

        return loginInformation;
    }

    private void login(LoginForm.LoginEvent event) {
        if (accessControl.signIn(event.getUsername(), event.getPassword())>=0) {
            //registerAdminViewIfApplicable();
            getUI().get().navigate("Termin");
        } else {
            event.getSource().setError(true);
        }
    }

    private void registerAdminViewIfApplicable() {
        // register the admin view dynamically only for any admin user logged in
        if (accessControl.isUserInRole(AccessControl.ADMIN_ROLE_NAME)>0) {
//            RouteConfiguration.forSessionScope().setRoute(AdminView.VIEW_NAME,
//                    AdminView.class, MainLayout.class);
            // as logout will purge the session route registry, no need to
            // unregister the view on logout
        }
    }

}
