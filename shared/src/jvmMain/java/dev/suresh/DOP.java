package dev.suresh;

import java.io.*;
import java.lang.reflect.RecordComponent;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static dev.suresh.Expr.eval;
import static java.io.IO.println;
import static java.lang.System.out;
import static java.util.Objects.requireNonNull;

public class DOP {
    public static void run() throws Exception {
        record Person(String name, int age) {
        }

        var future = new CompletableFuture<>();
        var textBlock = """
                This is text block
                This will join \
                with the line : %s
                "quote" = "added"
                Escape Start  \n \t \r \b \f end
                Space Escape-\s\s\s\s\s\s\s\s\s\s-end
                Regex \\S \\d \\D \\w \\W
                \\d+
                Escape char: \u00A0 \u2000 \u3000 \uFEFF \u200B \u200C \u200D \u2028 \u2029
                END
                """.formatted(new Person("Foo", 40));
        future.complete(textBlock);
        println(future.get());

        stringTemplates();
        amberReflections();
        genericRecordPattern();
        serializeRecord();

        final int count = 10;
        Expr expr = new Expr.Add(new Expr.Const.Int(count), new Expr.Const.Long(3));
        expr = new Expr.Div(expr, new Expr.Const.Int(2));
        expr = new Expr.Add(expr, new Expr.Const.Double(5.0));
        out.printf("Eval(%s) = %d%n", expr, eval(expr));
    }

    private static void stringTemplates() {
        int x = 10;
        int y = 20;
        out.printf("x + y = %d%n", x + y);
    }

    interface Name<T> {
    }

    record FullName<T>(T firstName, T lastName) implements Name<T> {
    }

    private static <T> void print(Name<T> name) {
        var result = switch (name) {
            case FullName(var first, var last) -> "%s, %s".formatted(first, last);
            default -> "Invalid name";
        };
        println(result);

        if (name instanceof FullName<?> f) {
            out.printf("%s, %s%n", f.firstName(), f.lastName());
        }

        // Named record pattern is not supported
        if (name instanceof FullName(var first, var last)) {
            out.printf("%s, %s%n", first, last);
        }
    }

    private static void genericRecordPattern() {
        print(new FullName<>("Foo", "Bar"));
        print(new FullName<>(1, 2));
        print(new FullName<>(10L, 20L));
    }

    private static void amberReflections() {
        var sealedClazz = Result.class;
        out.printf("Result (Interface) -> %s%n", sealedClazz.isInterface());
        out.printf("Result (Sealed Class) -> %s%n", sealedClazz.isSealed());

        for (Class<?> permittedSubclass : sealedClazz.getPermittedSubclasses()) {
            out.printf("%nPermitted Subclass : %s%n", permittedSubclass.getName());
            if (permittedSubclass.isRecord()) {
                out.printf("%s record components are,%n", permittedSubclass.getSimpleName());
                for (RecordComponent rc : permittedSubclass.getRecordComponents()) {
                    out.print(rc);
                }
            }
        }
    }


    private static void serializeRecord() throws Exception {
        // Local record
        record Lang(String name, int year) implements Serializable {
            Lang {
                requireNonNull(name);
                if (year <= 0) {
                    throw new IllegalArgumentException("Invalid year %s".formatted(year));
                }
            }
        }

        var serialFile = Files.createTempFile("record-serial", "data").toFile();
        serialFile.deleteOnExit();

        try (var oos = new ObjectOutputStream(new FileOutputStream(serialFile))) {
            List<Record> recs = List.of(new Lang("Java", 25), new Lang("Kotlin", 10), (Record) Result.success(100));

            for (Record rec : recs) {
                out.printf("Serializing record: %s%n", rec);
                oos.writeObject(rec);
            }
            oos.writeObject(null); // EOF
        }

        try (var ois = new ObjectInputStream(new FileInputStream(serialFile))) {
            Object rec;
            while ((rec = ois.readObject()) != null) {
                var result = switch (rec) {
                    case Lang l when l.year >= 20 -> l.toString();
                    case Lang(var name, var year) -> name;
                    case Result<?> r -> "Result value: %s".formatted(r.getOrNull());
                    default -> "Invalid serialized data. Expected Result, but found %s".formatted(rec);
                };

                out.printf("Deserialized record: %s%n", rec);
                println(result);
            }
        }

        results().forEach(r -> {
            var result = switch (r) {
                case null -> "n/a";
                case Result.Success<?> s -> s.toString();
                case Result.Failure<?> f -> f.toString();
            };
            out.printf("Result (Sealed Type): %s%n", result);
        });
    }

    static List<Result<?>> results() {
        return Arrays.asList(getResult(5L), getResult(25L), getResult(-1L), getResult("test"), getResult(null));
    }

    static Result<Number> getResult(Object obj) {
        return switch (obj) {
            case null -> Result.failure(new NullPointerException());
            case Long s when s > 0 && s < 10 -> Result.success(s);
            case Long s when s > 10 -> Result.failure(new IllegalArgumentException(String.valueOf(s)));
            default -> Result.failure(new IllegalArgumentException(obj.toString()));
        };
    }
}

