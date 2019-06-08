package uk.bauk.alexa.utils.languages.annotation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Test;

public class ParserUtilsTest implements ParserUtils {
	@Test
	public void testSimpleParameterParse() throws GeneratorException {
		assertTrue(parseText("Hello {name}").containsKey("name"));
		assertEquals(String.class, parseText("Hello {name}").get("name"));
	}
	@Test
	public void testMultipleParameterParse() throws GeneratorException {
		Map<String, Class<?>> parsed = parseText("Hello {n1}, {n2}{n3}");
		assertEquals(3, parsed.size());
		assertTrue(parsed.containsKey("n1"));
		assertTrue(parsed.containsKey("n2"));
		assertTrue(parsed.containsKey("n3"));
		assertEquals(String.class, parsed.get("n1"));
		assertEquals(String.class, parsed.get("n2"));
		assertEquals(String.class, parsed.get("n3"));
	}
	@Test(expected = GeneratorException.class)
	public void doNotAllowSpacesInParamName() throws GeneratorException {
		parseText("Hello {n1 }");
	}
	@Test(expected = GeneratorException.class)
	public void doNotAllowSpecialsInParamName() throws GeneratorException {
		parseText("Hello {n!}");
	}
	@Test(expected = GeneratorException.class)
	public void doNotAllowParamNameToStartWithNumber() throws GeneratorException {
		parseText("Hello {1n}");
	}
}
