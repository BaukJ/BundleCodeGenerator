package uk.bauk.alexa.utils.languages.annotation.generators;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.lang.model.element.Modifier;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import uk.bauk.alexa.utils.languages.annotation.ResourceParser;

public class FactoryGenerator extends BaseClassGenerator {
	private final ResourceParser resourceParser;
	public FactoryGenerator(ResourceParser resourceParser) {
		super(resourceParser.packageName, resourceParser.factoryName());
		this.resourceParser = resourceParser;
	}
	@Override
	protected void addClassFields() {
		super.addClassFields();
		classBuilder.addField(FieldSpec.builder(ParameterizedTypeName.get(ClassName.get(Map.class), ClassName.get(Locale.class), resourceParser.interfaceClassName()), "localeMap")
				.addModifiers(Modifier.PRIVATE, Modifier.STATIC)
				.initializer("new $T()", ParameterizedTypeName.get(ClassName.get(HashMap.class), TypeName.get(Locale.class), resourceParser.interfaceClassName()))
				.build());
	}
	@Override
	protected void addMethods() {
		MethodSpec.Builder method = MethodSpec.methodBuilder("getLocaleSpecific"+resourceParser.capitalisedPrefix())
				.addParameter(Locale.class, "locale")
				.addModifiers(Modifier.PUBLIC, Modifier.STATIC)
				.returns(resourceParser.interfaceClassName())
				.addStatement("if(localeMap.containsKey(locale)) return localeMap.get(locale)")
				.addStatement("$T ret;", resourceParser.interfaceClassName());
		for(int i = 0; i < resourceParser.locales.length; i++) {
			Locale locale = resourceParser.locales[i];
			if(i == 0) {
				method.beginControlFlow("if(locale == Locale.forLanguageTag($S))", locale.toLanguageTag());				
			} else {
				method.nextControlFlow("else if(locale == Locale.forLanguageTag($S))", locale.toLanguageTag());
			}
			method.addStatement("ret = new $T()", resourceParser.localeClassName(locale));
		}
		method.nextControlFlow("else")
			.addStatement("throw new $T(\"This locale has not been catered for: \"+locale)", InvalidParameterException.class)
			.endControlFlow()
			.addStatement("localeMap.put(locale, ret)")
			.addStatement("return ret");
		classBuilder.addMethod(method.build());
	}
}
