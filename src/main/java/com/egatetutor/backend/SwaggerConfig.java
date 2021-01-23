package com.egatetutor.backend;


import com.google.common.base.Predicate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import static com.google.common.base.Predicates.or;
import static springfox.documentation.builders.PathSelectors.regex;

@Configuration
@EnableSwagger2
public class SwaggerConfig {

    @Bean
    public Docket postsApi() {
        return new Docket(DocumentationType.SWAGGER_2).select()
                .apis(RequestHandlerSelectors.basePackage("com.egatetutor.backend"))
                .paths(postPaths()).build();
    }

    private Predicate<String> postPaths() {
        return or(regex("/coursesDetail.*"),
                regex("/users.*"), regex("/payment.*"),
                regex("/questionLayout.*"),
                regex("/coursesOffered.*"),
                regex("/subscriptionDetail.*"),
                regex("/reportDetail.*"),
                regex("/reportOverall.*"),
                regex("/download.*"),
                regex("/books.*"), regex("/banner.*"),
                regex("/questionBank.*"),
                regex("/coursesDetailForQB.*")
                );
    }
    private ApiInfo apiInfo() {
        return new ApiInfoBuilder().title("eGATE Tutor")
                .description("eGATE Tutor API reference for developer")
                .termsOfServiceUrl("http://www.egatetutor.in")
                .contact("egatetutor@gmail.com").license("eGATE Tutor License")
                .licenseUrl("support@egatetutor.in").version("1.0").build();
    }

}