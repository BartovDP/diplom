package com.example;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;

public class PasswordUtils {

    private static final Argon2 argon2 = Argon2Factory.create();

    public static String hashPassword(String password) {
        return argon2.hash(10, 65536, 1, password.toCharArray());
    }

    public static boolean checkPassword(String password, String hashed) {
        return argon2.verify(hashed, password.toCharArray());
    }
}
