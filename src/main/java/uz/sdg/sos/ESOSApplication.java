package uz.sdg.sos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
import uz.sdg.sos.base.BaseRepositoryImpl;
import uz.sdg.sos.component.InitConfig;

@SpringBootApplication
@EnableJpaRepositories(repositoryBaseClass = BaseRepositoryImpl.class)
@EnableSwagger2
@EnableScheduling
public class ESOSApplication {
    public static void main(String[] args) {
      SpringApplication.run(ESOSApplication.class, args);
        System.out.println("APPLICATION STARTED **********************************");

    }
}
