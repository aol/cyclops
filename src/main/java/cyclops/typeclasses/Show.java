package cyclops.typeclasses;


import com.aol.cyclops2.hkt.Higher;

public interface Show<W> {

    default <T> String show(Higher<W,T> ds){
        return ds.toString();
    }
}
