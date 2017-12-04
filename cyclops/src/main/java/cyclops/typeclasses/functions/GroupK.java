package cyclops.typeclasses.functions;


import com.oath.cyclops.hkt.Higher;
import cyclops.function.Group;
import cyclops.function.Monoid;

import java.util.function.Function;

public interface GroupK<W> extends MonoidK<W> {


    <T> Higher<W, T> invert(Higher<W, T> wtHigher);

  default <T> Group<Higher<W,T>> asGroup(){
    return Group.of(a->invert(a),asMonoid());
  }


}
