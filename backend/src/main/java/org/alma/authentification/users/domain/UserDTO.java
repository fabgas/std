package org.alma.authentification.users.domain;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public final class UserDTO {

    @NotNull
    @Size(min = 1, max = 50)
    private String username;

    @Size(min=0,max=120)
    private String fullName;
}