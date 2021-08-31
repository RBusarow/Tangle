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

package tangle.sample.core

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import com.squareup.anvil.annotations.ContributesBinding
import tangle.sample.core.di.ApplicationContext
import java.util.Locale
import javax.inject.Inject

fun interface TextToSpeechDelegate {
  fun speak(text: String)
}

@ContributesBinding(AppScope::class)
class RealTextToSpeechDelegate @Inject constructor(
  @ApplicationContext
  private val context: Context
) : TextToSpeechDelegate {

  private val tts = TextToSpeech(
    context,
    {},
    "com.google.android.tts"
  ).also {
    it.voice = Voice(
      "tangle-voice",
      Locale.US,
      Voice.QUALITY_VERY_HIGH,
      Voice.LATENCY_VERY_LOW,
      false,
      setOf()
    )
  }

  override fun speak(text: String) {
    tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
  }
}
