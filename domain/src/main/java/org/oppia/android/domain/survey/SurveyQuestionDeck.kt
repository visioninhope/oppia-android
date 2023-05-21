package org.oppia.android.domain.survey

import org.oppia.android.app.model.EphemeralSurveyQuestion
import org.oppia.android.app.model.SurveyQuestion

/** Tracks the dynamic behavior of the user through a survey session. */
class SurveyQuestionDeck constructor(
  initialQuestion: SurveyQuestion,
  private val isTopOfDeckTerminalChecker: (SurveyQuestion) -> Boolean
) {
  private val previousQuestions: MutableList<EphemeralSurveyQuestion> = ArrayList()
  private var questionIndex: Int = 0
  private var pendingTopQuestion: SurveyQuestion = initialQuestion

  /** Navigates to the previous question in the deck or fails if it is not possible. */
  fun navigateToPreviousQuestion() {
    check(!isCurrentQuestionInitial()) {
      "Cannot navigate to previous question; at initial question."
    }
    questionIndex--
  }

  /** Navigates to the next question in the deck or fails if it is not possible. */
  fun navigateToNextQuestion() {
    check(!isCurrentQuestionTopOfDeck()) {
      "Cannot navigate to next question; This is the most recent question."
    }
    val previousQuestion = previousQuestions[questionIndex]

    questionIndex++

    if (!previousQuestion.hasNextQuestion) {
      previousQuestions[questionIndex - 1] =
        previousQuestion.toBuilder().setHasNextQuestion(true).build()
    }
  }

  /**
   * Returns the [SurveyQuestion] corresponding to the latest question in the deck, regardless of
   * whichever state the learner is currently viewing.
   */
  fun getPendingTopQuestion(): SurveyQuestion = pendingTopQuestion

  /** Returns the index of the current selected question of the deck. */
  fun getTopQuestionIndex(): Int = questionIndex

  /**
   * Returns the number of unique questions that have been viewed so far in the deck
   * (i.e. the size of the deck).
   */
  fun getViewedQuestionCount(): Int = previousQuestions.size

  /** Returns the current [SurveyQuestion] being viewed by the learner. */
  fun getCurrentQuestion(): SurveyQuestion {
    return when {
      isCurrentQuestionTopOfDeck() -> pendingTopQuestion
      else -> previousQuestions[questionIndex].question
    }
  }

  private fun getPreviousQuestion(): EphemeralSurveyQuestion {
    return previousQuestions[questionIndex]
  }

  /** Returns whether this is the first question in the survey. */
  fun isCurrentQuestionInitial(): Boolean {
    return questionIndex == 0
  }

  /** Returns whether this is the most recent question in the survey. */
  fun isCurrentQuestionTopOfDeck(): Boolean {
    return questionIndex == previousQuestions.size
  }

  /** Returns whether this is the last question in the survey. */
  fun isCurrentQuestionTerminal(): Boolean {
    return isCurrentQuestionTopOfDeck() && isTopOfDeckTerminal()
  }

  /** Returns whether the most recent card on the deck is terminal. */
  private fun isTopOfDeckTerminal(): Boolean {
    return isTopOfDeckTerminalChecker(pendingTopQuestion)
  }
}
