package io.webby.auth.user;

import com.google.inject.Inject;
import io.webby.app.Settings;
import io.webby.db.model.LongIdGenerator;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class DefaultUserFactory implements UserFactory {
    @Inject private UserManager userManager;
    private LongIdGenerator generator;

    @Inject
    void init(@NotNull Settings settings) {
        generator = LongIdGenerator.autoIncrement(() -> userManager.getMaxId());
    }

    @Override
    public @NotNull Class<DefaultUser> getUserClass() {
        return DefaultUser.class;
    }

    @Override
    public @NotNull User createNewUser() {
        return tryToCreateNewUser(5, () -> {
            long userId = generator.nextId();
            return new DefaultUser(userId, UserAccess.Simple);
        });
    }

    protected @NotNull User tryToCreateNewUser(int maxAttempts, @NotNull Supplier<User> creator) {
        for (int i = 0; i < maxAttempts; i++) {
            User user = creator.get();
            if (userManager.tryInsert(user)) {
                return user;
            }
        }
        throw new RuntimeException("Too many failed attempts to create a user");
    }
}
