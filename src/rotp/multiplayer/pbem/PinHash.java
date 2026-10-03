package rotp.multiplayer.pbem;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** A salted PIN hash. Stops casual peeking, not a determined reader of the save. */
public record PinHash(byte[] salt, byte[] hash) implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final int MIN_LENGTH = 4;
    public static final int MAX_LENGTH = 12;
    private static final int ITERATIONS = 100_000;

    public PinHash {
        salt = salt.clone();
        hash = hash.clone();
    }

    public static boolean acceptable(String pin) {
        return pin != null && pin.length() >= MIN_LENGTH && pin.length() <= MAX_LENGTH
                && pin.chars().noneMatch(Character::isWhitespace);
    }

    public static PinHash create(String pin) {
        if (!acceptable(pin)) throw new IllegalArgumentException("PIN must be 4 to 12 characters");
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return new PinHash(salt, derive(pin, salt));
    }

    public boolean matches(String pin) {
        return pin != null && MessageDigest.isEqual(hash, derive(pin, salt));
    }

    private static byte[] derive(String pin, byte[] salt) {
        try {
            KeySpec spec = new PBEKeySpec(pin.toCharArray(), salt, ITERATIONS, 256);
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (Exception failure) { throw new IllegalStateException("PIN hashing unavailable", failure); }
    }
}
