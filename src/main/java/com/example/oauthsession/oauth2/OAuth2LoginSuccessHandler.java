package com.example.oauthsession.oauth2;

import com.example.oauthsession.dto.NaverResposne;
import com.example.oauthsession.entity.User;
import com.example.oauthsession.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String registrationId = ((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId();

        NaverResposne oAuth2Response = new NaverResposne(oAuth2User.getAttributes());
        String username = oAuth2Response.getProvider() + " " + oAuth2Response.getProviderId();

        User user = userRepository.findByUsername(username);

        // 세션에 User 저장
        request.getSession().setAttribute("LOGIN_USER", user);
        String targetUrl = "http://54.116.1.156/login-success?username=" + username;
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
