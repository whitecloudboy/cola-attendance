package com.cola.attendance.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger/OpenAPI 文档配置。
 * 提供「考勤与排班 API」标题与版本说明，供 /swagger-ui 访问。
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("考勤与排班 API")
                        .version("0.0.1")
                        .description("attendance-backend 接口文档"));
    }
}
