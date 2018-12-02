package org.alma.authentification.security;

import java.io.IOException;
import java.util.Collections;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.alma.authentification.users.domain.UserLoginDTO;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
;

public class JwtAuthenticationFilter extends   UsernamePasswordAuthenticationFilter  {


// We use auth manager to validate the user credentials
private AuthenticationManager authManager;
private final JwtConfig jwtConfig;
private TokenHelper tokenHelper;    
public JwtAuthenticationFilter(AuthenticationManager authManager,JwtConfig config,TokenHelper tokenHelper) {
    this.authManager = authManager;
    this.jwtConfig = config;
    this.tokenHelper = tokenHelper;
    // By default, UsernamePasswordAuthenticationFilter listens to "/login" path. 
    // In our case, we use "/auth". So, we need to override the defaults.
    this.setRequiresAuthenticationRequestMatcher(new AntPathRequestMatcher(config.getUri(), "POST"));
}

@Override
public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
        throws AuthenticationException {
    
    try {
        // 1. Get credentials from request
        UserLoginDTO creds = new ObjectMapper().readValue(request.getInputStream(), UserLoginDTO.class);

        // 2. Create auth object (contains credentials) which will be used by auth manager
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                creds.getUsername(), creds.getPassword(), Collections.emptyList());
        
        // 3. Authentication manager authenticate the user, and use UserDetialsServiceImpl::loadUserByUsername() method to load the user.
        return authManager.authenticate(authToken);
        
    } catch (IOException e) {
        throw new RuntimeException(e);
    }
}

// Upon successful authentication, generate a token.
// The 'auth' passed to successfulAuthentication() is the current authenticated user.
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
        Authentication auth) throws IOException, ServletException {
        String token = tokenHelper.createToken(auth);
        // Add token to header
        response.addHeader(jwtConfig.getHeader(), jwtConfig.getPrefix() + token);
    }
}