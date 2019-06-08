package uk.bauk.alexa.utils.languages.annotation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.bauk.alexa.utils.languages.annotation.generators.MethodGenerator;
import uk.bauk.alexa.utils.languages.annotation.messageparts.FunctionPart;
import uk.bauk.alexa.utils.languages.annotation.messageparts.MessagePart;
import uk.bauk.alexa.utils.languages.annotation.messageparts.ParamPart;
import uk.bauk.alexa.utils.languages.annotation.messageparts.StringPart;

public class MessageOption {
	public final int weight;
	private final String originalMessage;
	private final List<MessagePart> deconstructedMessage = new ArrayList<>();
	public final Map<String, Class<?>> parameters = new HashMap<String, Class<?>>();
	private final MethodGenerator methodGenerator;
	public MessageOption(MethodGenerator methodGenerator, String key, Object message) throws GeneratorException {
		this.methodGenerator = methodGenerator;
		if(message instanceof String) {
			this.originalMessage = (String) message;
			weight = 1;
		} else if(message instanceof Map) {
			Map<?, ?> map = (Map<?, ?>) message; 
			if(!map.containsKey("value")) throw new GeneratorException(String.format("Every item in the key '%s' needs a value", key));
			if(!(map.get("value") instanceof String)) throw new GeneratorException(String.format("Every value in the key '%s' needs to be a string", key));
			this.originalMessage = (String) map.get("value");
			Object weight = map.get("weight");
			if(weight != null) {
				if(weight instanceof Integer) {
					this.weight = (int) weight;
				} else {
					throw new GeneratorException(String.format("Weight for key '%s' needs to be an integer, found %s(%s)", key, weight.getClass().getSimpleName(), weight.toString()));
				}
			} else {
				this.weight = 1;
			}
		} else {
			throw new GeneratorException(String.format("Only support lists of Strings or maps (key: %s), found: %s(%s)", key, message.getClass().getSimpleName(), message.toString()));
		}
		parseText(this.originalMessage);
	}
	public String messageCode() {
		return String.format("String.join(\"\", %s)", String.join(", ", deconstructedMessage.stream().map(m -> m.toCodeMessage()).toArray(String[]::new)));
	}

	private Pattern basicVartiablePattern = Pattern.compile("^\\{(\\w*)\\}.*");
	private Pattern basicFunctionPattern = Pattern.compile("^\\[(\\w*)\\].*");
	private Pattern simpleStringPattern   = Pattern.compile("^([^\\{\\}\\[\\]]*).*");
	private void parseText(String text) throws GeneratorException {
		String processedText = text;
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
					parameters.putIfAbsent(match.group(1), String.class);
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
	}
}
