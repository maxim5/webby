package io.webby.db.count.vote;

import io.webby.db.StorageType;
import io.webby.util.collect.OneOf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record VotingOptions(@NotNull String name,
                            @NotNull VotingCounterType counterType,
                            @NotNull OneOf<StorageType, VotingStorage> store,
                            @Nullable VotingTableSpec tableSpec) {
    public static @NotNull VotingOptions ofKeyValue(@NotNull String name, @NotNull VotingCounterType counterType) {
        return new VotingOptions(name, counterType, OneOf.ofFirst(StorageType.KEY_VALUE_DB), null);
    }

    public static @NotNull VotingOptions ofSqlTable(@NotNull VotingCounterType counterType,
                                                    @NotNull VotingTableSpec tableSpec) {
        return new VotingOptions(tableSpec.table().sqlTableName(), counterType, OneOf.ofFirst(StorageType.SQL_DB), tableSpec);
    }
}
