package org.oppia.android.app.notice

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import org.oppia.android.app.fragment.InjectableDialogFragment
import javax.inject.Inject

/**
 * Dialog fragment to be shown when the user may be unaware that they've updated from a pre-release
 * version of the app to general availability.
 */
class GeneralAvailabilityUpgradeNoticeDialogFragment : InjectableDialogFragment() {
  companion object {
    /** Returns a new instance of [GeneralAvailabilityUpgradeNoticeDialogFragment]. */
    fun newInstance(): GeneralAvailabilityUpgradeNoticeDialogFragment =
      GeneralAvailabilityUpgradeNoticeDialogFragment()
  }

  @Inject lateinit var presenter: GeneralAvailabilityUpgradeNoticeDialogFragmentPresenter

  override fun onAttach(context: Context) {
    super.onAttach(context)
    (fragmentComponent as Injector).inject(this)
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    return presenter.handleOnCreateDialog()
  }

  interface Injector {
    fun inject(fragment: GeneralAvailabilityUpgradeNoticeDialogFragment)
  }
}
