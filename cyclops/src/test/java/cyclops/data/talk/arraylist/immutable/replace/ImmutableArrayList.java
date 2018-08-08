package cyclops.data.talk.arraylist.immutable.replace;

import java.util.Arrays;

public class ImmutableArrayList<E> {

    private final E[] elementData;

    private ImmutableArrayList(E[] elementData){
        this.elementData=elementData;
    }


    public ImmutableArrayList<E> set(int index, E e){
        E[] array = Arrays.copyOf(elementData,elementData.length);
        array[index]=e;
        return new ImmutableArrayList<>(array);
    }
}

