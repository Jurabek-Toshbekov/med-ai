package uz.sdg.sos.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.ApiKeyVehicle;
import springfox.documentation.swagger.web.SecurityConfiguration;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;



@Configuration
@EnableSwagger2
public class SwaggerConfig {

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
//                .host("52.53.242.81:7088")
//                .host("localhost:7088")
                .host("3ed4-185-139-137-95.ngrok-free.app")
                .select()
                // qaysi urllar olishi kerak
                .apis(RequestHandlerSelectors.basePackage("uz.sdg.sos.controller"))
                .paths(PathSelectors.any())
                .build()
                .securitySchemes(Collections.singletonList(apiKey()))
                .securityContexts(Collections.singletonList(securityContext())).pathMapping("/")
                .useDefaultResponseMessages(false)
                .apiInfo(apiInfo());
    }

//    private OAuth apiKey() {
//        return new OAuth(
//                "oauth2schema",
//                new ArrayList<>(),
//                Arrays.asList(new ResourceOwnerPasswordCredentialsGrant("/japan/edu/api/auth/login/web"))
//        );
//    }

    private ApiKey apiKey() {
        return new ApiKey("JWT", "Authorization", "header"); // JWT tokenni header orqali yuborish uchun
    }

    private SecurityContext securityContext() {
        return SecurityContext.builder()
                .securityReferences(defaultAuth())
                .build();
    }


    private ApiInfo apiInfo() {
        return new ApiInfo(
                "My REST API",
                "Some custom description of API.",
                "1.0",
                "This project does not have Terms of service",
                new Contact("Jurabek Toshbekov", "https://www.linkedin.com/in/jurabek-toshbekov/", "jurabek99899@gmail.com"),
                "This project does not have License of API",
                "This project does not have API license URL",
                Collections.emptyList());
    }


//    private List<SecurityReference> defaultAuth() {
//        AuthorizationScope[] authorizationScopes = new AuthorizationScope[2];
//        authorizationScopes[0] = new AuthorizationScope("write", "write all");
//        authorizationScopes[1] = new AuthorizationScope("read", "read all");
//        return Arrays.asList(new SecurityReference("oauth2schema", authorizationScopes));
//    }

    private List<SecurityReference> defaultAuth() {
        AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
        authorizationScopes[0] = new AuthorizationScope("global", "accessEverything");
        return Collections.singletonList(new SecurityReference("JWT", authorizationScopes));
    }
    @Bean
    public SecurityConfiguration securityInfo() {
        return new SecurityConfiguration("web", "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970", "", "", "", ApiKeyVehicle.HEADER, "", " ");
    }


}


