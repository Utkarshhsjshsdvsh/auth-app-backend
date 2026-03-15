package com.substring.auth.auth_app_backend.helper;

import java.util.UUID;

public class UserHelper {
    public static UUID parseUUID(String uuid) {
        try {
            return UUID.fromString(uuid);
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new IllegalArgumentException("Invalid UUID format: " + uuid);
        }
    }
}
