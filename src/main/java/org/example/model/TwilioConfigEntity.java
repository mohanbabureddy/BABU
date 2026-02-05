package org.example.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "twilio_config")
public class TwilioConfigEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_sid", nullable = false)
    private String accountSid;

    @Column(name = "auth_token", nullable = false)
    private String authToken;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;
}
