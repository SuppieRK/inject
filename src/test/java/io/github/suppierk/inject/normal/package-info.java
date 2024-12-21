/**
 * Non-static nested classes have additional first constructor argument, which refers to enclosing
 * parent class to provide user with the access to its elements.
 *
 * <p>This Java behavior causes us to have two sets of similar yet independent tests:
 *
 * <ul>
 *   <li>One set for non-static nested classes which is defined in a single class outside of this
 *       package.
 *   <li>Second set uses {@code static} nested classes which act as a normal classes - but have to
 *       be defined in separate files, located within this package.
 * </ul>
 */
package io.github.suppierk.inject.normal;
