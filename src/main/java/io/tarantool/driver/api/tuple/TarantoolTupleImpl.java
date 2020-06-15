package io.tarantool.driver.api.tuple;

import io.tarantool.driver.exceptions.TarantoolValueConverterNotFoundException;
import io.tarantool.driver.mappers.MessagePackObjectMapper;
import io.tarantool.driver.mappers.MessagePackValueMapper;
import org.msgpack.value.ArrayValue;
import org.msgpack.value.Value;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * Basic Tarantool tuple implementation
 *
 * @author Alexey Kuzin
 */
public class TarantoolTupleImpl implements TarantoolTuple {

    private List<TarantoolField> fields;

    /**
     * Basic constructor.
     * @param value messagePack entity
     * @param mapper for converting the entity into the Java objects
     */
    @SuppressWarnings("unchecked")
    public TarantoolTupleImpl(ArrayValue value, MessagePackValueMapper mapper) {
        this.fields = new ArrayList<>(value.size());
        for (Value fieldValue: value) {
            if (fieldValue.isNilValue()) {
                fields.add(new TarantoolNullField());
            } else {
                fields.add(new TarantoolFieldImpl(fieldValue, mapper));
            }
        }
    }

    @Override
    public Optional<TarantoolField> getField(int fieldPosition) {
        Assert.state(fieldPosition >= 0, "Field position starts with 0");

        if (fieldPosition < fields.size()) {
            return Optional.ofNullable(fields.get(fieldPosition));
        }
        return Optional.empty();
    }

    @Override
    public <O> Optional<O> getObject(int fieldPosition, Class<O> objectClass) throws TarantoolValueConverterNotFoundException {
        Optional<TarantoolField> field = getField(fieldPosition);
        return field.isPresent() ? Optional.ofNullable(field.get().getValue(objectClass)) : Optional.empty();
    }

    @Override
    public Iterator<TarantoolField> iterator() {
        return fields.iterator();
    }

    @Override
    public void forEach(Consumer<? super TarantoolField> action) {
        fields.forEach(action);
    }

    @Override
    public Spliterator<TarantoolField> spliterator() {
        return fields.spliterator();
    }

    @Override
    public Value toMessagePackValue(MessagePackObjectMapper mapper) {
        return mapper.toValue(fields);
    }
}