package cyclops.data.talk.arraylist.persistent.two.stub;

public class Two<E> {

    public static final int bitShiftDepth =5;
    static final int SIZE =32;
    private final Object[][] array; //An array of 32 arrays each potentially containing 32 elements

    public Two(Object[][] array) {
        this.array = array;
    }


    private E[] getNestedArrayAtIndex(int index) {
        int arrayIndex = (index >>> bitShiftDepth) & (SIZE-1);
        if(arrayIndex<array.length)
            return  (E[])array[arrayIndex];
        return (E[])new Object[0];

    }



}

