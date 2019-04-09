package nodes;

import visitors.Visitor;

public class DoubleConst extends Expression {
	private final double doubleValue;

	public DoubleConst(double doubleValue) {
		this.doubleValue = doubleValue;
	}

	public double getDoubleValue() {
		return doubleValue;
	}

	@Override
	public <T> T accept(Visitor<T> syntaxVisitor, T param) {
		return syntaxVisitor.visit(this, param);
	}
}
