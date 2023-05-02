package org.oppia.android.app.testing

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.InjectableAppCompatActivity
import org.oppia.android.util.platformparameter.PlatformParameterValue
import javax.inject.Inject

/**
 * A test activity to verify the injection of [PlatformParameterValue] in the ``SplashActivity``.
 * This test activity is used in integration tests for platform parameters.
 */
class SplashTestActivity : InjectableAppCompatActivity() {

  @Inject
  lateinit var splashTestActivityPresenter: SplashTestActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as Injector).inject(this)
    splashTestActivityPresenter.handleOnCreate()
  }

  interface Injector {
    fun inject(activity: SplashTestActivity)
  }

  companion object {
    fun createIntent(context: Context): Intent = Intent(context, SplashTestActivity::class.java)
  }
}
