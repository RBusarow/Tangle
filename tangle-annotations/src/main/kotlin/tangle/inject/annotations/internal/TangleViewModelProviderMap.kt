package tangle.inject.annotations.internal

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.FIELD, AnnotationTarget.FUNCTION)
public annotation class TangleViewModelProviderMap {
  @Qualifier
  @Retention(AnnotationRetention.RUNTIME)
  @Target(AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.FIELD, AnnotationTarget.FUNCTION)
  public annotation class KeySet
}
