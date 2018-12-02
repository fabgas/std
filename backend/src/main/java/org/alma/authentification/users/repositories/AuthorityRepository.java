package org.alma.authentification.users.repositories;

import org.alma.authentification.users.domain.Authority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthorityRepository  extends JpaRepository<Authority,String>{

}