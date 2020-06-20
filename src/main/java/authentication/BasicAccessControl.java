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
    public User getUser() {
        return CurrentUser.getUser();
    }

    @Override
    public void signOut() {
        VaadinSession.getCurrent().getSession().invalidate();
        UI.getCurrent().navigate("Login");
    }
}
