package cyclops.data.talk.arraylist.persistent.two.get;

import java.util.Collections;

public class Two<E> {

    public static final int bitShiftDepth =5;
    static final int SIZE =32;
    private final Object[][] array; //An array of 32 arrays each potentially containing 32 elements

    public Two(Object[][] array) {
        this.array = array;
    }


    public E getOrElse(int index, E alt) {
        E[] local = getNestedArrayAtIndex(index);
        int localIndex = index & 0x01f;
        if(localIndex<local.length){
            return local[localIndex];
        }
        return alt;
    }

    public static void main(String[] args){

        Object[] first = {0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31};
        Object[][]  array= {first,first};
        for(int i=0;i<100;i++)
            System.out.println("I is " + i + "  "  + new Two<>(array).getOrElse(i,-1));
    }


    private E[] getNestedArrayAtIndex(int index) {
        int arrayIndex = (index >>> bitShiftDepth) & (SIZE-1);
        return  (E[])array[arrayIndex];

    }
}

