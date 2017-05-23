package com.aol.cyclops2.types.mixins;

/**
 * Mixin a print method with a return value (can be used as a method reference to
 * methods that accept Functions).
 * 
 * @author johnmcclean
 *
 */
public interface Printable {

    /**
     * Print the supplied value  to the console
     * 
     * <pre>
     * {@code 
     *  class MyClass implements Printable {
     *  
     *  public void myMethod(){
     *     Optional.of(10)
                .map(i->print("optional " + (i+10)));
                
         }
     *   
     *  }
     * 
     * }
     * </pre>
     * 
     * @param object Value to print
     * @return Supplied value
     */
    default <T> T print(final T object) {
        System.out.println(object);
        return object;
    }

    static <T> T println(final T object){
        System.out.println(object);
        return object;
    }
}
