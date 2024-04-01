package ru.armagidon.advcolo.util;

import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class Unchecked {

  private static final Invoker SILENT_INVOKER;

  static {
    final MethodHandles.Lookup lookup = MethodHandles.lookup();
    final CallSite site;
    try {
      site = LambdaMetafactory.metafactory(lookup,
          "invoke",
          MethodType.methodType(Invoker.class),
          MethodType.methodType(Object.class, CheckedSupplier.class),
          lookup.findVirtual(CheckedSupplier.class, "supply", MethodType.methodType(Object.class)),
          MethodType.methodType(Object.class, CheckedSupplier.class));
      SILENT_INVOKER = (Invoker) site.getTarget().invokeExact();
    } catch (Throwable e) {
      throw new RuntimeException(e);
    }
  }

  public static <T> UnaryOperator<T> unaryOperator(CheckedUnaryOperator<T> operator) {
    return item -> function(operator).apply(item);
  }

  public static <T>Supplier<T> supplier(CheckedSupplier<T> supplier) {
    return () -> SILENT_INVOKER.invoke(supplier);
  }

  public static <T, R>Function<T,  R> function(CheckedFunction<T, R> function) {
    return input -> SILENT_INVOKER.invoke(() -> function.apply(input));
  }

  public static Runnable runnable(CheckedRunnable runnable) {
    return () -> SILENT_INVOKER.invoke(() -> {
      runnable.run();
      return null;
    });
  }

  public static <T, V> BiConsumer<T, V> biConsumer(CheckedBiConsumer<T, V> consumer) {
    return (t, v) -> runnable(() -> consumer.accept(t, v)).run();
  }

  public static <T> Consumer<T> consumer(CheckedConsumer<T> consumer) {
    return (t) -> runnable(() -> consumer.accept(t)).run();
  }

  @FunctionalInterface
  interface Invoker {

    <T> T invoke(CheckedSupplier<T> supplier);
  }

  @FunctionalInterface
  public interface CheckedSupplier<T> {

    T supply() throws Throwable;
  }

  @FunctionalInterface
  public interface CheckedFunction<T, R> {

    R apply(T input) throws Throwable;
  }

  @FunctionalInterface
  public interface CheckedConsumer<T> {
    void accept(T t) throws Throwable;
  }

  @FunctionalInterface
  public interface CheckedBiConsumer<T, V> {
    void accept(T t, V v) throws Throwable;
  }

  @FunctionalInterface
  public interface CheckedRunnable {
    void run() throws Throwable;
  }

  @FunctionalInterface
  public interface CheckedUnaryOperator<T> extends CheckedFunction<T, T> {}

}
