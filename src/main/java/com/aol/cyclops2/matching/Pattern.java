package com.aol.cyclops2.matching;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * A Predicate supertype to describe a DSL for sum types pattern matching.
 * <pre>
 *   {@code
 *
 *    class User {
 *      private String name;
 *      private Role role;
 *    }
 *
 *    interface UserPatterns {
 *      default Pattern<User> Name(String name) {
 *        return u -> name.equals(u.name);
 *      }
 *      default Pattern<User> Role(Role role) {
 *        return u -> role.equals(u.role);
 *      }
 *      default Pattern<User> Admin() {
 *        return u -> Role.ADMIN.equals(u.role);
 *      }
 *    }
 *
 *   }
 * </pre>
 *
 * @param <T>
 */
public interface Pattern<T> extends Predicate<T> {

  static <E extends Exception> Pattern<E> Message(String message) {
    return e -> Objects.equals(message, e.getMessage());
  }

  static <E extends Exception> Pattern<E> Class(Class<? extends Exception> type) {
    return type::isInstance;
  }

  static <E extends Exception> Pattern<E> Cause(Class<? extends Exception> cause) {
    return e -> cause.isInstance(e.getCause());
  }

}
