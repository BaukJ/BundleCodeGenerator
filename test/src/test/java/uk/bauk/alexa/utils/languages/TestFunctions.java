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

public class TestFunctions implements LocaleObjects {
	@Test
	public void testFunctionWeights() {
		Map<String, Integer> counts = new HashMap<>();
		counts.put("A1", 0);
		counts.put("A2", 0);
		counts.put("A3", 0);
		counts.put("B1", 0);
		counts.put("B2", 0);
		counts.put("B3", 0);
		counts.put("C1", 0);
		counts.put("C2", 0);
		assertEquals(8, counts.size());
		for(int i = 0; i <= 100; i++) {
			String message = GB().getListABandC();
			counts.put(message, counts.get(message)+1);
		}
		assertEquals("0 weight inside func should never appear", new Integer("0"), counts.get("B2"));
		assertTrue("1 weight should appear", 0 <= counts.get("B1"));
		int aTotal = counts.get("A1")+counts.get("A2")+counts.get("A3");
		assertTrue(String.format("2 weight (%s) should be more than 1 weight of functions with sub-weights(%s)", counts.get("C2"), aTotal), counts.get("C2") > aTotal);
		assertTrue("Sub message can be more common if weighted", counts.get("B1") > counts.get("C1"));
		assertEquals("No rogue messages were added", 8, counts.size());
	}
	@Test
	public void testFunctionGetsEmbedded() {
		assertEquals("Simple insertion works", "Hello Error", GB().getHelloError());
		Map<String, Integer> counts = new HashMap<>();
		for(int i = 0; i <= 100; i++) {
			String message = GB().getHellolistA();
			counts.put(message, counts.getOrDefault(message, 0)+1);
		}
		assertEquals("Output limited to available sub-messages", 3, counts.size());
		assertTrue("message A1 is present", counts.get("Hello A1") > 10);
		assertTrue("message A2 is present", counts.get("Hello A2") > 10);
		assertTrue("message A3 is present", counts.get("Hello A3") > 10);
	}
}
