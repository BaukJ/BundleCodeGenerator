package uk.bauk.alexa.utils.languages.annotation.messageparts;

public class ParamPart extends MessagePart {
	private final String paramName;

	public ParamPart(String paramName) {
		this.paramName = paramName;
	}
	@Override
	public String toCodeMessage() {
		return paramName;
	}
	
}
