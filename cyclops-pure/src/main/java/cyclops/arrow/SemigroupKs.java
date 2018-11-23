package cyclops.arrow;

import com.oath.cyclops.hkt.Higher;
import cyclops.control.*;
import com.oath.cyclops.hkt.DataWitness.*;
import com.oath.cyclops.hkt.DataWitness.optional;
import cyclops.data.LazySeq;
import cyclops.data.Seq;
import cyclops.data.Vector;
import cyclops.kinds.CompletableFutureKind;
import cyclops.kinds.OptionalKind;
import cyclops.kinds.StreamKind;
import cyclops.reactive.IO;
import cyclops.reactive.ReactiveSeq;
import cyclops.reactive.Spouts;
import cyclops.reactive.collections.immutable.LinkedListX;
import cyclops.reactive.collections.immutable.PersistentQueueX;
import cyclops.reactive.collections.immutable.PersistentSetX;
import cyclops.reactive.collections.immutable.VectorX;
import cyclops.reactive.collections.mutable.DequeX;
import cyclops.reactive.collections.mutable.ListX;
import cyclops.reactive.collections.mutable.QueueX;
import cyclops.reactive.collections.mutable.SetX;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import static com.oath.cyclops.data.ReactiveWitness.*;

public interface SemigroupKs{



    public static SemigroupK<optional> optionalPresent() {
        return new SemigroupK<optional>() {
          @Override
          public <T> Higher<optional, T> apply(Higher<optional, T> a, Higher<optional, T> b) {
            return  OptionalKind.narrowK(a).isPresent() ? a : b;
          }
        };
    }
    public static SemigroupK<list> listXConcat() {
      return new SemigroupK<list>() {

        @Override
        public <T> Higher<list, T> apply(Higher<list, T> a, Higher<list, T> b) {
          return ListX.narrowK(a).plusAll(ListX.narrowK(b));
        }
      };

    }


    static SemigroupK<set> setXConcat() {
      return new SemigroupK<set>() {
        @Override
        public <T> Higher<set, T> apply(Higher<set, T> a, Higher<set, T> b) {
          return SetX.narrowK(a).plusAll(SetX.narrowK(b));
        }
      };

    }



    static SemigroupK<queue> queueXConcat() {
      return new SemigroupK<queue>() {

        @Override
        public <T> Higher<queue, T> apply(Higher<queue, T> a, Higher<queue, T> b) {
          return QueueX.narrowK(a).plusAll(QueueX.narrowK(b));
        }
      };
    }

    /**
     * @return A combiner for DequeX (concatenates two DequeX into a single DequeX)
     */
    static SemigroupK<deque> dequeXConcat() {
      return new SemigroupK<deque>() {

        @Override
        public <T> Higher<deque, T> apply(Higher<deque, T> a, Higher<deque, T> b) {
          return DequeX.narrowK(a).plusAll(DequeX.narrowK(b));
        }
      };

    }

    /**
     * @return A combiner for LinkedListX (concatenates two LinkedListX into a single LinkedListX)
     */
    static SemigroupK<linkedListX> linkedListXConcat() {
      return new SemigroupK<linkedListX>() {

        @Override
        public <T> Higher<linkedListX, T> apply(Higher<linkedListX, T> a, Higher<linkedListX, T> b) {
          return LinkedListX.narrowK(a).plusAll(LinkedListX.narrowK(b));
        }
      };

    }
    static SemigroupK<lazySeq> lazySeqConcat() {
      return new SemigroupK<lazySeq>() {

        @Override
        public <T> Higher<lazySeq, T> apply(Higher<lazySeq, T> a, Higher<lazySeq, T> b) {
          return LazySeq.narrowK(a).plusAll(LazySeq.narrowK(b));
        }
      };

    }
    static SemigroupK<vector> vectorConcat() {
      return new SemigroupK<vector>() {

        @Override
        public <T> Higher<vector, T> apply(Higher<vector, T> a, Higher<vector, T> b) {
          return Vector.narrowK(a).plusAll(Vector.narrowK(b));
        }
      };

    }
    static SemigroupK<seq> seqConcat() {
      return new SemigroupK<seq>() {

        @Override
        public <T> Higher<seq, T> apply(Higher<seq, T> a, Higher<seq, T> b) {
          return Seq.narrowK(a).plusAll(Seq.narrowK(b));
        }
      };

    }

    /**
     * @return A combiner for VectorX (concatenates two VectorX into a single VectorX)
     */
    static SemigroupK<vectorX> vectorXConcat() {
      return new SemigroupK<vectorX>() {

        @Override
        public <T> Higher<vectorX, T> apply(Higher<vectorX, T> a, Higher<vectorX, T> b) {
          return VectorX.narrowK(a).plusAll(VectorX.narrowK(b));
        }
      };
    }


    static SemigroupK<persistentQueueX> persistentQueueXConcat() {
      return new SemigroupK<persistentQueueX>() {

        @Override
        public <T> Higher<persistentQueueX, T> apply(Higher<persistentQueueX, T> a, Higher<persistentQueueX, T> b) {
          return PersistentQueueX.narrowK(a).plusAll(PersistentQueueX.narrowK(b));
        }
      };
    }
  static SemigroupK<persistentSetX> persistentSetXConcat() {
    return new SemigroupK<persistentSetX>() {

      @Override
      public <T> Higher<persistentSetX, T> apply(Higher<persistentSetX, T> a, Higher<persistentSetX, T> b) {
        return PersistentSetX.narrowK(a).plusAll(PersistentSetX.narrowK(b));
      }
    };
  }


    static SemigroupK<io> combineIO() {
        return new SemigroupK<io>() {

            @Override
            public <T> Higher<io, T> apply(Higher<io, T> a, Higher<io, T> b) {
                return  IO.fromPublisher(Spouts.concat(IO.narrowK(a).stream(),IO.narrowK(b).stream()));
            }
        };

    }

    static SemigroupK<reactiveSeq> combineReactiveSeq() {
      return new SemigroupK<reactiveSeq>() {

        @Override
        public <T> Higher<reactiveSeq, T> apply(Higher<reactiveSeq, T> a, Higher<reactiveSeq, T> b) {
          return  ReactiveSeq.narrowK(a).appendStream(ReactiveSeq.narrowK(b));
        }
      };

    }

    static SemigroupK<reactiveSeq> firstNonEmptyReactiveSeq() {
      return new SemigroupK<reactiveSeq>() {

        @Override
        public <T> Higher<reactiveSeq, T> apply(Higher<reactiveSeq, T> a, Higher<reactiveSeq, T> b) {
          return  ReactiveSeq.narrowK(a).onEmptySwitch(()->ReactiveSeq.narrowK(b));
        }
      };

    }
    static SemigroupK<reactiveSeq> ambReactiveSeq() {
      return new SemigroupK<reactiveSeq>() {

        @Override
        public <T> Higher<reactiveSeq, T> apply(Higher<reactiveSeq, T> a, Higher<reactiveSeq, T> b) {
          return Spouts.amb(ReactiveSeq.narrowK(a),ReactiveSeq.narrowK(b));
        }
      };

    }

    static SemigroupK<reactiveSeq> mergeLatestReactiveSeq() {
      return new SemigroupK<reactiveSeq>() {

        @Override
        public <T> Higher<reactiveSeq, T> apply(Higher<reactiveSeq, T> a, Higher<reactiveSeq, T> b) {
          return Spouts.mergeLatest(ReactiveSeq.narrowK(a),ReactiveSeq.narrowK(b));
        }
      };

    }



    /**
     * @return Combination of two Stream's : b is appended to a
     */
    static SemigroupK<stream> combineStream() {
      return new SemigroupK<stream>() {

        @Override
        public <T> Higher<stream, T> apply(Higher<stream, T> a, Higher<stream, T> b) {
          return StreamKind.widen(Stream.concat(StreamKind.narrow(a), StreamKind.narrow(b)));
        }
      };

    }


    /**
     * @return Combine two CompletableFuture's by taking the first present
     */
    static SemigroupK<completableFuture> firstCompleteCompletableFuture() {
      return new SemigroupK<completableFuture>() {

        @Override
        public <T> Higher<completableFuture, T> apply(Higher<completableFuture, T> a, Higher<completableFuture, T> b) {
          CompletableFuture x = CompletableFuture.anyOf(CompletableFutureKind.<T>narrowK(a), CompletableFutureKind.<T>narrowK(b));
          return CompletableFutureKind.widen(x);
        }
      };

    }
    /**
     * @return Combine two Future's by taking the first result
     */
    static  SemigroupK<future> firstCompleteFuture() {
      return new SemigroupK<future>() {

        @Override
        public <T> Higher<future, T> apply(Higher<future, T> a, Higher<future, T> b) {
          return Future.anyOf(Future.narrowK(a),Future.narrowK(b));
        }
      };

    }


    /**
     * @return Combine two Future's by taking the first successful
     */
    static  SemigroupK<future> firstSuccessfulFuture() {
      return new SemigroupK<future>() {

        @Override
        public <T> Higher<future, T> apply(Higher<future, T> a, Higher<future, T> b) {
          return Future.firstSuccess(Future.narrowK(a),Future.narrowK(b));
        }
      };

    }
    /**
     * @return Combine two Xor's by taking the first right
     */
    static <ST> SemigroupK<Higher<either,ST>> firstRightEither() {
      return new SemigroupK<Higher<either,ST>>() {

        @Override
        public <T> Higher<Higher<either,ST>, T> apply(Higher<Higher<either,ST>, T> a, Higher<Higher<either,ST>, T> b) {
          return Either.narrowK(a).isRight() ? a : b;
        }
      };

    }
    /**
     * @return Combine two Xor's by taking the first left
     */
    static <ST> SemigroupK<Higher<either,ST>> firstLeftEither() {
      return new SemigroupK<Higher<either,ST>>() {

        @Override
        public <T> Higher<Higher<either,ST>, T> apply(Higher<Higher<either,ST>, T> a, Higher<Higher<either,ST>, T> b) {
          return  Either.narrowK(a).isLeft() ? a : b;
        }
      };
    }
    /**
     * @return Combine two Xor's by taking the last right
     */
    static <ST> SemigroupK<Higher<either,ST>> lastRightEither() {
      return new SemigroupK<Higher<either,ST>>() {

        @Override
        public <T> Higher<Higher<either,ST>, T> apply(Higher<Higher<either,ST>, T> a, Higher<Higher<either,ST>, T> b) {
          return Either.narrowK(b).isRight() ? b : a;
        }
      };

    }
    /**
     * @return Combine two Xor's by taking the last left
     */
    static <ST> SemigroupK<Higher<either,ST>> lastLeftEither() {
      return new SemigroupK<Higher<either,ST>>() {

        @Override
        public <T> Higher<Higher<either,ST>, T> apply(Higher<Higher<either,ST>, T> a, Higher<Higher<either,ST>, T> b) {
          return Either.narrowK(b).isLeft() ? b : a;
        }
      };
    }
    /**
     * @return Combine two Try's by taking the first right
     */
    static <X extends Throwable> SemigroupK<Higher<tryType,X>> firstTrySuccess() {
      return new SemigroupK<Higher<tryType,X>>() {

        @Override
        public <T> Higher<Higher<tryType,X>, T> apply(Higher<Higher<tryType,X>, T> a, Higher<Higher<tryType,X>, T> b) {
          return Try.narrowK(a).isSuccess() ? a : b;
        }
      };

    }
    /**
     * @return Combine two Try's by taking the first left
     */
    static <X extends Throwable> SemigroupK<Higher<tryType,X>> firstTryFailure() {
      return new SemigroupK<Higher<tryType,X>>() {

        @Override
        public <T> Higher<Higher<tryType,X>, T> apply(Higher<Higher<tryType,X>, T> a, Higher<Higher<tryType,X>, T> b) {
          return Try.narrowK(a).isFailure() ? a : b;
        }
      };

    }
    /**
     * @return Combine two Tryr's by taking the last right
     */
    static<X extends Throwable> SemigroupK<Higher<tryType,X>> lastTrySuccess() {
      return new SemigroupK<Higher<tryType,X>>() {

        @Override
        public <T> Higher<Higher<tryType,X>, T> apply(Higher<Higher<tryType,X>, T> a, Higher<Higher<tryType,X>, T> b) {
          return Try.narrowK(b).isSuccess() ? b : a;
        }
      };

    }
    /**
     * @return Combine two Try's by taking the last left
     */
    static <X extends Throwable> SemigroupK<Higher<tryType,X>> lastTryFailure() {
      return new SemigroupK<Higher<tryType,X>>() {

        @Override
        public <T> Higher<Higher<tryType,X>, T> apply(Higher<Higher<tryType,X>, T> a, Higher<Higher<tryType,X>, T> b) {
          return Try.narrowK(b).isFailure() ? b : a;
        }
      };

    }
    /**
     * @return Combine two Ior's by taking the first right
     */
    static <ST> SemigroupK<Higher<ior,ST>> firstPrimaryIor() {
      return new SemigroupK<Higher<ior,ST>>() {

        @Override
        public <T> Higher<Higher<ior,ST>, T> apply(Higher<Higher<ior,ST>, T> a, Higher<Higher<ior,ST>, T> b) {
          return Ior.narrowK(a).isRight() ? a : b;
        }
      };

    }
    /**
     * @return Combine two Ior's by taking the first left
     */
    static <ST> SemigroupK<Higher<ior,ST>> firstSecondaryIor() {
      return new SemigroupK<Higher<ior,ST>>() {

        @Override
        public <T> Higher<Higher<ior,ST>, T> apply(Higher<Higher<ior,ST>, T> a, Higher<Higher<ior,ST>, T> b) {
          return Ior.narrowK(a).isLeft() ? a : b;
        }
      };

    }
    /**
     * @return Combine two Ior's by taking the last right
     */
    static <ST> SemigroupK<Higher<ior,ST>> lastPrimaryIor() {
      return new SemigroupK<Higher<ior,ST>>() {

        @Override
        public <T> Higher<Higher<ior,ST>, T> apply(Higher<Higher<ior,ST>, T> a, Higher<Higher<ior,ST>, T> b) {
          return Ior.narrowK(b).isRight() ? b : a;
        }
      };

    }
    /**
     * @return Combine two Ior's by taking the last left
     */
    static <ST> SemigroupK<Higher<ior,ST>> lastSecondaryIor() {
      return new SemigroupK<Higher<ior,ST>>() {

        @Override
        public <T> Higher<Higher<ior,ST>, T> apply(Higher<Higher<ior,ST>, T> a, Higher<Higher<ior,ST>, T> b) {
          return Ior.narrowK(b).isLeft() ? b : a;
        }
      };

    }

    /**
     * @return Combine two Maybe's by taking the first present
     */
    static SemigroupK<option> firstPresentOption() {
      return new SemigroupK<option>() {

        @Override
        public <T> Higher<option, T> apply(Higher<option, T> a, Higher<option, T> b) {
          return Option.narrowK(a).isPresent() ? a : b;
        }
      };

    }

    /**
     * @return Combine two optionals by taking the first present
     */
    static SemigroupK<optional> firstPresentOptional() {
      return new SemigroupK<optional>() {

        @Override
        public <T> Higher<optional, T> apply(Higher<optional, T> a, Higher<optional, T> b) {
          return OptionalKind.narrowK(a).isPresent() ? a : b;
        }
      };

    }

    /**
     * @return Combine two Maybes by taking the last present
     */
    static SemigroupK<option> lastPresentMaybe() {
      return new SemigroupK<option>() {
        @Override
        public <T> Higher<option, T> apply(Higher<option, T> a, Higher<option, T> b) {
          return  Maybe.narrowK(b).isPresent() ? b : a;
        }
      };

    }

    /**
     * @return Combine two optionals by taking the last present
     */
    static SemigroupK<optional> lastPresentOptional() {
      return new SemigroupK<optional>() {
        @Override
        public <T> Higher<optional, T> apply(Higher<optional, T> a, Higher<optional, T> b) {
          return OptionalKind.narrowK(b).isPresent() ? b : a;
        }
      };

    }
}
