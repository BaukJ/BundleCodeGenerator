package uk.bauk.alexa.utils.languages.testutils;

import java.util.Locale;

import uk.bauk.alexa.utils.languages.Speech;
import uk.bauk.alexa.utils.languages.SpeechFactory;

public interface LocaleObjects {
	default Speech GB() {
		return SpeechFactory.getLocaleSpecificSpeech(Locale.forLanguageTag("en-GB"));
	}
	default Speech US() {
		return SpeechFactory.getLocaleSpecificSpeech(Locale.forLanguageTag("en-US"));
	}

}
