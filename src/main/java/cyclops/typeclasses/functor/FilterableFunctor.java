package cyclops.typeclasses.functor;

import cyclops.typeclasses.Filterable;

public interface FilterableFunctor<CRE> extends Functor<CRE>, Filterable<CRE> {

}
