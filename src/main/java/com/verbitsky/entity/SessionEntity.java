package com.verbitsky.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import com.verbitsky.api.entity.BaseEntity;

import java.io.Serial;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false, exclude = {
        "accessToken",
        "refreshToken",
})
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(schema = "login", name = "user_sessions")
public class SessionEntity extends BaseEntity {
    @Serial
    private static final long serialVersionUID = 9124563000631597328L;

    @Id
    @SequenceGenerator(name = "user_sessions", schema = "login", sequenceName = "user_sessions_sequence")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_sessions")
    private Long id;
    @Column(name = "keycloak_id")
    private String keycloakId;

    @Column(name = "session_id")
    private String sessionId;

    @Column(name = "device_id")
    private String deviceId;

    @Column(name = "login")
    private String login;

    @Column(name = "access_token")
    private String accessToken;

    @Column(name = "refresh_token")
    private String refreshToken;
}
