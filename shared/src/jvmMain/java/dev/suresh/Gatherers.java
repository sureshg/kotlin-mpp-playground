package dev.suresh;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Gatherer;
import java.util.stream.Gatherer.Downstream;
import java.util.stream.Gatherer.Integrator;
import java.util.stream.Stream;

public class Gatherers {

    static <T, R> Gatherer<T, ?, R> map(Function<T, R> f) {
        Integrator<Void, T, R> integrator = (_, e, ds) -> {
            var ir = f.apply(e);
            return ds.push(ir);
        };
        return Gatherer.of(integrator);
    }

    static <T> Gatherer<T, List<T>, List<T>> group(int size) {
        Supplier<List<T>> initializer = ArrayList::new;
        Integrator<List<T>, T, List<T>> integrator = (list, e, ds) -> {
            list.add(e);
            if (list.size() < size) {
                return true;
            } else {
                var group = List.copyOf(list);
                list.clear();
                return ds.push(group);
            }
        };
        BiConsumer<List<T>, Downstream<? super List<T>>> finisher = (list, ds) -> {
            var group = List.copyOf(list);
            if (!group.isEmpty()) {
                ds.push(group);
            }
        };
        return Gatherer.ofSequential(initializer, integrator, finisher);
    }

    public static void main(String[] args) {
        Stream.of(1, 2, 3, 4, 5).gather(map(e -> e + 1)).gather(group(2)).forEach(System.out::println);
    }
}


