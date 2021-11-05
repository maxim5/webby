package io.webby.examples.model;

import io.webby.testing.Testing;
import io.webby.util.sql.codegen.ModelAdaptersLocator;
import io.webby.util.sql.codegen.ModelClassInput;
import io.webby.util.sql.codegen.ModelSchemaFactory;
import io.webby.util.sql.schema.InvalidSqlModelException;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class ModelTableCodegenTest {
    private final ModelAdaptersLocator locator = Testing.testStartupNoHandlers().getInstance(ModelAdaptersLocator.class);

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
            List<ModelClassInput> inputs = Arrays.stream(models).map(ModelClassInput::new).toList();
            ModelSchemaFactory factory = new ModelSchemaFactory(locator, inputs);
            factory.build();
        });
    }
}
