package tests;

import util.PasswordUtil;

/** Small dependency-free checks for password hashing and migration behavior. */
public final class PasswordUtilTest {
    private static int checks;

    private PasswordUtilTest() {}

    public static void main(String[] args) {
        String password = "Example-Password-42!";
        String firstHash = PasswordUtil.hash(password);
        String secondHash = PasswordUtil.hash(password);

        check(PasswordUtil.isHashed(firstHash), "new passwords use the hash format");
        check(!firstHash.equals(secondHash), "each hash receives a fresh salt");
        check(PasswordUtil.verify(password, firstHash), "correct password verifies");
        check(!PasswordUtil.verify("incorrect", firstHash), "incorrect password is rejected");
        check(!PasswordUtil.verify(null, firstHash), "null password is rejected");
        check(!PasswordUtil.verify(password, "pbkdf2$bad$format"), "malformed hash is rejected");
        check(PasswordUtil.verify("legacy-pass", "legacy-pass"), "legacy plain-text row can authenticate");
        check(!PasswordUtil.verify("wrong", "legacy-pass"), "wrong legacy password is rejected");

        System.out.println("PasswordUtil checks passed: " + checks);
    }

    private static void check(boolean condition, String message) {
        checks++;
        if (!condition) {
            throw new AssertionError("Failed: " + message);
        }
    }
}
