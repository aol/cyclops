package com.aol.cyclops.types.mixins;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import com.aol.cyclops.internal.invokedynamic.ReflectionCache;
import com.aol.cyclops.util.ExceptionSoftener;

/**
 * Interface that represents an Object that can be converted to a map
 * 
 * @author johnmcclean
 *
 */
public interface Mappable {
    default Object unwrap() {
        return this;
    }

    /**
     * default implementation maps field values on the host object by name
     * 
     * @return Map representation
     */
    default Map<String, ?> toMap() {
        try {
            final Object o = unwrap();
            Map<String, Object> result = new HashMap<>();
            for (Field f : ReflectionCache.getFields(o.getClass())) {
                result.put(f.getName(), f.get(o));
            }
            return result;
        } catch (Exception e) {
            throw ExceptionSoftener.throwSoftenedException(e);

        }
    }
}
