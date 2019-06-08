package uk.bauk.alexa.utils.languages.annotation;

import javax.lang.model.element.Modifier;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringJoiner;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import uk.bauk.alexa.utils.languages.annotation.generators.LocaleClassGenerator;
import uk.bauk.alexa.utils.languages.annotation.generators.FactoryGenerator;
import uk.bauk.alexa.utils.languages.annotation.generators.InterfaceGenerator;
import uk.bauk.alexa.utils.languages.annotation.generators.MethodGenerator;

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class LanguageFactoryProcessor extends AbstractProcessor {
	//TODO: Use inheritance instead to stop common ones needing to be duplicated
	//TODO: Allow there to be a en class that allows any undefined en-XX to use it (couple this with inheritance)

	private Filer filer;
	private Messager messager;
	private Elements elements;

	@Override
	public Set<String> getSupportedAnnotationTypes() {
		Set<String> supportedAnnotations = new HashSet<>();
		supportedAnnotations.add(LanguageFactory.class.getCanonicalName());
		return supportedAnnotations;
	}

	@Override
	public synchronized void init(ProcessingEnvironment processingEnvironment) {
		super.init(processingEnvironment);
		filer = processingEnvironment.getFiler();
		messager = processingEnvironment.getMessager();
		elements = processingEnvironment.getElementUtils();
	}

	@Override
	public boolean process(Set<? extends TypeElement> arg0, RoundEnvironment roundEnvironment) {
		// TODO: do we need to check if annotation has already been processed?...
		try {
			/**
			 * 1- Find all annotated element
			 */
			for (Element element : roundEnvironment.getElementsAnnotatedWith(LanguageFactory.class)) {
				if (element.getKind() != ElementKind.CLASS) {
					error(element,
							"Can only be applied to Class, not: %s", element.getKind());
					return true;
				}
				TypeElement typeElement = (TypeElement) element;
				generateLanguageSet(new ResourceParser(elements, typeElement, filer));
			}
		} catch (IOException e) {
			e.printStackTrace();
			error(null, "Failed to generate language classes");
		} catch (GeneratorException e) {
			e.printStackTrace();
			error(null, "Failed to generate language classes: "+e.getMessage());
		}

		return true;
	}
	
	private void generateLanguageSet(ResourceParser resourceParser) throws IOException, GeneratorException {
		Set<String> languageKeys = null;
		for(Locale locale: resourceParser.locales) {
			Map<String, MethodGenerator> items = resourceParser.getLocaleMap(locale);
			if(languageKeys == null) {
				languageKeys = new HashSet<String>();
				languageKeys.addAll(items.keySet());
				new InterfaceGenerator(resourceParser, items).writeToFile(filer);
				note(resourceParser.typeElement, "Using default locale :%s", locale);
			}else {
				for(String key: items.keySet()) {
					if(!languageKeys.contains(key)) {
						error(resourceParser.typeElement, "Extra key (%s) found in locale (%s)", key, locale.toString());
					}
				}
				for(String key: languageKeys) {
					if(!items.containsKey(key)) {
						error(resourceParser.typeElement, "Key (%s) not found in locale (%s)", key, locale.toString());
					}
				}
				Set<String> localeKeys = items.keySet(); 
				languageKeys.addAll(localeKeys);
			}
			new LocaleClassGenerator(resourceParser, items, locale).writeToFile(filer);
		}
		new FactoryGenerator(resourceParser).writeToFile(filer);
	}

	// Utils
	private void error(Element e, String msg, Object... args) {
		messager.printMessage(Diagnostic.Kind.ERROR, String.format(msg, args), e);
	}
	private void warn(Element e, String msg, Object... args) {
		messager.printMessage(Diagnostic.Kind.WARNING, String.format(msg, args), e);
	}
	private void note(Element e, String msg, Object... args) {
		messager.printMessage(Diagnostic.Kind.NOTE, String.format(msg, args), e);
	}
	

}
