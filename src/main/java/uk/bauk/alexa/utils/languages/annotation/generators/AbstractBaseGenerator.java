package uk.bauk.alexa.utils.languages.annotation.generators;

import java.util.Random;

import javax.lang.model.element.Modifier;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;

import uk.bauk.alexa.utils.languages.annotation.ResourceParser;

public class AbstractBaseGenerator extends BaseClassGenerator {
	public static final String METHOD_RANDOM_INDEX_GENERATOR = "getRandIndexFromSize";
	public static final String FIELD_RAND = "rand";
	
	public AbstractBaseGenerator(ResourceParser resourceParser) {
		super(resourceParser.packageName, resourceParser.baseName());
	}
	@Override
	protected void addClassModifiers() {
		super.addClassModifiers();
		classBuilder.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);
	}
	@Override
	protected void addClassFields() {
		super.addClassFields();
		classBuilder.addField(FieldSpec.builder(Random.class, FIELD_RAND, Modifier.FINAL, Modifier.PRIVATE)
				.initializer("new $T()", Random.class)
				.build());
	}
	@Override
	protected void addMethods() {
		classBuilder.addMethod(MethodSpec.methodBuilder(METHOD_RANDOM_INDEX_GENERATOR)
				.addModifiers(Modifier.PROTECTED)
				.addParameter(int.class, "arraySize")
				.returns(int.class)
				.addStatement("return rand.nextInt(arraySize)")
				.build());
	}
}
