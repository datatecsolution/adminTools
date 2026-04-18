package net.datatecsolution.admin_tools.config;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordHasher {

    private static final int BCRYPT_ROUNDS = 10;

    public static String hash(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(BCRYPT_ROUNDS));
    }

    public static boolean verify(String password, String stored) {
        if (!isHashed(stored)) {
            return password.equals(stored);
        }
        return BCrypt.checkpw(password, stored);
    }

    public static boolean isHashed(String stored) {
        return stored != null && stored.startsWith("$2a$");
    }
}
