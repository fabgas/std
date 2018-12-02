package org.alma.authentification.users.services;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.alma.authentification.security.SecurityUtils;
import org.alma.authentification.users.domain.Authority;
import org.alma.authentification.users.domain.User;
import org.alma.authentification.users.domain.UserDTO;
import org.alma.authentification.users.errors.AuthorityNotFoundException;
import org.alma.authentification.users.errors.InvalidPasswordException;
import org.alma.authentification.users.errors.UserNotFoundException;
import org.alma.authentification.users.repositories.AuthorityRepository;
import org.alma.authentification.users.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService {

    @Autowired
    private  UserRepository userRepository;
    
    @Autowired 
    AuthorityRepository authorityRepository;
    
    @Qualifier("passwordEncoder")
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    Logger logger = LoggerFactory.getLogger(UserService.class);
    /**
     * Création d'un utilisateur
     * @param userDTO
     * @return
     */
    public User createUser(UserDTO userDTO) {
        // transformation du mot de passe
        String password = passwordEncoder.encode(userDTO.getUsername());
        User user = new User();
        user.setPassword(password);
        user.setFullname(userDTO.getFullName());
        user.setUsername(userDTO.getUsername());
        userRepository.save(user);
        return user;
    }    

    /**
     * Update un utilisateur
     * @param userDTO
     */
    public void updateUser(UserDTO userDTO) {
        userRepository.findOneByUsername(userDTO.getUsername()).ifPresent(user -> {
            user.setFullname(userDTO.getFullName());
         // sauvegarde automatique
        });;
    }

    /**
     * Delete a user identified by is userName
     * @param userDTO
     */
    public void deleteUser(UserDTO userDTO) {
        userRepository.findOneByUsername(userDTO.getUsername()).ifPresent(
            user -> {
                this.userRepository.delete(user);
            });         
    }

    public void updatePassword(String oldPassword,String newPassword) throws InvalidPasswordException{
        SecurityUtils.getCurrentUserName()
                    .flatMap(userRepository::findOneByUsername)
                    .ifPresent(user -> {
                        // on compare l'ancien mot de passe et le nouveau
                        String currentCryptePassword = user.getPassword();
                        if (!passwordEncoder.matches(oldPassword, currentCryptePassword)) {
                            // erreur (nécessairement une runtime pour passer outre la functionnal qui est une classe qui ne fait pas throw)
                            throw new InvalidPasswordException();
                        }
                        else {
                            String newEncrypedPassword = passwordEncoder.encode(newPassword);
                            user.setPassword(newEncrypedPassword);

                        }
                    });
    }
   
    @Transactional(readOnly = true)
    public Set<Authority> getAuthorities(String username) {
       Optional<User> oUser= userRepository.findOneWithAuthoritiesByUsername(username);
       if (oUser.isPresent()) {
        return oUser.get().getAuthorities();
       }
       return new HashSet<>();
    }

    public void affecteAuthority(String username, String authority) throws UserNotFoundException,AuthorityNotFoundException{
        // vérification de l'existence du user
        Optional<User> oUser= userRepository.findOneWithAuthoritiesByUsername(username);
        if (oUser.isPresent()) {
            Optional<Authority> oAuthority = authorityRepository.findById(authority);
            if (oAuthority.isPresent()) {
                User user = oUser.get();
                if (user.getAuthorities().stream().filter(autho -> authority.equals(autho.getName())).findFirst().isPresent()) {
                    // autority déjà affectée.
                    logger.debug("authority déjà affectée");
                    return;
                };
                user.getAuthorities().add(oAuthority.get());
                return;
            }
            logger.debug("authority not found");
            throw new AuthorityNotFoundException();
        }
        logger.debug("user not found");
        throw new UserNotFoundException();
    }
}