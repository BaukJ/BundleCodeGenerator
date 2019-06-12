package uk.bauk.alexa.utils.languages.annotation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.hamcrest.CoreMatchers.*;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

import org.junit.Test;
import org.mockito.Mockito;

import uk.bauk.alexa.utils.languages.annotation.generators.MethodGenerator;

public class ParserUtilsTest implements ParserUtils {
	@Test
	public void testSimpleParameterParse() throws GeneratorException {
		assertTrue(parseTextForParams("Hello {name}").containsKey("name"));
		assertEquals(String.class, parseTextForParams("Hello {name}").get("name"));
	}
	@Test
	public void testMultipleParameterParse() throws GeneratorException {
		Map<String, Class<?>> parsed = parseTextForParams("Hello {n1}, {n2}{n3}");
		assertEquals(3, parsed.size());
		assertTrue(parsed.containsKey("n1"));
		assertTrue(parsed.containsKey("n2"));
		assertTrue(parsed.containsKey("n3"));
		assertEquals(String.class, parsed.get("n1"));
		assertEquals(String.class, parsed.get("n2"));
		assertEquals(String.class, parsed.get("n3"));
	}
	@Test(expected = GeneratorException.class)
	public void doNotAllowParametersNotDefined() throws GeneratorException {
		parseText("Hello {name}");
	}
	@Test
	public void allowParametersDefined() throws GeneratorException {
		parseText("Hello {name}", "name");
	}
	@Test(expected = GeneratorException.class)
	public void doNotAllowSpacesInParamName() throws GeneratorException {
		parseTextForParams("Hello {n1 }");
	}
	@Test(expected = GeneratorException.class)
	public void doNotAllowSpecialsInParamName() throws GeneratorException {
		parseTextForParams("Hello {n!}");
	}
	@Test(expected = GeneratorException.class)
	public void doNotAllowParamNameToStartWithNumber() throws GeneratorException {
		parseTextForParams("Hello {1n}");
	}
	private void parseText(String text, String... validParams) throws GeneratorException {
		MethodGenerator methodGeneratorMock = Mockito.mock(MethodGenerator.class);
		for(String p: validParams) {			
			Mockito.when(methodGeneratorMock.paramExists(p)).thenReturn(true);
		}
		parseText(text, methodGeneratorMock, null, "key_a");
	}
	private Map<String, Class<?>> parseTextForParams(String text) throws GeneratorException {
		Map<String, Class<?>> params = new HashMap<>();
		parseText(text, null, params, "key_a");
		return params;
	}
	
	
	@Test
	public void testErrorOnInvalidKeys_HappyScenarios() throws GeneratorException {
		// No keys or valid keys
		errorOnInvalidKeys(new HashMap<String, Object>(), "");
		// No keys but valid keys given
		errorOnInvalidKeys(new HashMap<String, Object>(), "", "a", "b", "c");
		// Duplicate keys given
		errorOnInvalidKeys(new HashMap<String, Object>(), "", "a", "b", "a");
	}
	@Test(expected = GeneratorException.class)
	public void testErrorOnInvalidKeys_InvalidKeyGiven() throws GeneratorException {
		errorOnInvalidKeys(valuelessMap("a", "b", "d", "c"), "", "a", "b", "c");
	}
	@Test(expected = GeneratorException.class)
	public void testErrorOnInvalidKeys_NoValidKeys() throws GeneratorException {
		errorOnInvalidKeys(valuelessMap("a"), "");
	}
	@Test
	public void testErrorOnInvalidKeys_InvalidKeysErrorMessage() {
		try{
			errorOnInvalidKeys(valuelessMap("a", "b"), "Invalid keys: %s");
			fail("Should have errored on invalid key");
		}catch (GeneratorException e) {
			assertThat(e.getMessage(), anyOf(containsString("keys: a, b"), containsString("keys: b, a")));
		}
	}
	
	private Map<String, Object> valuelessMap(String... keys) {
		Map<String, Object> map = new HashMap<String, Object>();
		for(String key: keys) {
			map.put(key, null);
		}
		return map;
	}
}
