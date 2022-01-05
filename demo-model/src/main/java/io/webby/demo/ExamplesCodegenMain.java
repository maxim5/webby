package io.webby.demo;

import io.webby.auth.session.Session;
import io.webby.auth.user.DefaultUser;
import io.webby.db.model.BlobKv;
import io.webby.demo.model.*;
import io.webby.orm.arch.ArchJavaRunner;
import io.webby.orm.codegen.ModelInput;

import java.util.List;

public class ExamplesCodegenMain {
    private static final String DESTINATION_DIRECTORY = "demo-frontend/build/generated/sources/orm";
    private static final List<ModelInput> MODEL_INPUTS = List.of(
        ModelInput.of(DefaultUser.class),
        ModelInput.of(Session.class),
        ModelInput.of(BlobKv.class),

        ModelInput.of(PrimitiveModel.class),
        ModelInput.of(StringModel.class),
        ModelInput.of(TimingModel.class),
        ModelInput.of(WrappersModel.class),

        ModelInput.of(EnumModel.class),
        ModelInput.of(NestedModel.class),
        ModelInput.of(DeepNestedModel.class),
        ModelInput.of(PojoWithAdapterModel.class),
        ModelInput.of(NullableModel.class),
        ModelInput.of(InheritedModel.class),
        ModelInput.of(ComplexIdModel.class),
        ModelInput.of(AtomicModel.class),

        ModelInput.of(ForeignKeyModel.InnerInt.class),
        ModelInput.of(ForeignKeyModel.InnerLong.class),
        ModelInput.of(ForeignKeyModel.InnerString.class),
        ModelInput.of(ForeignKeyModel.class),

        ModelInput.of(M2mIntModel.class)
    );

    public static void main(String[] args) throws Exception {
        ArchJavaRunner runner = new ArchJavaRunner();
        runner.runGenerate(DESTINATION_DIRECTORY, MODEL_INPUTS);
    }
}
