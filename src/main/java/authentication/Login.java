package authentication;


import canban.entity.User;
import canban.repository.UserRepository;

import java.io.Serializable;

public class Login implements Serializable {

    private UserRepository userRepository;

    public User login(String user, String password) {
        if(userRepository!=null){
            User returnUser = userRepository.findByUsernameEqualsAndPasswordEquals(user,password);
            if(returnUser!=null){
                return returnUser;
            }
        }
            return null;
    }

    public UserRepository getUserRepository() {
        return userRepository;
    }

    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
}
