package canban.repository;

import canban.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;

@Transactional
public interface UserRepository extends JpaRepository<User,Long> {

    User findByUsernameEqualsAndPasswordEquals(String username, String password);

}
