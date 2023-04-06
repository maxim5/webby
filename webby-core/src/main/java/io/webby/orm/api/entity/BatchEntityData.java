package io.webby.orm.api.entity;

import io.webby.orm.api.query.Contextual;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Holds the batch of data associated with the columns. If a row is represented by an Entity, then this class
 * represents a {@code List<Entity>}, but can do it much more efficiently. For example, if the data is all int values,
 * the batch can be stored in a single int array.
 * <p>
 * The main requirement for the implementations is ability to split and add batches to the JDBC statement
 * using {@link PreparedStatement}'s {@code set} methods and {@link PreparedStatement#addBatch()}.
 * <p>
 * Instances of this class can be used to insert or update to the table using batch table API.
 * <p>
 * Example data:
 * <pre>
 *     Name  | Age
 *     -----------
 *     Maxim | 20
 *     Ivan  | 25
 *     Roman | 30
 * </pre>
 * <p>
 * This batch consists of 6 objects forming 3 rows by 2. The batch size (same as data size) is 2.
 * Given a statement, the parameters and {@link PreparedStatement#addBatch()} must be called 3 times.
 *
 * @param <B> the type used by implementations for each chunk (i.e. each row)
 * @see io.webby.orm.api.BaseTable#insertDataBatch(BatchEntityData)
 * @see io.webby.orm.api.BaseTable#updateDataWhereBatch(BatchEntityData, Contextual)
 */
public interface BatchEntityData<B> extends ColumnSet {
    /**
     * The size of each data row in this batch. Equals to the number of columns.
     */
    default int dataSize() {
        return columns().size();
    }

    /**
     * Updates the JDBC {@code statement} with the parameters from the data of this instance.
     */
    void provideBatchValues(@NotNull PreparedStatement statement,
                            @Nullable Contextual<?, B> contextual) throws SQLException;
}
