package uk.bauk.alexa.utils.languages.annotation.generators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.lang.model.element.Modifier;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;

import uk.bauk.alexa.utils.languages.annotation.GeneratorException;
import uk.bauk.alexa.utils.languages.annotation.MessageOption;
import uk.bauk.alexa.utils.languages.annotation.ParserUtils;
import uk.bauk.alexa.utils.languages.annotation.ResourceParser;

public class MethodGenerator implements ParserUtils {
	public final String key;
	private final ResourceParser resourceParser;
	private final Map<String, Class<?>> parameters = new HashMap<String, Class<?>>();
	private final List<MessageOption> messages = new ArrayList<MessageOption>();
	
	private static final String KEY_VALUES = "values";
	private static final String KEY_PARAMETERS = "parameters";
	
	public MethodGenerator(ResourceParser languageFactoryClass, String key, Object langObject) throws GeneratorException {
		this.resourceParser = languageFactoryClass;
		this.key = key;
		if(langObject instanceof String) {
			addMessageOptions(langObject);
		} else if (langObject instanceof List) {
			addMessageOptions(langObject);
		} else if (langObject instanceof Map) {
			if(!((Map) langObject).containsKey(KEY_VALUES)) {
				throw new GeneratorException(String.format("Every map object needs a list of "+KEY_VALUES+". Key: %s", key));
			}
			errorOnInvalidKeys((Map)langObject, "Invalid keys found in "+key+" map: %s", KEY_VALUES, KEY_PARAMETERS);
			if(((Map) langObject).containsKey(KEY_PARAMETERS)) {
				addParameters(((Map) langObject).get(KEY_PARAMETERS));
			}
			addMessageOptions(((Map) langObject).get(KEY_VALUES));
		} else {
			throw new GeneratorException(String.format(
					"Can only handle String, Array or Map types. Got: '%s' for key '%s'.",
					langObject.getClass().getSimpleName(),
					key));
		}
	}
	private void addMessageOptions(Object message) throws GeneratorException {
		if(message instanceof String) {
			messages.add(new MessageOption(this, key, message.toString(), parameters));
		} else if (message instanceof List) {
			List<?> list = (List<?>) message;
			if(list.isEmpty()) {
				throw new GeneratorException(String.format("List of values is empty for key '%s'", key));
			}
			for(Object value: list) {
				messages.add(new MessageOption(this, key, value));
			}
		} else {
			throw GeneratorException.format("Invalud message for key %s. Found %s but should be a String or List.", key, message.getClass().getSimpleName());
		}
	}
	private void addParameters(Object params) throws GeneratorException {
		if(params instanceof List) {
			for(Object param: (List)params) {
				if(param instanceof String) {
					parameters.put(param.toString(), String.class);
				} else {
					// TODO: Could add ability to add things other than String params
					throw GeneratorException.format("Parameter list needs to be a list of strings (param names), found: %s. For key: %s", param.getClass().getSimpleName(), key);
				}
			}
		} else {
			throw GeneratorException.format("Parameter list needs to be an array, found: %s. For key: %s", params.getClass().getSimpleName(), key);
		}
	}
	public boolean paramExists(String param) {
		return parameters.containsKey(param);
	}
	public String methodName() {
		return methodName(key);
	}
	public String methodName(String key) {
		return resourceParser.methodPrefix+capitalise(key);
	}
	public MethodSpec getRetrievalMethod(boolean forInterface) {
		MethodSpec.Builder method = MethodSpec.methodBuilder(methodName())
				.returns(String.class);
		for(Entry<String, Class<?>> param: parameters.entrySet()) {
			method.addParameter(param.getValue(), param.getKey());
		}
		if(forInterface) {
			method.addModifiers(Modifier.ABSTRACT, Modifier.PUBLIC);
		} else {
			method.addModifiers(Modifier.PUBLIC)
				.addCode(generateRetrievalCode());
		}
		return method.build();
	}
	private CodeBlock generateRetrievalCode() {
		CodeBlock.Builder builder = CodeBlock.builder();
		if(messages.size() == 1) {
			builder.addStatement("String ret = $L", messages.get(0).messageCode());
		} else {
			int totalWeight = messages.stream().mapToInt(m -> m.weight).sum();
			int currentWeight = 0;
			builder.addStatement("String ret")
				.addStatement("int weightIndex = getRandIndexFromSize($L)", totalWeight);
			for(int i = 0; i < messages.size(); i++) {
				MessageOption message = messages.get(i);
				currentWeight += message.weight;
				if(i == 0) {
					builder.beginControlFlow("if(weightIndex < $L)", currentWeight);
				} else if(i + 1 == messages.size()) {
					builder.nextControlFlow("else")
						.add("// else if(weightIndex < $L)\n", currentWeight);
				} else {
					builder.nextControlFlow("else if(weightIndex < $L)", currentWeight);
				}
				builder.add("ret = $L; // weight: $L\n", message.messageCode(), message.weight);
			}
			builder.endControlFlow();
		}
		for(Entry<String, Class<?>> param: parameters.entrySet()) {
			builder.addStatement("ret = ret.replaceAll(\"\\\\{$L\\\\}\", $L)", param.getKey(), param.getKey());
		}
		builder.addStatement("return ret");
		return builder.build();
	}
	

	public static String capitalise(String word) {
		return word.substring(0, 1).toUpperCase() + word.substring(1);
	}
}
