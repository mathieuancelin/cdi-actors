package com.mathieuancelin.actors.cdi.util;

import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;

public class Functionnal {
    
    final static None<Object> none = new None<Object>();

    private Functionnal() {}

    public static enum Unit {
        INSTANCE
    }

    public static interface Callable<T> {

        T apply();
    }
    
    public static interface CheckedCallable<T> {

        T apply() throws Throwable;
    }

    public static interface SimpleCallable extends Callable<Unit> {}
    
    public static interface SimpleCheckedCallable extends CheckedCallable<Unit> {}

    public static interface Action<T> {

        void apply(T t);
    }
    
    public static interface CheckedAction<T> {

        void apply(T t) throws Throwable;
    }

    public static interface Function<T, R> {

        R apply(T t);
    }
        
    public static interface CheckedFunction<T, R> {

        R apply(T t) throws Throwable;
    }

    public static interface Monad<T> {

        <R> Option<R> map(Function<T, R> function);

        Option<T> map(Action<T> function);

        <R> Option<R> map(CheckedFunction<T, R> function);

        Option<T> map(CheckedAction<T> function);
        
        Option<T> flatMap(Callable<Option<T>> action);

        Option<T> flatMap(CheckedCallable<Option<T>> action);

        Option<T> flatMap(Function<T, Option<T>> action);

        Option<T> flatMap(CheckedFunction<T, Option<T>> action);
    }

    public static abstract class Option<T> implements Iterable<T>, Monad<T>, Serializable {
        
        public abstract boolean isDefined();

        public abstract boolean isEmpty();
        
        public abstract T get();

        public Option<T> orElse(T value) {
            return isEmpty() ? Option.maybe(value) : this;
        }
        
        public T getOrElse(T value) {
            return isEmpty() ? value : get();
        }

        public T getOrElse(Function<Unit, T> function) {
            return isEmpty() ? function.apply(Unit.INSTANCE) : get();
        }

        public T getOrElse(Callable<T> function) {
            return isEmpty() ? function.apply() : get();
        }

        public T getOrNull() {
            return isEmpty() ? null : get();
        }
        
        public Option<T> filter(Function<T, Boolean> predicate) {
            if (isDefined()) {
                if (predicate.apply(get())) {
                    return this;
                } else {
                    return Option.none();
                }
            }
            return Option.none();
        }
        
        public Option<T> filterNot(Function<T, Boolean> predicate) {
            if (isDefined()) {
                if (!predicate.apply(get())) {
                    return this;
                } else {
                    return Option.none();
                }
            }
            return Option.none();
        }

//        public <X> Either<X, T> toRight(X left) {
//            if (isDefined()) {
//                return Either.eitherRight(get());
//            } else {
//                return Either.eitherLeft(left);
//            }
//        }
//
//        public <X> Either<T, X> toLeft(X right) {
//             if (isDefined()) {
//                return Either.eitherLeft(get());
//            } else {
//                return Either.eitherRight(right);
//            }
//        }

        @Override
        public <R> Option<R> map(Function<T, R> function) {
            if (isDefined()) {
                return Option.maybe(function.apply(get()));
            }
            return Option.none();
        }

        @Override
        public Option<T> map(Action<T> function) {
            if (isDefined()) {
                function.apply(get());
                return Option.maybe(get());
            }
            return Option.none();
        }

        @Override
        public <R> Option<R> map(CheckedFunction<T, R> function) {
            if (isDefined()) {
                try {
                    return Option.maybe(function.apply(get()));
                } catch (Throwable t) {
                    return Option.none();
                }
            }
            return Option.none();
        }

        @Override
        public Option<T> map(CheckedAction<T> function) {
            if (isDefined()) {
                try {
                    function.apply(get());
                    return Option.maybe(get());
                } catch (Throwable t) {
                    return Option.none();
                }
            }
            return Option.none();
        }
        
        @Override
        public Option<T> flatMap(Callable<Option<T>> action) {
            if (isDefined()) {
                return action.apply();
            }
            return Option.none();
        }
        
        @Override
        public Option<T> flatMap(CheckedCallable<Option<T>> action) {
           if (isDefined()) {
                try {
                    return action.apply();
                } catch (Throwable t) {
                    return this;
                }
            }
            return Option.none();
        }
        
        @Override
        public Option<T> flatMap(Function<T, Option<T>> action) {
            if (isDefined()) {
                return action.apply(get());
            }
            return Option.none();
        }

        @Override
        public Option<T> flatMap(CheckedFunction<T, Option<T>> action) {
           if (isDefined()) {
                try {
                    return action.apply(get());
                } catch (Throwable t) {
                    return this;
                }
            }
            return Option.none();
        }

        public static <T> None<T> none() {
            return (None<T>) (Object) none;
        }

        public static <T> Some<T> some(T value) {
            return new Some<T>(value);
        }

        public static <T> Maybe<T> maybe(T value) {
            return new Maybe<T>(value);
        }
    }

    public static class None<T> extends Option<T> {

        @Override
        public boolean isDefined() {
            return false;
        }

        @Override
        public T get() {
            throw new IllegalStateException("No value");
        }

        @Override
        public Iterator<T> iterator() {
            return Collections.<T>emptyList().iterator();
        }

        @Override
        public String toString() {
            return "None";
        }

        @Override
        public boolean isEmpty() {
            return true;
        }
    }

    public static class Some<T> extends Option<T> {

        final T value;

        public Some(T value) {
            this.value = value;
        }

        @Override
        public boolean isDefined() {
            return true;
        }

        @Override
        public T get() {
            return value;
        }

        @Override
        public Iterator<T> iterator() {
            return Collections.singletonList(value).iterator();
        }

        @Override
        public String toString() {
            return "Some ( " + value + " )";
        }

        @Override
        public boolean isEmpty() {
            return false;
        }
    }
    
    /**
     * A not so good version of some. Mostly used to wrap
     * return of library methods.
     *
     * @param <T>
     */
    public static class Maybe<T> extends Option<T> {

        private final T input;

        public Maybe(T input) {
            this.input = input;
        }

        @Override
        public boolean isDefined() {
            return !(input == null);
        }

        @Override
        public T get() {
            return input;
        }

        @Override
        public Iterator<T> iterator() {
            if (input == null) {
                return Collections.<T>emptyList().iterator();
            } else {
                return Collections.singletonList(input).iterator();
            }
        }

        @Override
        public String toString() {
            return "Maybe ( " + input + " )";
        }

        @Override
        public boolean isEmpty() {
            return !isDefined();
        }
    }
}
