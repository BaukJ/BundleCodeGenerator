package uk.bauk.alexa.utils.languages;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.junit.Test;

import uk.bauk.alexa.utils.languages.testutils.LocaleObjects;

public class TestParametisedFunctions implements LocaleObjects {
	@Test
	public void canPassInParams() {
		GB().getPFunc("A", "B");
	}
	@Test
	public void canPassInEmptyBrackets() {
		GB().getPFuncEmpty();
	}
	@Test
	public void canParamsGetPassedProperly() {
		GB().getPFunc("a", "b");
		Map<String, Integer> counts = new HashMap<>();
		for(int i = 0; i <= 100; i++) {
			String message = GB().getPFunc("a", "b");
			counts.put(message, counts.getOrDefault(message, 0)+1);
		}
		assertEquals("Only 4 different combinations can be made", 4, counts.keySet().size());
		assertTrue("message a-b_B1-a is present", counts.get("a-b_B1-a") > 10);
		assertTrue("message a-b_B2-a is present", counts.get("a-b_B2-a") > 10);
		assertTrue("message b-a_B1-a is present", counts.get("b-a_B1-a") > 10);
		assertTrue("message b-a_B2-a is present", counts.get("b-a_B2-a") > 10);
	}
}
