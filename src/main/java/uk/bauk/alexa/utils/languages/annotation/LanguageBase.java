package uk.bauk.alexa.utils.languages.annotation;

import java.util.Random;

public class LanguageBase {
	private Random rand = new Random();
	
	/*
	 * E.g. calling with 3 return 0-2
	 */
	protected int getRandIndexFromSize(int arraySize) {
		return rand.nextInt(arraySize);
	}
}
