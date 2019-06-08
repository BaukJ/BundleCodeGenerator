package uk.bauk.alexa.utils.languages.annotation;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface ParserUtils {
	Pattern basicVartiablePattern = Pattern.compile("^\\{(\\w*)\\}.*"); 
	default Map<String, Class<?>> parseText(String text) throws GeneratorException {
		Map<String, Class<?>> ret = new HashMap<String, Class<?>>();
		String processedText = text.replaceAll("\\{\\{", "").replaceAll("\\}\\}", "");
		while(!processedText.isEmpty()) {
			char start = processedText.charAt(0);
			switch (start) {
			case '{':
				Matcher match = basicVartiablePattern.matcher(processedText);
				if(match.matches()) {
					if(match.group(1).matches("^[0-9].*")) {
						throw new GeneratorException("String parameter cannot start with a number ("+match.group(1)+"): "+text);
					}
					ret.putIfAbsent(match.group(1), String.class);
					processedText = processedText.substring(match.end(1)+1); // Add 1 for closing curly brace
				} else {
					throw new GeneratorException("Could not parse text: "+text+"|"+processedText);
				}
				break;
			case '}':
				throw new GeneratorException("Could not parse text, rogue '}': "+text);
			default:
				processedText = processedText.replaceFirst("^[^\\{\\}]*", "");
				break;
			}
		}
		return ret;
	}
}
