package cyclops.data.talk.arraylist.persistent.one.get;

import cyclops.control.Option;

public class One<E> {

    private final E[] array;

    public One(E[] array) {
        this.array = array;
    }

    public Option<E> get(int index) {
        if(index>=0 && index<array.length)
            return Option.some((E)array[index]);
        return Option.none();
    }


}


/**
    public E getOrElse(int pos, E alt) {
        int index = pos & 0x01f;
        if(index>=0 && index<array.length)
            return (E)array[index];
        return alt;
    }
 **/
