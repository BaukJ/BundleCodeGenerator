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

public class TestWeights implements LocaleObjects {
	@Test
	public void testWeights() {
		Map<String, Integer> counts = new HashMap<>();
		counts.put("W0", 0);
		counts.put("W1", 0);
		counts.put("W2", 0);
		counts.put("W3", 0);
		assertEquals(4, counts.size());
		for(int i = 0; i <= 100; i++) {
			String message = GB().getWeightedList();
			counts.put(message, counts.get(message)+1);
		}
		assertEquals("0 weight should never appear", new Integer("0"), counts.get("W0"));
		assertTrue("1 weight should appear", 0 <= counts.get("W1"));
		assertTrue("2 weight should be more than 1 weight", counts.get("W2") > counts.get("W1"));
		assertTrue("10 weight should be more than 2 weight", counts.get("W3") > counts.get("W2"));
		assertTrue("10 weight should be more than 60%", counts.get("W3") > 60);
		assertEquals("No rogue messages were added", 4, counts.size());
	}
}
