package org.oppia.android.app.testing

import android.content.Context
import javax.inject.Inject
import org.oppia.android.app.fragment.InjectableFragment
import org.oppia.android.app.utility.SplitScreenManager

class ExplorationTestActivityFragment : InjectableFragment() {
  @Inject
  lateinit var splitScreenManager: SplitScreenManager

  override fun onAttach(context: Context) {
    super.onAttach(context)
    (fragmentComponent as Injector).inject(this)
  }

  interface Injector {
    fun inject(fragment: ExplorationTestActivityFragment)
  }
}
