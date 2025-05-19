package com.buildbetter.consult.util;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ArchitectUtils {
    public static String constructPasswordFromEmail(String email) {
        if (email == null || email.isEmpty()) {
            return null;
        }

        int atIndex = email.indexOf('@');

        return atIndex > 0 ? email.substring(0, atIndex) : email;
    }
}
