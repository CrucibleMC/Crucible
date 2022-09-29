package io.github.crucible.util;

/**
 * Used in place we need a custom toString, but changing the original class toString can break things
 */
public interface ICrucibleString {
    String crucible_toString();
}
