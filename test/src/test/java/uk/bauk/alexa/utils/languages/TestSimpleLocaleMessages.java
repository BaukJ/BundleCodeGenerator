package uk.bauk.alexa.utils.languages;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.junit.Test;

import uk.bauk.alexa.utils.languages.testutils.LocaleObjects;

public class TestSimpleLocaleMessages implements LocaleObjects {
	@Test
	public void testSimpleString() {
		assertEquals("England", GB().getCountry());
		assertEquals("America", US().getCountry());
	}
	@Test
	public void testSimpleArray() {
		Set<String> gb_friends = new HashSet<String>();
		Set<String> us_friends = new HashSet<String>();
		for(int i = 0; i < 20; i++) {
			gb_friends.add(GB().getFriends());
			us_friends.add(US().getFriends());
		}
		assertEquals(3, gb_friends.size());
		assertEquals(2, us_friends.size());
		assertTrue(gb_friends.contains("France"));
		assertTrue(us_friends.contains("Canada"));
	}
}
