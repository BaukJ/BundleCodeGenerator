package uk.bauk.alexa.utils.languages.annotation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.bauk.alexa.utils.languages.annotation.generators.MethodGenerator;
import uk.bauk.alexa.utils.languages.annotation.messageparts.FunctionPart;
import uk.bauk.alexa.utils.languages.annotation.messageparts.MessagePart;
import uk.bauk.alexa.utils.languages.annotation.messageparts.ParamPart;
import uk.bauk.alexa.utils.languages.annotation.messageparts.StringPart;

public interface ParserUtils {
	Pattern basicVartiablePattern = Pattern.compile("^\\{(\\w*)\\}.*");
	Pattern basicFunctionPattern = Pattern.compile("^\\[(\\w*(\\((\\w*, *)*\\w*\\))?)\\].*");
	Pattern simpleStringPattern   = Pattern.compile("^([^\\{\\}\\[\\]]*).*");
//	default Map<String, Class<?>> parseText(String text) throws GeneratorException {
//		Map<String, Class<?>> ret = new HashMap<String, Class<?>>();
//		String processedText = text.replaceAll("\\{\\{", "").replaceAll("\\}\\}", "");
//		while(!processedText.isEmpty()) {
//			char start = processedText.charAt(0);
//			switch (start) {
//			case '{':
//				Matcher match = basicVartiablePattern.matcher(processedText);
//				if(match.matches()) {
//					if(match.group(1).matches("^[0-9].*")) {
//						throw new GeneratorException("String parameter cannot start with a number ("+match.group(1)+"): "+text);
//					}
//					ret.putIfAbsent(match.group(1), String.class);
//					processedText = processedText.substring(match.end(1)+1); // Add 1 for closing curly brace
//				} else {
//					throw new GeneratorException("Could not parse text: "+text+"|"+processedText);
//				}
//				break;
//			case '}':
//				throw new GeneratorException("Could not parse text, rogue '}': "+text);
//			default:
//				processedText = processedText.replaceFirst("^[^\\{\\}]*", "");
//				break;
//			}
//		}
//		return ret;
//	}
	/*
	 * Parses a message text for extracting out parameters and function calls.
	 * If provided parameters variable is null, found parameters are checked against 
	 * the methodGenerator and an exception is throw if they are not found. 
	 * @param	text			the test to be parsed
	 * @param	methodGenerator	the parent methodgenerator, used to validate params
	 * @param	parameters		the parameter map to add found parameters to
	 * @return					the list of messageParts that comprise the given text.
	 */
	default List<MessagePart> parseText(String text, MethodGenerator methodGenerator, Map<String, Class<?>> parameters) throws GeneratorException {
		return parseText(text, methodGenerator, parameters, methodGenerator.key);
	}
	/*
	 * Split this method out for easier testing
	 */
	default List<MessagePart> parseText(String text, MethodGenerator methodGenerator, Map<String, Class<?>> parameters, String key) throws GeneratorException {
		String processedText = text;
		List<MessagePart> deconstructedMessage = new ArrayList<>();
		// TODO, see if we can just keep pointer to point in string to keep from having processedText keep reassigning
		Matcher match;
		while(!processedText.isEmpty()) {
			char start = processedText.charAt(0);
			switch (start) {
			case '{':
				if(processedText.charAt(1) == '{') {
					deconstructedMessage.add(new StringPart("{"));
					processedText = processedText.substring(2);
					break;
				}
				match = basicVartiablePattern.matcher(processedText);
				if(match.matches()) {
					if(match.group(1).matches("^[0-9].*")) {
						throw new GeneratorException("String parameter cannot start with a number ("+match.group(1)+"): "+text);
					}
					if(parameters != null) {
						parameters.put(match.group(1), String.class);
					} else {
						if(!methodGenerator.paramExists(match.group(1))) {
							throw GeneratorException.format("Found undeclared parameter(%s) in message for key %s: %s", match.group(1), key, text);
						}						
					}
					deconstructedMessage.add(new ParamPart(match.group(1)));
					processedText = processedText.substring(match.end(1)+1); // Add 1 for closing curly brace
				} else {
					throw new GeneratorException("Could not parse text, invalid {param}: "+text+"|"+processedText);
				}
				break;
			case '}':
				if(processedText.charAt(1) == '}') {
					deconstructedMessage.add(new StringPart("}"));
					processedText = processedText.substring(2);
					break;
				}
				throw new GeneratorException("Could not parse text, rogue '}': "+text);
			case '[':
				if(processedText.charAt(1) == '[') {
					deconstructedMessage.add(new StringPart("["));
					processedText = processedText.substring(2);
					break;
				}
				match = basicFunctionPattern.matcher(processedText);
				if(match.matches()) {
					if(match.group(1).matches("^[0-9].*")) {
						throw new GeneratorException("Function parameter cannot start with a number ("+match.group(1)+"): "+text);
					}
					// TODO: add parameters from calling function (and ability to pass parameters)
					deconstructedMessage.add(new FunctionPart(methodGenerator, match.group(1)));
					processedText = processedText.substring(match.end(1)+1); // Add 1 for closing curly brace
				} else {
					throw new GeneratorException("Could not parse text, invalid [function]: "+text+"|"+processedText);
				}
				break;
			case ']':
				if(processedText.charAt(1) == ']') {
					deconstructedMessage.add(new StringPart("]"));
					processedText = processedText.substring(2);
					break;
				}
				throw new GeneratorException("Could not parse text, rogue ']': "+text);
			default:
				match = simpleStringPattern.matcher(processedText);
				if(!match.matches()) {
					throw new GeneratorException("TECHNICAL ERROR: Should have found a match: "+processedText);
				}
				deconstructedMessage.add(new StringPart(match.group(1)));
				processedText = processedText.substring(match.group(1).length());
				break;
			}
		}
		return deconstructedMessage;
	}
	
	default void errorOnInvalidKeys(Map<String, Object> map, String errorMessage, String... validKeys) throws GeneratorException {
		Set<String> validKeysSet   = new HashSet<String>();
		Set<String> invalidKeysSet = new HashSet<String>();
		for(String key: validKeys) {
			validKeysSet.add(key); 
		}
		for(String key: map.keySet()) {
			if(!validKeysSet.contains(key)) invalidKeysSet.add(key);
		}
		if(!invalidKeysSet.isEmpty()) {
			throw GeneratorException.format(errorMessage, String.join(", ", invalidKeysSet));
		}
	}
}
