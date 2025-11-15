package com.example.oauthsession.config;

import com.example.oauthsession.oauth2.CustomClientRegistrationRepo;
import com.example.oauthsession.oauth2.CustomOAuth2AuthorizedClientService;
import com.example.oauthsession.oauth2.OAuth2LoginSuccessHandler;
import com.example.oauthsession.service.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;

import java.util.List;


@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomClientRegistrationRepo customClientRegistrationRepo;
    private final CustomOAuth2AuthorizedClientService customOAuth2AuthorizedClientService;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    private final JdbcTemplate jdbcTemplate;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.csrf((csrf)->csrf.disable());
        http.formLogin((formLogin)->formLogin.disable());
        http.httpBasic((httpBasic)->httpBasic.disable());
        http.oauth2Login(Customizer.withDefaults());

        http.oauth2Login(oauth2 -> oauth2
                .clientRegistrationRepository(customClientRegistrationRepo.clientRegistrationRepository())
                .authorizedClientService(customOAuth2AuthorizedClientService.
                        oAuth2AuthorizedClientService(jdbcTemplate, customClientRegistrationRepo.clientRegistrationRepository()))
                .userInfoEndpoint(userInfo -> userInfo
                        .userService(customOAuth2UserService))
                .successHandler(oAuth2LoginSuccessHandler)
        );

        http
                .authorizeHttpRequests((auth)-> auth
                        .requestMatchers("/**", "/oauth2/**", "/login/**").permitAll()
                        .anyRequest().authenticated());
        return http.build();
    }

    @Bean
    public HttpFirewall httpFirewall() {
        StrictHttpFirewall firewall = new StrictHttpFirewall();
        // 쿼리 파라미터에 포함될 수 있는 특정 문자를 허용합니다.
        // 이 설정은 URL에 포함된 줄 바꿈 문자(`\n`, `\r`)나 기타 특수 문자가 공격으로 오인되어 차단되는 것을 방지합니다.
        firewall.setAllowedHttpMethods(List.of("GET", "POST"));
        firewall.setAllowUrlEncodedCarriageReturn(true);
        firewall.setAllowUrlEncodedPercent(true);
        firewall.setAllowUrlEncodedSlash(true);
        firewall.setAllowSemicolon(true);
        firewall.setAllowUrlEncodedLineFeed(true);


        return firewall;
    }
}


