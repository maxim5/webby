package io.webby.examples.model;

import io.webby.testing.Testing;
import io.webby.util.sql.DataClassAdaptersLocator;
import io.webby.util.sql.SchemaFactory;
import io.webby.util.sql.schema.InvalidSqlModelException;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class DataTableCodegenTest {
    private final DataClassAdaptersLocator locator = Testing.testStartupNoHandlers().getInstance(DataClassAdaptersLocator.class);

    @Test
    public void invalid_list_field() {
        record ListModel(List<Object> value) {}
        assertInvalidModel(ListModel.class);
    }

    @Test
    public void invalid_set_field() {
        record SetModel(Set<String> value) {}
        assertInvalidModel(SetModel.class);
    }

    @Test
    public void invalid_collection_field() {
        record CollectionModel(Collection<String> value) {}
        assertInvalidModel(CollectionModel.class);
    }

    @Test
    public void invalid_interface_field() {
        record SerializableModel(Serializable value) {}
        assertInvalidModel(SerializableModel.class);
    }

    private void assertInvalidModel(@NotNull Class<?> ... models) {
        assertThrows(InvalidSqlModelException.class, () -> {
            List<SchemaFactory.DataClassInput> inputs = Arrays.stream(models).map(SchemaFactory.DataClassInput::new).toList();
            SchemaFactory factory = new SchemaFactory(locator, inputs);
            factory.build();
        });
    }
}
