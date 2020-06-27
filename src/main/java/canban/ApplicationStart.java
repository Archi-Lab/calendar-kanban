package canban;

import canban.entity.Category;
import canban.entity.User;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import canban.repository.UserRepository;

@SpringBootApplication
public class ApplicationStart {

    private static final Logger log = LoggerFactory.getLogger(ApplicationStart.class);

    public static void main(String[] args) {
        SpringApplication.run(ApplicationStart.class);
    }

    @Bean
    public CommandLineRunner initData(UserRepository userRepository) {
        return (args) -> {

            User user = userRepository.findByUsernameEqualsAndPasswordEquals("test", DigestUtils.md5Hex("test"));
            if(user==null){
                log.info("INIT USER");
                log.info("-------------------------------");

                user = new User("test","test", User.Rolle.NUTZER);
                userRepository.save(user); Category bachelor = new Category("Bachelor");

            }
            User user2 = userRepository.findByUsernameEqualsAndPasswordEquals("admin",DigestUtils.md5Hex("admin"));
            if (user2==null){
                log.info("INIT ADMIN");
                log.info("-------------------------------");
                user2 = new User("admin","admin", User.Rolle.ADMIN);
                userRepository.save(user2);
            }

            log.info("Gestartet");
        };
    }

}