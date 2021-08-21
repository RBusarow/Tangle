package samples

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import tangle.inject.TangleParam
import tangle.viewmodel.VMInject
import tangle.viewmodel.tangleViewModel

class VMInjectSample {

  @Sample
  fun vmInjectSample() {

    class MyViewModel @VMInject constructor(
      val repository: MyRepository,
      @TangleParam("userId")
      val userId: Int
    ) : ViewModel()

    class MyFragment : Fragment() {
      val viewModel by tangleViewModel<MyViewModel>()
    }
  }
}
