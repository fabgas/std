package org.alma.authentification.security;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.alma.authentification.users.domain.Authority;
import org.alma.authentification.users.domain.User;
import org.alma.authentification.users.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
/**
 * Classe standard poure rechercher un utilisateur dans une BDD et vérifier son mot de passe
 * par exemple
 */
@Service
public final class UserDetailServiceImpl implements UserDetailsService {
    Logger logger = LoggerFactory.getLogger(UserDetailServiceImpl.class);

    @Autowired
    UserRepository userRepository;
    /**
     * Renvoie du USER.
     */
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // So, we need to set it to that format, so we can verify and compare roles (i.e. hasRole("ADMIN")).
		
        Optional<User> user = userRepository.findOneWithAuthoritiesByUsername(username);
        if (user.isPresent()) {
            User userBdd = user.get();
            Set<Authority> authoritiesSet = userBdd.getAuthorities();
            authoritiesSet.stream().map(Authority::getName).forEach(str -> logger.debug(str +","));
            
            String authorities = authoritiesSet.stream().map(auth -> "ROLE_"+auth.getName()).collect (Collectors.joining (","));
            List<GrantedAuthority> grantedAuthorities = AuthorityUtils.commaSeparatedStringToAuthorityList(authorities);
            UserDetails userDetails = new org.springframework.security.core.userdetails.User(userBdd.getUsername(),userBdd.getPassword(),grantedAuthorities);
            return userDetails;
        }
        	// If user not found. Throw this exception.
		throw new UsernameNotFoundException("Username: " + username + " not found");
    }
}