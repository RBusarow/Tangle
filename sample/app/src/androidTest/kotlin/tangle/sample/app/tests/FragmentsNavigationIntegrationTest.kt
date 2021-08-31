/*
 * Copyright (C) 2021 Rick Busarow
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tangle.sample.app.tests

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import tangle.sample.ui.fragmentsWithArchNavigation.FragmentsArchNavigationMainActivity

@RunWith(AndroidJUnit4::class)
class FragmentsNavigationIntegrationTest {

  @Test
  fun selected_item_is_passed_to_next_screen() {
    ActivityScenario.launch(FragmentsArchNavigationMainActivity::class.java)

    Thread.sleep(500)

    Espresso.onView(ViewMatchers.withText("Goldendoodle"))
      .perform(ViewActions.click())

    Thread.sleep(500)

    Espresso.onView(ViewMatchers.withText("awesome temperament"))
      .perform(ViewActions.scrollTo())
      .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
  }
}
