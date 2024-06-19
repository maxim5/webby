package io.spbx.webby.testing;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.spbx.orm.api.BridgeTable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;

public class TestingTables {
    public static <IL, EL, IR, ER, T extends BridgeTable<IL, EL, IR, ER>>
            @NotNull BridgeTableSubject<IL, EL, IR, ER, T> withTable(@NotNull T table) {
        return new BridgeTableSubject<>(table);
    }

    public record BridgeTableSubject<IL, EL, IR, ER, T extends BridgeTable<IL, EL, IR, ER>>(@NotNull T table) {
        public @NotNull BridgeRightIdSubject<IL, EL, IR, T> forRightId(@NotNull IR rightId) {
            return new BridgeRightIdSubject<>(table, rightId);
        }

        public @NotNull BridgeLeftIdSubject<IL, IR, ER, T> forLeftId(@NotNull IL leftId) {
            return new BridgeLeftIdSubject<>(table, leftId);
        }
    }

    @CanIgnoreReturnValue
    public record BridgeRightIdSubject<IL, EL, IR, T extends BridgeTable<IL, EL, IR, ?>>(@NotNull T table, @NotNull IR rightId) {
        public @NotNull BridgeRightIdSubject<IL, EL, IR, T> assertSize(int size) {
            assertThat(table.existsRight(rightId)).isEqualTo(size > 0);
            assertThat(table.countLefts(rightId)).isEqualTo(size);
            return this;
        }

        @SafeVarargs
        public final BridgeRightIdSubject<IL, EL, IR, T> assertLeftIds(@NotNull IL @NotNull... ids) {
            assertSize(ids.length);
            List<IL> each = new ArrayList<>();
            table.forEachLeftId(rightId, each::add);
            assertThat(each).containsExactlyElementsIn(ids);
            assertThat(table.fetchAllLeftIds(rightId)).containsExactlyElementsIn(ids);
            return this;
        }

        public @NotNull BridgeRightIdSubject<IL, EL, IR, T> assertNoLeftIds() {
            return assertLeftIds();
        }

        @SafeVarargs
        public final BridgeRightIdSubject<IL, EL, IR, T> assertLeftEntities(@NotNull EL @NotNull... entities) {
            assertSize(entities.length);
            List<EL> each = new ArrayList<>();
            table.forEachLeft(rightId, each::add);
            assertThat(each).containsExactlyElementsIn(entities);
            assertThat(table.fetchAllLefts(rightId)).containsExactlyElementsIn(entities);
            return this;
        }

        public @NotNull BridgeRightIdSubject<IL, EL, IR, T> assertNoLeftEntities() {
            return assertLeftEntities();
        }

        public @NotNull BridgeRightIdSubject<IL, EL, IR, T> assertOneLeft(@NotNull IL id, @NotNull EL entity) {
            return assertLeftIds(id).assertLeftEntities(entity);
        }

        public @NotNull BridgeRightIdSubject<IL, EL, IR, T> assertNothing() {
            return assertNoLeftIds().assertNoLeftEntities();
        }
    }

    @CanIgnoreReturnValue
    public record BridgeLeftIdSubject<IL, IR, ER, T extends BridgeTable<IL, ?, IR, ER>>(@NotNull T table, @NotNull IL leftId) {
        public @NotNull BridgeLeftIdSubject<IL, IR, ER, T> assertSize(int size) {
            assertThat(table.existsLeft(leftId)).isEqualTo(size > 0);
            assertThat(table.countRights(leftId)).isEqualTo(size);
            return this;
        }

        @SafeVarargs
        public final BridgeLeftIdSubject<IL, IR, ER, T> assertRightIds(@NotNull IR @NotNull... ids) {
            assertSize(ids.length);
            List<IR> each = new ArrayList<>();
            table.forEachRightId(leftId, each::add);
            assertThat(each).containsExactlyElementsIn(ids);
            assertThat(table.fetchAllRightIds(leftId)).containsExactlyElementsIn(ids);
            return this;
        }

        public @NotNull BridgeLeftIdSubject<IL, IR, ER, T> assertNoRightIds() {
            return assertRightIds();
        }

        @SafeVarargs
        public final BridgeLeftIdSubject<IL, IR, ER, T> assertRightEntities(@NotNull ER @NotNull... entities) {
            assertSize(entities.length);
            List<ER> each = new ArrayList<>();
            table.forEachRight(leftId, each::add);
            assertThat(each).containsExactlyElementsIn(entities);
            assertThat(table.fetchAllRights(leftId)).containsExactlyElementsIn(entities);
            return this;
        }

        public @NotNull BridgeLeftIdSubject<IL, IR, ER, T> assertNoRightEntities() {
            return assertRightEntities();
        }

        public @NotNull BridgeLeftIdSubject<IL, IR, ER, T> assertOneRight(@NotNull IR id, @NotNull ER entity) {
            return assertRightIds(id).assertRightEntities(entity);
        }

        public @NotNull BridgeLeftIdSubject<IL, IR, ER, T> assertNothing() {
            return assertNoRightIds().assertNoRightEntities();
        }
    }
}
