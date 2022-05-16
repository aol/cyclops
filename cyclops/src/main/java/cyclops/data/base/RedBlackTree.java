package cyclops.data.base;


import com.oath.cyclops.matching.Deconstruct.Deconstruct5;
import com.oath.cyclops.matching.Sealed2;
import cyclops.control.Option;

import cyclops.reactive.ReactiveSeq;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import cyclops.data.tuple.Tuple;
import cyclops.data.tuple.Tuple2;
import cyclops.data.tuple.Tuple3;
import cyclops.data.tuple.Tuple5;
import lombok.experimental.Wither;


import java.io.Serializable;
import java.util.Comparator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static cyclops.data.base.RedBlackTree.Node.*;


public interface RedBlackTree extends Serializable{
    static <K,V> Tree<K,V> rootIsBlack(Tree<K,V> root){
        return switch(root){
            case Node<K,V> node -> node.withBlack(true);
            case Leaf<K,V> leaf -> leaf;
            default ->  root;
        };

    }
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
        default boolean isRed(){
            return !isBlack();
        }
        Option<V> get(K key);
        V getOrElse(K key, V alt);
        V getOrElseGet(K key, Supplier<? extends V> alt);
        Tree<K,V> plus(K key, V value);
        Tree<K,V> minus(K key);
        Comparator<? super K> comparator();
        ReactiveSeq<Tuple2<K,V>> stream();
        int size();
        String tree();




        default Tree<K,V> balance(boolean isBlack, Tree<K, V> left, Tree<K, V> right, K key, V value){


            if(isBlack && !isEmpty())
            {
                if(left.isRed() && !left.isEmpty()){
                    Node<K,V> leftNode = left.fold(n->n, leaf->//unreachable
                            null);
                    if(!leftNode.left.isBlack() && !leftNode.left.isEmpty()){
                        Node<K,V> nestedLeftNode = leftNode.left.fold(n->n, leaf->//unreachable
                                null);

                        return RED(
                                LEFT_BLACK(nestedLeftNode.left,nestedLeftNode.right,nestedLeftNode.key,nestedLeftNode.value,comparator()),
                                RIGHT_BLACK(leftNode.right,right,key,value,comparator()),leftNode.key,leftNode.value,comparator());
                    }
                    if(!leftNode.right.isBlack() && !leftNode.right.isEmpty()){
                        Node<K,V> nestedRightNode = leftNode.right.fold(n->n, leaf->//unreachable
                                null);

                        return RED(
                                LEFT_BLACK(leftNode.left,nestedRightNode.left,leftNode.key,leftNode.value,comparator()),
                                RIGHT_BLACK(nestedRightNode.right,right,key,value,comparator()),
                                nestedRightNode.key,nestedRightNode.value,comparator());
                    }
                }
                if(right.isRed() && !right.isEmpty()){

                    Node<K,V> rightNode = right.fold(n->n, leaf->//unreachable
                            null);
                    if(rightNode.left.isRed() && !rightNode.left.isEmpty()){
                        Node<K,V> nestedLeftNode = rightNode.left.fold(n->n, leaf->//unreachable
                                null);
                        return RED(
                                LEFT_BLACK(left,nestedLeftNode.left,key,value,comparator()),
                                RIGHT_BLACK(nestedLeftNode.right,rightNode.right,rightNode.key,rightNode.value,comparator()),nestedLeftNode.key,nestedLeftNode.value,comparator());

                    }
                    if(rightNode.right.isRed() && !rightNode.right.isEmpty()){
                        Node<K,V> nestedRightNode = rightNode.right.fold(n->n, leaf->//unreachable
                                null);

                        Node<K,V> res =  RED(
                                LEFT_BLACK(left,rightNode.left,key,value,comparator()),
                                RIGHT_BLACK(nestedRightNode.left,nestedRightNode.right,nestedRightNode.key,nestedRightNode.value,comparator()),
                                rightNode.key,rightNode.value,comparator());

                        return res;
                    }
                }

            }

            return new Node(isBlack,left,right,key,value,comparator());
        }
    }

    @AllArgsConstructor
    @Wither
    public static final class Node<K,V> implements Tree<K,V>, Deconstruct5<Boolean,Tree<K,V>,Tree<K,V>, K,V> {
        private final boolean isBlack;
        private final Tree<K,V> left;
        private final Tree<K,V> right;
        private final K key;
        private final V value;
        private final Comparator<K> comp;

        private static final long serialVersionUID = 1L;


        static <K,V> Node<K,V> RED(Tree<K,V> left, Tree<K,V> right,K key, V value,Comparator<? super K> comp){
            return new Node(false,left,right,key,value,comp);
        }
        static <K,V> Node<K,V> BLACK(Tree<K,V> left, Tree<K,V> right,K key, V value,Comparator<? super K> comp){
            return new Node(true,left,right,key,value,comp);
        }
        static <K,V> Node<K,V> LEFT_BLACK(Tree<K,V> left, Tree<K,V> right,K key, V value,Comparator<? super K> comp){
            return new Node(true,left,right,key,value,comp);
        }
        static <K,V> Node<K,V> RIGHT_BLACK(Tree<K,V> left, Tree<K,V> right,K key, V value,Comparator<? super K> comp){
            return new Node(true,left,right,key,value,comp);
        }

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
        public V getOrElseGet(K key,Supplier<? extends V> alt) {
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
            if (compRes>0) {
                return balance(isBlack, left.plus(key, value), right, this.key, this.value);
            }
            else if (compRes==0)
                return new Node(isBlack, left,right, key, value,comp);

            Tree<K, V> n = balance(isBlack, left, right.plus(key, value), this.key, this.value);

            return n;
        }

        @Override
        public String tree() {
            String value = (this.isBlack ? "BLACK" : "RED") + ":" + this.value;
            String left = this.left.isEmpty() ? "" : " " + this.left.tree();
            String right = this.right.isEmpty() ? "" : " " + this.right.tree();
             return "{" + value + left + right + "}";
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
    public static final class Leaf<K,V> implements Tree<K,V> {
        private static final long serialVersionUID = 1L;
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
        public V getOrElseGet(K key, Supplier<? extends V> alt) {
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
        public String tree() {
            return "{LEAF}";
        }

        @Override
        public <R> R fold(Function<? super Node<K, V>, ? extends R> fn1, Function<? super Leaf<K, V>, ? extends R> fn2) {
            return fn2.apply(this);
        }

    }
}
