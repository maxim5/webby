package io.webby.util.sql.codegen;

import io.webby.testing.Testing;
import io.webby.util.sql.api.ForeignInt;
import io.webby.util.sql.schema.ModelSchemaFactory;
import io.webby.util.sql.schema.TableSchema;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Map;

import static io.webby.util.sql.api.ReadFollow.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SelectMakerTest {
    private final ModelAdaptersLocator locator = Testing.testStartupNoHandlers().getInstance(ModelAdaptersLocator.class);

    @Test
    public void one_level() {
        record User(int userId, String name) {}
        record Song(ForeignInt<User> author) {}

        Map<Class<?>, TableSchema> schemaMap = buildSchema(User.class, Song.class);
        SelectMaker selectMaker = new SelectMaker(schemaMap.get(Song.class));

        assertMaker(selectMaker,
                    """
                    SELECT author_id
                    FROM song
                    """,
                    """
                    SELECT song.author_id, user.user_id, user.name
                    FROM song
                    LEFT JOIN user ON song.author_id = user.user_id
                    """);
    }

    @Test
    public void two_levels() {
        record User(int userId, String name) {}
        record Song(int songId, ForeignInt<User> author) {}
        record Single(ForeignInt<Song> hitSong) {}

        Map<Class<?>, TableSchema> schemaMap = buildSchema(User.class, Song.class, Single.class);
        SelectMaker selectMaker = new SelectMaker(schemaMap.get(Single.class));

        assertMaker(selectMaker,
                    """
                    SELECT hit_song_id
                    FROM single
                    """,
                    """
                    SELECT single.hit_song_id, song.song_id, song.author_id
                    FROM single
                    LEFT JOIN song ON single.hit_song_id = song.song_id
                    """,
                    """
                    SELECT single.hit_song_id, song.song_id, song.author_id, user.user_id, user.name
                    FROM single
                    LEFT JOIN song ON single.hit_song_id = song.song_id
                    LEFT JOIN user ON song.author_id = user.user_id
                    """);
    }

    private @NotNull Map<Class<?>, TableSchema> buildSchema(@NotNull Class<?> @NotNull ...  models) {
        ModelSchemaFactory factory = new ModelSchemaFactory(locator, Arrays.stream(models).map(ModelClassInput::new).toList());
        factory.build();
        return factory.getAllTables();
    }

    private static void assertMaker(@NotNull SelectMaker selectMaker, @NotNull String expected) {
        assertMaker(selectMaker, expected, expected, expected);
    }

    private static void assertMaker(@NotNull SelectMaker selectMaker,
                                    @NotNull String expectedNoFollow,
                                    @NotNull String expectedOneLevel) {
        assertMaker(selectMaker, expectedNoFollow, expectedOneLevel, expectedOneLevel);
    }

    private static void assertMaker(@NotNull SelectMaker selectMaker,
                                    @NotNull String expectedNoFollow,
                                    @NotNull String expectedOneLevel,
                                    @NotNull String expectedFollowAll) {
        assertEquals(expectedNoFollow.strip(), selectMaker.make(NO_FOLLOW).join().strip());
        assertEquals(expectedOneLevel.strip(), selectMaker.make(FOLLOW_ONE_LEVEL).join().strip());
        assertEquals(expectedFollowAll.strip(), selectMaker.make(FOLLOW_ALL).join().strip());
    }
}
