package tangle.viewmodel.compose

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import dagger.assisted.Assisted
import tangle.viewmodel.VMInject
import javax.inject.Inject

class MyRepository @Inject constructor()

class MyViewModel @VMInject constructor(
  private val repository: MyRepository,
  @Assisted foo: String
) : ViewModel() {

  interface Factory {
    fun create(foo: String): MyViewModel
  }
}

@Composable
fun Test() {
  val vm = tangleViewModel<MyViewModel, MyViewModel.Factory> { create("foo") }
}

public inline fun <reified VM : ViewModel, reified F> tangleViewModel(
  factory: F.() -> VM
): VM {
  return TODO()
}
