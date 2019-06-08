package uk.bauk.alexa.utils.languages;

import static org.junit.Assert.fail;

import java.security.InvalidParameterException;
import java.util.Locale;

import org.junit.Test;

import uk.bauk.alexa.utils.languages.testutils.LocaleObjects;

public class TestLocales {
	@Test
	public void testValidLocales() {
		SpeechFactory.getLocaleSpecificSpeech(Locale.forLanguageTag("en-GB"));
		SpeechFactory.getLocaleSpecificSpeech(Locale.forLanguageTag("en-US"));
		try {
			SpeechFactory.getLocaleSpecificSpeech(Locale.forLanguageTag("en"));
			fail("Should not have allowed this locale through");
		}catch (InvalidParameterException e) {
		}
	}
}
