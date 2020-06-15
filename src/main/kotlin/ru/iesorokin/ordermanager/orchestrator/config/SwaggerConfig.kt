package ru.iesorokin.ordermanager.orchestrator.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.bind.annotation.RestController
import springfox.documentation.builders.ApiInfoBuilder
import springfox.documentation.builders.PathSelectors
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.service.ApiInfo
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger2.annotations.EnableSwagger2

@Configuration
@EnableSwagger2
class ConfigSwagger {
    @Value("\${build.version}")
    private val buildVersion: String? = null

    @Bean
    fun swaggerConfig(): Docket {
        return Docket(DocumentationType.SWAGGER_2)
                .apiInfo(
                        ApiInfoBuilder()
                                .title(" Orchestrator")
                                .version(buildVersion)
                                .contact(ApiInfo.DEFAULT_CONTACT)
                                .build())
                .select()
                .apis(RequestHandlerSelectors.withClassAnnotation(RestController::class.java))
                .paths(PathSelectors.any())
                .build()
    }
}