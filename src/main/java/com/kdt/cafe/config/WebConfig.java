package com.kdt.cafe.config;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS 설정 (명세 3절 "브라우저의 API 호출 허용")
 * 제공 화면(127.0.0.1:8000)이 백엔드(8080)로 보내는 요청을 브라우저가 차단하지 않도록 허용한다
 * 쿠키·인증 토큰은 사용하지 않으므로 allowCredentials는 설정하지 않음
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {

        registry.addMapping("/api/**")                              // 허용할 API 경로
                .allowedOrigins("http://127.0.0.1:8000")                      // 화면 주소 (localhost는 다른 Origin으로 취급되므로 127.0.0.1로 통일)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")    // HTTP 요청(OPTIONS는 사전요청 용도)
                .allowedHeaders("Content-Type");                              // JSON 본문 전송에 필요한 헤더

    }

}
