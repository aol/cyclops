package cyclops.typeclasses;


import com.oath.cyclops.hkt.Higher;

public interface Show<W> {

    default <T> String show(Higher<W,T> ds){
        return ds.toString();
    }
}
