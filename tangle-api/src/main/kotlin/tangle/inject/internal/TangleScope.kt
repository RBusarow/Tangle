package tangle.inject.internal

/**
 * Used for precise scoping of Tangle dependencies,
 * such as a `ViewModel` scoped to a single `Fragment`.
 */
public abstract class TangleScope private constructor()

/**
 * Used for singleton scoping of Tangle dependencies,
 * running parallel to the App-scoped component.
 * This scope eliminates some need for additional scoping annotations.
 */
public abstract class TangleAppScope private constructor()
