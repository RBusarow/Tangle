package samples

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import tangle.viewmodel.ContributesViewModel
import tangle.viewmodel.VMInject
import tangle.viewmodel.tangle

public class TangleFragmentDelegateSample {

  @Sample
  public fun byTangleSample() {
    class MyFragment : Fragment() {

      val viewModel: MyViewModel by tangle()
    }
  }
}

@ContributesViewModel(Unit::class)
public class MyViewModel @VMInject constructor() : ViewModel()
