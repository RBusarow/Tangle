package tangle.inject

@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.BINARY)
public annotation class TangleParam(val name: String)
