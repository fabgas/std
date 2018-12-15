package org.alma.authentification.users.controller;

import java.util.List;

import javax.validation.Valid;

import org.alma.authentification.users.domain.Authority;
import org.alma.authentification.users.repositories.AuthorityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AuthorityController {

    @Autowired
    AuthorityRepository authorityRepository;
    
    @GetMapping("/authorities")
    public ResponseEntity<List<Authority>> getAuthorities() {
        List<Authority> authorities = authorityRepository.findAll();
        return new ResponseEntity<List<Authority>>(authorities, HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping("/authorities")
    public ResponseEntity<Authority> createAuthority(@Valid @RequestBody Authority authority) {
        return ResponseEntity.ok(authorityRepository.save(authority));
    }  
     
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @DeleteMapping("/authorities")
    public ResponseEntity<Void> deleteAuthority(@Valid @RequestBody Authority authority) {
        authorityRepository.delete(authority);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    

}