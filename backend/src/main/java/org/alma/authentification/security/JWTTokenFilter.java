package org.alma.authentification.security;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class JWTTokenFilter extends OncePerRequestFilter {
    JwtConfig jwtConfig;
    TokenHelper tokenHelper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        // 1 Récupération du token dans le header
        String header = request.getHeader(jwtConfig.getHeader()); // utilisation de la constante
        // 2. validation du header
        if (header == null || !header.startsWith(jwtConfig.getPrefix())) {
            chain.doFilter(request, response); // If not valid, go to the next filter.
            return;
        }
        // On a un token.
        String token = header.replace(jwtConfig.getPrefix(), ""); // Bearer

        try { 
            // Catch et potentiellement log les exceptions
            Boolean validate = tokenHelper.validateToken(token);

            if (validate) {
                // 4. Validate the token
                Claims claims = Jwts.parser().setSigningKey(jwtConfig.getSecret().getBytes()).parseClaimsJws(token)
                        .getBody();
                // On récupère le username
                String username = claims.getSubject();
                if (username != null) {
                    @SuppressWarnings("unchecked")
                    List<String> authorities = (List<String>) claims.get("authorities");
                    // 5 On crée un objet d'authentification,UsernamePasswordAuthenticationToken 
                    // Il est utilisé par Spring pour représenté l'utilisateur couramment logger.
                    // Il contient une liste d'authorities, qui sotn du type GrantedAuthority(SimpleGrandtedAuthority)
                    // La liste des autorisation ont été positionné dans le token.
                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(username, null,
                            authorities.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList()));

                    // 6. Authenticate the user
                    // Now, user is authenticated
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
            else {
                // dans le cas ou le token n'est pas valide, il vaut mieux effacer ce context
                SecurityContextHolder.clearContext();
            }
        } catch (Exception e) {
            // In case of failure. Make sure it's clear; so guarantee user won't be
            // authenticated
            SecurityContextHolder.clearContext();
        }
        chain.doFilter(request, response);
    }

}