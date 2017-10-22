package cyclops.data.base;


import com.aol.cyclops2.matching.Deconstruct.Deconstruct2;
import cyclops.control.Option;
import cyclops.data.ImmutableList;
import cyclops.data.LazySeq;
import cyclops.reactive.ReactiveSeq;
import lombok.AllArgsConstructor;
import cyclops.data.tuple.Tuple;
import cyclops.data.tuple.Tuple2;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Supplier;


@AllArgsConstructor
public class HAMT<K, V>  implements Serializable {

    private static final long serialVersionUID = 1L;
    static final int BITS_IN_INDEX = 5;
    static final int SIZE = (int) StrictMath.pow(2, BITS_IN_INDEX);
    static final int MIN_INDEX = 0;
    static final int MAX_INDEX = SIZE - 1;
    static final int MASK = (1 << BITS_IN_INDEX) - 1;

    public static <K,V> Node<K,V> empty(){
        return EmptyNode.Instance;
    }

   public interface Node<K,V> extends Serializable{



       public Node<K,V> plus(int bitShiftDepth, int hash, K key, V value);
       public Option<V> get(int bitShiftDepth, int hash, K key);
       public V getOrElse(int bitShiftDepth, int hash, K key, V alt);
       public V getOrElseGet(int bitShiftDepth, int hash, K key, Supplier<? extends V> alt);
       public Node<K,V> minus(int bitShiftDepth, int hash, K key);
       int size();
       LazySeq<Tuple2<K,V>> lazyList();
       ReactiveSeq<Tuple2<K, V>> stream();
   }

   public static final class EmptyNode<K,V> implements Node<K,V>{
       private static final long serialVersionUID = 1L;
     static final EmptyNode Instance = new EmptyNode();
       @Override
       public Node<K, V> plus(int bitShiftDepth, int hash, K key, V value) {
           return new ValueNode<>(hash,key,value);
       }

       @Override
       public Option<V> get(int bitShiftDepth, int hash, K key) {
           return Option.none();
       }

       @Override
       public V getOrElse(int bitShiftDepth, int hash, K key, V alt) {
           return alt;
       }

       @Override
       public V getOrElseGet(int bitShiftDepth, int hash, K key, Supplier<? extends V> alt) {
           return alt.get();
       }

       @Override
       public Node<K, V> minus(int bitShiftDepth,int hash, K key) {
           return this;
       }

       @Override
       public int size() {
           return 0;
       }

       @Override
       public LazySeq<Tuple2<K, V>> lazyList() {
           return LazySeq.empty();
       }

       @Override
       public ReactiveSeq<Tuple2<K, V>> stream() {
           return ReactiveSeq.empty();
       }
       public String toString(){
           return "[]";
       }
   }
   @AllArgsConstructor
   @EqualsAndHashCode
   public static final class ValueNode<K,V> implements Node<K,V>, Deconstruct2<K,V> {
       private static final long serialVersionUID = 1L;
       private final int hash;
       public final K key;
       public final V value;
       @Override
       public Node<K, V> plus(int bitShiftDepth, int hash, K key, V value) {
           ValueNode<K,V> newNode = new ValueNode<>(hash,key,value);
           return isMatch(hash, key) ? newNode : merge(bitShiftDepth,newNode);
       }

       private Node<K,V> merge(int bitShiftDepth,  ValueNode<K, V> that) {
           //hash each merge into a collision node if hashes are the same, otherwise store in new location under a BitsetNode
           if(hash==that.hash)
                return new CollisionNode<>(hash, LazySeq.of(Tuple.tuple(key,value),that.unapply()));
           //create new BitsetNode
           int mask1 = BitsetNode.mask(hash,bitShiftDepth);
           int mask2 = BitsetNode.mask(that.hash,bitShiftDepth);
           int posThis = BitsetNode.bitpos(mask1);
           int posThat = BitsetNode.bitpos(mask2);
           int newBitset = posThis | posThat;
           if(mask1==mask2) { //collision
              Node<K,V> merged = merge(bitShiftDepth+BITS_IN_INDEX,that);
              return new BitsetNode<>(newBitset,2,new Node[]{merged});
           }
           Node<K,V>[] ordered = mask1<mask2 ? new Node[]{this,that} : new Node[]{that,this};
           return new BitsetNode<>(newBitset,2,ordered);
       }


       @Override
       public Option<V> get(int bitShiftDepth, int hash, K key) {
           return isMatch(hash, key) ? Option.of(value) : Option.none();
       }

       @Override
       public V getOrElse(int bitShiftDepth, int hash, K key, V alt) {
           return isMatch(hash, key) ? value : alt;
       }

       @Override
       public V getOrElseGet(int bitShiftDepth, int hash, K key, Supplier<? extends V> alt) {
           return isMatch(hash, key) ? value : alt.get();
       }

       private boolean isMatch(int hash, K key) {
           return this.hash==hash && Objects.equals(this.key,key);
       }

       @Override
       public Node<K, V> minus(int bitShiftDepth,int hash,K key) {
           return isMatch(hash, key) ? EmptyNode.Instance : this;
       }
       public int hash(){
           return hash;
       }

       @Override
       public int size() {
           return 1;
       }

       @Override
       public LazySeq<Tuple2<K, V>> lazyList() {
           return LazySeq.of(unapply());
       }

       @Override
       public ReactiveSeq<Tuple2<K, V>> stream() {
           return ReactiveSeq.of(Tuple.tuple(key,value));
       }

       @Override
       public Tuple2<K, V> unapply() {
           return Tuple.tuple(key,value);
       }
       public String toString(){
           return "[h:"+hash+",k:"+key+",v:"+value+"]";
       }
   }

    @EqualsAndHashCode
   public static final class CollisionNode<K,V> implements Node<K,V>{

       private final int hash;
       private final int size;
       private final ImmutableList<Tuple2<K,V>> bucket;

        public CollisionNode(int hash, ImmutableList<Tuple2<K, V>> bucket) {
            this.hash = hash;
            this.size = bucket.size();
            this.bucket = bucket;
        }

        private static final long serialVersionUID = 1L;
       @Override
       public Node<K, V> plus(int bitShiftDepth, int hash, K key, V value) {
           ImmutableList<Tuple2<K, V>> filtered = bucket.filter(t -> !Objects.equals(key, t._1()));

           if(this.hash==hash){
               return filtered.size()==0 ?  new ValueNode<>(hash,key,value) : new CollisionNode<>(hash,filtered.prepend(Tuple.tuple(key,value)));
           }
           return merge(bitShiftDepth,hash,new ValueNode<>(hash,key,value));
       }

        private Node<K,V> merge(int bitShiftDepth, int thatHash,Node<K, V> that) {
            //hash each merge into a collision node if hashes are the same, otherwise store in new location under a BitsetNode
            if(hash==thatHash)
                return new CollisionNode<>(hash,bucket.prependAll(that.lazyList()));
            //create new BitsetNode
            int mask1 = BitsetNode.mask(hash,bitShiftDepth);
            int mask2 = BitsetNode.mask(thatHash,bitShiftDepth);
            int posThis = BitsetNode.bitpos(mask1);
            int posThat = BitsetNode.bitpos(mask2);
            int newBitset = posThis | posThat;
            if(mask1==mask2) { //collision
                Node<K,V> merged = merge(bitShiftDepth+BITS_IN_INDEX,thatHash,that);
                return new BitsetNode<>(newBitset,2,new Node[]{merged});
            }
            Node<K,V>[] ordered = mask1<mask2 ? new Node[]{this,that} : new Node[]{that,this};
            return new BitsetNode<>(newBitset,2,ordered);
        }

        @Override
       public Option<V> get(int bitShiftDepth, int hash, K key) {
           if(this.hash==hash){
               return bucket.stream().filter(t->Objects.equals(key,t._1())).takeOne().map(Tuple2::_2);
           }
           return Option.none();
       }

        @Override
        public V getOrElse(int bitShiftDepth, int hash, K key, V alt) {
            return get(bitShiftDepth,hash,key).orElse(alt);
        }

        @Override
        public V getOrElseGet(int bitShiftDepth, int hash, K key, Supplier<? extends V> alt) {
            return get(bitShiftDepth,hash,key).orElseGet(alt);
        }

        @Override
       public Node<K, V> minus(int bitShiftDepth,int hash, K key) {
           if(this.hash==hash){
               return new CollisionNode<>(hash,bucket.filter(t->!Objects.equals(key,t._1())));
           }
           return this;
       }

        @Override
        public int size() {
            return bucket.size();
        }

        @Override
        public LazySeq<Tuple2<K, V>> lazyList() {
            return bucket.lazySeq();
        }

        @Override
        public ReactiveSeq<Tuple2<K, V>> stream() {
            return bucket.stream();
        }

        public String toString(){
           return "[h:"+hash+","+bucket.toString()+"]";
        }
    }
    @AllArgsConstructor
    @EqualsAndHashCode
   public static final class BitsetNode<K,V> implements Node<K,V>{
       private final int bitset;
       private final int size;
       private final Node<K,V>[] nodes;
        private static final long serialVersionUID = 1L;
        @Override
        public Node<K, V> plus(int bitShiftDepth, int hash, K key, V value) {
            int bitPos = bitpos(hash, bitShiftDepth);
            int arrayPos = index(bitPos);
            Node<K,V> node = (absent(bitPos) ? EmptyNode.Instance : nodes[arrayPos]).plus(bitShiftDepth +BITS_IN_INDEX,hash,key,value);
            if(absent(bitPos)) {
                int addedBit = bitset | bitPos;
                Node<K, V>[] addedNodes = new Node[nodes.length + 1];
                System.arraycopy(nodes, 0, addedNodes, 0, arrayPos);
                addedNodes[arrayPos] = node;
                System.arraycopy(nodes, arrayPos, addedNodes, arrayPos + 1, nodes.length - arrayPos);
                return new BitsetNode<>(addedBit, size(addedNodes), addedNodes);
            }else{
                Node<K,V>[] updatedNodes = Arrays.copyOf(nodes, nodes.length);
                updatedNodes[arrayPos] = node;
                return new BitsetNode<>(bitset, size(updatedNodes), updatedNodes);
            }

        }

        static int size(Node[] n){
            int res =0;
            for(Node next : n){
                res = res + next.size();
            }
            return res;
        }

        @Override
        public Option<V> get(int bitShiftDepth, int hash, K key) {
            int pos = bitpos(hash, bitShiftDepth);
            return absent(pos)? Option.none() : find(bitShiftDepth,pos,hash,key);
        }

        @Override
        public V getOrElse(int bitShiftDepth, int hash, K key, V alt) {
            int pos = bitpos(hash, bitShiftDepth);
            return absent(pos)? alt : find(bitShiftDepth,pos,hash,key,alt);
        }

        @Override
        public V getOrElseGet(int bitShiftDepth, int hash, K key, Supplier<? extends V> alt) {
            int pos = bitpos(hash, bitShiftDepth);
            return absent(pos)? alt.get() : findOrGet(bitShiftDepth,pos,hash,key,alt);
        }

        public boolean absent(int pos){
            return (bitset & pos)==0;
        }
        private V findOrGet(int shift,int pos, int hash, K key,Supplier<? extends V> alt) {
            return nodes[index(pos)].getOrElse(shift+BITS_IN_INDEX,hash,key,alt.get());
        }
        private V find(int shift,int pos, int hash, K key,V alt) {
            return nodes[index(pos)].getOrElse(shift+BITS_IN_INDEX,hash,key,alt);
        }
        private Option<V> find(int shift, int pos, int hash, K key) {
            return nodes[index(pos)].get(shift+BITS_IN_INDEX,hash,key);
        }
        private Node<K,V> findNode(int pos) {
            return nodes[index(pos)];
        }

        @Override
        public Node<K, V> minus(int bitShiftDepth, int hash, K key) {
            int bitPos = bitpos(hash, bitShiftDepth);
            int arrayPos = index(bitPos);
            Node<K,V> node = (absent(bitPos) ? EmptyNode.Instance : nodes[arrayPos]).minus(bitShiftDepth +BITS_IN_INDEX,hash,key);
            int removedBit =   bitset & ~bitPos;
            Node<K,V>[] removedNodes = new Node[nodes.length - 1];
            System.arraycopy(nodes, 0, removedNodes, 0, arrayPos);
            System.arraycopy(nodes, arrayPos + 1, removedNodes, arrayPos, nodes.length - arrayPos - 1);
            return new BitsetNode<>(removedBit, size(removedNodes), removedNodes);
        }

        @Override
        public int size() {
            return size;
        }

        @Override
        public LazySeq<Tuple2<K, V>> lazyList() {
            return LazySeq.fromStream(stream());
        }
        @Override
        public ReactiveSeq<Tuple2<K, V>> stream() {
            return ReactiveSeq.of(nodes).flatMap(n -> n.stream());
        }
        static int bitpos(int hash, int shift){
            return 1 << mask(hash, shift);
        }
        static int bitpos(int mask){
            return 1 << mask;
        }

        static int mask(int hash, int shift){
            return (hash >>> shift) & (SIZE-1);
        }

        int index(int bit){
            return Integer.bitCount(bitset & (bit - 1));
        }

        @Override
        public String toString() {
            StringBuilder s = new StringBuilder("{b:" +  Integer.toBinaryString(bitset) + ",s:"+size);
            for(Node<K,V> next : nodes){
                s.append(","+next.toString());
            }
            return s.append("}").toString();

        }
    }

}

