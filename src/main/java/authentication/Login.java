package authentication;


import canban.entity.User;
import canban.repository.UserRepository;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.Serializable;

public class Login implements Serializable {

    private UserRepository userRepository;

    public User login(String user, String password) {
        if(userRepository!=null){
            User returnUser = userRepository.findByUsernameEqualsAndPasswordEquals(user, DigestUtils.md5Hex(password));
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
