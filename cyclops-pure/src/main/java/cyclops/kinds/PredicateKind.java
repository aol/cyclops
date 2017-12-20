package cyclops.kinds;

import com.oath.cyclops.hkt.DataWitness;
import com.oath.cyclops.hkt.DataWitness.predicate;
import com.oath.cyclops.hkt.Higher;

import java.util.function.Predicate;

public interface PredicateKind<T> extends Higher<predicate,T>,Predicate<T> {

  public static <T> PredicateKind<T> narrow(Higher<predicate,T> ds){
    return (PredicateKind<T>)ds;
  }
}
