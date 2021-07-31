package io.webby.auth.user;

public record UserAccess(int level) {
    private static final short MAX_LEVEL = Short.MAX_VALUE;

    public static final UserAccess Simple = new UserAccess(1);
    public static final UserAccess Admin = new UserAccess(MAX_LEVEL);

    public UserAccess {
        assert 1 <= level && level <= MAX_LEVEL : "Incorrect level: %s".formatted(level);
    }
}
