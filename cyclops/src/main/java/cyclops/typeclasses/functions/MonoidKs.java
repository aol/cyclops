package cyclops.typeclasses.functions;

import com.oath.cyclops.hkt.Higher;
import cyclops.async.Future;
import cyclops.collections.immutable.LinkedListX;
import cyclops.collections.immutable.PersistentQueueX;
import cyclops.collections.immutable.PersistentSetX;
import cyclops.collections.immutable.VectorX;
import cyclops.collections.mutable.DequeX;
import cyclops.collections.mutable.ListX;
import cyclops.collections.mutable.QueueX;
import cyclops.collections.mutable.SetX;
import cyclops.companion.CompletableFutures;
import cyclops.companion.Optionals.OptionalKind;
import cyclops.companion.Streams;
import cyclops.control.Ior;
import cyclops.control.Maybe;
import cyclops.control.Try;
import cyclops.control.Either;
import cyclops.monads.DataWitness;
import cyclops.monads.DataWitness.*;
import cyclops.reactive.ReactiveSeq;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;


public interface MonoidKs {


    public static MonoidK<optional> optionalPresent() {
      return new MonoidK<optional>() {
        @Override
        public <T> Higher<optional, T> zero() {
          return OptionalKind.empty();
        }

        @Override
        public <T> Higher<optional, T> apply(Higher<optional, T> t1, Higher<optional, T> t2) {
          return SemigroupKs.optionalPresent().apply(t1,t2);
        }
      };
    }
    public static MonoidK<list> listXConcat() {
        return new MonoidK<list>() {
          @Override
          public <T> Higher<list, T> zero() {
            return ListX.empty();
          }

          @Override
          public <T> Higher<list, T> apply(Higher<list, T> t1, Higher<list, T> t2) {
            return SemigroupKs.listXConcat().apply(t1,t2);
          }
        };
    }
  public static  MonoidK<deque> dequeXConcat() {
    return new MonoidK<deque>() {
      @Override
      public <T> Higher<deque, T> zero() {
        return DequeX.empty();
      }

      @Override
      public <T> Higher<deque, T> apply(Higher<deque, T> t1, Higher<deque, T> t2) {
        return SemigroupKs.dequeXConcat().apply(t1,t2);
      }
    };
  }




    /**
     * @return A combiner for SetX (concatenates two SetX into a single SetX)
     */
    static MonoidK<set> setXConcat() {
        return new MonoidK<set>() {
          @Override
          public <T> Higher<set, T> zero() {
            return SetX.empty();
          }

          @Override
          public <T> Higher<set, T> apply(Higher<set, T> t1, Higher<set, T> t2) {
            return SemigroupKs.setXConcat().apply(t1,t2);
          }
        };

    }



    /**
     * @return A combiner for QueueX (concatenates two QueueX into a single QueueX)
     */
    static MonoidK<queue> queueXConcat() {
        return new MonoidK<queue>() {
          @Override
          public <T> Higher<queue, T> zero() {
            return QueueX.empty();
          }

          @Override
          public <T> Higher<queue, T> apply(Higher<queue, T> t1, Higher<queue, T> t2) {
            return SemigroupKs.queueXConcat().apply(t1,t2);
          }
        };
    }


    /**
     * @return A combiner for LinkedListX (concatenates two LinkedListX into a single LinkedListX)
     */
    static MonoidK<linkedListX> linkedListXConcat() {
        return new MonoidK<linkedListX>() {
          @Override
          public <T> Higher<linkedListX, T> zero() {
            return LinkedListX.empty();
          }

          @Override
          public <T> Higher<linkedListX, T> apply(Higher<linkedListX, T> t1, Higher<linkedListX, T> t2) {
            return SemigroupKs.linkedListXConcat().apply(t1,t2);
          }
        };
    }

    /**
     * @return A combiner for VectorX (concatenates two VectorX into a single VectorX)
     */
    static MonoidK<vectorX> vectorXConcat() {
      return new MonoidK<vectorX>() {
        @Override
        public <T> Higher<vectorX, T> zero() {
          return VectorX.empty();
        }

        @Override
        public <T> Higher<vectorX, T> apply(Higher<vectorX, T> t1, Higher<vectorX, T> t2) {
          return SemigroupKs.vectorXConcat().apply(t1, t2);
        }
      };
    }

    /**
     * @return A combiner for PersistentQueueX (concatenates two PersistentQueueX into a single PersistentQueueX)
     */
    static MonoidK<persistentQueueX> persistentQueueXConcat() {
        return new MonoidK<persistentQueueX>() {
          @Override
          public <T> Higher<persistentQueueX, T> zero() {
            return PersistentQueueX.empty();
          }

          @Override
          public <T> Higher<persistentQueueX, T> apply(Higher<persistentQueueX, T> t1, Higher<persistentQueueX, T> t2) {
            return SemigroupKs.persistentQueueXConcat().apply(t1,t2);
          }
        };
    }

  static MonoidK<persistentSetX> persistentSetXConcat() {
    return new MonoidK<persistentSetX>() {
      @Override
      public <T> Higher<persistentSetX, T> zero() {
        return PersistentSetX.empty();
      }

      @Override
      public <T> Higher<persistentSetX, T> apply(Higher<persistentSetX, T> t1, Higher<persistentSetX, T> t2) {
        return SemigroupKs.persistentSetXConcat().apply(t1,t2);
      }
    };
  }




    /**
     * @return Combination of two ReactiveSeq Streams b is appended to a
     */
    static MonoidK<reactiveSeq> combineReactiveSeq() {
        return new MonoidK<reactiveSeq>() {
          @Override
          public <T> Higher<reactiveSeq, T> zero() {
            return ReactiveSeq.empty();
          }

          @Override
          public <T> Higher<reactiveSeq, T> apply(Higher<reactiveSeq, T> t1, Higher<reactiveSeq, T> t2) {
            return SemigroupKs.combineReactiveSeq().apply(t1,t2);
          }
        };
    }

    static MonoidK<reactiveSeq> firstNonEmptyReactiveSeq() {
        return new MonoidK<reactiveSeq>() {
          @Override
          public <T> Higher<reactiveSeq, T> zero() {
            return ReactiveSeq.empty();
          }

          @Override
          public <T> Higher<reactiveSeq, T> apply(Higher<reactiveSeq, T> t1, Higher<reactiveSeq, T> t2) {
            return SemigroupKs.firstNonEmptyReactiveSeq().apply(t1,t2);
          }
        };
    }
    static MonoidK<reactiveSeq> ambReactiveSeq() {
        return new MonoidK<reactiveSeq>() {
          @Override
          public <T> Higher<reactiveSeq, T> zero() {
            return ReactiveSeq.empty();
          }

          @Override
          public <T> Higher<reactiveSeq, T> apply(Higher<reactiveSeq, T> t1, Higher<reactiveSeq, T> t2) {
            return SemigroupKs.ambReactiveSeq().apply(t1,t2);
          }
        };
    }

    static MonoidK<reactiveSeq> mergeLatestReactiveSeq() {
        return new MonoidK<reactiveSeq>() {
          @Override
          public <T> Higher<reactiveSeq, T> zero() {
            return ReactiveSeq.empty();
          }

          @Override
          public <T> Higher<reactiveSeq, T> apply(Higher<reactiveSeq, T> t1, Higher<reactiveSeq, T> t2) {
            return SemigroupKs.mergeLatestReactiveSeq().apply(t1,t2);
          }
        };
    }



    /**
     * @return Combination of two Stream's : b is appended to a
     */
    static MonoidK<stream> combineStream() {
        return new MonoidK<stream>() {
          @Override
          public <T> Higher<stream, T> zero() {
            return Streams.StreamKind.widen(Stream.empty());
          }

          @Override
          public <T> Higher<stream, T> apply(Higher<stream, T> t1, Higher<stream, T> t2) {
            return SemigroupKs.combineStream().apply(t1,t2);
          }
        };
    }


    /**
     * @return Combine two CompletableFuture's by taking the first present
     */
    static MonoidK<completableFuture> firstCompleteCompletableFuture() {
        return new MonoidK<completableFuture>() {
          @Override
          public <T> Higher<completableFuture, T> zero() {
            return CompletableFutures.CompletableFutureKind.widen(new CompletableFuture<>());
          }

          @Override
          public <T> Higher<completableFuture, T> apply(Higher<completableFuture, T> t1, Higher<completableFuture, T> t2) {
            return SemigroupKs.firstCompleteCompletableFuture().apply(t1,t2);
          }
        };
    }
    /**
     * @return Combine two Future's by taking the first result
     */
    static MonoidK<future> firstCompleteFuture() {
            return new MonoidK<future>() {
              @Override
              public <T> Higher<future, T> zero() {
                return Future.future();
              }

              @Override
              public <T> Higher<future, T> apply(Higher<future, T> t1, Higher<future, T> t2) {
                return SemigroupKs.firstCompleteFuture().apply(t1,t2);
              }
            };
    }


    /**
     * @return Combine two Future's by taking the first successful
     */
    static MonoidK<future> firstSuccessfulFuture() {
        return new MonoidK<future>() {
          @Override
          public <T> Higher<future, T> zero() {
            return Future.future();
          }

          @Override
          public <T> Higher<future, T> apply(Higher<future, T> t1, Higher<future, T> t2) {
            return SemigroupKs.firstSuccessfulFuture().apply(t1,t2);
          }
        };
    }
    /**
     * @return Combine two Xor's by taking the first right
     */
    static <ST> MonoidK<Higher<either,ST>> firstRightEither(ST zero) {
        return new MonoidK<Higher<either, ST>>() {
          @Override
          public <T> Higher<Higher<either, ST>, T> zero() {
            return Either.left(zero);
          }

          @Override
          public <T> Higher<Higher<either, ST>, T> apply(Higher<Higher<either, ST>, T> t1, Higher<Higher<either, ST>, T> t2) {
            return SemigroupKs.<ST>firstRightEither().apply(t1,t2);
          }
        };
    }

    /**
     * @return Combine two Xor's by taking the last right
     */
    static <ST> MonoidK<Higher<either,ST>> lastRightEither(ST zero) {
        return new MonoidK<Higher<either, ST>>() {
          @Override
          public <T> Higher<Higher<either, ST>, T> zero() {
            return Either.left(zero);
          }

          @Override
          public <T> Higher<Higher<either, ST>, T> apply(Higher<Higher<either, ST>, T> t1, Higher<Higher<either, ST>, T> t2) {
            return SemigroupKs.<ST>lastRightEither().<T>apply(t1,t2);
          }
        };
    }

    /**
     * @return Combine two Try's by taking the first right
     */
    static <X extends Throwable> MonoidK<Higher<tryType,X>> firstTrySuccess(X zero) {
        return new MonoidK<Higher<tryType, X>>() {
          @Override
          public <T> Higher<Higher<tryType, X>, T> zero() {
            return Try.failure(zero);
          }

          @Override
          public <T> Higher<Higher<tryType, X>, T> apply(Higher<Higher<tryType, X>, T> t1, Higher<Higher<tryType, X>, T> t2) {
            return SemigroupKs.<X>firstTrySuccess().apply(t1,t2);
          }
        };
    }

    /**
     * @return Combine two Tryr's by taking the last right
     */
    static<X extends Throwable> MonoidK<Higher<tryType,X>> lastTrySuccess(X zero) {
        return new MonoidK<Higher<tryType, X>>() {
          @Override
          public <T> Higher<Higher<tryType, X>, T> zero() {
            return Try.failure(zero);
          }

          @Override
          public <T> Higher<Higher<tryType, X>, T> apply(Higher<Higher<tryType, X>, T> t1, Higher<Higher<tryType, X>, T> t2) {
            return SemigroupKs.<X>lastTrySuccess().apply(t1,t2);
          }
        };
    }

    /**
     * @return Combine two Ior's by taking the first right
     */
    static <ST> MonoidK<Higher<ior,ST>> firstPrimaryIor(ST zero) {
        return new MonoidK<Higher<ior, ST>>() {
          @Override
          public <T> Higher<Higher<ior, ST>, T> zero() {
            return Ior.left(zero);
          }

          @Override
          public <T> Higher<Higher<ior, ST>, T> apply(Higher<Higher<ior, ST>, T> t1, Higher<Higher<ior, ST>, T> t2) {
            return SemigroupKs.<ST>firstPrimaryIor().apply(t1,t2);
          }
        };
    }

    /**
     * @return Combine two Ior's by taking the last right
     */
    static <ST> MonoidK<Higher<ior,ST>> lastPrimaryIor(ST zero) {
        return new MonoidK<Higher<ior, ST>>() {
          @Override
          public <T> Higher<Higher<ior, ST>, T> zero() {
            return Ior.left(zero);
          }

          @Override
          public <T> Higher<Higher<ior, ST>, T> apply(Higher<Higher<ior, ST>, T> t1, Higher<Higher<ior, ST>, T> t2) {
            return SemigroupKs.<ST>lastPrimaryIor().apply(t1,t2);
          }
        };
    }


    /**
     * @return Combine two Maybe's by taking the first present
     */
    static MonoidK<maybe> firstPresentMaybe() {
        return new MonoidK<maybe>() {
          @Override
          public <T> Higher<maybe, T> zero() {
            return Maybe.nothing();
          }

          @Override
          public <T> Higher<maybe, T> apply(Higher<maybe, T> t1, Higher<maybe, T> t2) {
            return SemigroupKs.firstPresentMaybe().apply(t1,t2);
          }
        };
    }

    /**
     * @return Combine two optionals by taking the first present
     */
    static <T> MonoidK<optional> firstPresentOptional() {
        return new MonoidK<optional>() {
          @Override
          public <T> Higher<optional, T> zero() {
            return OptionalKind.empty();
          }

          @Override
          public <T> Higher<optional, T> apply(Higher<optional, T> t1, Higher<optional, T> t2) {
            return SemigroupKs.firstPresentOptional().apply(t1,t2);
          }
        };
    }

    /**
     * @return Combine two Maybes by taking the last present
     */
    static <T> MonoidK<maybe> lastPresentMaybe() {
        return new MonoidK<maybe>() {
          @Override
          public <T> Higher<maybe, T> zero() {
            return Maybe.nothing();
          }

          @Override
          public <T> Higher<maybe, T> apply(Higher<maybe, T> t1, Higher<maybe, T> t2) {
            return SemigroupKs.lastPresentMaybe().apply(t1,t2);
          }
        };
    }

    /**
     * @return Combine two optionals by taking the last present
     */
    static <T> MonoidK<optional> lastPresentOptional() {
        return new MonoidK<optional>() {
          @Override
          public <T> Higher<optional, T> zero() {
            return OptionalKind.empty();
          }

          @Override
          public <T> Higher<optional, T> apply(Higher<optional, T> t1, Higher<optional, T> t2) {
            return SemigroupKs.lastPresentOptional().apply(t1,t2);
          }
        };
    }
}
