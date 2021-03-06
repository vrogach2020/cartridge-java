package io.tarantool.driver.mappers;

import io.tarantool.driver.api.TarantoolResultImpl;
import io.tarantool.driver.exceptions.TarantoolFunctionCallException;
import org.msgpack.value.ArrayValue;
import org.msgpack.value.ImmutableStringValue;
import org.msgpack.value.Value;
import org.msgpack.value.impl.ImmutableArrayValueImpl;
import org.msgpack.value.impl.ImmutableStringValueImpl;

import java.util.Map;

/**
 * Special tuple mapper for API function call results.
 *
 * The result is always an array since Lua <code>return</code> is a multi-return, and if th first value is
 * <code>nil</code>, the second non-null value is interpreted as an error object or error message.
 *
 * @param <T> target tuple type
 * @author Alexey Kuzin
 * @author Sergey Volgin
 */
public class TarantoolCallResultMapper<T> extends AbstractTarantoolResultMapper<T> {

    private static final ImmutableStringValue RESULT_META = new ImmutableStringValueImpl("metadata");
    private static final ImmutableStringValue RESULT_ROWS = new ImmutableStringValueImpl("rows");

    public TarantoolCallResultMapper(MessagePackValueMapper valueMapper,
                                     ValueConverter<ArrayValue, T> tupleConverter) {
        super(valueMapper,  v -> {
            ArrayValue tuples = v;

            // [nil, "Error msg..."] or [nil, {str="Error msg...", stack="..."}]
            if (v.size() == 2 && (v.get(0).isNilValue() && !v.get(1).isNilValue())) {
                if (v.get(1).isMapValue()) {
                    throw new TarantoolFunctionCallException(v.get(1).asMapValue());
                } else {
                    throw new TarantoolFunctionCallException(v.get(1).toString());
                }
            }

            // [nil] or [[[],...]]
            // [{"metadata" : [...], "rows": [...]}]
            if (v.size() == 1) {
                if (v.get(0).isNilValue()) {
                    tuples = ImmutableArrayValueImpl.empty();
                } else if (v.get(0).isMapValue() && hasRowsAndMetadata(v.get(0).asMapValue().map())) {
                    tuples = v.get(0).asMapValue().map().get(RESULT_ROWS).asArrayValue();
                } else if (v.get(0).isArrayValue()) {
                    tuples = v.get(0).asArrayValue();
                }
            }

            // default case: [[],...]
            return new TarantoolResultImpl<>(tuples, tupleConverter);
        });
    }

    private static boolean hasRowsAndMetadata(Map<Value, Value> valueMap) {
        return valueMap.containsKey(RESULT_META) && valueMap.containsKey(RESULT_ROWS);
    }
}
