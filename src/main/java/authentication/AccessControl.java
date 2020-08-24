package authentication;

import canban.entity.User;
import canban.repository.UserRepository;

import java.io.Serializable;

/**
 * Simple interface for authentication and authorization checks.
 */
public interface AccessControl extends Serializable {

    int signIn(String username, String password);

    boolean isUserSignedIn();

    User getUser();

    void signOut();

    void setUserRepository(UserRepository userRepository);
}
