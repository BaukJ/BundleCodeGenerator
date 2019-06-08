package uk.bauk.alexa.utils.languages.annotation.generators;

import java.io.IOException;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

public abstract class BaseClassGenerator {
	protected final String packageName;
	protected final String className;
	protected final TypeSpec.Builder classBuilder;
	private boolean generated = false;
	public BaseClassGenerator(String packageName, String className) {
		this.packageName = packageName;
		this.className = className;
		this.classBuilder = initBuilder();
	}
	private void generate() {
		if(generated) return;
		addClassModifiers();
		addClassFields();
		addMethods();
		generated = true;
	}
	protected TypeSpec.Builder initBuilder() {
		return TypeSpec.classBuilder(className);
	}
	protected void addClassModifiers() {
		classBuilder.addModifiers(Modifier.PUBLIC);
	}

	protected void addClassFields() {}
	protected abstract void addMethods();
	public void writeToFile(Filer filer) throws IOException {
		generate();
		JavaFile.builder(packageName, classBuilder.build()).build().writeTo(filer);
	}
}
