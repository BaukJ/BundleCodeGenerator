package uk.bauk.alexa.utils.languages.annotation.generators;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import javax.lang.model.element.Modifier;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import uk.bauk.alexa.utils.languages.annotation.ResourceParser;

public class InterfaceGenerator extends BaseClassGenerator {
	private final Map<String, MethodGenerator> items;
	public InterfaceGenerator(ResourceParser resourceParser, Map<String, MethodGenerator> items) {
		super(resourceParser.packageName, resourceParser.interfaceName());
		this.items = items;
	}
	@Override
	protected TypeSpec.Builder initBuilder() {
		return TypeSpec.interfaceBuilder(className)
				.addModifiers(Modifier.PUBLIC);
	}
	@Override
	protected void addMethods() {
		items.entrySet();
		for(Entry<String, MethodGenerator> entry: items.entrySet()) {
			classBuilder.addMethod(entry.getValue().getRetrievalMethod(true));
		}
	}
}
