package dev.suresh;

import java.io.Serializable;

/**
 * A discriminated union that encapsulates a successful outcome with a value of type T or a failure
 * with an arbitrary Throwable exception.
 *
 * @param <T> Result value type.
 */
sealed interface Result<T> extends Serializable {

    static <T> Result<T> success(T value) {
        return new Success<>(value);
    }

    static <T> Result<T> failure(Throwable error) {
        return new Failure<>(error);
    }

    default boolean isSuccess() {
        return this instanceof Success<T>;
    }

    default boolean isFailure() {
        return this instanceof Failure<T>;
    }

    default T getOrNull() {
        return this instanceof Success<T> s ? s.value() : null;
    }

    default Throwable exceptionOrNull() {
        return this instanceof Failure<T> t ? t.error() : null;
    }

    default String fString() {
        return """
                ToString  -> %1$s
                Result    -> %2$s
                Success   -> %3$s
                Failure   -> %4$s
                Exception -> %5$s
                """.formatted(toString(), getOrNull(), isSuccess(), isFailure(), exceptionOrNull());
    }

    record Success<T>(T value) implements Result<T> {
    }

    record Failure<T>(Throwable error) implements Result<T> {
    }
}
