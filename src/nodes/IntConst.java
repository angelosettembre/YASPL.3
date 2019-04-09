package nodes;


import visitors.Visitor;

public class IntConst extends Expression {
	private final int intValue;

	public IntConst(int intValue) {
		this.intValue = intValue;
	}

	public int getIntValue() {
		return intValue;
	}

	@Override
	public <T> T accept(Visitor<T> syntaxVisitor, T param) {
		return syntaxVisitor.visit(this, param);
	}
}
