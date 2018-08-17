package cyclops.data.talk;

import cyclops.data.Vector;

public class VectorExample {

    public static void main(String[] args){

        Vector<Integer> v = Vector.of(1,2,3);

        System.out.println("Out of range : " + v.get(-1));

        Vector<Integer> v2 = v.append(10);

        System.out.println("First " + v);
        System.out.println("Second " + v2);


        System.out.println(v2.set(5,100));

        System.out.println(v.delete(-100));


    }
}
