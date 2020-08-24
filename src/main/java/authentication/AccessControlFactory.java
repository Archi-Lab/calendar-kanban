package authentication;

import canban.repository.UserRepository;

public class AccessControlFactory {
    private static final AccessControlFactory INSTANCE = new AccessControlFactory();
    private final AccessControl accessControl = new BasicAccessControl();

    private AccessControlFactory() {
    }

    public static AccessControlFactory getInstance() {
        return INSTANCE;
    }

    public AccessControl createAccessControl(UserRepository userRepository) {
        accessControl.setUserRepository(userRepository);
        return accessControl;
    }
    public AccessControl createAccessControl() {
        return accessControl;
    }
}
