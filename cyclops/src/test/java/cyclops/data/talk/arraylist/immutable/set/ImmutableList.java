package cyclops.data.talk.arraylist.immutable.set;

public class ImmutableList<E> {

    private final E[] elementData;

    private ImmutableList(E[] elementData){
        this.elementData=elementData;
    }

    public E set(int index,E e) {
        throw new UnsupportedOperationException("Set is not supported");
    }


}


