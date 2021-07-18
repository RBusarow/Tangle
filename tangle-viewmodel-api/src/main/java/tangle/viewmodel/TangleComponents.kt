package tangle.viewmodel

public object TangleComponents {

  @PublishedApi
  @Suppress("ObjectPropertyNaming")
  internal val _components: MutableSet<Any> = mutableSetOf()

  public fun add(component: Any) {
    _components.add(component)
  }

  public inline fun <reified T> get(): T = _components
    .filterIsInstance<T>()
    .single()

  @Suppress("UNCHECKED_CAST")
  @JvmStatic
  public fun <T : Any> get(clazz: Class<T>): T = _components
    .single { clazz.kotlin.isInstance(it) } as T
}
