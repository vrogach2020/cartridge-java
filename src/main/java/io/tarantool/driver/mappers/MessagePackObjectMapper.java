package io.tarantool.driver.mappers;

import org.msgpack.value.Value;

/**
 * Basic interface for generic converters between Java objects and MessagePack entities.
 * Object converters must be added using the {@link #registerObjectConverter(Class, Class, ObjectConverter)} method
 *
 * @author Alexey Kuzin
 */
public interface MessagePackObjectMapper {
    /**
     * Create MessagePack entity representation for an object.
     * @param o an object to be converted
     * @param <V> the target MessagePack entity type
     * @param <O> the source object type
     * @throws MessagePackObjectMapperException if the corresponding conversion cannot be performed
     * @return instance of MessagePack {@link Value}
     */
    <V extends Value, O> V toValue(O o) throws MessagePackObjectMapperException;

    /**
     * Adds a Java object converter to this mappers instance
     * @param objectClass source object class
     * @param valueClass target value class
     * @param converter entity-to-object converter
     * @param <V> the target MessagePack entity type
     * @param <O> the source object type
     * @see ObjectConverter
     */
    <V extends Value, O> void registerObjectConverter(Class<O> objectClass, Class<V> valueClass, ObjectConverter<O, V> converter);
}