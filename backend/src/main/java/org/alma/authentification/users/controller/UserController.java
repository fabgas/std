package org.alma.authentification.users.controller;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.alma.authentification.users.domain.Authority;
import org.alma.authentification.users.domain.ChangePasswordDTO;
import org.alma.authentification.users.domain.User;
import org.alma.authentification.users.domain.UserDTO;
import org.alma.authentification.users.errors.AuthorityNotFoundException;
import org.alma.authentification.users.errors.InvalidPasswordException;
import org.alma.authentification.users.errors.UserNotFoundException;
import org.alma.authentification.users.repositories.UserRepository;
import org.alma.authentification.users.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public  class UserController {
    final Logger logger = LoggerFactory.getLogger(UserController.class);
    @Autowired 
    UserRepository userRepository;
    
    @Autowired 
    UserService userService;

    @GetMapping("/users")
    public final ResponseEntity<List<User>> getUsers() {
        List<User> users = userRepository.findAll();
        return new ResponseEntity<List<User>>(users, HttpStatus.OK);
    }
     
    @GetMapping("/users/{username}")
    public final ResponseEntity<User> getUser(@PathVariable String username) {
      
        Optional<User> oUser = userRepository.findOneByUsername(username);
        if (oUser.isPresent()) {
            return new ResponseEntity<User>(oUser.get(), HttpStatus.OK);
        }
        return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
    }
     

    @PutMapping("/users")
    public final ResponseEntity<User> createUser(@Valid @RequestBody UserDTO userDTO) {
        User user = userService.createUser(userDTO);
        return new ResponseEntity<User>(user, HttpStatus.CREATED);
    }

    @PostMapping("/users")
    public final ResponseEntity<UserDTO> updateUser(@Valid @RequestBody UserDTO userDTO) {
         userService.updateUser(userDTO);
        return new ResponseEntity<UserDTO>(userDTO, HttpStatus.OK);
    }
    
    @DeleteMapping("/users/{username}")
    public final ResponseEntity<Void> deleteUser(@PathVariable String username) {
         userService.deleteUser(username);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    
    @PostMapping("/users/updatepassword")
    public final ResponseEntity<Void> updatePassword(@Valid @RequestBody ChangePasswordDTO changePasswordDTO) {
        try {
         userService.updatePassword(changePasswordDTO.getCurrentPassword(), changePasswordDTO.getNewPassword());
         return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        catch( InvalidPasswordException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
    
    // ---------------- operations d'affectations des authorities -------------------
   
    @GetMapping("/users/authorities/{username}")
    public ResponseEntity<List<Authority>> getAuthorities(@PathVariable String username) {
        Set<Authority> authorities = userService.getAuthorities(username);
        if (authorities.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(authorities.stream().collect(Collectors.toList()),HttpStatus.OK);
    }
    /**
     * Ajoute une autorité à l'utilisateur
     * @param username
     * @param authority
     * @return
     */

    @PostMapping("/users/authorities/{username}/{authority}")
    public ResponseEntity<Void> setAuthority(@PathVariable String username, @PathVariable String authority) {
        try {
            userService.affecteAuthority(username,authority);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        catch(UserNotFoundException | AuthorityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
    /**
     * Supprime l'autorité à l'utilisateur
     * @param username
     * @param authority
     * @return
     */
    @DeleteMapping("/users/authorities/{username}/{authority}")
    public ResponseEntity<Void> deleteAuthority(@PathVariable String username, @PathVariable String authority) {
        try {
            userService.deleteAuthority(username,authority);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        catch(UserNotFoundException | AuthorityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}