package io.webby.demo;

import io.webby.auth.session.Session;
import io.webby.auth.user.DefaultUser;
import io.webby.db.model.BlobKv;
import io.webby.demo.model.*;
import io.webby.orm.arch.factory.ArchJavaRunner;
import io.webby.orm.arch.factory.RunInputs;
import io.webby.orm.codegen.ModelInput;

public class DemoTablesCodegenMain {
    private static final String DESTINATION_DIRECTORY = "demo-frontend/build/generated/sources/orm";
    private static final RunInputs RUN_INPUTS = RunInputs.of(
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
        ModelInput.of(MapperModel.class),
        ModelInput.of(PojoWithMapperModel.class),
        ModelInput.of(PojoWithAdapterModel.class),
        ModelInput.of(NullableModel.class),
        ModelInput.of(InheritedModel.class),
        ModelInput.of(ComplexIdModel.class),
        ModelInput.of(AtomicModel.class),
        ModelInput.of(OptionalModel.class),

        ModelInput.of(ForeignKeyModel.InnerInt.class),
        ModelInput.of(ForeignKeyModel.InnerLong.class),
        ModelInput.of(ForeignKeyModel.InnerString.class),
        ModelInput.of(ForeignKeyModel.class),
        ModelInput.of(ConstraintsModel.class),

        ModelInput.of(M2mIntModel.class),
        ModelInput.of(M2mLongModel.class),
        ModelInput.of(M2mIntLongModel.class),

        ModelInput.of(IntsModel.class),
        ModelInput.of(LongsModel.class),

        ModelInput.of(UserRateModel.class)
    );

    public static void main(String[] args) throws Exception {
        ArchJavaRunner runner = new ArchJavaRunner();
        runner.runGenerate(DESTINATION_DIRECTORY, RUN_INPUTS);
    }
}
