package uk.bauk.alexa.utils.languages.annotation;

import java.util.List;
import java.util.Map;

import uk.bauk.alexa.utils.languages.annotation.generators.MethodGenerator;
import uk.bauk.alexa.utils.languages.annotation.messageparts.MessagePart;

public class MessageOption implements ParserUtils {
	public final int weight;
	private final String originalMessage;
	private final List<MessagePart> deconstructedMessage;
	public MessageOption(MethodGenerator methodGenerator, String key, Object message) throws GeneratorException {
		this(methodGenerator, key, message, null);
	}
	/*
	 * Pass in the parameters if you want found params to be added to the map, rather than throwing exceptions when not already defined 
	 */
	public MessageOption(MethodGenerator methodGenerator, String key, Object message, Map<String, Class<?>> parameters) throws GeneratorException {
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
		deconstructedMessage = parseText(this.originalMessage, methodGenerator, parameters);
	}
	public String messageCode() {
		return String.format("String.join(\"\", %s)", String.join(", ", deconstructedMessage.stream().map(m -> m.toCodeMessage()).toArray(String[]::new)));
	}
}
