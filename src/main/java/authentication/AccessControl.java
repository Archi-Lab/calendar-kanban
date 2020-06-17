package authentication;

import canban.entity.User;
import canban.repository.UserRepository;

import java.io.Serializable;

/**
 * Simple interface for authentication and authorization checks.
 */
public interface AccessControl extends Serializable {

    String ADMIN_ROLE_NAME = "admin";
    String ADMIN_USERNAME = "admin";

    int signIn(String username, String password);

    boolean isUserSignedIn();

    int isUserInRole(String role);

    String getPrincipalName();

    User getRole();

    void signOut();

    void setUserRepository(UserRepository userRepository);
}
