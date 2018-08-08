package cyclops.data.talk.arraylist.notneeded;

import java.util.Arrays;

public class PersistentListTwoAppend<E> {

    public static final int bitShiftDepth =5;
    static final int SIZE =32;
    private final Object[][] array; //An array of 32 arrays each potentially containing 32 elements

    public PersistentListTwoAppend(Object[][] array) {
        this.array = array;
    }


    public PersistentListTwoAppend<E> append(PersistentTail<E> tail) {
        if(array.length<32){
            Object[][] updatedNodes = Arrays.copyOf(array, array.length+1,Object[][].class);
            updatedNodes[array.length]=tail.getArray();
            return new PersistentListTwoAppend<>(updatedNodes);
        }
        return null;// create PersistentListThree!!;

    }


}

