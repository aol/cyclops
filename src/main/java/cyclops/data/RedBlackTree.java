package cyclops.data;


import com.aol.cyclops2.matching.Deconstruct.Deconstruct5;
import com.aol.cyclops2.matching.Sealed2;
import cyclops.control.Option;

import cyclops.stream.ReactiveSeq;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import cyclops.collections.tuple.Tuple;
import cyclops.collections.tuple.Tuple2;
import cyclops.collections.tuple.Tuple3;
import cyclops.collections.tuple.Tuple5;

import java.util.Comparator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;


public interface RedBlackTree {

    public static <K,V> Tree<K,V> fromStream(Comparator<? super K> comp, Stream<? extends Tuple2<? extends K, ? extends V>> stream){
        Tree<K,V> tree[] = new Tree[1];
        tree[0]= new Leaf(comp);
        stream.forEach(t->{
            tree[0] = tree[0].plus(t._1(),t._2());
        });
        return tree[0];
    }
    public static <K,V> Tree<K,V> empty(Comparator<? super K> comp){
        return new Leaf<K,V>(comp);
    }
    public static interface Tree<K,V> extends Sealed2<Node<K,V>,Leaf<K,V>> {

        boolean isEmpty();
        boolean isBlack();
        Option<V> get(K key);
        V getOrElse(K key, V alt);
        V getOrElseGet(K key, Supplier<V> alt);
        Tree<K,V> plus(K key, V value);
        Tree<K,V> minus(K key);
        Comparator<? super K> comparator();
        ReactiveSeq<Tuple2<K,V>> stream();
        int size();



        default Tree<K,V> balance(boolean isBlack, Tree<K, V> left, Tree<K, V> right, K key, V value){



            if(isBlack && !isEmpty()){
                if(!left.isBlack() && !left.isEmpty()){
                    Node<K,V> leftNode = left.fold(n->n, leaf->//unreachable
                            null);
                    if(!leftNode.left.isBlack() && !leftNode.left.isEmpty()){
                        Node<K,V> nestedLeftNode = leftNode.left.fold(n->n, leaf->//unreachable
                                null);
                        return new Node(false,
                                new Node(true,nestedLeftNode.left,nestedLeftNode.right,nestedLeftNode.key,nestedLeftNode.value,comparator()),
                                new Node(true,leftNode.right,right,key,value,comparator()),leftNode.key,leftNode.value,comparator());
                    }
                    if(!leftNode.right.isBlack() && !leftNode.right.isEmpty()){
                        Node<K,V> nestedRightNode = leftNode.right.fold(n->n, leaf->//unreachable
                                null);
                        return new Node(false,
                                new Node(true,leftNode.left,nestedRightNode.left,leftNode.key,leftNode.value,comparator()),
                                new Node(true,nestedRightNode.right,right,key,value,comparator()),
                                nestedRightNode.key,nestedRightNode.value,comparator());
                    }
                }
                else if(!right.isBlack() && !right.isEmpty()){

                    Node<K,V> rightNode = right.fold(n->n, leaf->//unreachable
                            null);
                    if(!rightNode.left.isBlack() && !rightNode.left.isEmpty()){
                        Node<K,V> nestedLeftNode = rightNode.left.fold(n->n, leaf->//unreachable
                                null);
                        return new Node(false,
                                new Node(true,left,nestedLeftNode.left,key,value,comparator()),
                                new Node(true,nestedLeftNode.right,rightNode.right,rightNode.key,rightNode.value,comparator()),nestedLeftNode.key,nestedLeftNode.value,comparator());
                    }
                    if(!rightNode.right.isBlack() && !rightNode.right.isEmpty()){
                        Node<K,V> nestedRightNode = rightNode.right.fold(n->n, leaf->//unreachable
                                null);
                        return new Node(false,
                                new Node(true,left,nestedRightNode.left,key,value,comparator()),
                                new Node(true,nestedRightNode.left,nestedRightNode.right,nestedRightNode.key,nestedRightNode.value,comparator()),
                                rightNode.key,rightNode.value,comparator());
                    }
                }

            }

            return new Node(isBlack,left,right,key,value,comparator());
        }
    }

    @AllArgsConstructor
    public static class Node<K,V> implements Tree<K,V>, Deconstruct5<Boolean,Tree<K,V>,Tree<K,V>, K,V> {
        private final boolean isBlack;
        private final Tree<K,V> left;
        private final Tree<K,V> right;
        private final K key;
        private final V value;
        private final Comparator<K> comp;



        public Tree<K, V> left() {
            return left;
        }


        public Tree<K, V> right() {
            return right;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public boolean isBlack() {
            return isBlack;
        }

        @Override
        public Option<V> get(K key) {
            int compRes = comp.compare(this.key,key);
            if (compRes>0)
                return left.get(key);
            else if (compRes==0)
                return Option.of(value);
            return right.get(key);
        }

        @Override
        public V getOrElse(K key, V alt) {
            int compRes = comp.compare(this.key,key);
            if (compRes>0)
                return left.getOrElse(key,alt);
            else if (compRes==0)
                return value;
            return right.getOrElse(key,alt);
        }

        @Override
        public V getOrElseGet(K key,Supplier<V> alt) {
            int compRes = comp.compare(this.key,key);
            if (compRes>0)
                return left.getOrElseGet(key,alt);
            else if (compRes==0)
                return value;
            return right.getOrElseGet(key,alt);
        }

        @Override
        public Tree<K, V> plus(K key, V value) {
            int compRes = comp.compare(this.key,key);
            if (compRes>0)
                return balance(isBlack, left.plus(key, value), right,this.key, this.value);
            else if (compRes==0)
                return new Node(isBlack, left,right, key, value,comp);

            return balance(isBlack, left, right.plus(key, value),this.key, this.value);
        }

        @Override
        public Tree<K, V> minus(K key) {
            int compRes = comp.compare(this.key,key);
            if (compRes>0)
                return balance(isBlack, left.minus(key), right,this.key, this.value);
            else if (compRes==0){
               return left.fold(leftNode->{
                    return right.fold(rightNode->{
                        Tuple3<Tree<K, V>, K, V> t3 = rightNode.removeMin();
                        return balance(isBlack,left,t3._1(),t3._2(),t3._3());
                    },leftLeaf->left);

                },leftLeaf->{
                    return right.fold(rightNode->right, leftNode->new Leaf(comp));
                });

            }
            return balance(isBlack, left,right.minus(key), this.key, this.value);

        }

        public Tuple3<Tree<K, V>,K,V> removeMin() {
            return left.fold(node->{
                Tuple3<Tree<K, V>, K, V> t3 = node.removeMin();
                return Tuple.tuple(balance(isBlack, t3._1(), right, key, value),t3._2(),t3._3());
            },leaf->Tuple.tuple(right,key,value));
        }

        @Override
        public Tuple5<Boolean, Tree<K, V>, Tree<K, V>, K, V> unapply() {
            return Tuple.tuple(isBlack,left,right,key,value);
        }

        @Override
        public <R> R fold(Function<? super Node<K, V>, ? extends R> fn1, Function<? super Leaf<K, V>, ? extends R> fn2) {
            return fn1.apply(this);
        }
        @Override
        public Comparator<K> comparator() {
            return comp;
        }
        public ReactiveSeq<Tuple2<K,V>> stream(){
            ReactiveSeq<Tuple2<K, V>> current = ReactiveSeq.of(Tuple.tuple(key, value));
            return ReactiveSeq.concat(left.stream(),current,right.stream());
        }

        @Override
        public int size() {
            return left.size() +right.size() +1;
        }
    }
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Leaf<K,V> implements Tree<K,V> {
        private final Comparator<? super K> comp;
        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public boolean isBlack() {
            return true;
        }

        @Override
        public Option<V> get(K key) {
            return Option.none();
        }

        @Override
        public V getOrElse(K key, V alt) {
            return alt;
        }

        @Override
        public V getOrElseGet(K key, Supplier<V> alt) {
            return alt.get();
        }

        @Override
        public Tree<K, V> plus(K key, V value) {
            return new Node(false,new Leaf<>(comp),new Leaf<>(comp),key,value,comp);
        }

        @Override
        public Tree<K, V> minus(K key) {
            return this;
        }

        @Override
        public Comparator<? super K> comparator() {
            return comp;
        }

        @Override
        public ReactiveSeq<Tuple2<K, V>> stream() {
            return ReactiveSeq.empty();
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public <R> R fold(Function<? super Node<K, V>, ? extends R> fn1, Function<? super Leaf<K, V>, ? extends R> fn2) {
            return fn2.apply(this);
        }

    }
}
