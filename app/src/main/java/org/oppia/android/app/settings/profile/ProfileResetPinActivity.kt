package org.oppia.android.app.settings.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.InjectableAppCompatActivity
import org.oppia.android.app.model.ScreenName.PROFILE_RESET_PIN_ACTIVITY
import org.oppia.android.app.settings.profile.ProfileResetPinActivityPresenter.Companion.PROFILE_RESET_PIN_IS_ADMIN_EXTRA_KEY
import org.oppia.android.app.settings.profile.ProfileResetPinActivityPresenter.Companion.PROFILE_RESET_PIN_PROFILE_ID_EXTRA_KEY
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import javax.inject.Inject

/** Activity that allows user to change a profile's PIN. */
class ProfileResetPinActivity : InjectableAppCompatActivity() {
  @Inject
  lateinit var profileResetPinActivityPresenter: ProfileResetPinActivityPresenter

  companion object {

    /** Returns [Intent] for opening [ProfileResetPinActivity]. */
    fun createIntent(context: Context, profileId: Int, isAdmin: Boolean): Intent {
      return Intent(context, ProfileResetPinActivity::class.java).apply {
        putExtra(PROFILE_RESET_PIN_PROFILE_ID_EXTRA_KEY, profileId)
        putExtra(PROFILE_RESET_PIN_IS_ADMIN_EXTRA_KEY, isAdmin)
        decorateWithScreenName(PROFILE_RESET_PIN_ACTIVITY)
      }
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as Injector).inject(this)
    profileResetPinActivityPresenter.handleOnCreate()
  }

  override fun onSupportNavigateUp(): Boolean {
    finish()
    return false
  }

  /** Dagger injector for [ProfileResetPinActivity]. */
  interface Injector {
    /** Injects dependencies into the [activity]. */
    fun inject(activity: ProfileResetPinActivity)
  }
}
