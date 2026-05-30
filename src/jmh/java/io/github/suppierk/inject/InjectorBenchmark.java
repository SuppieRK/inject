/*
 * MIT License
 *
 * Copyright 2026 Roman Khlebnov
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the “Software”), to deal in the Software without
 * restriction, including without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package io.github.suppierk.inject;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.Main;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@SuppressWarnings("unused")
public class InjectorBenchmark {
  private static final Class<?>[] CHAIN_CLASSES = {
    Chain00.class,
    Chain01.class,
    Chain02.class,
    Chain03.class,
    Chain04.class,
    Chain05.class,
    Chain06.class,
    Chain07.class,
    Chain08.class,
    Chain09.class,
    Chain10.class,
    Chain11.class,
    Chain12.class,
    Chain13.class,
    Chain14.class,
    Chain15.class,
    Chain16.class,
    Chain17.class,
    Chain18.class,
    Chain19.class,
    Chain20.class,
    Chain21.class,
    Chain22.class,
    Chain23.class,
    Chain24.class,
    Chain25.class,
    Chain26.class,
    Chain27.class,
    Chain28.class,
    Chain29.class,
    Chain30.class,
    Chain31.class,
    Chain32.class,
    Chain33.class,
    Chain34.class,
    Chain35.class,
    Chain36.class,
    Chain37.class,
    Chain38.class,
    Chain39.class,
    Chain40.class,
    Chain41.class,
    Chain42.class,
    Chain43.class,
    Chain44.class,
    Chain45.class,
    Chain46.class,
    Chain47.class,
    Chain48.class,
    Chain49.class,
    Chain50.class,
    Chain51.class,
    Chain52.class,
    Chain53.class,
    Chain54.class,
    Chain55.class,
    Chain56.class,
    Chain57.class,
    Chain58.class,
    Chain59.class,
    Chain60.class,
    Chain61.class,
    Chain62.class,
    Chain63.class
  };

  private static final Class<?>[] SINGLETON_CHAIN_CLASSES = {
    SingletonChain00.class,
    SingletonChain01.class,
    SingletonChain02.class,
    SingletonChain03.class,
    SingletonChain04.class,
    SingletonChain05.class,
    SingletonChain06.class,
    SingletonChain07.class,
    SingletonChain08.class,
    SingletonChain09.class,
    SingletonChain10.class,
    SingletonChain11.class,
    SingletonChain12.class,
    SingletonChain13.class,
    SingletonChain14.class,
    SingletonChain15.class,
    SingletonChain16.class,
    SingletonChain17.class,
    SingletonChain18.class,
    SingletonChain19.class,
    SingletonChain20.class,
    SingletonChain21.class,
    SingletonChain22.class,
    SingletonChain23.class,
    SingletonChain24.class,
    SingletonChain25.class,
    SingletonChain26.class,
    SingletonChain27.class,
    SingletonChain28.class,
    SingletonChain29.class,
    SingletonChain30.class,
    SingletonChain31.class,
    SingletonChain32.class,
    SingletonChain33.class,
    SingletonChain34.class,
    SingletonChain35.class,
    SingletonChain36.class,
    SingletonChain37.class,
    SingletonChain38.class,
    SingletonChain39.class,
    SingletonChain40.class,
    SingletonChain41.class,
    SingletonChain42.class,
    SingletonChain43.class,
    SingletonChain44.class,
    SingletonChain45.class,
    SingletonChain46.class,
    SingletonChain47.class,
    SingletonChain48.class,
    SingletonChain49.class,
    SingletonChain50.class,
    SingletonChain51.class,
    SingletonChain52.class,
    SingletonChain53.class,
    SingletonChain54.class,
    SingletonChain55.class,
    SingletonChain56.class,
    SingletonChain57.class,
    SingletonChain58.class,
    SingletonChain59.class,
    SingletonChain60.class,
    SingletonChain61.class,
    SingletonChain62.class,
    SingletonChain63.class
  };

  private static final Class<?>[] PROVIDER_FACTORY_CLASSES = {
    ProviderFactory00.class,
    ProviderFactory01.class,
    ProviderFactory02.class,
    ProviderFactory03.class,
    ProviderFactory04.class,
    ProviderFactory05.class,
    ProviderFactory06.class,
    ProviderFactory07.class,
    ProviderFactory08.class,
    ProviderFactory09.class,
    ProviderFactory10.class,
    ProviderFactory11.class,
    ProviderFactory12.class,
    ProviderFactory13.class,
    ProviderFactory14.class,
    ProviderFactory15.class,
    ProviderFactory16.class,
    ProviderFactory17.class,
    ProviderFactory18.class,
    ProviderFactory19.class,
    ProviderFactory20.class,
    ProviderFactory21.class,
    ProviderFactory22.class,
    ProviderFactory23.class,
    ProviderFactory24.class,
    ProviderFactory25.class,
    ProviderFactory26.class,
    ProviderFactory27.class,
    ProviderFactory28.class,
    ProviderFactory29.class,
    ProviderFactory30.class,
    ProviderFactory31.class,
    ProviderFactory32.class,
    ProviderFactory33.class,
    ProviderFactory34.class,
    ProviderFactory35.class,
    ProviderFactory36.class,
    ProviderFactory37.class,
    ProviderFactory38.class,
    ProviderFactory39.class,
    ProviderFactory40.class,
    ProviderFactory41.class,
    ProviderFactory42.class,
    ProviderFactory43.class,
    ProviderFactory44.class,
    ProviderFactory45.class,
    ProviderFactory46.class,
    ProviderFactory47.class,
    ProviderFactory48.class,
    ProviderFactory49.class,
    ProviderFactory50.class,
    ProviderFactory51.class,
    ProviderFactory52.class,
    ProviderFactory53.class,
    ProviderFactory54.class,
    ProviderFactory55.class,
    ProviderFactory56.class,
    ProviderFactory57.class,
    ProviderFactory58.class,
    ProviderFactory59.class,
    ProviderFactory60.class,
    ProviderFactory61.class,
    ProviderFactory62.class,
    ProviderFactory63.class
  };

  private static final Class<?>[] PROVIDER_NODE_CLASSES = {
    ProviderNode00.class,
    ProviderNode01.class,
    ProviderNode02.class,
    ProviderNode03.class,
    ProviderNode04.class,
    ProviderNode05.class,
    ProviderNode06.class,
    ProviderNode07.class,
    ProviderNode08.class,
    ProviderNode09.class,
    ProviderNode10.class,
    ProviderNode11.class,
    ProviderNode12.class,
    ProviderNode13.class,
    ProviderNode14.class,
    ProviderNode15.class,
    ProviderNode16.class,
    ProviderNode17.class,
    ProviderNode18.class,
    ProviderNode19.class,
    ProviderNode20.class,
    ProviderNode21.class,
    ProviderNode22.class,
    ProviderNode23.class,
    ProviderNode24.class,
    ProviderNode25.class,
    ProviderNode26.class,
    ProviderNode27.class,
    ProviderNode28.class,
    ProviderNode29.class,
    ProviderNode30.class,
    ProviderNode31.class,
    ProviderNode32.class,
    ProviderNode33.class,
    ProviderNode34.class,
    ProviderNode35.class,
    ProviderNode36.class,
    ProviderNode37.class,
    ProviderNode38.class,
    ProviderNode39.class,
    ProviderNode40.class,
    ProviderNode41.class,
    ProviderNode42.class,
    ProviderNode43.class,
    ProviderNode44.class,
    ProviderNode45.class,
    ProviderNode46.class,
    ProviderNode47.class,
    ProviderNode48.class,
    ProviderNode49.class,
    ProviderNode50.class,
    ProviderNode51.class,
    ProviderNode52.class,
    ProviderNode53.class,
    ProviderNode54.class,
    ProviderNode55.class,
    ProviderNode56.class,
    ProviderNode57.class,
    ProviderNode58.class,
    ProviderNode59.class,
    ProviderNode60.class,
    ProviderNode61.class,
    ProviderNode62.class,
    ProviderNode63.class
  };

  @Benchmark
  public Injector buildChain(ChainState state) {
    return newInjector(CHAIN_CLASSES, state.classCount);
  }

  @Benchmark
  public void getChainLeaf(ChainState state, Blackhole blackhole) {
    blackhole.consume(state.injector.get(state.leafClass()));
  }

  @Benchmark
  public void renderChain(ChainState state, Blackhole blackhole) {
    blackhole.consume(state.injector.toString());
  }

  @Benchmark
  public Injector buildSingletonChain(SingletonChainState state) {
    return newInjector(SINGLETON_CHAIN_CLASSES, state.classCount);
  }

  @Benchmark
  public void getSingletonChainLeafFirstTime(
      SingletonFirstCreationState state, Blackhole blackhole) {
    blackhole.consume(state.injector.get(state.leafClass()));
  }

  @Benchmark
  public void getSingletonChainLeafCached(SingletonChainState state, Blackhole blackhole) {
    blackhole.consume(state.injector.get(state.leafClass()));
  }

  @Benchmark
  public Injector buildProviderGraph(ProviderGraphState state) {
    return newProviderInjector(state.classCount);
  }

  @Benchmark
  public void getProviderLeaf(ProviderGraphState state, Blackhole blackhole) {
    blackhole.consume(state.injector.get(state.leafClass()));
  }

  @Benchmark
  public void renderProviderGraph(ProviderGraphState state, Blackhole blackhole) {
    blackhole.consume(state.injector.toString());
  }

  private static Injector newInjector(Class<?>[] classes, int classCount) {
    checkClassCount(classes, classCount);
    final var additionalClasses = Arrays.copyOfRange(classes, 1, classCount);
    return Injector.injector().add(classes[0], additionalClasses).build();
  }

  private static Injector newProviderInjector(int classCount) {
    checkClassCount(PROVIDER_FACTORY_CLASSES, classCount);
    return Injector.injector().add(PROVIDER_FACTORY_CLASSES[classCount - 1]).build();
  }

  private static void checkClassCount(Class<?>[] classes, int classCount) {
    if (classCount <= 0 || classCount > classes.length) {
      throw new IllegalArgumentException("Class count must be between 1 and " + classes.length);
    }
  }

  public static void main(String[] args) throws Exception {
    Main.main(args);
  }

  @State(Scope.Benchmark)
  public static class ChainState {
    @Param({"8", "32", "64"})
    int classCount;

    private Injector injector;

    @Setup(Level.Trial)
    public void setup() {
      injector = newInjector(CHAIN_CLASSES, classCount);
    }

    private Class<?> leafClass() {
      return CHAIN_CLASSES[classCount - 1];
    }
  }

  @State(Scope.Benchmark)
  public static class SingletonChainState {
    @Param({"8", "32", "64"})
    int classCount;

    private Injector injector;

    @Setup(Level.Trial)
    public void setup() {
      injector = newInjector(SINGLETON_CHAIN_CLASSES, classCount);
      injector.get(leafClass());
    }

    private Class<?> leafClass() {
      return SINGLETON_CHAIN_CLASSES[classCount - 1];
    }
  }

  @State(Scope.Thread)
  public static class SingletonFirstCreationState {
    @Param({"8", "32", "64"})
    int classCount;

    private Injector injector;

    @Setup(Level.Invocation)
    public void setup() {
      injector = newInjector(SINGLETON_CHAIN_CLASSES, classCount);
    }

    private Class<?> leafClass() {
      return SINGLETON_CHAIN_CLASSES[classCount - 1];
    }
  }

  @State(Scope.Benchmark)
  public static class ProviderGraphState {
    @Param({"8", "32", "64"})
    int classCount;

    private Injector injector;

    @Setup(Level.Trial)
    public void setup() {
      injector = newProviderInjector(classCount);
    }

    private Class<?> leafClass() {
      return PROVIDER_NODE_CLASSES[classCount - 1];
    }
  }

  static class Chain00 {}

  static class Chain01 {
    @Inject
    Chain01(Chain00 ignored) {}
  }

  static class Chain02 {
    @Inject
    Chain02(Chain01 ignored) {}
  }

  static class Chain03 {
    @Inject
    Chain03(Chain02 ignored) {}
  }

  static class Chain04 {
    @Inject
    Chain04(Chain03 ignored) {}
  }

  static class Chain05 {
    @Inject
    Chain05(Chain04 ignored) {}
  }

  static class Chain06 {
    @Inject
    Chain06(Chain05 ignored) {}
  }

  static class Chain07 {
    @Inject
    Chain07(Chain06 ignored) {}
  }

  static class Chain08 {
    @Inject
    Chain08(Chain07 ignored) {}
  }

  static class Chain09 {
    @Inject
    Chain09(Chain08 ignored) {}
  }

  static class Chain10 {
    @Inject
    Chain10(Chain09 ignored) {}
  }

  static class Chain11 {
    @Inject
    Chain11(Chain10 ignored) {}
  }

  static class Chain12 {
    @Inject
    Chain12(Chain11 ignored) {}
  }

  static class Chain13 {
    @Inject
    Chain13(Chain12 ignored) {}
  }

  static class Chain14 {
    @Inject
    Chain14(Chain13 ignored) {}
  }

  static class Chain15 {
    @Inject
    Chain15(Chain14 ignored) {}
  }

  static class Chain16 {
    @Inject
    Chain16(Chain15 ignored) {}
  }

  static class Chain17 {
    @Inject
    Chain17(Chain16 ignored) {}
  }

  static class Chain18 {
    @Inject
    Chain18(Chain17 ignored) {}
  }

  static class Chain19 {
    @Inject
    Chain19(Chain18 ignored) {}
  }

  static class Chain20 {
    @Inject
    Chain20(Chain19 ignored) {}
  }

  static class Chain21 {
    @Inject
    Chain21(Chain20 ignored) {}
  }

  static class Chain22 {
    @Inject
    Chain22(Chain21 ignored) {}
  }

  static class Chain23 {
    @Inject
    Chain23(Chain22 ignored) {}
  }

  static class Chain24 {
    @Inject
    Chain24(Chain23 ignored) {}
  }

  static class Chain25 {
    @Inject
    Chain25(Chain24 ignored) {}
  }

  static class Chain26 {
    @Inject
    Chain26(Chain25 ignored) {}
  }

  static class Chain27 {
    @Inject
    Chain27(Chain26 ignored) {}
  }

  static class Chain28 {
    @Inject
    Chain28(Chain27 ignored) {}
  }

  static class Chain29 {
    @Inject
    Chain29(Chain28 ignored) {}
  }

  static class Chain30 {
    @Inject
    Chain30(Chain29 ignored) {}
  }

  static class Chain31 {
    @Inject
    Chain31(Chain30 ignored) {}
  }

  static class Chain32 {
    @Inject
    Chain32(Chain31 ignored) {}
  }

  static class Chain33 {
    @Inject
    Chain33(Chain32 ignored) {}
  }

  static class Chain34 {
    @Inject
    Chain34(Chain33 ignored) {}
  }

  static class Chain35 {
    @Inject
    Chain35(Chain34 ignored) {}
  }

  static class Chain36 {
    @Inject
    Chain36(Chain35 ignored) {}
  }

  static class Chain37 {
    @Inject
    Chain37(Chain36 ignored) {}
  }

  static class Chain38 {
    @Inject
    Chain38(Chain37 ignored) {}
  }

  static class Chain39 {
    @Inject
    Chain39(Chain38 ignored) {}
  }

  static class Chain40 {
    @Inject
    Chain40(Chain39 ignored) {}
  }

  static class Chain41 {
    @Inject
    Chain41(Chain40 ignored) {}
  }

  static class Chain42 {
    @Inject
    Chain42(Chain41 ignored) {}
  }

  static class Chain43 {
    @Inject
    Chain43(Chain42 ignored) {}
  }

  static class Chain44 {
    @Inject
    Chain44(Chain43 ignored) {}
  }

  static class Chain45 {
    @Inject
    Chain45(Chain44 ignored) {}
  }

  static class Chain46 {
    @Inject
    Chain46(Chain45 ignored) {}
  }

  static class Chain47 {
    @Inject
    Chain47(Chain46 ignored) {}
  }

  static class Chain48 {
    @Inject
    Chain48(Chain47 ignored) {}
  }

  static class Chain49 {
    @Inject
    Chain49(Chain48 ignored) {}
  }

  static class Chain50 {
    @Inject
    Chain50(Chain49 ignored) {}
  }

  static class Chain51 {
    @Inject
    Chain51(Chain50 ignored) {}
  }

  static class Chain52 {
    @Inject
    Chain52(Chain51 ignored) {}
  }

  static class Chain53 {
    @Inject
    Chain53(Chain52 ignored) {}
  }

  static class Chain54 {
    @Inject
    Chain54(Chain53 ignored) {}
  }

  static class Chain55 {
    @Inject
    Chain55(Chain54 ignored) {}
  }

  static class Chain56 {
    @Inject
    Chain56(Chain55 ignored) {}
  }

  static class Chain57 {
    @Inject
    Chain57(Chain56 ignored) {}
  }

  static class Chain58 {
    @Inject
    Chain58(Chain57 ignored) {}
  }

  static class Chain59 {
    @Inject
    Chain59(Chain58 ignored) {}
  }

  static class Chain60 {
    @Inject
    Chain60(Chain59 ignored) {}
  }

  static class Chain61 {
    @Inject
    Chain61(Chain60 ignored) {}
  }

  static class Chain62 {
    @Inject
    Chain62(Chain61 ignored) {}
  }

  static class Chain63 {
    @Inject
    Chain63(Chain62 ignored) {}
  }

  @Singleton
  static class SingletonChain00 {}

  @Singleton
  static class SingletonChain01 {
    @Inject
    SingletonChain01(SingletonChain00 ignored) {}
  }

  @Singleton
  static class SingletonChain02 {
    @Inject
    SingletonChain02(SingletonChain01 ignored) {}
  }

  @Singleton
  static class SingletonChain03 {
    @Inject
    SingletonChain03(SingletonChain02 ignored) {}
  }

  @Singleton
  static class SingletonChain04 {
    @Inject
    SingletonChain04(SingletonChain03 ignored) {}
  }

  @Singleton
  static class SingletonChain05 {
    @Inject
    SingletonChain05(SingletonChain04 ignored) {}
  }

  @Singleton
  static class SingletonChain06 {
    @Inject
    SingletonChain06(SingletonChain05 ignored) {}
  }

  @Singleton
  static class SingletonChain07 {
    @Inject
    SingletonChain07(SingletonChain06 ignored) {}
  }

  @Singleton
  static class SingletonChain08 {
    @Inject
    SingletonChain08(SingletonChain07 ignored) {}
  }

  @Singleton
  static class SingletonChain09 {
    @Inject
    SingletonChain09(SingletonChain08 ignored) {}
  }

  @Singleton
  static class SingletonChain10 {
    @Inject
    SingletonChain10(SingletonChain09 ignored) {}
  }

  @Singleton
  static class SingletonChain11 {
    @Inject
    SingletonChain11(SingletonChain10 ignored) {}
  }

  @Singleton
  static class SingletonChain12 {
    @Inject
    SingletonChain12(SingletonChain11 ignored) {}
  }

  @Singleton
  static class SingletonChain13 {
    @Inject
    SingletonChain13(SingletonChain12 ignored) {}
  }

  @Singleton
  static class SingletonChain14 {
    @Inject
    SingletonChain14(SingletonChain13 ignored) {}
  }

  @Singleton
  static class SingletonChain15 {
    @Inject
    SingletonChain15(SingletonChain14 ignored) {}
  }

  @Singleton
  static class SingletonChain16 {
    @Inject
    SingletonChain16(SingletonChain15 ignored) {}
  }

  @Singleton
  static class SingletonChain17 {
    @Inject
    SingletonChain17(SingletonChain16 ignored) {}
  }

  @Singleton
  static class SingletonChain18 {
    @Inject
    SingletonChain18(SingletonChain17 ignored) {}
  }

  @Singleton
  static class SingletonChain19 {
    @Inject
    SingletonChain19(SingletonChain18 ignored) {}
  }

  @Singleton
  static class SingletonChain20 {
    @Inject
    SingletonChain20(SingletonChain19 ignored) {}
  }

  @Singleton
  static class SingletonChain21 {
    @Inject
    SingletonChain21(SingletonChain20 ignored) {}
  }

  @Singleton
  static class SingletonChain22 {
    @Inject
    SingletonChain22(SingletonChain21 ignored) {}
  }

  @Singleton
  static class SingletonChain23 {
    @Inject
    SingletonChain23(SingletonChain22 ignored) {}
  }

  @Singleton
  static class SingletonChain24 {
    @Inject
    SingletonChain24(SingletonChain23 ignored) {}
  }

  @Singleton
  static class SingletonChain25 {
    @Inject
    SingletonChain25(SingletonChain24 ignored) {}
  }

  @Singleton
  static class SingletonChain26 {
    @Inject
    SingletonChain26(SingletonChain25 ignored) {}
  }

  @Singleton
  static class SingletonChain27 {
    @Inject
    SingletonChain27(SingletonChain26 ignored) {}
  }

  @Singleton
  static class SingletonChain28 {
    @Inject
    SingletonChain28(SingletonChain27 ignored) {}
  }

  @Singleton
  static class SingletonChain29 {
    @Inject
    SingletonChain29(SingletonChain28 ignored) {}
  }

  @Singleton
  static class SingletonChain30 {
    @Inject
    SingletonChain30(SingletonChain29 ignored) {}
  }

  @Singleton
  static class SingletonChain31 {
    @Inject
    SingletonChain31(SingletonChain30 ignored) {}
  }

  @Singleton
  static class SingletonChain32 {
    @Inject
    SingletonChain32(SingletonChain31 ignored) {}
  }

  @Singleton
  static class SingletonChain33 {
    @Inject
    SingletonChain33(SingletonChain32 ignored) {}
  }

  @Singleton
  static class SingletonChain34 {
    @Inject
    SingletonChain34(SingletonChain33 ignored) {}
  }

  @Singleton
  static class SingletonChain35 {
    @Inject
    SingletonChain35(SingletonChain34 ignored) {}
  }

  @Singleton
  static class SingletonChain36 {
    @Inject
    SingletonChain36(SingletonChain35 ignored) {}
  }

  @Singleton
  static class SingletonChain37 {
    @Inject
    SingletonChain37(SingletonChain36 ignored) {}
  }

  @Singleton
  static class SingletonChain38 {
    @Inject
    SingletonChain38(SingletonChain37 ignored) {}
  }

  @Singleton
  static class SingletonChain39 {
    @Inject
    SingletonChain39(SingletonChain38 ignored) {}
  }

  @Singleton
  static class SingletonChain40 {
    @Inject
    SingletonChain40(SingletonChain39 ignored) {}
  }

  @Singleton
  static class SingletonChain41 {
    @Inject
    SingletonChain41(SingletonChain40 ignored) {}
  }

  @Singleton
  static class SingletonChain42 {
    @Inject
    SingletonChain42(SingletonChain41 ignored) {}
  }

  @Singleton
  static class SingletonChain43 {
    @Inject
    SingletonChain43(SingletonChain42 ignored) {}
  }

  @Singleton
  static class SingletonChain44 {
    @Inject
    SingletonChain44(SingletonChain43 ignored) {}
  }

  @Singleton
  static class SingletonChain45 {
    @Inject
    SingletonChain45(SingletonChain44 ignored) {}
  }

  @Singleton
  static class SingletonChain46 {
    @Inject
    SingletonChain46(SingletonChain45 ignored) {}
  }

  @Singleton
  static class SingletonChain47 {
    @Inject
    SingletonChain47(SingletonChain46 ignored) {}
  }

  @Singleton
  static class SingletonChain48 {
    @Inject
    SingletonChain48(SingletonChain47 ignored) {}
  }

  @Singleton
  static class SingletonChain49 {
    @Inject
    SingletonChain49(SingletonChain48 ignored) {}
  }

  @Singleton
  static class SingletonChain50 {
    @Inject
    SingletonChain50(SingletonChain49 ignored) {}
  }

  @Singleton
  static class SingletonChain51 {
    @Inject
    SingletonChain51(SingletonChain50 ignored) {}
  }

  @Singleton
  static class SingletonChain52 {
    @Inject
    SingletonChain52(SingletonChain51 ignored) {}
  }

  @Singleton
  static class SingletonChain53 {
    @Inject
    SingletonChain53(SingletonChain52 ignored) {}
  }

  @Singleton
  static class SingletonChain54 {
    @Inject
    SingletonChain54(SingletonChain53 ignored) {}
  }

  @Singleton
  static class SingletonChain55 {
    @Inject
    SingletonChain55(SingletonChain54 ignored) {}
  }

  @Singleton
  static class SingletonChain56 {
    @Inject
    SingletonChain56(SingletonChain55 ignored) {}
  }

  @Singleton
  static class SingletonChain57 {
    @Inject
    SingletonChain57(SingletonChain56 ignored) {}
  }

  @Singleton
  static class SingletonChain58 {
    @Inject
    SingletonChain58(SingletonChain57 ignored) {}
  }

  @Singleton
  static class SingletonChain59 {
    @Inject
    SingletonChain59(SingletonChain58 ignored) {}
  }

  @Singleton
  static class SingletonChain60 {
    @Inject
    SingletonChain60(SingletonChain59 ignored) {}
  }

  @Singleton
  static class SingletonChain61 {
    @Inject
    SingletonChain61(SingletonChain60 ignored) {}
  }

  @Singleton
  static class SingletonChain62 {
    @Inject
    SingletonChain62(SingletonChain61 ignored) {}
  }

  @Singleton
  static class SingletonChain63 {
    @Inject
    SingletonChain63(SingletonChain62 ignored) {}
  }

  static class ProviderNode00 {}

  static class ProviderNode01 {
    final ProviderNode00 previous;

    ProviderNode01(ProviderNode00 previous) {
      this.previous = previous;
    }
  }

  static class ProviderNode02 {
    final ProviderNode01 previous;

    ProviderNode02(ProviderNode01 previous) {
      this.previous = previous;
    }
  }

  static class ProviderNode03 {
    final ProviderNode02 previous;

    ProviderNode03(ProviderNode02 previous) {
      this.previous = previous;
    }
  }

  static class ProviderNode04 {
    final ProviderNode03 previous;

    ProviderNode04(ProviderNode03 previous) {
      this.previous = previous;
    }
  }

  static class ProviderNode05 {
    final ProviderNode04 previous;

    ProviderNode05(ProviderNode04 previous) {
      this.previous = previous;
    }
  }

  static class ProviderNode06 {
    final ProviderNode05 previous;

    ProviderNode06(ProviderNode05 previous) {
      this.previous = previous;
    }
  }

  static class ProviderNode07 {
    final ProviderNode06 previous;

    ProviderNode07(ProviderNode06 previous) {
      this.previous = previous;
    }
  }

  static class ProviderNode08 {
    final ProviderNode07 previous;

    ProviderNode08(ProviderNode07 previous) {
      this.previous = previous;
    }
  }

  static class ProviderNode09 {
    final ProviderNode08 previous;

    ProviderNode09(ProviderNode08 previous) {
      this.previous = previous;
    }
  }

  static class ProviderNode10 {
    final ProviderNode09 previous;

    ProviderNode10(ProviderNode09 previous) {
      this.previous = previous;
    }
  }

  static class ProviderNode11 {
    final ProviderNode10 previous;

    ProviderNode11(ProviderNode10 previous) {
      this.previous = previous;
    }
  }

  static class ProviderNode12 {
    final ProviderNode11 previous;

    ProviderNode12(ProviderNode11 previous) {
      this.previous = previous;
    }
  }

  static class ProviderNode13 {
    final ProviderNode12 previous;

    ProviderNode13(ProviderNode12 previous) {
      this.previous = previous;
    }
  }

  static class ProviderNode14 {
    final ProviderNode13 previous;

    ProviderNode14(ProviderNode13 previous) {
      this.previous = previous;
    }
  }

  static class ProviderNode15 {
    final ProviderNode14 previous;

    ProviderNode15(ProviderNode14 previous) {
      this.previous = previous;
    }
  }

  static class ProviderNode16 {
    final ProviderNode15 previous;

    ProviderNode16(ProviderNode15 previous) {
      this.previous = previous;
    }
  }

  static class ProviderNode17 {
    final ProviderNode16 previous;

    ProviderNode17(ProviderNode16 previous) {
      this.previous = previous;
    }
  }

  static class ProviderNode18 {
    final ProviderNode17 previous;

    ProviderNode18(ProviderNode17 previous) {
      this.previous = previous;
    }
  }

  static class ProviderNode19 {
    final ProviderNode18 previous;

    ProviderNode19(ProviderNode18 previous) {
      this.previous = previous;
    }
  }

  static class ProviderNode20 {
    final ProviderNode19 previous;

    ProviderNode20(ProviderNode19 previous) {
      this.previous = previous;
    }
  }

  static class ProviderNode21 {
    final ProviderNode20 previous;

    ProviderNode21(ProviderNode20 previous) {
      this.previous = previous;
    }
  }

  static class ProviderNode22 {
    final ProviderNode21 previous;

    ProviderNode22(ProviderNode21 previous) {
      this.previous = previous;
    }
  }

  static class ProviderNode23 {
    final ProviderNode22 previous;

    ProviderNode23(ProviderNode22 previous) {
      this.previous = previous;
    }
  }

  static class ProviderNode24 {
    final ProviderNode23 previous;

    ProviderNode24(ProviderNode23 previous) {
      this.previous = previous;
    }
  }

  static class ProviderNode25 {
    final ProviderNode24 previous;

    ProviderNode25(ProviderNode24 previous) {
      this.previous = previous;
    }
  }

  static class ProviderNode26 {
    final ProviderNode25 previous;

    ProviderNode26(ProviderNode25 previous) {
      this.previous = previous;
    }
  }

  static class ProviderNode27 {
    final ProviderNode26 previous;

    ProviderNode27(ProviderNode26 previous) {
      this.previous = previous;
    }
  }

  static class ProviderNode28 {
    final ProviderNode27 previous;

    ProviderNode28(ProviderNode27 previous) {
      this.previous = previous;
    }
  }

  static class ProviderNode29 {
    final ProviderNode28 previous;

    ProviderNode29(ProviderNode28 previous) {
      this.previous = previous;
    }
  }

  static class ProviderNode30 {
    final ProviderNode29 previous;

    ProviderNode30(ProviderNode29 previous) {
      this.previous = previous;
    }
  }

  static class ProviderNode31 {
    final ProviderNode30 previous;

    ProviderNode31(ProviderNode30 previous) {
      this.previous = previous;
    }
  }

  static class ProviderNode32 {
    final ProviderNode31 previous;

    ProviderNode32(ProviderNode31 previous) {
      this.previous = previous;
    }
  }

  static class ProviderNode33 {
    final ProviderNode32 previous;

    ProviderNode33(ProviderNode32 previous) {
      this.previous = previous;
    }
  }

  static class ProviderNode34 {
    final ProviderNode33 previous;

    ProviderNode34(ProviderNode33 previous) {
      this.previous = previous;
    }
  }

  static class ProviderNode35 {
    final ProviderNode34 previous;

    ProviderNode35(ProviderNode34 previous) {
      this.previous = previous;
    }
  }

  static class ProviderNode36 {
    final ProviderNode35 previous;

    ProviderNode36(ProviderNode35 previous) {
      this.previous = previous;
    }
  }

  static class ProviderNode37 {
    final ProviderNode36 previous;

    ProviderNode37(ProviderNode36 previous) {
      this.previous = previous;
    }
  }

  static class ProviderNode38 {
    final ProviderNode37 previous;

    ProviderNode38(ProviderNode37 previous) {
      this.previous = previous;
    }
  }

  static class ProviderNode39 {
    final ProviderNode38 previous;

    ProviderNode39(ProviderNode38 previous) {
      this.previous = previous;
    }
  }

  static class ProviderNode40 {
    final ProviderNode39 previous;

    ProviderNode40(ProviderNode39 previous) {
      this.previous = previous;
    }
  }

  static class ProviderNode41 {
    final ProviderNode40 previous;

    ProviderNode41(ProviderNode40 previous) {
      this.previous = previous;
    }
  }

  static class ProviderNode42 {
    final ProviderNode41 previous;

    ProviderNode42(ProviderNode41 previous) {
      this.previous = previous;
    }
  }

  static class ProviderNode43 {
    final ProviderNode42 previous;

    ProviderNode43(ProviderNode42 previous) {
      this.previous = previous;
    }
  }

  static class ProviderNode44 {
    final ProviderNode43 previous;

    ProviderNode44(ProviderNode43 previous) {
      this.previous = previous;
    }
  }

  static class ProviderNode45 {
    final ProviderNode44 previous;

    ProviderNode45(ProviderNode44 previous) {
      this.previous = previous;
    }
  }

  static class ProviderNode46 {
    final ProviderNode45 previous;

    ProviderNode46(ProviderNode45 previous) {
      this.previous = previous;
    }
  }

  static class ProviderNode47 {
    final ProviderNode46 previous;

    ProviderNode47(ProviderNode46 previous) {
      this.previous = previous;
    }
  }

  static class ProviderNode48 {
    final ProviderNode47 previous;

    ProviderNode48(ProviderNode47 previous) {
      this.previous = previous;
    }
  }

  static class ProviderNode49 {
    final ProviderNode48 previous;

    ProviderNode49(ProviderNode48 previous) {
      this.previous = previous;
    }
  }

  static class ProviderNode50 {
    final ProviderNode49 previous;

    ProviderNode50(ProviderNode49 previous) {
      this.previous = previous;
    }
  }

  static class ProviderNode51 {
    final ProviderNode50 previous;

    ProviderNode51(ProviderNode50 previous) {
      this.previous = previous;
    }
  }

  static class ProviderNode52 {
    final ProviderNode51 previous;

    ProviderNode52(ProviderNode51 previous) {
      this.previous = previous;
    }
  }

  static class ProviderNode53 {
    final ProviderNode52 previous;

    ProviderNode53(ProviderNode52 previous) {
      this.previous = previous;
    }
  }

  static class ProviderNode54 {
    final ProviderNode53 previous;

    ProviderNode54(ProviderNode53 previous) {
      this.previous = previous;
    }
  }

  static class ProviderNode55 {
    final ProviderNode54 previous;

    ProviderNode55(ProviderNode54 previous) {
      this.previous = previous;
    }
  }

  static class ProviderNode56 {
    final ProviderNode55 previous;

    ProviderNode56(ProviderNode55 previous) {
      this.previous = previous;
    }
  }

  static class ProviderNode57 {
    final ProviderNode56 previous;

    ProviderNode57(ProviderNode56 previous) {
      this.previous = previous;
    }
  }

  static class ProviderNode58 {
    final ProviderNode57 previous;

    ProviderNode58(ProviderNode57 previous) {
      this.previous = previous;
    }
  }

  static class ProviderNode59 {
    final ProviderNode58 previous;

    ProviderNode59(ProviderNode58 previous) {
      this.previous = previous;
    }
  }

  static class ProviderNode60 {
    final ProviderNode59 previous;

    ProviderNode60(ProviderNode59 previous) {
      this.previous = previous;
    }
  }

  static class ProviderNode61 {
    final ProviderNode60 previous;

    ProviderNode61(ProviderNode60 previous) {
      this.previous = previous;
    }
  }

  static class ProviderNode62 {
    final ProviderNode61 previous;

    ProviderNode62(ProviderNode61 previous) {
      this.previous = previous;
    }
  }

  static class ProviderNode63 {
    final ProviderNode62 previous;

    ProviderNode63(ProviderNode62 previous) {
      this.previous = previous;
    }
  }

  static class ProviderFactory00 {
    @Provides
    ProviderNode00 providerNode00() {
      return new ProviderNode00();
    }
  }

  static class ProviderFactory01 extends ProviderFactory00 {
    @Provides
    ProviderNode01 providerNode01(ProviderNode00 previous) {
      return new ProviderNode01(previous);
    }
  }

  static class ProviderFactory02 extends ProviderFactory01 {
    @Provides
    ProviderNode02 providerNode02(ProviderNode01 previous) {
      return new ProviderNode02(previous);
    }
  }

  static class ProviderFactory03 extends ProviderFactory02 {
    @Provides
    ProviderNode03 providerNode03(ProviderNode02 previous) {
      return new ProviderNode03(previous);
    }
  }

  static class ProviderFactory04 extends ProviderFactory03 {
    @Provides
    ProviderNode04 providerNode04(ProviderNode03 previous) {
      return new ProviderNode04(previous);
    }
  }

  static class ProviderFactory05 extends ProviderFactory04 {
    @Provides
    ProviderNode05 providerNode05(ProviderNode04 previous) {
      return new ProviderNode05(previous);
    }
  }

  static class ProviderFactory06 extends ProviderFactory05 {
    @Provides
    ProviderNode06 providerNode06(ProviderNode05 previous) {
      return new ProviderNode06(previous);
    }
  }

  static class ProviderFactory07 extends ProviderFactory06 {
    @Provides
    ProviderNode07 providerNode07(ProviderNode06 previous) {
      return new ProviderNode07(previous);
    }
  }

  static class ProviderFactory08 extends ProviderFactory07 {
    @Provides
    ProviderNode08 providerNode08(ProviderNode07 previous) {
      return new ProviderNode08(previous);
    }
  }

  static class ProviderFactory09 extends ProviderFactory08 {
    @Provides
    ProviderNode09 providerNode09(ProviderNode08 previous) {
      return new ProviderNode09(previous);
    }
  }

  static class ProviderFactory10 extends ProviderFactory09 {
    @Provides
    ProviderNode10 providerNode10(ProviderNode09 previous) {
      return new ProviderNode10(previous);
    }
  }

  static class ProviderFactory11 extends ProviderFactory10 {
    @Provides
    ProviderNode11 providerNode11(ProviderNode10 previous) {
      return new ProviderNode11(previous);
    }
  }

  static class ProviderFactory12 extends ProviderFactory11 {
    @Provides
    ProviderNode12 providerNode12(ProviderNode11 previous) {
      return new ProviderNode12(previous);
    }
  }

  static class ProviderFactory13 extends ProviderFactory12 {
    @Provides
    ProviderNode13 providerNode13(ProviderNode12 previous) {
      return new ProviderNode13(previous);
    }
  }

  static class ProviderFactory14 extends ProviderFactory13 {
    @Provides
    ProviderNode14 providerNode14(ProviderNode13 previous) {
      return new ProviderNode14(previous);
    }
  }

  static class ProviderFactory15 extends ProviderFactory14 {
    @Provides
    ProviderNode15 providerNode15(ProviderNode14 previous) {
      return new ProviderNode15(previous);
    }
  }

  static class ProviderFactory16 extends ProviderFactory15 {
    @Provides
    ProviderNode16 providerNode16(ProviderNode15 previous) {
      return new ProviderNode16(previous);
    }
  }

  static class ProviderFactory17 extends ProviderFactory16 {
    @Provides
    ProviderNode17 providerNode17(ProviderNode16 previous) {
      return new ProviderNode17(previous);
    }
  }

  static class ProviderFactory18 extends ProviderFactory17 {
    @Provides
    ProviderNode18 providerNode18(ProviderNode17 previous) {
      return new ProviderNode18(previous);
    }
  }

  static class ProviderFactory19 extends ProviderFactory18 {
    @Provides
    ProviderNode19 providerNode19(ProviderNode18 previous) {
      return new ProviderNode19(previous);
    }
  }

  static class ProviderFactory20 extends ProviderFactory19 {
    @Provides
    ProviderNode20 providerNode20(ProviderNode19 previous) {
      return new ProviderNode20(previous);
    }
  }

  static class ProviderFactory21 extends ProviderFactory20 {
    @Provides
    ProviderNode21 providerNode21(ProviderNode20 previous) {
      return new ProviderNode21(previous);
    }
  }

  static class ProviderFactory22 extends ProviderFactory21 {
    @Provides
    ProviderNode22 providerNode22(ProviderNode21 previous) {
      return new ProviderNode22(previous);
    }
  }

  static class ProviderFactory23 extends ProviderFactory22 {
    @Provides
    ProviderNode23 providerNode23(ProviderNode22 previous) {
      return new ProviderNode23(previous);
    }
  }

  static class ProviderFactory24 extends ProviderFactory23 {
    @Provides
    ProviderNode24 providerNode24(ProviderNode23 previous) {
      return new ProviderNode24(previous);
    }
  }

  static class ProviderFactory25 extends ProviderFactory24 {
    @Provides
    ProviderNode25 providerNode25(ProviderNode24 previous) {
      return new ProviderNode25(previous);
    }
  }

  static class ProviderFactory26 extends ProviderFactory25 {
    @Provides
    ProviderNode26 providerNode26(ProviderNode25 previous) {
      return new ProviderNode26(previous);
    }
  }

  static class ProviderFactory27 extends ProviderFactory26 {
    @Provides
    ProviderNode27 providerNode27(ProviderNode26 previous) {
      return new ProviderNode27(previous);
    }
  }

  static class ProviderFactory28 extends ProviderFactory27 {
    @Provides
    ProviderNode28 providerNode28(ProviderNode27 previous) {
      return new ProviderNode28(previous);
    }
  }

  static class ProviderFactory29 extends ProviderFactory28 {
    @Provides
    ProviderNode29 providerNode29(ProviderNode28 previous) {
      return new ProviderNode29(previous);
    }
  }

  static class ProviderFactory30 extends ProviderFactory29 {
    @Provides
    ProviderNode30 providerNode30(ProviderNode29 previous) {
      return new ProviderNode30(previous);
    }
  }

  static class ProviderFactory31 extends ProviderFactory30 {
    @Provides
    ProviderNode31 providerNode31(ProviderNode30 previous) {
      return new ProviderNode31(previous);
    }
  }

  static class ProviderFactory32 extends ProviderFactory31 {
    @Provides
    ProviderNode32 providerNode32(ProviderNode31 previous) {
      return new ProviderNode32(previous);
    }
  }

  static class ProviderFactory33 extends ProviderFactory32 {
    @Provides
    ProviderNode33 providerNode33(ProviderNode32 previous) {
      return new ProviderNode33(previous);
    }
  }

  static class ProviderFactory34 extends ProviderFactory33 {
    @Provides
    ProviderNode34 providerNode34(ProviderNode33 previous) {
      return new ProviderNode34(previous);
    }
  }

  static class ProviderFactory35 extends ProviderFactory34 {
    @Provides
    ProviderNode35 providerNode35(ProviderNode34 previous) {
      return new ProviderNode35(previous);
    }
  }

  static class ProviderFactory36 extends ProviderFactory35 {
    @Provides
    ProviderNode36 providerNode36(ProviderNode35 previous) {
      return new ProviderNode36(previous);
    }
  }

  static class ProviderFactory37 extends ProviderFactory36 {
    @Provides
    ProviderNode37 providerNode37(ProviderNode36 previous) {
      return new ProviderNode37(previous);
    }
  }

  static class ProviderFactory38 extends ProviderFactory37 {
    @Provides
    ProviderNode38 providerNode38(ProviderNode37 previous) {
      return new ProviderNode38(previous);
    }
  }

  static class ProviderFactory39 extends ProviderFactory38 {
    @Provides
    ProviderNode39 providerNode39(ProviderNode38 previous) {
      return new ProviderNode39(previous);
    }
  }

  static class ProviderFactory40 extends ProviderFactory39 {
    @Provides
    ProviderNode40 providerNode40(ProviderNode39 previous) {
      return new ProviderNode40(previous);
    }
  }

  static class ProviderFactory41 extends ProviderFactory40 {
    @Provides
    ProviderNode41 providerNode41(ProviderNode40 previous) {
      return new ProviderNode41(previous);
    }
  }

  static class ProviderFactory42 extends ProviderFactory41 {
    @Provides
    ProviderNode42 providerNode42(ProviderNode41 previous) {
      return new ProviderNode42(previous);
    }
  }

  static class ProviderFactory43 extends ProviderFactory42 {
    @Provides
    ProviderNode43 providerNode43(ProviderNode42 previous) {
      return new ProviderNode43(previous);
    }
  }

  static class ProviderFactory44 extends ProviderFactory43 {
    @Provides
    ProviderNode44 providerNode44(ProviderNode43 previous) {
      return new ProviderNode44(previous);
    }
  }

  static class ProviderFactory45 extends ProviderFactory44 {
    @Provides
    ProviderNode45 providerNode45(ProviderNode44 previous) {
      return new ProviderNode45(previous);
    }
  }

  static class ProviderFactory46 extends ProviderFactory45 {
    @Provides
    ProviderNode46 providerNode46(ProviderNode45 previous) {
      return new ProviderNode46(previous);
    }
  }

  static class ProviderFactory47 extends ProviderFactory46 {
    @Provides
    ProviderNode47 providerNode47(ProviderNode46 previous) {
      return new ProviderNode47(previous);
    }
  }

  static class ProviderFactory48 extends ProviderFactory47 {
    @Provides
    ProviderNode48 providerNode48(ProviderNode47 previous) {
      return new ProviderNode48(previous);
    }
  }

  static class ProviderFactory49 extends ProviderFactory48 {
    @Provides
    ProviderNode49 providerNode49(ProviderNode48 previous) {
      return new ProviderNode49(previous);
    }
  }

  static class ProviderFactory50 extends ProviderFactory49 {
    @Provides
    ProviderNode50 providerNode50(ProviderNode49 previous) {
      return new ProviderNode50(previous);
    }
  }

  static class ProviderFactory51 extends ProviderFactory50 {
    @Provides
    ProviderNode51 providerNode51(ProviderNode50 previous) {
      return new ProviderNode51(previous);
    }
  }

  static class ProviderFactory52 extends ProviderFactory51 {
    @Provides
    ProviderNode52 providerNode52(ProviderNode51 previous) {
      return new ProviderNode52(previous);
    }
  }

  static class ProviderFactory53 extends ProviderFactory52 {
    @Provides
    ProviderNode53 providerNode53(ProviderNode52 previous) {
      return new ProviderNode53(previous);
    }
  }

  static class ProviderFactory54 extends ProviderFactory53 {
    @Provides
    ProviderNode54 providerNode54(ProviderNode53 previous) {
      return new ProviderNode54(previous);
    }
  }

  static class ProviderFactory55 extends ProviderFactory54 {
    @Provides
    ProviderNode55 providerNode55(ProviderNode54 previous) {
      return new ProviderNode55(previous);
    }
  }

  static class ProviderFactory56 extends ProviderFactory55 {
    @Provides
    ProviderNode56 providerNode56(ProviderNode55 previous) {
      return new ProviderNode56(previous);
    }
  }

  static class ProviderFactory57 extends ProviderFactory56 {
    @Provides
    ProviderNode57 providerNode57(ProviderNode56 previous) {
      return new ProviderNode57(previous);
    }
  }

  static class ProviderFactory58 extends ProviderFactory57 {
    @Provides
    ProviderNode58 providerNode58(ProviderNode57 previous) {
      return new ProviderNode58(previous);
    }
  }

  static class ProviderFactory59 extends ProviderFactory58 {
    @Provides
    ProviderNode59 providerNode59(ProviderNode58 previous) {
      return new ProviderNode59(previous);
    }
  }

  static class ProviderFactory60 extends ProviderFactory59 {
    @Provides
    ProviderNode60 providerNode60(ProviderNode59 previous) {
      return new ProviderNode60(previous);
    }
  }

  static class ProviderFactory61 extends ProviderFactory60 {
    @Provides
    ProviderNode61 providerNode61(ProviderNode60 previous) {
      return new ProviderNode61(previous);
    }
  }

  static class ProviderFactory62 extends ProviderFactory61 {
    @Provides
    ProviderNode62 providerNode62(ProviderNode61 previous) {
      return new ProviderNode62(previous);
    }
  }

  static class ProviderFactory63 extends ProviderFactory62 {
    @Provides
    ProviderNode63 providerNode63(ProviderNode62 previous) {
      return new ProviderNode63(previous);
    }
  }
}
