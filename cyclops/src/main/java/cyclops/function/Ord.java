package cyclops.function;


import lombok.AllArgsConstructor;

import java.util.Comparator;

import static cyclops.function.Ord.Ordering.*;


public interface Ord<T> {
    enum Ordering {LESS, EQUAL, MORE}


    Ordering compare(T first, T second);

    static <T> Ord<T> ord(Comparator<T> comp){
        return new OrdByComparotor(comp);
    }
    @AllArgsConstructor
    class OrdByComparotor<T> implements Ord<T>{
        private final Comparator<T> comp;


        @Override
        public Ordering compare(T first, T second) {
            int pos =  comp.compare(first,second);
            if(pos < 0)
                return LESS;
            if(pos>0)
                return MORE;
            return EQUAL;
        }
    }
}
