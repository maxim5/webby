package io.spbx.orm.codegen;

import io.spbx.orm.arch.model.TableArch;
import io.spbx.webby.demo.model.NullableModel;
import io.spbx.webby.demo.model.PrimitiveModel;
import io.spbx.webby.demo.model.StringModel;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.StringWriter;

import static io.spbx.orm.arch.factory.IntegrationTestingArch.buildTableArch;

@Tag("slow") @Tag("integration")
public class ModelTableCodegenIntegrationTest {
    // Static to do classpath scan once.
    private static final ModelAdaptersLocator locator = new DefaultModelAdaptersLocator();

    @ParameterizedTest
    @ValueSource(classes = {
        PrimitiveModel.class,
        StringModel.class,
        NullableModel.class,
    })
    public void generate_table_for_model(Class<?> model) throws Exception {
        try (StringWriter writer = new StringWriter(65536)) {
            TableArch table = buildTableArch(model, locator);
            ModelTableCodegen generator = new ModelTableCodegen(locator, table, writer);
            generator.generateJava();
        }
    }
}
