package uk.bauk.alexa.utils.languages.annotation.messageparts;

public class StringPart extends MessagePart {
	private final String string;
	public StringPart(String string) {
		this.string = string;
	}
	@Override
	public String toCodeMessage() {
		return String.format("\"%s\"", string);
	}
}
