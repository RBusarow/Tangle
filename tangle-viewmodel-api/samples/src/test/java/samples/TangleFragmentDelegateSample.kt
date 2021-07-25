package samples

import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import tangle.viewmodel.tangleViewModel

class TangleFragmentDelegateSample {

  @Sample
  fun byTangleViewModelSample() {
    class MyFragment : Fragment() {

      val viewModel: MyViewModel by tangleViewModel()
    }
  }
}

class TangleActivityDelegateSample {

  @Sample
  fun byTangleViewModelSample() {
    class MyActivity : ComponentActivity() {

      val viewModel: MyViewModel by tangleViewModel()
    }
  }
}
