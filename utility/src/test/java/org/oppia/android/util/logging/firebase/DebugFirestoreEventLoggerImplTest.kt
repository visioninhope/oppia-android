package org.oppia.android.util.logging.firebase

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.model.EventLog
import org.oppia.android.testing.FakeFirestoreEventLogger
import org.oppia.android.testing.assertThrows
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.logging.AnalyticsEventLogger
import org.oppia.android.util.logging.EnableConsoleLog
import org.oppia.android.util.logging.EnableFileLog
import org.oppia.android.util.logging.ExceptionLogger
import org.oppia.android.util.logging.GlobalLogLevel
import org.oppia.android.util.logging.LogLevel
import org.oppia.android.util.logging.performancemetrics.PerformanceMetricsEventLogger
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [DebugFirestoreEventLoggerImpl]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class DebugFirestoreEventLoggerImplTest {
  @Inject
  lateinit var debugFirestoreLoggerImpl: DebugFirestoreEventLoggerImpl

  @Inject
  lateinit var debugFirestoreLogger: DebugFirestoreEventLogger

  private val eventLog1 = EventLog.newBuilder().setPriority(EventLog.Priority.ESSENTIAL).build()
  private val eventLog2 = EventLog.newBuilder().setPriority(EventLog.Priority.ESSENTIAL).build()

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testDebugFirestoreEventLogger_logEvent_returnsEvent() {
    debugFirestoreLogger.uploadEvent(eventLog1)
    val event = debugFirestoreLoggerImpl.getMostRecentEvent()

    Truth.assertThat(event).isEqualTo(eventLog1)
    Truth.assertThat(event.priority).isEqualTo(EventLog.Priority.ESSENTIAL)
  }

  @Test
  fun testDebugFirestoreEventLogger_logEventTwice_returnsLatestEvent() {
    debugFirestoreLogger.uploadEvent(eventLog1)
    debugFirestoreLogger.uploadEvent(eventLog2)
    val event = debugFirestoreLoggerImpl.getMostRecentEvent()

    Truth.assertThat(event).isEqualTo(eventLog2)
  }

  @Test
  fun testDebugFirestoreEventLogger_logEvent_clearAllEvents_logEventAgain_returnsLatestEvent() {
    debugFirestoreLogger.uploadEvent(eventLog1)
    debugFirestoreLoggerImpl.clearAllEvents()
    debugFirestoreLogger.uploadEvent(eventLog2)
    val event = debugFirestoreLoggerImpl.getMostRecentEvent()

    Truth.assertThat(event).isEqualTo(eventLog2)
  }

  @Test
  fun testDebugFirestoreEventLogger_logNothing_getMostRecent_returnsFailure() {
    assertThrows(NoSuchElementException::class) { debugFirestoreLoggerImpl.getMostRecentEvent() }
  }

  @Test
  fun testDebugFirestoreEventLogger_logEvent_clearAllEvents_getMostRecent_returnsFailure() {
    debugFirestoreLogger.uploadEvent(eventLog1)
    debugFirestoreLoggerImpl.clearAllEvents()

    val eventException = assertThrows(NoSuchElementException::class) {
      debugFirestoreLoggerImpl.getMostRecentEvent()
    }

    Truth.assertThat(eventException).isInstanceOf(NoSuchElementException::class.java)
  }

  @Test
  fun testDebugFirestoreEventLogger_clearAllEvents_returnsEmptyList() {
    debugFirestoreLoggerImpl.clearAllEvents()
    val isListEmpty = debugFirestoreLoggerImpl.getEventList().isEmpty()

    Truth.assertThat(isListEmpty).isTrue()
  }

  @Test
  fun testDebugFirestoreEventLogger_logEvent_clearAllEvents_returnsEmptyList() {
    debugFirestoreLogger.uploadEvent(eventLog1)
    debugFirestoreLoggerImpl.clearAllEvents()
    val isListEmpty = debugFirestoreLoggerImpl.getEventList().isEmpty()

    Truth.assertThat(isListEmpty).isTrue()
  }

  @Test
  fun testDebugFirestoreEventLogger_logMultipleEvents_clearAllEvents_returnsEmptyList() {
    debugFirestoreLogger.uploadEvent(eventLog1)
    debugFirestoreLogger.uploadEvent(eventLog2)
    debugFirestoreLoggerImpl.clearAllEvents()
    val isListEmpty = debugFirestoreLoggerImpl.getEventList().isEmpty()

    Truth.assertThat(isListEmpty).isTrue()
  }

  @Test
  fun testDebugFirestoreEventLogger_logEvent_returnsNonEmptyList() {
    debugFirestoreLogger.uploadEvent(eventLog1)
    val isListEmpty = debugFirestoreLoggerImpl.getEventList().isEmpty()

    Truth.assertThat(isListEmpty).isFalse()
  }

  private fun setUpTestApplicationComponent() {
    DaggerDebugFirestoreEventLoggerImplTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  // TODO(#89): Move this to a common test application component.
  @Module
  class TestModule {
    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
      return application
    }

    // TODO(#59): Either isolate these to their own shared test module, or use the real logging
    // module in tests to avoid needing to specify these settings for tests.
    @EnableConsoleLog
    @Provides
    fun provideEnableConsoleLog(): Boolean = true

    @EnableFileLog
    @Provides
    fun provideEnableFileLog(): Boolean = false

    @GlobalLogLevel
    @Provides
    fun provideGlobalLogLevel(): LogLevel = LogLevel.VERBOSE
  }

  @Module
  class TestLogReportingModule {
    @Provides
    @Singleton
    fun provideExceptionLogger(): ExceptionLogger =
      FirebaseExceptionLogger(FirebaseCrashlytics.getInstance())

    @Provides
    @Singleton
    fun provideDebugEventLogger(debugAnalyticsEventLogger: DebugAnalyticsEventLogger):
      AnalyticsEventLogger = debugAnalyticsEventLogger

    @Provides
    @Singleton
    fun providePerformanceMetricsEventLogger(
      factory: FirebaseAnalyticsEventLogger.Factory
    ): PerformanceMetricsEventLogger =
      factory.createPerformanceMetricEventLogger()

    @Provides
    fun provideDebugFirestoreEventLogger(
      debugFirestoreEventLogger: DebugFirestoreEventLoggerImpl
    ): DebugFirestoreEventLogger = debugFirestoreEventLogger

    @Provides
    fun provideFirestoreEventLogger(
      fakeFirestoreEventLogger: FakeFirestoreEventLogger
    ): FirestoreEventLogger = fakeFirestoreEventLogger
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      TestModule::class, TestLogReportingModule::class, RobolectricModule::class,
      TestDispatcherModule::class, FakeOppiaClockModule::class,
    ]
  )

  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder
      fun build(): TestApplicationComponent
    }

    fun inject(debugEventLoggerTest: DebugFirestoreEventLoggerImplTest)
  }
}