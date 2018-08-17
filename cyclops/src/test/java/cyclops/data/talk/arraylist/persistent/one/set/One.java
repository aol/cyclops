package cyclops.data.talk.arraylist.persistent.one.set;

import cyclops.control.Either;
import cyclops.control.Option;

import java.util.Arrays;

public class One<E> {

    private final E[] array; //an array of 32 values


    public One(E[] array) {
        this.array = array;
    }

    public Option<One<E>> set(int pos, E t) {
        if(pos<0||pos>31)
            return Option.none();
        E[] updatedNodes = Arrays.copyOf(array, array.length);
        updatedNodes[pos] = t;
        return Option.some(new One<>(updatedNodes));
    }
}




