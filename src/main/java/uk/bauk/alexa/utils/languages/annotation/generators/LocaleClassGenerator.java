package uk.bauk.alexa.utils.languages.annotation.generators;

import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import javax.lang.model.element.Modifier;

import com.squareup.javapoet.TypeSpec;

import uk.bauk.alexa.utils.languages.annotation.LanguageBase;
import uk.bauk.alexa.utils.languages.annotation.ResourceParser;

public class LocaleClassGenerator extends BaseClassGenerator {
	private final Map<String, MethodGenerator> items;
	private final ResourceParser resourceParser;
	public LocaleClassGenerator(ResourceParser resourceParser, Map<String, MethodGenerator> items, Locale locale) {
		super(resourceParser.packageName, resourceParser.localeName(locale));
		this.items = items;
		this.resourceParser = resourceParser;
	}
	@Override
	protected void addClassModifiers() {
		super.addClassModifiers();
		classBuilder.addSuperinterface(resourceParser.interfaceClassName())
			.superclass(LanguageBase.class);
	}
	@Override
	protected void addMethods() {
		for(Entry<String, MethodGenerator> entry: items.entrySet()) {
			classBuilder.addMethod(entry.getValue().getRetrievalMethod(false));
		}
	}
}
