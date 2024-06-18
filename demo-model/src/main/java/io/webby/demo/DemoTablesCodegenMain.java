package io.webby.demo;

import io.spbx.orm.arch.factory.ArchJavaRunner;
import io.spbx.orm.arch.factory.ModelInput;
import io.spbx.orm.arch.factory.PojoInput;
import io.spbx.orm.arch.factory.RunInputs;
import io.spbx.webby.auth.session.DefaultSession;
import io.spbx.webby.auth.user.DefaultUser;
import io.spbx.webby.db.model.BlobKv;
import io.webby.demo.model.*;

import java.util.List;

public class DemoTablesCodegenMain {
    private static final String DESTINATION_DIRECTORY = "demo-frontend/build/generated/sources/orm";
    private static final List<ModelInput> MODEL_INPUTS = List.of(
        ModelInput.of(DefaultUser.class),
        ModelInput.of(DefaultSession.class),
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
        ModelInput.of(ConstraintsModel.class),

        ModelInput.of(ForeignKeyModel.InnerInt.class),
        ModelInput.of(ForeignKeyModel.InnerLong.class),
        ModelInput.of(ForeignKeyModel.InnerString.class),
        ModelInput.of(ForeignKeyModel.class),
        ModelInput.of(ForeignKeyModel.Nullable.class),

        ModelInput.of(BridgeIntModel.class),
        ModelInput.of(BridgeLongModel.class),
        ModelInput.of(BridgeIntLongModel.class),

        ModelInput.of(IntsModel.class),
        ModelInput.of(LongsModel.class),

        ModelInput.of(UserRateModel.class)
    );
    private static final List<PojoInput> POJO_INPUTS = List.of(
        PojoInput.of(PrimitiveModel.PrimitiveDuo.class),
        PojoInput.of(PrimitiveModel.PrimitiveTrio.class),
        PojoInput.of(StringModel.StringDuo.class)
    );
    private static final RunInputs RUN_INPUTS = new RunInputs(MODEL_INPUTS, POJO_INPUTS);

    public static void main(String[] args) throws Exception {
        ArchJavaRunner runner = new ArchJavaRunner();
        runner.runGenerate(DESTINATION_DIRECTORY, RUN_INPUTS);
    }
}
