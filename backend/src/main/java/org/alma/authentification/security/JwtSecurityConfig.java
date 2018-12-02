package org.alma.authentification.security;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.reactive.PathRequest;
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

/**
 * Classe pour définir la politique générale de sécurité
 */
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public  class JwtSecurityConfig extends WebSecurityConfigurerAdapter {

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
            .antMatchers(HttpMethod.OPTIONS, "/**")
            .antMatchers(HttpMethod.GET, "/**")
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
        http.csrf().disable()
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
	
}