package tangle.fragment

import androidx.fragment.app.Fragment
import dagger.MapKey
import kotlin.reflect.KClass

@Target(AnnotationTarget.CONSTRUCTOR)
@Retention(AnnotationRetention.BINARY)
@MustBeDocumented
public annotation class FragmentInject

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
@MustBeDocumented
public annotation class FragmentInjectFactory

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
public annotation class ContributesFragment(
  /**
   * The scope in which to include this module.
   */
  val scope: KClass<*>,
  /**
   * This contributed module will replace these contributed classes. The array is allowed to
   * include other contributed bindings, multibindings and Dagger modules. All replaced classes
   * must use the same scope.
   */
  val replaces: Array<KClass<*>> = []
)

@Target(
  AnnotationTarget.FUNCTION,
  AnnotationTarget.PROPERTY_GETTER,
  AnnotationTarget.PROPERTY_SETTER,
  AnnotationTarget.CLASS
)
@Retention(AnnotationRetention.RUNTIME)
@MapKey
annotation class FragmentKey(val value: KClass<out Fragment>)
