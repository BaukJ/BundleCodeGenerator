/**
 * 
 */
package uk.bauk.alexa.utils.languages.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Locale;

@Retention(SOURCE)
@Target(TYPE)
/**
 * @author Bauk
 *
 */
public @interface LanguageFactory {
	/*
	 * The resource prefix used to get data files and generate class names
	 */
	String prefix();
	/*
	 * The list of locales that will be generated
	 */
	String[] locales();
	/*
	 * package name to create language as, e.g. uk.bauk.languages
	 */
	String packageName() default "";
	/*
	 * Base class all language classes extend.
	 * Useful for overwriting common methods such as rand.
	 */
	Class<? extends LanguageBase> languageBase() default LanguageBase.class;
	/*
	 * Prefix given to text retrieval methods (defaults to get, so sets welcome method to getWelcome())
	 */
	String methodPrefix() default "get";
	/*
	 * Internally used to get round resources not loaded problem
	 */
	boolean dummy() default false;
}
