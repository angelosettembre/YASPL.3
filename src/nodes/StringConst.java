package nodes;


import visitors.Visitor;

public class StringConst extends Expression {
	private final String stringValue;

	public StringConst(String stringValue) {
		this.stringValue = stringValue;
	}

	public String getStringValue() {
		return stringValue;
	}

	@Override
	public <T> T accept(Visitor<T> syntaxVisitor, T param) {
		return syntaxVisitor.visit(this, param);
	}
}
