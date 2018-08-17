package cyclops.data.talk.arraylist.notneeded;

public class PersistentTailAppend<E> {

    private final E[] array;

    public PersistentTailAppend(E[] array) {
        this.array = array;
    }

    public PersistentTailAppend<E> append(E t) {
        if(array.length<32){
            return  new PersistentTailAppend<>(append(array,t));
        }
        return this;
    }
    public static <T> T[] append(T[] array, T value) {
        T[] newArray = (T[])new Object[array.length + 1];
        System.arraycopy(array, 0, newArray, 0, array.length);
        newArray[array.length] = value;
        return newArray;
    }
}



