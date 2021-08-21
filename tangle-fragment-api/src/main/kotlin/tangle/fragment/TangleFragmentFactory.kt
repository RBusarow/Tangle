package tangle.fragment

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import javax.inject.Provider

public class TangleFragmentFactory(
  private val providerMap: Map<Class<out Fragment>, Provider<@JvmSuppressWildcards Fragment>>,
  @TangleFragmentProviderMap
  private val assistedProviderMap: Map<Class<out Fragment>, Provider<@JvmSuppressWildcards Fragment>>
) : FragmentFactory() {

  override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
    val fragmentClass = loadFragmentClass(classLoader, className)

    return providerMap[fragmentClass]?.get()
      ?: assistedProviderMap[fragmentClass]?.get()
      ?: return super.instantiate(classLoader, className)
  }
}
