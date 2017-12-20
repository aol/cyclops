package cyclops.data;


import com.oath.cyclops.hkt.Higher;

public interface Eq<CRE>{

    default <T> boolean equals(Higher<CRE,T> c1, Higher<CRE,T> c2){
        return c1.equals(c2);
    }

    default <T> boolean notEquals(Higher<CRE,T> c1, Higher<CRE,T> c2){
        return !equals(c1,c2);
    }
}
