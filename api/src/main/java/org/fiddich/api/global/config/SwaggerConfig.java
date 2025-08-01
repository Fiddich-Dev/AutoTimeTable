package org.fiddich.api.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI api() {
        // 개발 환경에서는 로컬 호스트를, 운영 환경에서는 실제 도메인을 서버 URL로 설정할 수 있습니다.
        // 예: Server server = new Server().url("https://api.autotimetable.com");
        Server server = new Server().url("/");

        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(authSetting())
                .info(getSwaggerInfo())
                .addServersItem(server);
    }

    private Info getSwaggerInfo() {
        License license = new License();
        // 라이선스 정보가 있다면 여기에 추가합니다. 없다면 이 부분은 생략해도 됩니다.
        license.setName("Apache 2.0");
//                .url("http://www.apache.org/licenses/LICENSE-2.0.html");

        return new Info()
                .title("AutoTimeTable API Document") // <-- 수정됨
                .description("자동 시간표 생성 서비스, AutoTimeTable의 API 문서입니다.") // <-- 수정됨
                .version("v0.0.1")
                .license(license);
    }

    private Components authSetting() {
        return new Components()
                .addSecuritySchemes(SECURITY_SCHEME_NAME,
                        new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT"));
    }

}
