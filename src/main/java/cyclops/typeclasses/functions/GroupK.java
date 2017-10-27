package cyclops.typeclasses.functions;


import com.oath.cyclops.hkt.Higher;
import cyclops.function.Group;

import java.util.function.Function;

public interface GroupK<W,T> extends MonoidK<W,T>, Group<Higher<W,T>> {

    @Override
    Higher<W, T> invert(Higher<W, T> wtHigher);

    public static <W,T> GroupK<W,T> of(Function<? super Higher<W, T> ,? extends Higher<W,T>> inv, MonoidK<W, T> sg){
         return new GroupK<W, T>() {
             @Override
             public Higher<W, T> invert(Higher<W, T> wtHigher) {
                 return inv.apply(wtHigher);
             }

             @Override
             public Higher<W, T> zero() {
                 return sg.zero();
             }

             @Override
             public Higher<W, T> apply(Higher<W, T> t1, Higher<W, T> t2) {
                 return sg.apply(t1,t2);
             }
         };
     }

}
