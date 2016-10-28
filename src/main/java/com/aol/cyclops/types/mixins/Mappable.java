package com.aol.cyclops.types.mixins;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.aol.cyclops.internal.invokedynamic.ReflectionCache;
import com.aol.cyclops.lambda.mixins.CoerceToMapTest;
import com.aol.cyclops.lambda.mixins.CoerceToMapTest.MyEntity;
import com.aol.cyclops.util.ExceptionSoftener;

import lombok.Value;

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
     * <pre>
     * {@code 
     *  @Value static class MyEntity { int num; String str;}
     * 
     *  Map<String,?> map = AsMappable.asMappable(new MyEntity(10,"hello")).toMap();
       
        assertThat(map.get("num"),equalTo(10));
        assertThat(map.get("str"),equalTo("hello"));
   
        Map<String,?> map = AsMappable.asMappable(new MyEntity(10,null)).toMap();
        
        assertThat(map.get("num"),equalTo(10));
        assertThat(map.get("str"),nullValue());
    

     * 
     * }
     * </pre>
     * 
     * 
     * 
     * @return Map representation
     */
    default Map<String, ?> toMap() {
        try {
            final Object o = unwrap();
            final Map<String, Object> result = new HashMap<>();
            for (final Field f : ReflectionCache.getFields(o.getClass())) {
                result.put(f.getName(), f.get(o));
            }
            return result;
        } catch (final Exception e) {
            throw ExceptionSoftener.throwSoftenedException(e);

        }
    }
}
