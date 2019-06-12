package uk.bauk.alexa.utils.languages.annotation;

public class GeneratorException extends Exception {

	public GeneratorException(String string) {
		super(string);
	}
	
	public static GeneratorException format(String text, Object... args) {
		return new GeneratorException(String.format(text, args));
	}
}
