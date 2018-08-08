package cyclops.data.talk.arraylist.mutable.set;

public class MutableArrayList<E> {

    private int size;
    private E[] elementData;
    private int modCount;


    public E set(int index, E e) {
        rangeCheck(index);
        checkForComodification();
        E oldValue = elementData[index];
        elementData[index] = e;
        return oldValue;
    }


    private void checkForComodification() {
        //check for concurrent modification
    }


    private void rangeCheck(int index) {
        if (index < 0 || index >= this.size)
            throw new IndexOutOfBoundsException("Index: "+index+", Size: "+this.size);
    }

}



