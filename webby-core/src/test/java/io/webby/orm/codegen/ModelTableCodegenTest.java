package io.webby.orm.codegen;

import io.webby.orm.arch.InvalidSqlModelException;
import io.webby.orm.arch.factory.ArchFactory;
import io.webby.orm.arch.factory.TestingArch;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class ModelTableCodegenTest {
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
        assertThrows(InvalidSqlModelException.class, () ->
            new ArchFactory(TestingArch.newRunContext(models)).build()
        );
    }
}
