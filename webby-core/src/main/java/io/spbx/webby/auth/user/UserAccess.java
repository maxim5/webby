package io.spbx.webby.auth.user;

public record UserAccess(int level) {
    public static final int SimpleLevel = 1;
    public static final int SuperAdminLevel = Short.MAX_VALUE;

    public static final UserAccess Simple = new UserAccess(SimpleLevel);
    public static final UserAccess SuperAdmin = new UserAccess(SuperAdminLevel);

    public UserAccess {
        assert 1 <= level && level <= SuperAdminLevel : "Incorrect level: %s".formatted(level);
    }
}
