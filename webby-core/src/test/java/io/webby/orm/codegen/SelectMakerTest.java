package io.webby.orm.codegen;

import io.webby.orm.api.ForeignInt;
import io.webby.orm.arch.factory.ArchFactory;
import io.webby.orm.arch.factory.ArchTesting;
import io.webby.orm.arch.factory.RunContext;
import io.webby.orm.arch.field.TableArch;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.webby.orm.api.ReadFollow.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SelectMakerTest {
    @Test
    public void one_level() {
        record User(int userId, String name) {}
        record Song(ForeignInt<User> author) {}

        Map<Class<?>, TableArch> archMap = buildArch(User.class, Song.class);
        SelectMaker selectMaker = new SelectMaker(archMap.get(Song.class));

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

        Map<Class<?>, TableArch> archMap = buildArch(User.class, Song.class, Single.class);
        SelectMaker selectMaker = new SelectMaker(archMap.get(Single.class));

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

    private @NotNull Map<Class<?>, TableArch> buildArch(@NotNull Class<?> @NotNull ...  models) {
        RunContext runContext = ArchTesting.newRunContext(models);
        new ArchFactory(runContext).build();
        return runContext.tables().getAllTables();
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
