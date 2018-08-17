package cyclops.data.talk.linkedlist;

import lombok.AllArgsConstructor;


public  interface Seq<E> {

    @AllArgsConstructor
    static class Cons<E> implements Seq<E> {

        public final E head;
        private final Seq<E> tail;

    }

    static class Nil<E> implements Seq<E>{

    }

}

