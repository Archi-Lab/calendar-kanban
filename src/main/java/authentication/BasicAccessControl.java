package authentication;

import canban.entity.User;
import canban.repository.UserRepository;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinSession;


/**
 * Default mock implementation of {@link AccessControl}. This implementation
 * accepts any string as a password, and considers the user "admin" as the only
 * administrator.
 */
public class BasicAccessControl implements AccessControl {
private Login login = new Login();
private UserRepository userRepository;

    public UserRepository getUserRepository() {
        return userRepository;
    }

    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
        login.setUserRepository(this.userRepository);
    }

    @Override
    public int signIn(String username, String password) {
        User result = login.login(username, password);
        if(result==null){
            return -1;
        }
        CurrentUser.set(username,result);
        return 1;
    }

    @Override
    public boolean isUserSignedIn() {
        return !CurrentUser.get().isEmpty();
    }

    @Override
    public int isUserInRole(String role) {
        if ("admin".equals(role)) {
            // Only the "admin" user is in the "admin" role
            return 1;
//            return getPrincipalName().equals("admin");
        }

        // All users are in all non-admin roles
        return 0;
    }

    @Override
    public String getPrincipalName() {
        return CurrentUser.get();
    }

    @Override
    public User getRole() {
        return CurrentUser.getRole();
    }

    @Override
    public void signOut() {
        VaadinSession.getCurrent().getSession().invalidate();
        UI.getCurrent().navigate("Login");
    }
}
