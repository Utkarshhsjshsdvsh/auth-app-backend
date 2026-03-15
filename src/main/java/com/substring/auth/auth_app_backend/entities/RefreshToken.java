package com.substring.auth.auth_app_backend.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name="refresh_token",indexes = {
        @Index(name = "idx_refresh_token_jti", columnList = "jti", unique = true),
        @Index(name = "idx_refresh_token_user", columnList = "user_id")
})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "jti",unique = true,nullable = false,updatable = false)
    private String jti;
    @ManyToOne(optional = false,fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id",nullable = false)
    private User user;
    @Column(nullable = false)
    private Instant createdAt;
    @Column(nullable = false)
    private Instant expiredAt;
    @Column(nullable = false)
    private boolean revoked;
    private String replacedByToken;
}
