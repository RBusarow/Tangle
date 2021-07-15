package tangle.inject.annotations.internal

/**
 * This is an internal implementation for Tangle.  Do not use.
 */
@RequiresOptIn
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY)
public annotation class InternalTangleApi
