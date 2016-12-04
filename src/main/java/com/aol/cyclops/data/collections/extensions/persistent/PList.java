package com.aol.cyclops.data.collections.extensions.persistent;



import java.util.AbstractSequentialList;
import java.util.Collection;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.function.Function;

import org.pcollections.ConsPStack;
import org.pcollections.PStack;

import com.aol.cyclops.Reducers;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.control.Trampoline;
import com.aol.cyclops.data.collections.extensions.standard.ListX;

public interface PList {
    
    public static void main(String[] args){
       System.out.println("minus " + Cons.from(ListX.range(0, 5_000))
             .minus(new Integer(0)));
       /**
        System.out.println(PStackX.of(1,2,3));
        PStackX<Integer> reved = reverse(PStackX.range(0, 5_000),PStackX.of());
        PStackX<Integer> summed = sum(PStackX.range(0, 5_000),0);**/
    //    System.out.println(summed); 
      // System.out.println("reversed = " +reved);
    //     System.out.println(reved.getClass());
  //      PStackXImplLazy<Integer> stack = (PStackXImplLazy)reved;
   //     System.out.println(stack.getStack());
    }
    static PStackX<Integer> sum(PStackX<Integer> list,int total){
        System.out.println(list.size());
        return list.tailRec(s->s.size() ==0 ? PStackX.of(total) :sum(s.tail(),s.head()+total));
    }
    static PStackX<Integer> reverse(PStackX<Integer> list, PStack<Integer> append){
       return list.tailRec(s->s.size() ==0 ? s.plusAll(0, append) : reverse(s.tail(),append.plus(s.head())));                                             
    }
    static final class Nil<T> extends Cons<T> {
        private static final Cons<?> EMPTY = new Nil<>();
        private Nil(){
           
        }
        public <R> R visit(Function<? super NonEmptyList<T>,? extends R> nel,
                Function<? super Nil<T>,? extends R> nil){
            return nil.apply(this);
       }
        
    }
    
    static final class NonEmptyList<T> extends Cons<T> {
        

        private NonEmptyList(final T first, final Cons<T> rest) {
            super(first,rest);
            
        }
        public T head(){
            return get(0);
        }
        public Cons<T> tail(){
            return minus(0);
        }
        public <R> R visit(Function<? super NonEmptyList<T>,? extends R> nel,
                Function<? super Nil<T>,? extends R> nil){
            return nel.apply(this);
        }
        
    }
    /**
     * 
     * A simple persistent stack of non-null values.
     * <p>
     * This implementation is thread-safe (assuming Java's AbstractSequentialList is thread-safe),
     * although its iterators may not be.
     * 
     * Modified to separate NonEmptyList and Nil
     * More efficient tail() implmentation
     * Support removal from longer lists
     * 
     * @author harold
     *
     * 
     */
    static abstract class Cons<T> extends AbstractSequentialList<T>implements PStack<T> {

        private final T head;
        private final Cons<T> tail;
        private final int size;

      
       
        public static <T> Cons<T> empty() {
            return (Cons<T>) Nil.EMPTY;
        }

        
        public static <T> NonEmptyList<T> singleton(final T e) {
            return new NonEmptyList<T>(e,Cons.<T> empty());
        }
        
        public static <T> NonEmptyList<T> nel(PStack<T> pstack, T defaultValue){
            if(pstack instanceof NonEmptyList)
                return (NonEmptyList<T>)pstack;
            if(pstack instanceof Nil || pstack.size()==0)
                return singleton(defaultValue);
            Cons<T> result =  empty();
            return nel(ReactiveSeq.fromList(pstack).reverse().mapReduce(Reducers.toPStack()),defaultValue);
                
            
        }

        
        
        public static <T> Cons<T> from(final Collection<? extends T> list) {
            if (list instanceof Cons)
                return (Cons<T>) list; 
            return from(list.iterator());
        }

        private static <T> Cons<T> from(final Iterator<? extends T> it) {
            if (!it.hasNext())
                return empty();
            Cons<T> cons = empty();
            while(it.hasNext()){
                cons = cons.plus(it.next());
            }
            return cons;
            
        }

       

        private Cons(){
            size = 0; 
            head=null; 
            tail=null;
        }
        
        private Cons(final T first, final Cons<T> rest) {
            this.head = first;
            this.tail = rest;
            size =  rest.size+1;
        }

        @Override
        public int size() {
            return size;
        }


        @Override
        public ListIterator<T> listIterator(final int index) {
            if (index < 0 || index > size)
                throw new IndexOutOfBoundsException();

            return new ListIterator<T>() {
                int i = index;
                Cons<T> next = subList(index);

                public boolean hasNext() {
                    return next.size > 0;
                }

                public boolean hasPrevious() {
                    return i > 0;
                }

                public int nextIndex() {
                    return index;
                }

                public int previousIndex() {
                    return index - 1;
                }

                public T next() {
                    T e = next.head;
                    next = next.tail;
                    return e;
                }

                public T previous() {
                    System.err.println("Cons.listIterator().previous() is inefficient, don't use it!");
                    next = subList(index - 1); // go from beginning...
                    return next.head;
                }

                public void add(final T o) {
                    throw new UnsupportedOperationException();
                }

                public void remove() {
                    throw new UnsupportedOperationException();
                }

                public void set(final T o) {
                    throw new UnsupportedOperationException();
                }
            };
        }

        @Override
        public Cons<T> subList(final int start, final int end) {
            if (start < 0 || end > size || start > end)
                throw new IndexOutOfBoundsException();
            if (end == size) // want a substack
                return subList(start); // this is faster
            if (start == end) // want nothing
                return empty();
            if (start == 0) // want the current element
                return new NonEmptyList<T>(
                                   head, tail.subList(0, end - 1));
            // otherwise, don't want the current element:
            return tail.subList(start - 1, end - 1);
        }

        public Cons<T> plus(final T e) {
            return new NonEmptyList<T>(
                               e, this);
        }

        public Cons<T> plusAll(final Collection<? extends T> list) {
            Cons<T> result = this;
            for (T e : list)
                result = result.plus(e);
            return result;
        }

        public Cons<T> plus(final int i, final T e) {
            if (i < 0 || i > size)
                throw new IndexOutOfBoundsException();
            if (i == 0) 
                return plus(e);
            return new NonEmptyList<T>(
                               head, tail.plus(i - 1, e));
        }

        public Cons<T> plusAll(final int i, final Collection<? extends T> list) {
            if(list.isEmpty())
                return this;
            if (i < 0 || i > size)
                throw new IndexOutOfBoundsException();
            if (i == 0)
                return plusAll(list);
            return new NonEmptyList<T>(
                               head, tail.plusAll(i - 1, list));
        }
        
        public Cons<T> minus(final Object e) {
            if(size==0)
                return this;
            if(head.equals(e)) 
                return tail; 
            T headToUse = head;
            return findTail(headToUse,this,e).get(); //use  a trampoline in case the list is large
        }
        private Trampoline<Cons<T>> findTail(T head,Cons<T> cons,Object o) {
            if (cons.head.equals(o))
                return Trampoline.done(new NonEmptyList<>(head,cons.tail));
            T headToUse = cons.head;
            return Trampoline.more(()-> findTail(headToUse,cons.tail,o));
        }

        public Cons<T> minus(final int i) {
            if (size == 0)
                return this;
            if (i == 0) //pretty efficient for tail()
                return tail;
          
            
            T headToUse = head;
            return findTail(headToUse,this,i-1).get(); //use  a trampoline in case the list is large
         
        }

        private Trampoline<Cons<T>> findTail(T head,Cons<T> cons,int i) {
            if (i == 0)
                return Trampoline.done(new NonEmptyList<>(head,cons.tail));
            T headToUse = cons.head;
            return Trampoline.more(()-> findTail(headToUse,cons.tail,i-1));
        }


        public Cons<T> minusAll(final Collection<?> list) {
            if(list.isEmpty())
                return this;
            if (size == 0)
                return this;
            
            if(list.contains(head)) 
                return findTail(tail.head,tail.tail,list).get();
           
            return findTail(head,this,list).get();
        }
        private Trampoline<Cons<T>> findTail(T head,Cons<T> cons,Collection<?> list) {
            if (list.contains(cons.head))
                return Trampoline.done(new NonEmptyList<>(head,cons.tail));
            T headToUse = cons.head;
            return Trampoline.more(()-> findTail(headToUse,cons.tail,list));
        }

        public Cons<T> with(final int i, final T e) {
            if (i < 0 || i >= size)
                throw new IndexOutOfBoundsException();
            if (i == 0) {
                if (head.equals(e))
                    return this;
                return new NonEmptyList<T>(
                                   e, tail);
            }
            Cons<T> newRest = tail.with(i - 1, e);
            if (newRest == tail)
                return this;
            return new NonEmptyList<T>(
                               head, newRest);
        }
        

        public Cons<T> subList(final int start) {
            if (start < 0 || start > size)
                throw new IndexOutOfBoundsException();
            if (start == 0)
                return this;
            return tail.subList(start - 1);
        }

    }
}
