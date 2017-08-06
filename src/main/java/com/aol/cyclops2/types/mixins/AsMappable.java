package com.aol.cyclops2.types.mixins;

import lombok.AllArgsConstructor;

/**
 * Don't break encapsulation of classes for testing purposes
 * Coerce Objects to Map form in testing, to test their values.
 * 
 * @author johnmcclean
 *
 */
public class AsMappable {

    /**
     * Convert supplied object to a Mappable instance.
     * Mappable will convert the (non-static) fields of the supplied object into a map
     * 
     * <pre>
     * {@code 
     * 
     *  Map<String,?> map = AsMappable.asMappable(new MyEntity(10,"hello")).toMap();
        
        assertThat(map.get("num"),equalTo(10));
        assertThat(map.get("str"),equalTo("hello"));
     * 
     * }
     * </pre>
     * 
     * 
     * @param toCoerce Object to convert to a Mappable
     * @return  Mappable instance
     */
    public static Mappable asMappable(final Object toCoerce) {
        return new CoercedMappable(
                                   toCoerce);
    }

    @AllArgsConstructor
    public static class CoercedMappable implements Mappable {
        private final Object dValue;

        @Override
        public Object unwrap() {
            return dValue;
        }
    }
}
