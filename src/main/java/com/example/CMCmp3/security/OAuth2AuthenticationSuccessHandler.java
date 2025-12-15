package com.example.CMCmp3.security;

import com.example.CMCmp3.entity.AuthProvider;
import com.example.CMCmp3.entity.Role;
import com.example.CMCmp3.entity.User;
import com.example.CMCmp3.entity.UserStatus;
import com.example.CMCmp3.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        if (!(authentication instanceof OAuth2AuthenticationToken)) {
            super.onAuthenticationSuccess(request, response, authentication);
            return;
        }

        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        DefaultOAuth2User oauthUser = (DefaultOAuth2User) authentication.getPrincipal();
        Map<String, Object> attributes = oauthUser.getAttributes();
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        String avatarUrl = (String) attributes.get("picture");
        String providerId = oauthUser.getName();

        String registrationId = oauthToken.getAuthorizedClientRegistrationId();
        AuthProvider provider = AuthProvider.valueOf(registrationId.toUpperCase());

        try {
            Optional<User> userOptional = userRepository.findByEmail(email);
            User user;
            if (userOptional.isPresent()) {
                user = userOptional.get();
                if (user.getProvider() == AuthProvider.LOCAL) {
                    user.setProvider(provider);
                    user.setProviderId(providerId);
                    userRepository.save(user);
                }
            } else {
                user = new User();
                user.setEmail(email);
                user.setUsername(email);
                user.setDisplayName(name);
                user.setAvatarUrl(avatarUrl);
                user.setProvider(provider);
                user.setProviderId(providerId);
                user.setPassword("");
                user.setRole(Role.USER);
                user.setStatus(UserStatus.ACTIVE);
                userRepository.save(user);
            }

            String token = jwtService.generateToken(user);

            String targetUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/oauth2/redirect")
                    .queryParam("token", token)
                    .build().toUriString();

            getRedirectStrategy().sendRedirect(request, response, targetUrl);
        } catch (Exception ex) {
            log.error("Error processing OAuth2 authentication", ex);

            String targetUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/oauth2/redirect")
                    .queryParam("error", "true")
                    .build().toUriString();
            getRedirectStrategy().sendRedirect(request, response, targetUrl);
        }
    }
}