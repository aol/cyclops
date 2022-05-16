package cyclops.data;

import com.oath.cyclops.types.foldable.To;
import cyclops.control.Either;
import cyclops.control.Option;
import cyclops.control.LazyEither;
import cyclops.control.LazyEither3;
import cyclops.control.LazyEither4;
import cyclops.reactive.ReactiveSeq;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import cyclops.data.tuple.Tuple2;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

public interface DMap{



    public static <K1,V1,K2,V2> Two<K1,V1,K2,V2> two(ImmutableMap<K1, V1> map1, ImmutableMap<K2, V2> map2){
        return new DMap2<>(map1, map2);
    }
    public static <K1,V1,K2,V2,K3,V3> Three<K1,V1,K2,V2,K3,V3> three(ImmutableMap<K1, V1> map1, ImmutableMap<K2, V2> map2, ImmutableMap<K3, V3> map3){
        return new DMap3<>(map1, map2,map3);
    }
    public static <K1,V1,K2,V2> Two<K1,V1,K2,V2> twoEmpty(){
        return new DMap2<>(HashMap.empty(), HashMap.empty());
    }
    public static <K1,V1,K2,V2,K3,V3> Three<K1,V1,K2,V2,K3,V3> threeEmpty(){
        return new DMap3<>(HashMap.empty(), HashMap.empty(), HashMap.empty());
    }


    static interface Two<K1,V1,K2,V2> extends DMap{

        Two<K1,V1,K2,V2> put1(K1 key, V1 value);
        Two<K1,V1,K2,V2> put2(K2 key, V2 value);
        Two<K1,V1,K2,V2> put1(Tuple2<K1, V1> keyAndValue);
        Two<K1,V1,K2,V2> put2(Tuple2<K2, V2> keyAndValue);
        LazyEither3<V1,V2,Nothing> get(Either<K1, K2> key);
        Option<V1> get1(K1 key);
        Option<V2> get2(K2 key);
        V1 getOrElse1(K1 key, V1 alt);
        V2 getOrElse2(K2 key, V2 alt);
        V1 getOrElseGet1(K1 key, Supplier<V1> alt);
        V2 getOrElseGet2(K2 key, Supplier<V2> alt);
        int size();
        <K3,V3> Three<K1,V1,K2,V2,K3,V3> merge(ImmutableMap<K3, V3> one);
        ReactiveSeq<Either<Tuple2<K1,V1>,Tuple2<K2,V2>>> stream();
        ReactiveSeq<Either<V1, V2>> streamValues();
        ReactiveSeq<Either<K1, K2>> streamKeys();
        ImmutableMap<K1,V1> map1();
        ImmutableMap<K2,V2> map2();

         <KR1,VR1,KR2,VR2> Two<KR1,VR1,KR2,VR2> map(Function<? super K1, ? extends KR1> keyMapper1,
                                                    Function<? super V1, ? extends VR1> valueMapper1,
                                                    Function<? super K2, ? extends KR2> keyMapper2,
                                                    Function<? super V2, ? extends VR2> valueMapper2);

    }
    static interface Three<K1,V1,K2,V2,K3,V3> extends DMap, To<Three<K1,V1,K2,V2,K3,V3>> {

        Three<K1,V1,K2,V2,K3,V3> put(LazyEither3<Tuple2<K1, V1>, Tuple2<K2, V2>, Tuple2<K3, V3>> keyAndValue);
        Three<K1,V1,K2,V2,K3,V3> put1(K1 key, V1 value);
        Three<K1,V1,K2,V2,K3,V3> put2(K2 key, V2 value);
        Three<K1,V1,K2,V2,K3,V3> put3(K3 key, V3 value);
        Three<K1,V1,K2,V2,K3,V3> put1(Tuple2<K1, V1> keyAndValue);
        Three<K1,V1,K2,V2,K3,V3> put2(Tuple2<K2, V2> keyAndValue);
        Three<K1,V1,K2,V2,K3,V3> put3(Tuple2<K3, V3> keyAndValue);
        LazyEither4<V1,V2,V3,Nothing> get(LazyEither3<K1, K2, K3> key);
        Option<V1> get1(K1 key);
        Option<V2> get2(K2 key);
        Option<V3> get3(K3 key);
        V1 getOrElse1(K1 key, V1 alt);
        V2 getOrElse2(K2 key, V2 alt);
        V3 getOrElse3(K3 key, V3 alt);
        V1 getOrElseGet1(K1 key, Supplier<V1> alt);
        V2 getOrElseGet2(K2 key, Supplier<V2> alt);
        V3 getOrElseGet3(K3 key, Supplier<V3> alt);
        int size();
        ReactiveSeq<LazyEither3<Tuple2<K1,V1>,Tuple2<K2,V2>,Tuple2<K3,V3>>> stream();
        ReactiveSeq<LazyEither3<K1, K2, K3>> streamKeys();
        ReactiveSeq<LazyEither3<V1, V2, V3>> streamValues();
        ImmutableMap<K1,V1> map1();
        ImmutableMap<K2,V2> map2();
        ImmutableMap<K3,V3> map3();

        <KR1,VR1,KR2,VR2,KR3,VR3> Three<KR1,VR1,KR2,VR2,KR3,VR3> map(Function<? super K1, ? extends KR1> keyMapper1,
                                                                     Function<? super V1, ? extends VR1> valueMapper1,
                                                                     Function<? super K2, ? extends KR2> keyMapper2,
                                                                     Function<? super V2, ? extends VR2> valueMapper2,
                                                                     Function<? super K3, ? extends KR3> keyMapper3,
                                                                     Function<? super V3, ? extends VR3> valueMapper3);
    }

    @AllArgsConstructor
    static class DMap2<K1,V1,K2,V2> implements Two<K1,V1,K2,V2> {

        private final ImmutableMap<K1,V1> map1;
        private final ImmutableMap<K2,V2> map2;

        @Override
        public Two<K1, V1, K2, V2> put1(K1 key, V1 value) {

            return new DMap2<>(map1.put(key,value),map2);
        }

        @Override
        public Option<V1> get1(K1 key) {
            return map1.get(key);
        }


        @Override
        public int size() {
            return map1.size() + map2.size();
        }

        @Override
        public < K3, V3> Three<K1, V1, K2, V2, K3, V3> merge(ImmutableMap<K3, V3> one) {
            return new DMap3<K1, V1, K2, V2, K3, V3>(map1,map2,one);
        }

        @Override
        public Two<K1, V1, K2, V2> put2(K2 key, V2 value) {
            return new DMap2<>(map1,map2.put(key,value));
        }

        @Override
        public Two<K1, V1, K2, V2> put1(Tuple2<K1, V1> keyAndValue) {
            return put1(keyAndValue._1(),keyAndValue._2());
        }

        @Override
        public Two<K1, V1, K2, V2> put2(Tuple2<K2, V2> keyAndValue) {
            return put2(keyAndValue._1(),keyAndValue._2());
        }

        @Override
        public LazyEither3<V1, V2, Nothing> get(Either<K1, K2> key) {
            return key.fold(k1->{
                V1 r = getOrElse1(k1, null);
                return r==null ? LazyEither3.right(Nothing.EMPTY) : LazyEither3.left1(r);
            },k2->{
                V2 r = getOrElse2(k2, null);
                return r==null ? LazyEither3.right(Nothing.EMPTY) : LazyEither3.left2(r);
            });
        }

        @Override
        public Option<V2> get2(K2 key) {
            return map2.get(key);
        }

        @Override
        public V1 getOrElse1(K1 key, V1 alt) {
            return map1.getOrElse(key,alt);
        }

        @Override
        public V2 getOrElse2(K2 key, V2 alt) {
            return map2.getOrElse(key,alt);
        }

        @Override
        public V1 getOrElseGet1(K1 key, Supplier<V1> alt) {
            return map1.getOrElseGet(key,alt);
        }

        @Override
        public V2 getOrElseGet2(K2 key, Supplier<V2> alt) {
            return map2.getOrElseGet(key,alt);
        }


        @Override
        public ReactiveSeq<Either<Tuple2<K1, V1>, Tuple2<K2, V2>>> stream() {
            ReactiveSeq<Either<Tuple2<K1, V1>, Tuple2<K2, V2>>> x = map1.stream().map(LazyEither::left);
            return x.merge(map2.stream().map(LazyEither::right));
        }
        @Override
        public ReactiveSeq<Either<K1, K2>> streamKeys() {
            ReactiveSeq<Either<K1, K2>> x = map1.stream().map(t->t._1()).map(LazyEither::left);
            return x.merge(map2.stream().map(t->t._1()).map(LazyEither::right));
        }

        @Override
        public ImmutableMap<K1, V1> map1() {
            return map1;
        }

        @Override
        public ImmutableMap<K2, V2> map2() {
            return map2;
        }

        @Override
        public <KR1, VR1, KR2, VR2> Two<KR1, VR1, KR2, VR2> map(Function<? super K1, ? extends KR1> keyMapper1, Function<? super V1, ? extends VR1> valueMapper1, Function<? super K2, ? extends KR2> keyMapper2, Function<? super V2, ? extends VR2> valueMapper2) {
            return new DMap2<>(map1.bimap(keyMapper1,valueMapper1),map2.bimap(keyMapper2,valueMapper2));
        }

        @Override
        public ReactiveSeq<Either<V1, V2>> streamValues() {
            ReactiveSeq<Either<V1, V2>> x = map1.stream().map(t->t._2()).map(LazyEither::left);
            return x.merge(map2.stream().map(t->t._2()).map(LazyEither::right));
        }

        @Override
        public String toString() {
            StringBuilder b = new StringBuilder("[");
            b =b.append(map1.stream().join(","));
            b= b.append(map2.stream().join(","));
            return b.append("]").toString();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DMap2<?, ?, ?, ?> dMap2 = (DMap2<?, ?, ?, ?>) o;
            return Objects.equals(map1, dMap2.map1) &&
                    Objects.equals(map2, dMap2.map2);
        }

        @Override
        public int hashCode() {
            return Objects.hash(map1, map2);
        }
    }
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Nothing{
        public final static Nothing EMPTY = new Nothing();

        @Override
        public String toString() {
            return "Nothing[]";
        }
    }
    @AllArgsConstructor
    static class DMap3<K1,V1,K2,V2,K3,V3> implements Three<K1,V1,K2,V2,K3,V3> {

        private final ImmutableMap<K1,V1> map1;
        private final ImmutableMap<K2,V2> map2;
        private final ImmutableMap<K3,V3> map3;

        @Override
        public Three<K1, V1, K2, V2, K3, V3> put(LazyEither3<Tuple2<K1, V1>, Tuple2<K2, V2>, Tuple2<K3, V3>> keyAndValue) {
            return keyAndValue.fold(kv1->put1(kv1), kv2->put2(kv2), kv3->put3(kv3));
        }

        @Override
        public Three<K1, V1, K2, V2, K3, V3> put1(K1 key, V1 value) {

            return new DMap3<>(map1.put(key,value),map2,map3);
        }

        @Override
        public Option<V1> get1(K1 key) {
            return map1.get(key);
        }


        @Override
        public int size() {
            return map1.size() + map2.size() + map3.size();
        }

        @Override
        public Three<K1, V1, K2, V2, K3, V3> put2(K2 key, V2 value) {
            return new DMap3<>(map1,map2.put(key,value),map3);
        }

        @Override
        public Option<V2> get2(K2 key) {
            return map2.get(key);
        }

        @Override
        public Three<K1, V1, K2, V2, K3, V3> put3(K3 key, V3 value) {
            return new DMap3<>(map1,map2,map3.put(key,value));
        }

        @Override
        public Three<K1, V1, K2, V2, K3, V3> put1(Tuple2<K1, V1> keyAndValue) {
            return put1(keyAndValue._1(),keyAndValue._2());
        }

        @Override
        public Three<K1, V1, K2, V2, K3, V3> put2(Tuple2<K2, V2> keyAndValue) {
            return put2(keyAndValue._1(),keyAndValue._2());
        }

        @Override
        public Three<K1, V1, K2, V2, K3, V3> put3(Tuple2<K3, V3> keyAndValue) {
            return put3(keyAndValue._1(),keyAndValue._2());
        }

        @Override
        public LazyEither4<V1, V2, V3, Nothing> get(LazyEither3<K1, K2, K3> key) {
            return key.fold(k1->{
                V1 r = getOrElse1(k1, null);
                return r==null ? LazyEither4.right(Nothing.EMPTY) : LazyEither4.left1(r);
            },k2->{
                V2 r = getOrElse2(k2, null);
                return r==null ? LazyEither4.right(Nothing.EMPTY) : LazyEither4.left2(r);
            },k3->{
                V3 r = getOrElse3(k3, null);
                return r==null ? LazyEither4.right(Nothing.EMPTY) : LazyEither4.left3(r);
            });
        }

        @Override
        public Option<V3> get3(K3 key) {
            return map3.get(key);
        }

        @Override
        public V1 getOrElse1(K1 key,V1 alt) {
            return map1.getOrElse(key,alt);
        }

        @Override
        public V2 getOrElse2(K2 key, V2 alt) {
            return map2.getOrElse(key,alt);
        }

        @Override
        public V3 getOrElse3(K3 key, V3 alt) {
            return map3.getOrElse(key,alt);
        }

        @Override
        public V1 getOrElseGet1(K1 key, Supplier<V1> alt) {
            return map1.getOrElseGet(key,alt);
        }

        @Override
        public V2 getOrElseGet2(K2 key, Supplier<V2> alt) {
            return map2.getOrElseGet(key,alt);
        }

        @Override
        public V3 getOrElseGet3(K3 key, Supplier<V3> alt) {
            return map3.getOrElseGet(key,alt);
        }

        @Override
        public ReactiveSeq<LazyEither3<Tuple2<K1, V1>, Tuple2<K2, V2>, Tuple2<K3, V3>>> stream() {
            ReactiveSeq<LazyEither3<Tuple2<K1, V1>, Tuple2<K2, V2>, Tuple2<K3, V3>>> x = map1.stream().map(LazyEither3::left1);
            return x.merge(map2.stream().map(LazyEither3::left2), map3.stream().map(LazyEither3::right));
        }
        @Override
        public ReactiveSeq<LazyEither3<K1, K2, K3>> streamKeys() {
            ReactiveSeq<LazyEither3<K1, K2, K3>> x = map1.stream().map(t->t._1()).map(LazyEither3::left1);
            return x.merge(map2.stream().map(t->t._1()).map(LazyEither3::left2), map3.stream().map(t->t._1()).map(LazyEither3::right));
        }
        @Override
        public ReactiveSeq<LazyEither3<V1, V2, V3>> streamValues() {
            ReactiveSeq<LazyEither3<V1, V2, V3>> x = map1.stream().map(t->t._2()).map(LazyEither3::left1);
            return x.merge(map2.stream().map(t->t._2()).map(LazyEither3::left2), map3.stream().map(t->t._2()).map(LazyEither3::right));
        }

        @Override
        public ImmutableMap<K1, V1> map1() {
            return map1;
        }

        @Override
        public ImmutableMap<K2, V2> map2() {
            return map2;
        }

        @Override
        public ImmutableMap<K3, V3> map3() {
            return map3;
        }

        @Override
        public <KR1, VR1, KR2, VR2, KR3, VR3> Three<KR1, VR1, KR2, VR2, KR3, VR3> map(Function<? super K1, ? extends KR1> keyMapper1, Function<? super V1, ? extends VR1> valueMapper1, Function<? super K2, ? extends KR2> keyMapper2, Function<? super V2, ? extends VR2> valueMapper2, Function<? super K3, ? extends KR3> keyMapper3, Function<? super V3, ? extends VR3> valueMapper3) {
            return new DMap3<>(map1.bimap(keyMapper1,valueMapper1),map2.bimap(keyMapper2,valueMapper2),map3.bimap(keyMapper3,valueMapper3));
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DMap3<?, ?, ?, ?, ?, ?> dMap3 = (DMap3<?, ?, ?, ?, ?, ?>) o;
            return Objects.equals(map1, dMap3.map1) &&
                    Objects.equals(map2, dMap3.map2) &&
                    Objects.equals(map3, dMap3.map3);
        }

        @Override
        public int hashCode() {
            return Objects.hash(map1, map2, map3);
        }

        @Override
        public String toString() {
            StringBuilder b = new StringBuilder("[");
            b =b.append(map1.stream().join(","));
            b= b.append(map2.stream().join(","));
            b= b.append(map3.stream().join(","));
            return b.append("]").toString();
        }
    }

}
