package org.alma.authentification.security;

import java.util.Arrays;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Classe pour définir la politique générale de sécurité
 */
@EnableWebSecurity
//@EnableGlobalMethodSecurity(prePostEnabled = true)
public  class JwtSecurityConfig extends WebSecurityConfigurerAdapter {
    Logger logger = LoggerFactory.getLogger(JwtSecurityConfig.class);

    // Besoin de déclarer comment récupérer les users
    @Autowired
    UserDetailsService userDetailsService;
    @Autowired
    JwtConfig jwtConfig;
    @Autowired
    TokenHelper tokenHelper;
    
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring()
            .antMatchers("**/*.{js,html}")
            .antMatchers("/i18n/**")
            .antMatchers("/content/**")
            .antMatchers("/h2-console/**")
            .antMatchers("/swagger-ui/index.html")
            .antMatchers("/test/**");
    }
    /**
     * Méthode pour déclarer la politique de sécurité HTTP.
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.cors().and().csrf().disable()
          // make sure we use stateless session; session won't be used to store user's state.
             .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
             .and()
            // handle an authorized attempts 
            .exceptionHandling().authenticationEntryPoint((req, rsp, e) -> rsp.sendError(HttpServletResponse.SC_UNAUTHORIZED)) 
            .and()
            .headers()
            .frameOptions()
            .disable() // h2
            .and()
            .addFilter(new JwtAuthenticationFilter(authenticationManager(), jwtConfig,tokenHelper))	
            .addFilterAfter(new JWTTokenFilter(jwtConfig,tokenHelper), JwtAuthenticationFilter.class)
			// authorization requests config
            .authorizeRequests()
 		   // allow all who are accessing "auth" service
           .antMatchers(HttpMethod.POST,  jwtConfig.getUri()).permitAll()  
           .antMatchers("/h2-console/**").permitAll() // pour H2
		  // Any other request must be authenticated
 //         .antMatchers("**/*.{js,html}").permitAll()
          .requestMatchers(org.springframework.boot.autoconfigure.security.servlet.PathRequest.toStaticResources().atCommonLocations()).permitAll()
          .anyRequest().authenticated(); 
    }
     // Spring has UserDetailsService interface, which can be overriden to provide our implementation for fetching user from database (or any other source).
	// The UserDetailsService object is used by the auth manager to load the user from database.
	// In addition, we need to define the password encoder also. So, auth manager can compare and verify passwords.
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        //sera appelé par le filter
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
    }
    @Bean
	public BCryptPasswordEncoder passwordEncoder() {
	   
		return new BCryptPasswordEncoder();
    }
    @Bean
	public JwtConfig jwtConfig() {
        	return new JwtConfig();
	}
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "content-type", "x-auth-token","X-Requested-With"));
        configuration.setAllowCredentials(true);
        configuration.addExposedHeader("Authorization, x-xsrf-token, Access-Control-Allow-Headers, Origin, Accept, X-Requested-With, " +
        "Content-Type, Access-Control-Request-Method, Custom-Filter-Header"); //obligatooire
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}