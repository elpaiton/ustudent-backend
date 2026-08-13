package co.edu.usta.ustudent;

import org.springframework.boot.SpringApplication;

public class TestUstudentBackendApplication {

    public static void main(String[] args) {
        SpringApplication.from(UstudentBackendApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
