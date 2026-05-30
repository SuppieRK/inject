/*
 * MIT License
 *
 * Copyright 2024 Roman Khlebnov
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package io.github.suppierk.inject;

import io.github.suppierk.inject.graph.ConstructsNew;
import io.github.suppierk.inject.graph.ConstructsSingleton;
import io.github.suppierk.inject.graph.Node;
import io.github.suppierk.inject.graph.ProvidesNew;
import io.github.suppierk.inject.graph.ProvidesSingleton;
import io.github.suppierk.inject.graph.RefersTo;
import io.github.suppierk.inject.graph.ReflectionNode;
import io.github.suppierk.inject.graph.Value;
import io.github.suppierk.inject.query.KeyAnnotationsPredicate;
import io.github.suppierk.utils.ConsoleConstants;
import io.github.suppierk.utils.Memoized;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Qualifier;
import jakarta.inject.Singleton;
import java.io.Closeable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;

/**
 * Main dependency injection functionality entry point.
 *
 * <p>During instantiation, {@link IllegalArgumentException} can be thrown.
 *
 * <p>After instantiation, {@link IllegalStateException} can be thrown.
 */
public final class Injector implements Closeable {
  private static final String NULL_VALUE_TEMPLATE = "%s is null";
  private static final String DUPLICATE_VALUE_TEMPLATE = "Duplicate: %s";
  private static final String MISSING_VALUE_TEMPLATE = "Missing: %s";
  private static final String MULTIPLE_VALUES_TEMPLATE =
      "Multiple values available for %s and predicate %s";
  private static final String ALREADY_REPLACED_VALUE_TEMPLATE = "Already replaced: %s";
  private static final String CAPTIVE_DEPENDENCY_TEMPLATE =
      "Captive dependency detected: @Singleton %s depends directly on non-singleton %s. "
          + "Mark %s as @Singleton, register it as an object, or inject Provider<%s>/Supplier<%s> instead.";
  private static final String CYCLE_TEMPLATE = "Found cycle: %s";
  private static final String MULTIPLE_INJECT_CONSTRUCTORS_TEMPLATE =
      "Multiple @Inject constructors found for class: %s";
  private static final String NO_SUITABLE_CONSTRUCTORS_TEMPLATE =
      "No default or @Inject constructors found for class: %s";
  private static final String NOT_SUPPORTED_WRAPPER_TEMPLATE =
      "Invalid wrapper type: %s (only "
          + Provider.class.getName()
          + " or "
          + Supplier.class.getName()
          + " are supported)";
  private static final String NESTED_WRAPPER_TEMPLATE = "Invalid nested wrapping of %s by %s";
  private static final String NO_WRAPPER_EXPECTED_TEMPLATE =
      "Expected to have non-generic, plain class";
  private static final String NON_INSTANTIABLE_CLASS_TEMPLATE =
      "Class is abstract or an interface and cannot be instantiated";
  private static final Comparator<Annotation> ANNOTATION_COMPARATOR =
      Comparator.comparing((Annotation annotation) -> annotation.annotationType().getName())
          .thenComparing(Annotation::toString);
  private static final Comparator<Key<?>> KEY_COMPARATOR =
      Comparator.comparing((Key<?> key) -> key.type().getName())
          .thenComparing(Injector::annotationSortValue);

  private final Node<Injector> currentInjector;
  private final Map<Key<?>, Node<?>> providers;
  private final AtomicBoolean closed;

  /**
   * Default constructor.
   *
   * @param injectorReference for deferred lookups to the current instance
   * @param providers of the dependencies to be used
   */
  private Injector(InjectorReference injectorReference, Map<Key<?>, Node<?>> providers) {
    injectorReference.set(this);

    this.providers = Map.copyOf(providers);
    this.currentInjector = new Value<>(injectorReference, Injector.this);
    this.closed = new AtomicBoolean(false);
  }

  /**
   * Retrieve fully initialized instance of the class.
   *
   * <p>This call will also create all required dependencies for this class.
   *
   * @param clazz to retrieve
   * @param <T> is the type of the instance
   * @return initialized instance
   * @throws IllegalArgumentException if the class argument is {@code null}
   */
  public <T> T get(@Nullable Class<T> clazz) {
    if (clazz == null) {
      throw new IllegalArgumentException(String.format(NULL_VALUE_TEMPLATE, "Class"));
    }

    final var node = getNode(new Key<>(clazz, getQualifierAnnotations(clazz.getAnnotations())));
    return node.get();
  }

  /**
   * Retrieve fully initialized instance of the class.
   *
   * <p>This call will also create all required dependencies for this class.
   *
   * <p>Useful in conjunction with {@link #findOne(Class, KeyAnnotationsPredicate)} or {@link
   * #findAll(Class, KeyAnnotationsPredicate)}.
   *
   * @param key to retrieve
   * @param <T> is the type of the instance
   * @return initialized instance
   * @throws IllegalArgumentException if the key argument is {@code null}
   */
  public <T> T get(@Nullable Key<T> key) {
    if (key == null) {
      throw new IllegalArgumentException(String.format(NULL_VALUE_TEMPLATE, "Key"));
    }

    final var node = getNode(key);
    return node.get();
  }

  /**
   * Find if {@link Injector} has an instance of specified class with certain annotations and their
   * values.
   *
   * @param clazz to find
   * @param keyAnnotationsPredicate to match
   * @return an {@link Optional} {@link Key} which can be used to retrieve dependency
   * @param <T> is the type of the instance
   * @throws IllegalArgumentException if class or predicate arguments is {@code null}
   * @throws IllegalStateException if there is more than one dependency match
   */
  public <T> Optional<Key<T>> findOne(
      @Nullable Class<T> clazz, @Nullable KeyAnnotationsPredicate keyAnnotationsPredicate) {
    final var keys = findAll(clazz, keyAnnotationsPredicate);

    if (keys.isEmpty()) {
      return Optional.empty();
    }

    if (keys.size() > 1) {
      throw new IllegalStateException(
          String.format(MULTIPLE_VALUES_TEMPLATE, clazz, keyAnnotationsPredicate));
    }

    return Optional.of(keys.get(0));
  }

  /**
   * Find if {@link Injector} has an instance of specified class with certain annotations and their
   * values.
   *
   * @param clazz to find
   * @param keyAnnotationsPredicate to match
   * @return a {@link List} of {@link Key}s which can be used to retrieve dependencies
   * @param <T> is the type of the instance
   * @throws IllegalArgumentException if class or predicate arguments is {@code null}
   */
  @SuppressWarnings("unchecked")
  public <T> List<Key<T>> findAll(
      @Nullable Class<T> clazz, @Nullable KeyAnnotationsPredicate keyAnnotationsPredicate) {
    if (clazz == null) {
      throw new IllegalArgumentException(String.format(NULL_VALUE_TEMPLATE, "Class"));
    }

    if (keyAnnotationsPredicate == null) {
      throw new IllegalArgumentException(String.format(NULL_VALUE_TEMPLATE, "Injector predicate"));
    }

    final var keys = new ArrayList<Key<T>>();

    for (Key<?> key : providers.keySet()) {
      if (clazz.isAssignableFrom(key.type()) && keyAnnotationsPredicate.test(key)) {
        keys.add((Key<T>) key);
      }
    }

    return List.copyOf(keys);
  }

  /**
   * Find if {@link Injector} has an instance of specified class.
   *
   * @param clazz to find
   * @return an {@link Optional} {@link Key} which can be used to retrieve dependency
   * @param <T> is the type of the instance
   * @throws IllegalArgumentException if class argument is {@code null}
   * @throws IllegalStateException if there is more than one dependency match
   */
  public <T> Optional<Key<T>> findOne(@Nullable Class<T> clazz) {
    return findOne(clazz, KeyAnnotationsPredicate.alwaysMatch());
  }

  /**
   * Find if {@link Injector} has an instance of specified class.
   *
   * @param clazz to find
   * @return a {@link List} of {@link Key}s which can be used to retrieve dependencies
   * @param <T> is the type of the instance
   * @throws IllegalArgumentException if class argument is {@code null}
   */
  public <T> List<Key<T>> findAll(@Nullable Class<T> clazz) {
    return findAll(clazz, KeyAnnotationsPredicate.alwaysMatch());
  }

  /**
   * Package-private retriever of specific nodes to be used in {@link #providers} via {@link
   * InjectorReference}.
   *
   * @param key of the dependency to fetch
   * @return a respective dependency graph node which instantiates this particular dependency
   * @param <T> is the type of the dependency
   * @throws NoSuchElementException if dependency for the key is not present
   */
  @SuppressWarnings("unchecked")
  <T> Node<T> getNode(Key<T> key) {
    if (isUnqualifiedInjectorKey(key)) {
      return (Node<T>) currentInjector;
    }

    if (providers.containsKey(key)) {
      return (Node<T>) providers.get(key);
    } else {
      throw new NoSuchElementException(String.format(MISSING_VALUE_TEMPLATE, key));
    }
  }

  /**
   * @return a {@link Builder} instance to construct {@link Injector}
   */
  public static Builder injector() {
    return new Builder();
  }

  /**
   * @return a {@link CopyBuilder} instance to modify {@link Injector}
   */
  public CopyBuilder copy() {
    return new CopyBuilder(this);
  }

  /** {@inheritDoc} */
  @Override
  public void close() {
    if (!closed.compareAndSet(false, true)) {
      return;
    }

    final var keys = topologicallySortedKeys(providers);

    // Going in the reverse order to close dependencies
    for (int i = keys.size() - 1; i >= 0; i--) {
      try {
        getNode(keys.get(i)).close();
      } catch (IOException e) {
        throw new UncheckedIOException("Failed to close dependency: " + keys.get(i), e);
      }
    }
  }

  /** {@inheritDoc} */
  @Override
  public boolean equals(@Nullable Object o) {
    if (!(o instanceof Injector)) return false;
    Injector injector = (Injector) o;
    return Objects.equals(providers, injector.providers);
  }

  /** {@inheritDoc} */
  @Override
  public int hashCode() {
    return Objects.hash(providers);
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return String.format(
        "injector:%s",
        providers.isEmpty()
            ? ConsoleConstants.YAML_EMPTY_ARRAY
            : String.format(
                "%n%s",
                topologicallySortedKeys(providers).stream()
                    .map(
                        key ->
                            String.format(
                                "%s%n%s",
                                key.toYamlString(true, 1),
                                Objects.requireNonNull(providers.get(key)).toYamlString(2)))
                    .collect(Collectors.joining(String.format("%n%n")))));
  }

  /**
   * Checks whether the key represents the injector's built-in unqualified self-reference.
   *
   * @param key to check
   * @return {@code true} if the key requests {@link Injector} without qualifier annotations
   */
  private static boolean isUnqualifiedInjectorKey(Key<?> key) {
    return Injector.class.equals(key.type()) && key.annotations().isEmpty();
  }

  /**
   * Identify annotations marked as {@link Qualifier}.
   *
   * @param annotations to filter
   * @return an immutable {@link Set} of {@link Qualifier} annotations
   */
  private static Set<Annotation> getQualifierAnnotations(Annotation[] annotations) {
    if (annotations.length == 0) {
      return Set.of();
    }

    final var qualifierAnnotations = new HashSet<Annotation>(annotations.length);
    for (Annotation annotation : annotations) {
      if (annotation.annotationType().isAnnotationPresent(Qualifier.class)) {
        qualifierAnnotations.add(annotation);
      }
    }

    return Set.copyOf(qualifierAnnotations);
  }

  /**
   * Defines algorithm to topologically sort providers map to ease YAML inspection output.
   *
   * <p>Due to sorting, related entities should be brought closed together.
   *
   * <p>At the very least, this will ensure that root dependencies will be closer to the top of the
   * list.
   *
   * @param providers dependency graph nodes keyed by injectable keys
   * @return topologically sorted dependencies.
   */
  private static List<Key<?>> topologicallySortedKeys(Map<Key<?>, Node<?>> providers) {
    final var inDegreeMap = new HashMap<Key<?>, Integer>(providers.size());
    final var childrenByParent = new HashMap<Key<?>, List<Key<?>>>(providers.size());

    for (Key<?> key : providers.keySet()) {
      childrenByParent.put(key, new ArrayList<>());
    }

    for (Map.Entry<Key<?>, Node<?>> entry : providers.entrySet()) {
      int inbound = 0;

      for (Key<?> parent : entry.getValue().requiredParentKeys()) {
        if (providers.containsKey(parent)) {
          inbound++;
          Objects.requireNonNull(childrenByParent.get(parent)).add(entry.getKey());
        }
      }

      inDegreeMap.put(entry.getKey(), inbound);
    }

    for (List<Key<?>> children : childrenByParent.values()) {
      children.sort(KEY_COMPARATOR);
    }

    final var nodesWithNoIncomingEdges = new PriorityQueue<>(KEY_COMPARATOR);
    for (Map.Entry<Key<?>, Integer> entry : inDegreeMap.entrySet()) {
      if (entry.getValue() == 0) {
        nodesWithNoIncomingEdges.add(entry.getKey());
      }
    }

    final var topologicalOrder = new ArrayList<Key<?>>(providers.size());
    while (!nodesWithNoIncomingEdges.isEmpty()) {
      final var key = nodesWithNoIncomingEdges.poll();
      topologicalOrder.add(key);

      for (Key<?> child : Objects.requireNonNull(childrenByParent.get(key))) {
        final int previous = Objects.requireNonNull(inDegreeMap.get(child));

        if (previous > 0) {
          final int updated = previous - 1;
          inDegreeMap.put(child, updated);

          if (updated == 0) {
            nodesWithNoIncomingEdges.add(child);
          }
        }
      }
    }

    return List.copyOf(topologicalOrder);
  }

  /**
   * Creates deterministic string representation of key annotations for sorting.
   *
   * @param key to create annotation sort value for
   * @return deterministic annotation sort value
   */
  private static String annotationSortValue(Key<?> key) {
    return key.annotations().stream()
        .sorted(ANNOTATION_COMPARATOR)
        .map(Annotation::toString)
        .collect(Collectors.joining("\n"));
  }

  private static final class AdjustedNode<T> extends Node<T> {
    private final Node<T> delegate;
    private final BiConsumer<Injector, ? super T> adjuster;
    private final boolean singleton;
    private final Supplier<T> supplier;

    /**
     * Default constructor.
     *
     * @param injectorReference for dependency lookups
     * @param delegate node to create and adjust values with
     * @param adjuster to tweak created instances after injection
     * @param singleton whether adjusted values must be memoized
     */
    private AdjustedNode(
        InjectorReference injectorReference,
        Node<T> delegate,
        BiConsumer<Injector, ? super T> adjuster,
        boolean singleton) {
      super(injectorReference, delegate.parentKeys());

      this.delegate = delegate;
      this.adjuster = adjuster;
      this.singleton = singleton;
      this.supplier =
          singleton ? Memoized.memoizedProvider(this::createAdjusted) : this::createAdjusted;
    }

    @Override
    public T get() {
      return supplier.get();
    }

    @Override
    public Set<Key<?>> requiredParentKeys() {
      return delegate.requiredParentKeys();
    }

    @Override
    public Node<T> copy(InjectorReference newInjector) {
      return new AdjustedNode<>(newInjector, delegate.copy(newInjector), adjuster, singleton);
    }

    @Override
    public String toYamlString(int indentationLevel) {
      return delegate.toYamlString(indentationLevel);
    }

    @Override
    public void close() throws IOException {
      delegate.close();
    }

    @Override
    public boolean equals(@Nullable Object o) {
      if (!(o instanceof AdjustedNode)) return false;
      if (!super.equals(o)) return false;
      AdjustedNode<?> that = (AdjustedNode<?>) o;
      return singleton == that.singleton
          && Objects.equals(delegate, that.delegate)
          && Objects.equals(adjuster, that.adjuster);
    }

    @Override
    public int hashCode() {
      return Objects.hash(super.hashCode(), delegate, adjuster, singleton);
    }

    /**
     * Creates value via delegate node and applies adjuster to it.
     *
     * @return adjusted value
     */
    private T createAdjusted() {
      final var instance = delegate.get();
      adjuster.accept(injectorReference().get(), instance);
      return instance;
    }
  }

  /** Contains additional logic to help construct {@link Injector} */
  public static final class Builder extends AbstractBuilder {
    /** Default constructor. */
    private Builder() {
      super();
    }

    /**
     * Adds classes to the {@link Injector}.
     *
     * @param clazz to add
     * @param additionalClasses to add
     * @return current builder
     */
    public Builder add(@Nullable Class<?> clazz, Class<?> @Nullable ... additionalClasses) {
      return synchronizeMutation(
          this,
          () -> {
            addClass(clazz);

            if (additionalClasses != null) {
              for (Class<?> additionalClass : additionalClasses) {
                addClass(additionalClass);
              }
            }
          });
    }

    /**
     * Adds class to the {@link Injector} with a post-construction adjuster.
     *
     * <p>The adjuster runs after constructor and field injection.
     *
     * @param clazz to add
     * @param adjuster to tweak created instances after injection
     * @return current builder
     * @param <T> is the type of the added class
     */
    public <T> Builder add(
        @Nullable Class<T> clazz, @Nullable BiConsumer<Injector, ? super T> adjuster) {
      if (adjuster == null) {
        throw new IllegalArgumentException(String.format(NULL_VALUE_TEMPLATE, "Adjuster"));
      }

      return synchronizeMutation(this, () -> addClass(clazz, adjuster));
    }

    /**
     * Adds plain objects to the {@link Injector}.
     *
     * @param object to add
     * @param additionalObjects to add
     * @return current builder
     */
    public Builder add(@Nullable Object object, Object @Nullable ... additionalObjects) {
      return synchronizeMutation(
          this,
          () -> {
            addObject(object);

            if (additionalObjects != null) {
              for (Object additionalObject : additionalObjects) {
                addObject(additionalObject);
              }
            }
          });
    }

    /**
     * Adds a class to the {@link Injector}.
     *
     * <p>Contains a special behavior to handle nested non-static classes - the default constructor
     * for those classes will always have an enclosing class instance as a first argument (to
     * provide access to the enclosing class members). To correctly instantiate such classes, we
     * need to register their enclosing classes as providers as well.
     *
     * @param clazz to add
     * @throws IllegalArgumentException if class is {@code null}, {@code abstract} or {@code
     *     interface}, as well as from any methods called by this method
     */
    private void addClass(@Nullable Class<?> clazz) {
      addClass(clazz, null);
    }

    /**
     * Adds a class to the {@link Injector} with an optional post-construction adjuster.
     *
     * @param clazz to add
     * @param adjuster to tweak created instances after injection, or {@code null} if not needed
     * @param <T> is the type of the added class
     */
    private <T> void addClass(
        @Nullable Class<T> clazz, @Nullable BiConsumer<Injector, ? super T> adjuster) {
      if (clazz == null) {
        throw new IllegalArgumentException(String.format(NULL_VALUE_TEMPLATE, "Class"));
      }

      if (Modifier.isAbstract(clazz.getModifiers()) || Modifier.isInterface(clazz.getModifiers())) {
        throw new IllegalArgumentException(NON_INSTANTIABLE_CLASS_TEMPLATE);
      }

      parseClassForGraph(clazz, false, adjuster);

      if (isNestedNonStaticClass(clazz)) {
        Class<?> current = clazz.getEnclosingClass();
        do {
          parseClassForGraph(current, true, null);
          current = current.getEnclosingClass();
        } while (current != null);
      }
    }

    /**
     * Adds a plain object instance to the {@link Injector}.
     *
     * @param value to add
     * @throws IllegalArgumentException if the value is {@code null} or graph already contains this
     *     value
     */
    private void addObject(@Nullable Object value) {
      if (value == null) {
        throw new IllegalArgumentException(String.format(NULL_VALUE_TEMPLATE, "Value"));
      }

      final var classKey =
          new Key<>(value.getClass(), getQualifierAnnotations(value.getClass().getAnnotations()));

      if (providers.containsKey(classKey)) {
        throw new IllegalArgumentException(String.format(DUPLICATE_VALUE_TEMPLATE, classKey));
      }

      providers.put(classKey, new Value<>(injectorReference, value));
    }
  }

  /** Contains additional logic to help modify {@link Injector} */
  public static final class CopyBuilder extends AbstractBuilder {
    /**
     * Default constructor which will copy existing providers to this builder.
     *
     * <p>Copying providers required to change {@link InjectorReference} instance used by them.
     *
     * @param injector to copy providers from
     */
    private CopyBuilder(Injector injector) {
      super();

      for (Map.Entry<Key<?>, Node<?>> injectorEntry : injector.providers.entrySet()) {
        this.providers.put(
            injectorEntry.getKey(), injectorEntry.getValue().copy(injectorReference));
      }
    }

    /**
     * Replaces existing class onto another class.
     *
     * @param from class to be replaced
     * @param to class to replace to
     * @param <F> is the type of existing class
     * @param <T> is the type of the new class, which is expected to be a subtype of the original
     *     class
     * @return current builder instance
     * @throws IllegalArgumentException if any of the arguments is {@code null} or from another
     *     method invocations
     */
    public <F, T extends F> CopyBuilder replace(@Nullable Class<F> from, @Nullable Class<T> to) {
      return replaceClass(from, to, null);
    }

    /**
     * Replaces existing class onto another class with a post-construction adjuster.
     *
     * <p>The adjuster runs after constructor and field injection.
     *
     * @param from class to be replaced
     * @param to class to replace to
     * @param adjuster to tweak created instances after injection
     * @param <F> is the type of existing class
     * @param <T> is the type of the new class, which is expected to be a subtype of the original
     *     class
     * @return current builder instance
     * @throws IllegalArgumentException if any of the arguments is {@code null} or from another
     *     method invocations
     */
    public <F, T extends F> CopyBuilder replace(
        @Nullable Class<F> from,
        @Nullable Class<T> to,
        @Nullable BiConsumer<Injector, ? super T> adjuster) {
      if (adjuster == null) {
        throw new IllegalArgumentException(String.format(NULL_VALUE_TEMPLATE, "Adjuster"));
      }

      return replaceClass(from, to, adjuster);
    }

    /**
     * Replaces existing class registration with another class registration.
     *
     * @param from class to be replaced
     * @param to class to replace to
     * @param adjuster to tweak created replacement instances after injection, or {@code null} if
     *     not needed
     * @param <F> is the type of existing class
     * @param <T> is the type of the new class, which is expected to be a subtype of the original
     *     class
     * @return current builder instance
     */
    private <F, T extends F> CopyBuilder replaceClass(
        @Nullable Class<F> from,
        @Nullable Class<T> to,
        @Nullable BiConsumer<Injector, ? super T> adjuster) {
      checkArguments(from, to);
      final var sourceClass = Objects.requireNonNull(from);
      final var targetClass = Objects.requireNonNull(to);

      if (sourceClass.equals(targetClass)) {
        return this;
      }

      if (!sourceClass.isAssignableFrom(targetClass)) {
        throw new IllegalArgumentException(NON_INSTANTIABLE_CLASS_TEMPLATE);
      }

      if (Modifier.isAbstract(targetClass.getModifiers())
          || Modifier.isInterface(targetClass.getModifiers())) {
        throw new IllegalArgumentException(NON_INSTANTIABLE_CLASS_TEMPLATE);
      }

      final var keys = createKeys(sourceClass, targetClass);

      // Closed via Injector.close()
      @SuppressWarnings("squid:S2095")
      final var node = createConstructsNode(targetClass, adjuster);

      return synchronize(
          () -> {
            providers.put(keys.getKey(), new RefersTo<>(injectorReference, keys.getValue()));
            providers.put(keys.getValue(), node);
            return this;
          });
    }

    /**
     * Replaces existing plain object instance onto another plain object instance.
     *
     * @param from instance to be replaced
     * @param to instance to replace to
     * @param <F> is the type of existing instance
     * @param <T> is the type of the new instance, which is expected to be a subtype of the original
     *     instance
     * @return current builder instance
     * @throws IllegalArgumentException if any of the arguments is {@code null} or from another
     *     method invocations
     */
    public <F, T extends F> CopyBuilder replace(@Nullable F from, @Nullable T to) {
      checkArguments(from, to);
      final var source = Objects.requireNonNull(from);
      final var target = Objects.requireNonNull(to);

      if (source.equals(target)) {
        return this;
      }

      if (!source.getClass().isAssignableFrom(target.getClass())) {
        throw new IllegalArgumentException(NON_INSTANTIABLE_CLASS_TEMPLATE);
      }

      final var keys = createKeys(source.getClass(), target.getClass());

      return synchronize(
          () -> {
            final var currentNode = providers.get(keys.getKey());
            if (!(currentNode instanceof Value<?>) || currentNode.get() != source) {
              throw new IllegalArgumentException(
                  String.format(MISSING_VALUE_TEMPLATE, keys.getKey()));
            }

            providers.put(keys.getKey(), new RefersTo<>(injectorReference, keys.getValue()));
            providers.put(keys.getValue(), new Value<>(injectorReference, target));
            return this;
          });
    }

    /**
     * Small shortcut to get rid of duplicate checks, providing named exceptions.
     *
     * @param from parameter to check
     * @param to parameter to check
     */
    private void checkArguments(@Nullable Object from, @Nullable Object to) {
      if (from == null) {
        throw new IllegalArgumentException(String.format(NULL_VALUE_TEMPLATE, "From"));
      }

      if (to == null) {
        throw new IllegalArgumentException(String.format(NULL_VALUE_TEMPLATE, "To"));
      }
    }

    /**
     * Create replacement qualifier keys and verify their integrity.
     *
     * @param from class to be replaced
     * @param to class to replace to
     * @param <F> is the type of existing class
     * @param <T> is the type of the new class, which is expected to be a subtype of the original
     *     class
     * @return immutable entry where key is source key and value is replacement key
     * @throws IllegalArgumentException if {@code from} was not present before or has been replaced,
     *     or {@code to} is already registered
     */
    private <F, T> Map.Entry<Key<F>, Key<T>> createKeys(Class<F> from, Class<T> to) {
      final var fromKey = new Key<>(from, getQualifierAnnotations(from.getAnnotations()));
      final var toKey = new Key<>(to, getQualifierAnnotations(to.getAnnotations()));

      if (!providers.containsKey(fromKey)) {
        throw new IllegalArgumentException(String.format(MISSING_VALUE_TEMPLATE, fromKey));
      }

      if (providers.get(fromKey) instanceof RefersTo<?>) {
        throw new IllegalArgumentException(String.format(ALREADY_REPLACED_VALUE_TEMPLATE, fromKey));
      }

      if (providers.containsKey(toKey)) {
        throw new IllegalArgumentException(String.format(DUPLICATE_VALUE_TEMPLATE, toKey));
      }

      return new AbstractMap.SimpleImmutableEntry<>(fromKey, toKey);
    }
  }

  /** Shared builder logic. */
  private abstract static class AbstractBuilder {
    /** Injector reference used by graph nodes created by this builder. */
    protected final InjectorReference injectorReference;

    /** Mutable provider graph accumulated by this builder. */
    protected final Map<Key<?>, Node<?>> providers;

    private final Lock providersLock;

    /** Default constructor. */
    protected AbstractBuilder() {
      this.injectorReference = new InjectorReference();
      this.providers = new HashMap<>();
      this.providersLock = new ReentrantLock();
    }

    /**
     * Creates new {@link Injector} instance, verifying that all necessary objects are present in
     * the dependency graph.
     *
     * @return new {@link Injector} instance
     * @throws IllegalArgumentException if created dependency graph does not contain the required
     *     dependency
     */
    public final Injector build() {
      return synchronize(
          () -> {
            for (Map.Entry<Key<?>, Node<?>> entry : providers.entrySet()) {
              for (Key<?> key : entry.getValue().parentKeys()) {
                if (!isUnqualifiedInjectorKey(key) && !providers.containsKey(key)) {
                  throw new IllegalArgumentException(String.format(MISSING_VALUE_TEMPLATE, key));
                }
              }
            }

            checkForCaptiveDependencies();

            for (Map.Entry<Key<?>, Node<?>> entry : providers.entrySet()) {
              checkForCycle(entry.getKey(), entry.getValue());
            }

            final var newInjectorReference = new InjectorReference();
            final var copiedProviders = new HashMap<Key<?>, Node<?>>(providers.size());
            for (Map.Entry<Key<?>, Node<?>> entry : providers.entrySet()) {
              copiedProviders.put(entry.getKey(), entry.getValue().copy(newInjectorReference));
            }

            return new Injector(newInjectorReference, Map.copyOf(copiedProviders));
          });
    }

    /**
     * Run a builder operation under {@link Lock} to maintain thread-safety.
     *
     * <p>Its primary purpose is to avoid possible mistakes.
     *
     * @param supplier of the result for the operation
     * @return operation outcome
     * @param <T> is the type of the operation outcome
     */
    protected final <T> T synchronize(Supplier<T> supplier) {
      providersLock.lock();
      try {
        return supplier.get();
      } finally {
        providersLock.unlock();
      }
    }

    /**
     * Runs a mutating builder operation under {@link Lock}, rolling back provider changes if the
     * operation fails.
     *
     * @param builder to return after successful mutation
     * @param mutation to run
     * @param <B> is the type of the builder
     * @return provided builder after successful mutation
     */
    protected final <B extends AbstractBuilder> B synchronizeMutation(
        B builder, Runnable mutation) {
      return synchronize(
          () -> {
            final var snapshot = new HashMap<>(providers);
            try {
              mutation.run();
              return builder;
            } catch (RuntimeException e) {
              providers.clear();
              providers.putAll(snapshot);
              throw e;
            }
          });
    }

    /**
     * Allows us to perform recursive class lookups, if required.
     *
     * <p>Contains a special behavior to handle nested non-static classes - the default constructor
     * for those classes will always have an enclosing class instance as a first argument (to
     * provide access to the enclosing class members). To correctly instantiate such classes, we
     * need to register their enclosing classes as providers as well.
     *
     * @param clazz to add
     * @param skipDuplicates marking whether we should tolerate duplicates to save execution time or
     *     throw an exception
     * @param adjuster to tweak created instances after injection, or {@code null} if not needed
     * @param <T> is the type of the class to parse
     * @throws IllegalArgumentException if non-tolerable duplicate was created or attempted to use
     *     {@link Collection} or {@link Map} for injection
     */
    protected <T> void parseClassForGraph(
        Class<T> clazz,
        boolean skipDuplicates,
        @Nullable BiConsumer<Injector, ? super T> adjuster) {
      final var classKey = new Key<>(clazz, getQualifierAnnotations(clazz.getAnnotations()));

      if (providers.containsKey(classKey)) {
        if (skipDuplicates) {
          return;
        } else {
          throw new IllegalArgumentException(String.format(DUPLICATE_VALUE_TEMPLATE, classKey));
        }
      }

      final var node = createConstructsNode(clazz, adjuster);
      providers.put(classKey, node);
      checkForCycle(classKey, node);

      for (Method providerMethod : getProviderMethods(clazz)) {
        parseProviderMethodForGraph(classKey, providerMethod);
      }
    }

    /**
     * Parses a {@link Provides} method and adds its node to the dependency graph.
     *
     * @param classKey key of the factory/configuration class declaring the method
     * @param providerMethod method to parse
     * @throws IllegalArgumentException if provider method return type is unsupported or duplicates
     *     an existing key
     */
    private void parseProviderMethodForGraph(Key<?> classKey, Method providerMethod) {
      final var methodReturnClass = getPlainClass(providerMethod.getGenericReturnType());

      if (Void.TYPE.equals(methodReturnClass)
          || Void.class.equals(methodReturnClass)
          || Provider.class.equals(methodReturnClass)
          || Supplier.class.equals(methodReturnClass)) {
        throw new IllegalArgumentException(NO_WRAPPER_EXPECTED_TEMPLATE);
      }

      final var methodAnnotations = getQualifierAnnotations(providerMethod.getAnnotations());
      final var methodKey =
          new Key<>(
              methodReturnClass,
              methodAnnotations.isEmpty()
                  ? getQualifierAnnotations(methodReturnClass.getAnnotations())
                  : methodAnnotations);
      final var methodReturnTypeFields = getFields(methodReturnClass);

      if (providers.containsKey(methodKey)) {
        throw new IllegalArgumentException(String.format(DUPLICATE_VALUE_TEMPLATE, methodKey));
      }

      final var methodParameters = getParameters(providerMethod);

      Node<?> methodNode;
      if (providerMethod.isAnnotationPresent(Singleton.class)
          || methodReturnClass.isAnnotationPresent(Singleton.class)) {
        methodNode =
            new ProvidesSingleton<>(
                injectorReference,
                classKey,
                providerMethod,
                methodReturnClass,
                methodParameters,
                methodReturnTypeFields);
      } else {
        methodNode =
            new ProvidesNew<>(
                injectorReference,
                classKey,
                providerMethod,
                methodReturnClass,
                methodParameters,
                methodReturnTypeFields);
      }

      providers.put(methodKey, methodNode);
      checkForCycle(methodKey, methodNode);
    }

    /**
     * Dismantles class definition to the {@link ConstructsNew} or {@link ConstructsSingleton} node.
     *
     * @param clazz to dismantle
     * @param adjuster to tweak created instances after injection, or {@code null} if not needed
     * @return new node for the dependency graph
     * @param <T> is the type of the node
     */
    protected <T> Node<T> createConstructsNode(
        Class<T> clazz, @Nullable BiConsumer<Injector, ? super T> adjuster) {
      final var constructor = getInjectableConstructor(clazz);
      final var constructorParameters = getParameters(constructor);
      final var classFields = getFields(clazz);

      final var isSingleton = clazz.isAnnotationPresent(Singleton.class);
      final Node<T> node =
          isSingleton
              ? new ConstructsSingleton<>(
                  injectorReference, constructor, constructorParameters, classFields)
              : new ConstructsNew<>(
                  injectorReference, constructor, constructorParameters, classFields);

      return adjuster == null
          ? node
          : new AdjustedNode<>(injectorReference, node, adjuster, isSingleton);
    }

    /**
     * Checks that singleton-scoped nodes do not directly depend on non-singleton nodes.
     *
     * @throws IllegalArgumentException if captive dependency is found
     */
    private void checkForCaptiveDependencies() {
      for (Map.Entry<Key<?>, Node<?>> entry : providers.entrySet()) {
        if (!isSingletonScoped(entry.getValue())) {
          continue;
        }

        for (Key<?> parentKey : entry.getValue().requiredParentKeys()) {
          if (mustSkipCaptiveDependencyCheck(entry.getKey(), entry.getValue(), parentKey)) {
            continue;
          }

          if (!isSingletonScoped(parentKey)) {
            throw new IllegalArgumentException(
                String.format(
                    CAPTIVE_DEPENDENCY_TEMPLATE,
                    entry.getKey(),
                    parentKey,
                    parentKey,
                    parentKey.type().getName(),
                    parentKey.type().getName()));
          }
        }
      }
    }

    /**
     * Determines whether a dependency edge should be ignored by captive dependency validation.
     *
     * @param ownerKey key of the node whose dependency is checked
     * @param ownerNode node whose dependency is checked
     * @param parentKey dependency key to evaluate
     * @return {@code true} if validation must skip the edge
     */
    private boolean mustSkipCaptiveDependencyCheck(
        Key<?> ownerKey, Node<?> ownerNode, Key<?> parentKey) {
      if (isUnqualifiedInjectorKey(parentKey) || !providers.containsKey(parentKey)) {
        return true;
      }

      if (ownerNode instanceof ProvidesNew<?>
          && ((ProvidesNew<?>) ownerNode).getClassKey().equals(parentKey)) {
        return true;
      }

      return isNestedNonStaticClass(ownerKey.type())
          && parentKey.type().equals(ownerKey.type().getEnclosingClass());
    }

    /**
     * Checks whether the node registered under the key is singleton-scoped.
     *
     * @param key to check
     * @return {@code true} if the registered node is singleton-scoped
     */
    private boolean isSingletonScoped(Key<?> key) {
      return isSingletonScoped(Objects.requireNonNull(providers.get(key)));
    }

    /**
     * Checks whether the node is singleton-scoped.
     *
     * @param node to check
     * @return {@code true} if the node is singleton-scoped
     */
    private boolean isSingletonScoped(Node<?> node) {
      if (node instanceof RefersTo<?>) {
        final var targetKey = node.parentKeys().iterator().next();
        return isSingletonScoped(targetKey);
      }

      return node instanceof ConstructsSingleton<?>
          || node instanceof ProvidesSingleton<?>
          || node instanceof Value<?>
          || (node instanceof AdjustedNode<?> && ((AdjustedNode<?>) node).singleton);
    }

    /**
     * Checks if there are any cycles in the dependency graph.
     *
     * <p>Nested non-static classes have additional behavior when it comes to constructors where the
     * enclosing class is passed as a first parameter to the class constructor.
     *
     * <p>Here for cycle detection, we want to exclude any enclosing classes from the visited set -
     * otherwise any other nested class within the same enclosing class will trigger a failure.
     *
     * @param key to start check from
     * @param node which corresponds to the key
     */
    protected void checkForCycle(Key<?> key, Node<?> node) {
      checkForCycle(key, node, new HashSet<>(), new HashSet<>());
    }

    /**
     * Checks recursively (BFS-like) if there are any cycles in the dependency graph.
     *
     * <p>Nested non-static classes have additional behavior when it comes to constructors where the
     * enclosing class is passed as a first parameter to the class constructor.
     *
     * <p>Here for cycle detection, we want to exclude any enclosing classes from the visited set -
     * otherwise any other nested class within the same enclosing class will trigger a failure.
     *
     * @param key to start check from
     * @param node which corresponds to the key
     * @param visiting keys in the current traversal path
     * @param processed keys already checked for cycles
     * @throws IllegalArgumentException if the cycle was found
     */
    private void checkForCycle(
        Key<?> key, Node<?> node, Set<Key<?>> visiting, Set<Key<?>> processed) {
      if (visiting.contains(key)) {
        throw new IllegalArgumentException(String.format(CYCLE_TEMPLATE, key));
      }

      if (processed.contains(key)) {
        return;
      }

      visiting.add(key);
      final var isNestedNonStaticClass = isNestedNonStaticClass(key.type());

      for (Key<?> consumedKey : node.requiredParentKeys()) {
        if (isNestedNonStaticClass && consumedKey.type().equals(key.type().getEnclosingClass())) {
          continue;
        }

        if (visiting.contains(consumedKey)) {
          throw new IllegalArgumentException(String.format(CYCLE_TEMPLATE, consumedKey));
        }

        if (providers.containsKey(consumedKey)) {
          checkForCycle(consumedKey, providers.get(consumedKey), visiting, processed);
        }
      }

      visiting.remove(key);
      processed.add(key);
    }

    /**
     * Identifies constructor for the class that should be used for injection, following priority
     * order:
     *
     * <ul>
     *   <li>First constructor with {@link Inject} annotation.
     *   <li>Default constructor.
     * </ul>
     *
     * <p>Contains a special behavior to handle nested non-static classes - the default constructor
     * for those classes will always have an enclosing class instance as a first argument (to
     * provide access to the enclosing class members), and we cover this scenario with additional
     * condition.
     *
     * @param clazz to look for constructor in
     * @param <T> is the type constructed by the returned constructor
     * @return {@link Constructor} to create an instance of the object
     * @throws IllegalArgumentException if there is no suitable constructor or if there is ambiguity
     *     between constructors
     */
    @SuppressWarnings("unchecked")
    protected static <T> Constructor<T> getInjectableConstructor(Class<T> clazz) {
      Constructor<T> injectConstructor = null;
      Constructor<T> defaultConstructor = null;

      for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
        if (constructor.isAnnotationPresent(Inject.class)) {
          if (injectConstructor == null) {
            injectConstructor = (Constructor<T>) constructor;
          } else {
            throw new IllegalArgumentException(
                String.format(MULTIPLE_INJECT_CONSTRUCTORS_TEMPLATE, clazz.getName()));
          }
        } else if (constructor.getParameterCount() == 0
            || (isNestedNonStaticClass(clazz) && constructor.getParameterCount() == 1)) {
          defaultConstructor = (Constructor<T>) constructor;
        }
      }

      if ((injectConstructor == null) == (defaultConstructor == null)) {
        throw new IllegalArgumentException(
            String.format(NO_SUITABLE_CONSTRUCTORS_TEMPLATE, clazz.getName()));
      }

      return injectConstructor == null ? defaultConstructor : injectConstructor;
    }

    /**
     * Identifies the keys which must be used to properly call specific {@link Constructor} or
     * {@link Method}.
     *
     * @param executable to identify {@link Key} for
     * @return an immutable list of {@link ParameterInformation} whose declaration order matches
     *     argument declaration order of the {@link Executable} parameters
     */
    protected static List<ParameterInformation> getParameters(Executable executable) {
      final var parameters = executable.getParameters();

      if (parameters.length == 0) {
        return List.of();
      }

      // For non-static nested classes returns all parameters, except first
      final var genericParameterTypes = executable.getGenericParameterTypes();

      final var information = new ArrayList<ParameterInformation>(parameters.length);

      final var indexDifference = parameters.length - genericParameterTypes.length;
      for (int i = 0; i < parameters.length; i++) {
        final var parameter = parameters[i];

        Class<?> valueClass;
        Class<?> wrapperClass;

        // Workaround for non-static nested classes
        final var j = i - indexDifference;
        if (j < 0) {
          valueClass = parameter.getType();
          wrapperClass = null;
        } else {
          final var genericParameterType = genericParameterTypes[j];
          final var valueWithWrapper = getGenericValueAndWrapper(genericParameterType);

          valueClass = valueWithWrapper.getKey();
          wrapperClass = valueWithWrapper.getValue();
        }

        information.add(
            new ParameterInformation(
                parameter,
                new Key<>(valueClass, getQualifierAnnotations(parameter.getAnnotations())),
                wrapperClass));
      }

      return List.copyOf(information);
    }

    /**
     * Identifies the key which must be used to fetch a value for the given {@link Field}.
     *
     * @param clazz to identify injectable {@link Field}s with their {@link Key}s
     * @return an immutable list of {@link FieldInformation} for injectable fields
     */
    protected static List<FieldInformation> getFields(Class<?> clazz) {
      final var fields = new ArrayList<FieldInformation>();

      Class<?> current = clazz;
      while (current != null && !Object.class.equals(current)) {
        for (Field field : current.getDeclaredFields()) {
          if (!field.isAnnotationPresent(Inject.class)) {
            continue;
          }

          if (Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers())) {
            throw new IllegalArgumentException("Injected field must not be static or final");
          }

          final var fieldType = field.getGenericType();
          final var valueWithWrapper = getGenericValueAndWrapper(fieldType);

          fields.add(
              new FieldInformation(
                  field,
                  new Key<>(
                      valueWithWrapper.getKey(), getQualifierAnnotations(field.getAnnotations())),
                  valueWithWrapper.getValue()));
        }

        current = current.getSuperclass();
      }

      return List.copyOf(fields);
    }

    /**
     * Identifies methods which provide dependencies to the rest of the classes.
     *
     * @param clazz to identify methods in
     * @return a {@link Set} of {@link Method} which return new dependencies
     */
    protected static Set<Method> getProviderMethods(Class<?> clazz) {
      final var methods = new HashSet<Method>();
      final var descendantMethods = new ArrayList<Method>();

      Class<?> current = clazz;
      while (!current.equals(Object.class)) {
        for (Method method : current.getDeclaredMethods()) {
          if (descendantMethods.stream().anyMatch(descendant -> overrides(descendant, method))) {
            continue;
          }

          descendantMethods.add(method);

          if (method.isAnnotationPresent(Provides.class)) {
            methods.add(method);
          }
        }

        current = current.getSuperclass();
      }

      return Set.copyOf(methods);
    }

    /**
     * Checks whether one method overrides another.
     *
     * @param descendant possible overriding method
     * @param ancestor possible overridden method
     * @return {@code true} if descendant overrides ancestor
     */
    private static boolean overrides(Method descendant, Method ancestor) {
      if (!ancestor.getDeclaringClass().isAssignableFrom(descendant.getDeclaringClass())) {
        return false;
      }

      final int descendantModifiers = descendant.getModifiers();
      final int ancestorModifiers = ancestor.getModifiers();
      if (Modifier.isPrivate(descendantModifiers)
          || Modifier.isPrivate(ancestorModifiers)
          || Modifier.isStatic(descendantModifiers)
          || Modifier.isStatic(ancestorModifiers)
          || Modifier.isFinal(ancestorModifiers)) {
        return false;
      }

      if (!descendant.getName().equals(ancestor.getName())
          || !Arrays.equals(descendant.getParameterTypes(), ancestor.getParameterTypes())) {
        return false;
      }

      return Modifier.isPublic(ancestorModifiers)
          || Modifier.isProtected(ancestorModifiers)
          || descendant
              .getDeclaringClass()
              .getPackageName()
              .equals(ancestor.getDeclaringClass().getPackageName());
    }

    /**
     * Fetch a non-generic class from a {@link Type}.
     *
     * @param type to analyze
     * @return plain class represented by the type
     * @throws IllegalArgumentException if the type cannot be represented by a plain class key
     */
    protected static Class<?> getPlainClass(Type type) {
      if (type instanceof Class<?>) {
        return (Class<?>) type;
      }

      if (type instanceof ParameterizedType
          || type instanceof TypeVariable<?>
          || type instanceof WildcardType
          || type instanceof GenericArrayType) {
        throw new IllegalArgumentException(NO_WRAPPER_EXPECTED_TEMPLATE);
      }

      throw new IllegalArgumentException(NO_WRAPPER_EXPECTED_TEMPLATE);
    }

    /**
     * Fetch the class of the value type and its generic wrapper class from the {@link Type}.
     *
     * <p>Used to identify {@link Provider} and {@link Supplier} usage.
     *
     * @param type to analyze
     * @return a {@link Map.Entry} where the key is the type of the value and value is the type of
     *     the wrapper
     */
    protected static Map.Entry<Class<?>, @Nullable Class<?>> getGenericValueAndWrapper(Type type) {
      Class<?> wrapperClass;
      Class<?> valueClass;

      if (type instanceof ParameterizedType) {
        final var parameterizedType = (ParameterizedType) type;
        wrapperClass = getPlainClass(parameterizedType.getRawType());

        if (ReflectionNode.isNotSupportedWrapperClass(wrapperClass)) {
          throw new IllegalArgumentException(
              String.format(NOT_SUPPORTED_WRAPPER_TEMPLATE, wrapperClass.getName()));
        }

        final var actualTypeArgument = parameterizedType.getActualTypeArguments()[0];

        if (actualTypeArgument instanceof ParameterizedType) {
          throw new IllegalArgumentException(
              String.format(
                  NESTED_WRAPPER_TEMPLATE,
                  wrapperClass.getSimpleName(),
                  actualTypeArgument.getTypeName()));
        }

        valueClass = getPlainClass(actualTypeArgument);
      } else {
        valueClass = getPlainClass(type);
        wrapperClass = null;

        if (Provider.class.equals(valueClass) || Supplier.class.equals(valueClass)) {
          throw new IllegalArgumentException(NO_WRAPPER_EXPECTED_TEMPLATE);
        }
      }

      return new AbstractMap.SimpleImmutableEntry<>(valueClass, wrapperClass);
    }

    /**
     * Checks if current {@link Class} is a nested non-static class.
     *
     * @param clazz to check
     * @return {@code true}, if the {@link Class} is a member class and it is not {@code static}
     */
    protected static boolean isNestedNonStaticClass(Class<?> clazz) {
      return clazz.isMemberClass() && !Modifier.isStatic(clazz.getModifiers());
    }
  }
}
