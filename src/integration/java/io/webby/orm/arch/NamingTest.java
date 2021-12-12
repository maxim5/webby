package io.webby.orm.arch;

import io.webby.auth.session.Session;
import io.webby.auth.user.User;
import io.webby.examples.model.NestedModel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NamingTest {
    @Test
    public void generatedSimpleJavaName_simple() {
        assertEquals("User", Naming.generatedSimpleJavaName(User.class));
        assertEquals("Session", Naming.generatedSimpleJavaName(Session.class));

        assertEquals("NestedModel", Naming.generatedSimpleJavaName(NestedModel.class));
        assertEquals("NestedModel_Level1", Naming.generatedSimpleJavaName(NestedModel.Level1.class));
        assertEquals("NestedModel_Simple", Naming.generatedSimpleJavaName(NestedModel.Simple.class));
    }

    @Test
    public void shortCanonicalJavaName_simple() {
        assertEquals("User", Naming.shortCanonicalJavaName(User.class));
        assertEquals("Session", Naming.shortCanonicalJavaName(Session.class));

        assertEquals("NestedModel", Naming.shortCanonicalJavaName(NestedModel.class));
        assertEquals("NestedModel.Level1", Naming.shortCanonicalJavaName(NestedModel.Level1.class));
        assertEquals("NestedModel.Simple", Naming.shortCanonicalJavaName(NestedModel.Simple.class));
    }
}
