package cyclops.data.talk.arraylist.mutable.get;

public class MutableArrayList<E> {

    private int size;
    private E[] elementData;


    public E get(int index) {
        rangeCheck(index);
        return (E) elementData[index];
    }


    private void rangeCheck(int index) {
        if (index < 0 || index >= this.size)
            throw new IndexOutOfBoundsException("Index: "+index+", Size: "+this.size);
    }


}


