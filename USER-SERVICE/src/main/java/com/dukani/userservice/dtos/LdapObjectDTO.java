package com.dukani.userservice.dtos;

import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class LdapObjectDTO {
    private String firstName;
    private String lastName;
    private String emailAddress;
}
