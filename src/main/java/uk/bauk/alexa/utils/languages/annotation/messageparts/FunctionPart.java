package uk.bauk.alexa.utils.languages.annotation.messageparts;

import uk.bauk.alexa.utils.languages.annotation.generators.MethodGenerator;

public class FunctionPart extends MessagePart {
	private final String functionName;

	public FunctionPart(MethodGenerator methodGenerator, String messageKey) {
		String func = methodGenerator.methodName(messageKey);
		this.functionName = func.endsWith(")") ? func : func+"()";
	}
	@Override
	public String toCodeMessage() {
		return functionName;
	}
	
}
