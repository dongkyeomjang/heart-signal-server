package com.heartsignal.dev.filter;



import com.heartsignal.dev.domain.rds.Role;
import com.heartsignal.dev.domain.rds.User;
import com.heartsignal.dev.oauth.PrincipalDetails;
import com.heartsignal.dev.repository.rds.UserRepository;
import com.heartsignal.dev.service.jwt.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.NullAuthoritiesMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


@Slf4j
@Component
public class JwtAuthenticationProcessingFilter extends OncePerRequestFilter {

    private static final String NO_CHECK_URL_LOGIN = "/oauth2/authorization/kakao";
    private static final String NO_CHECK_URL_REDIRECT = "/login/oauth2/code/kakao";
    private static final String CHECK_URL_EXISTED_NICKNAME ="/api/v1/users/existed-nickname/**";
    private static final String CHECK_URL_USERINFO = "/api/v1/users/additional";

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final AntPathMatcher antPathMatcher;
    private final GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper();


    public JwtAuthenticationProcessingFilter(JwtService jwtService, UserRepository userRepository, AntPathMatcher antPathMatcher) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.antPathMatcher = antPathMatcher;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (antPathMatcher.match(NO_CHECK_URL_LOGIN, request.getRequestURI()) || antPathMatcher.match(NO_CHECK_URL_REDIRECT, request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        String refreshToken = getValidRefreshToken(request);

        if (refreshToken != null) {
            // 토큰을 재발급 해야하는 경우
            reIssueAccessTokenAndRefreshToken(refreshToken, response);
            return;
        }

        log.info("request.getRequestURI ={} ", request.getRequestURI());
        if (antPathMatcher.match(CHECK_URL_USERINFO, request.getRequestURI())|| antPathMatcher.match(CHECK_URL_EXISTED_NICKNAME, request.getRequestURI())){
            checkAccessTokenAndAuthentication(request, Role.GUEST);
        } else {
            checkAccessTokenAndAuthentication(request, Role.USER);
        }

        filterChain.doFilter(request, response);
    }

    private String getValidRefreshToken(HttpServletRequest request) {
        return jwtService.extractRefreshToken(request)
                .filter(jwtService::isTokenValid)
                .orElse(null);
    }

    private void checkAccessTokenAndAuthentication(HttpServletRequest request, Role role) {
        jwtService.extractAccessToken(request)
                .ifPresent(accessToken -> {
                            String socialId = jwtService.extractSocialId(accessToken);
                            userRepository.findBySocialIdAndRole(socialId, role)     //GUEST일때는 throw날리기
                                    .ifPresent(
                                            this::saveAuthentication
                                    );
                        }
                );
    }

    private void reIssueAccessTokenAndRefreshToken(String refreshToken, HttpServletResponse response) {
        userRepository.findByRefreshToken(refreshToken)
                .ifPresent(
                        user -> {
                            String reIssuedAccessToken = jwtService.createAccessToken(user.getSocialId());
                            jwtService.sendAccessToken(response, reIssuedAccessToken);
                            saveAuthentication(user);
                        }
                );
    }

    public void saveAuthentication(User user) {
        PrincipalDetails principalDetails = new PrincipalDetails(user);

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(principalDetails, null,
                        authoritiesMapper.mapAuthorities(principalDetails.getAuthorities()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}