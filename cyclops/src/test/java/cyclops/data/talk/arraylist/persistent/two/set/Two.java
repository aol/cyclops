package cyclops.data.talk.arraylist.persistent.two.set;

import cyclops.control.Option;

import java.util.Arrays;

public class Two<E> {

    public static final int bitShiftDepth =5;
    static final int SUBARRAY_SIZE =32;

    //An array of up to 32 arrays each containing 32 elements
    private final Object[][] array;
    private final int size;

    public Two(int size, Object[][] array) {
        this.array = array;
        this.size =size;
    }



    public Option<Two<E>> set(int index, E t) {
        if(index<0 || index >size)
            return Option.none();
        Object[][] updatedNodes = Arrays.copyOf(array, array.length);
        int nodeIndex = (index >>> bitShiftDepth) & (SUBARRAY_SIZE -1);;
        Object[] e = updatedNodes[nodeIndex];
        Object[] newNode = Arrays.copyOf(e,e.length);
        updatedNodes[nodeIndex] = newNode;
        newNode[index & (SUBARRAY_SIZE -1)]=t;
        return Option.some(new Two<>(size,updatedNodes));

    }

}

