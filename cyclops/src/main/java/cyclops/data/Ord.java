package cyclops.data;

import com.oath.cyclops.hkt.Higher;
import lombok.AllArgsConstructor;

import java.util.Comparator;

import static cyclops.data.Ord.Ordering.*;


public interface Ord<W,T> {
    public enum Ordering {LESS, EQUAL, MORE}


    Ordering compare(Higher<W,T> first, Higher<W,T> second);

    public static <W,T> Ord<W,T> ord(Comparator<Higher<W,T>> comp){
        return new OrdByComparotor(comp);
    }
    @AllArgsConstructor
    public static class OrdByComparotor<W,T> implements Ord<W,T>{
        private final Comparator<Higher<W,T>> comp;


        @Override
        public Ordering compare(Higher<W, T> first, Higher<W, T> second) {
            int pos =  comp.compare(first,second);
            if(pos < 0)
                return LESS;
            if(pos>0)
                return MORE;
            return EQUAL;
        }
    }
}
