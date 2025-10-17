/*
 * MIT License
 *
 * Copyright 2024 Roman Khlebnov
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
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.AbstractMap;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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

  private final Node<Injector> currentInjector;
  private final Map<Key<?>, Node<?>> providers;

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
  public <T> T get(Class<T> clazz) {
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
   * @throws IllegalArgumentException if the class argument is {@code null}
   */
  public <T> T get(Key<T> key) {
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
      Class<T> clazz, KeyAnnotationsPredicate keyAnnotationsPredicate) {
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
  public <T> List<Key<T>> findAll(Class<T> clazz, KeyAnnotationsPredicate keyAnnotationsPredicate) {
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
   * @throws IllegalArgumentException if class or predicate arguments is {@code null}
   * @throws IllegalStateException if there is more than one dependency match
   */
  public <T> Optional<Key<T>> findOne(Class<T> clazz) {
    return findOne(clazz, KeyAnnotationsPredicate.alwaysMatch());
  }

  /**
   * Find if {@link Injector} has an instance of specified class.
   *
   * @param clazz to find
   * @return a {@link List} of {@link Key}s which can be used to retrieve dependencies
   * @param <T> is the type of the instance
   * @throws IllegalArgumentException if class or predicate arguments is {@code null}
   */
  public <T> List<Key<T>> findAll(Class<T> clazz) {
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
    if (Injector.class.equals(key.type())) {
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
  public boolean equals(Object o) {
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
                                key.toYamlString(true, 1), providers.get(key).toYamlString(2)))
                    .collect(Collectors.joining(String.format("%n%n")))));
  }

  /**
   * Identify annotations marked as {@link Qualifier}.
   *
   * @param annotations to filter
   * @return an immutable {@link Set} of {@link Qualifier} annotations
   */
  private static Set<Annotation> getQualifierAnnotations(Annotation[] annotations) {
    if (annotations == null || annotations.length == 0) {
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
   * @return topologically sorted dependencies.
   */
  private static List<Key<?>> topologicallySortedKeys(Map<Key<?>, Node<?>> providers) {
    final var inDegreeMap = new HashMap<Key<?>, Integer>(providers.size());
    for (Map.Entry<Key<?>, Node<?>> entry : providers.entrySet()) {
      inDegreeMap.put(entry.getKey(), entry.getValue().requiredParentKeys().size());
    }

    final var nodesWithNoIncomingEdges = new ArrayDeque<Key<?>>(providers.size());
    for (Map.Entry<Key<?>, Integer> entry : inDegreeMap.entrySet()) {
      if (entry.getValue() == 0) {
        nodesWithNoIncomingEdges.add(entry.getKey());
      }
    }

    final var topologicalOrder = new ArrayList<Key<?>>(providers.size());
    while (!nodesWithNoIncomingEdges.isEmpty()) {
      final var key = nodesWithNoIncomingEdges.pollFirst();
      topologicalOrder.add(key);

      for (Map.Entry<Key<?>, Node<?>> entry : providers.entrySet()) {
        if (entry.getValue().requiredParentKeys().contains(key)) {
          inDegreeMap.put(entry.getKey(), inDegreeMap.get(entry.getKey()) - 1);

          if (inDegreeMap.get(entry.getKey()) == 0) {
            nodesWithNoIncomingEdges.add(entry.getKey());
          }
        }
      }
    }

    return List.copyOf(topologicalOrder);
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
    public Builder add(Class<?> clazz, Class<?>... additionalClasses) {
      addClass(clazz);

      if (additionalClasses != null) {
        for (Class<?> additionalClass : additionalClasses) {
          addClass(additionalClass);
        }
      }

      return this;
    }

    /**
     * Adds plain objects to the {@link Injector}.
     *
     * @param object to add
     * @param additionalObjects to add
     * @return current builder
     */
    public Builder add(Object object, Object... additionalObjects) {
      addObject(object);

      if (additionalObjects != null) {
        for (Object additionalObject : additionalObjects) {
          addObject(additionalObject);
        }
      }

      return this;
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
    private void addClass(Class<?> clazz) {
      if (clazz == null) {
        throw new IllegalArgumentException(String.format(NULL_VALUE_TEMPLATE, "Class"));
      }

      if (Modifier.isAbstract(clazz.getModifiers()) || Modifier.isInterface(clazz.getModifiers())) {
        throw new IllegalArgumentException(NON_INSTANTIABLE_CLASS_TEMPLATE);
      }

      synchronize(
          () -> {
            parseClassForGraph(clazz, false);

            if (isNestedNonStaticClass(clazz)) {
              Class<?> current = clazz.getEnclosingClass();
              do {
                parseClassForGraph(current, true);
                current = current.getEnclosingClass();
              } while (current != null);
            }

            return this;
          });
    }

    /**
     * Adds a plain object instance to the {@link Injector}.
     *
     * @param value to add
     * @throws IllegalArgumentException if the value is {@code null} or graph already contains this
     *     value
     */
    private void addObject(Object value) {
      if (value == null) {
        throw new IllegalArgumentException(String.format(NULL_VALUE_TEMPLATE, "Value"));
      }

      final var classKey =
          new Key<>(value.getClass(), getQualifierAnnotations(value.getClass().getAnnotations()));

      synchronize(
          () -> {
            if (providers.containsKey(classKey)) {
              throw new IllegalArgumentException(String.format(DUPLICATE_VALUE_TEMPLATE, classKey));
            }

            providers.put(classKey, new Value<>(injectorReference, value));
            return this;
          });
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
    public <F, T extends F> CopyBuilder replace(Class<F> from, Class<T> to) {
      checkArguments(from, to);

      if (from.equals(to)) {
        return this;
      }

      final var keys = createKeys(from, to);

      // Closed via Injector.close()
      @SuppressWarnings("squid:S2095")
      final var node = createConstructsNode(to);

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
    public <F, T extends F> CopyBuilder replace(F from, T to) {
      checkArguments(from, to);

      if (from.equals(to)) {
        return this;
      }

      final var keys = createKeys(from.getClass(), to.getClass());

      return synchronize(
          () -> {
            providers.put(keys.getKey(), new RefersTo<>(injectorReference, keys.getValue()));
            providers.put(keys.getValue(), new Value<>(injectorReference, to));
            return this;
          });
    }

    /**
     * Small shortcut to get rid of duplicate checks, providing named exceptions.
     *
     * @param from parameter to check
     * @param to parameter to check
     */
    private void checkArguments(Object from, Object to) {
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
     * @return current builder instance
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
    protected final InjectorReference injectorReference;
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
            for (Node<?> node : providers.values()) {
              for (Key<?> key : node.parentKeys()) {
                if (!Injector.class.equals(key.type()) && !providers.containsKey(key)) {
                  throw new IllegalArgumentException(String.format(MISSING_VALUE_TEMPLATE, key));
                }
              }
            }

            return new Injector(injectorReference, Map.copyOf(providers));
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
     * @throws IllegalArgumentException if non-tolerable duplicate was created or attempted to use
     *     {@link Collection} or {@link Map} for injection
     */
    protected void parseClassForGraph(Class<?> clazz, boolean skipDuplicates) {
      final var classKey = new Key<>(clazz, getQualifierAnnotations(clazz.getAnnotations()));

      if (providers.containsKey(classKey)) {
        if (skipDuplicates) {
          return;
        } else {
          throw new IllegalArgumentException(String.format(DUPLICATE_VALUE_TEMPLATE, classKey));
        }
      }

      final var node = createConstructsNode(clazz);
      providers.put(classKey, node);
      checkForCycle(classKey, node);

      for (Method providerMethod : getProviderMethods(clazz)) {
        final var methodReturnType = providerMethod.getGenericReturnType();

        if (methodReturnType instanceof ParameterizedType) {
          throw new IllegalArgumentException(NO_WRAPPER_EXPECTED_TEMPLATE);
        }

        final var methodReturnClass = (Class<?>) methodReturnType;
        final var methodAnnotations = getQualifierAnnotations(providerMethod.getAnnotations());
        final var methodKey = new Key<>(methodReturnClass, methodAnnotations);
        final var methodReturnTypeFields = getFields(methodReturnClass);

        if (providers.containsKey(methodKey)) {
          throw new IllegalArgumentException(String.format(DUPLICATE_VALUE_TEMPLATE, classKey));
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
    }

    /**
     * Dismantles class definition to the {@link ConstructsNew} or {@link ConstructsSingleton} node.
     *
     * @param clazz to dismantle
     * @return new node for the dependency graph
     * @param <T> is the type of the node
     */
    protected <T> Node<T> createConstructsNode(Class<T> clazz) {
      final var constructor = getInjectableConstructor(clazz);
      final var constructorParameters = getParameters(constructor);
      final var classFields = getFields(clazz);

      if (clazz.isAnnotationPresent(Singleton.class)) {
        return new ConstructsSingleton<>(
            injectorReference, constructor, constructorParameters, classFields);
      } else {
        return new ConstructsNew<>(
            injectorReference, constructor, constructorParameters, classFields);
      }
    }

    /**
     * Checks if there are any cycles in the dependency graph.
     *
     * <p>Nested non-static classes have additional behavior when it comes to constructors where
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
     * <p>Nested non-static classes have additional behavior when it comes to constructors where
     * enclosing class is passed as a first parameter to the class constructor.
     *
     * <p>Here for cycle detection, we want to exclude any enclosing classes from the visited set -
     * otherwise any other nested class within the same enclosing class will trigger a failure.
     *
     * @param key to start check from
     * @param node which corresponds to the key
     * @param visited set of nodes for validation
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
        if (
        // Skip nested node required classes
        (isNestedNonStaticClass && consumedKey.type().equals(key.type().getEnclosingClass()))
            // Skip method's enclosing class
            || (node instanceof ProvidesNew
                && ((ProvidesNew<?>) node).getClassKey().equals(consumedKey))) {
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
     * @return a fixed size map of {@link Parameter} and their {@link Key} which declaration order
     *     matches argument declaration order of the {@link Executable} parameters
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
     * @return a fixed size map of {@link Field} and their {@link Key}s
     */
    protected static List<FieldInformation> getFields(Class<?> clazz) {
      final var fields = new ArrayList<FieldInformation>();

      Class<?> current = clazz;
      while (current != null && !Object.class.equals(current)) {
        for (Field field : current.getDeclaredFields()) {
          if (!field.isAnnotationPresent(Inject.class)) {
            continue;
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

      Class<?> current = clazz;
      while (!current.equals(Object.class)) {
        for (Method method : current.getDeclaredMethods()) {
          if (method.isAnnotationPresent(Provides.class)) {
            methods.add(method);
          }
        }

        current = current.getSuperclass();
      }

      return Set.copyOf(methods);
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
    protected static Map.Entry<Class<?>, Class<?>> getGenericValueAndWrapper(Type type) {
      Class<?> wrapperClass;
      Class<?> valueClass;

      if (type instanceof ParameterizedType) {
        final var parameterizedType = (ParameterizedType) type;

        wrapperClass = (Class<?>) parameterizedType.getRawType();

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

        valueClass = (Class<?>) actualTypeArgument;
      } else {
        valueClass = (Class<?>) type;
        wrapperClass = null;
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
