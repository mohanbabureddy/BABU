package org.example.dto;

import lombok.Getter;

@Getter
public class UserDto {
    private final Long id;
    private final String username;
    private final String phone;
    private final String mail;
    private final String role;
    private final boolean registrationCompleted;

    public UserDto(Long id, String username, String phone, String mail, String role, boolean registrationCompleted) {
        this.id = id;
        this.username = username;
        this.phone = phone;
        this.mail = mail;
        this.role = role;
        this.registrationCompleted = registrationCompleted;
    }

}
