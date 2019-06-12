package uk.bauk.alexa.utils.languages;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.junit.Test;

import uk.bauk.alexa.utils.languages.annotation.GeneratorException;
import uk.bauk.alexa.utils.languages.testutils.LocaleObjects;

public class TestParametisedMessages implements LocaleObjects {
	@Test
	public void testStringParam() {
		assertEquals("Hello Kuba", GB().getWelcome("Kuba"));
	}
	@Test
	public void testReusingtringParam() {
		assertEquals("Hello KubaKubaKuba", GB().getTrippleWelcome("Kuba"));
	}
	@Test
	public void testMultipleStringParam() {
		assertEquals("Hello ABC", GB().getThreeWelcome("A", "B", "C"));
		assertEquals("Hello 1 222,1![]", GB().getThreeWelcome("1 ", "222,", "1![]"));
	}
	@Test
	public void specialCharsFailGeneration1() {
		assertEquals("Hello NAME\\1", GB().getWelcome("NAME\\1"));
	}
	public void specialCharsFailGeneration2() {
		assertEquals("Hello NAME$1", GB().getWelcome("NAME$1"));
	}

	@Test
	public void testParametersOrdering() {
		assertEquals("Hello 1 222,1![]", GB().getThreeWelcome("1 ", "222,", "1![]"));
		assertEquals("Hello 1 222,1![]", GB().getThreeWelcomeMap("1 ", "222,", "1![]"));
		assertEquals("Hello 54123", GB().getHelloRandomOrder54123("1", "4", "2", "5", "3"));
	}
}
