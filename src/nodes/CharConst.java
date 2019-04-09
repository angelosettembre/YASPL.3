package nodes;

import visitors.Visitor;

public class CharConst extends Expression {
	private final char charValue;

	public CharConst(char charValue) {
		this.charValue = charValue;
	}

	public char getCharValue() {
		return charValue;
	}

	@Override
	public <T> T accept(Visitor<T> syntaxVisitor, T param) {
		return syntaxVisitor.visit(this, param);
	}
}
