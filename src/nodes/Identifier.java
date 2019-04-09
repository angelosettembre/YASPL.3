package nodes;

import visitors.Visitor;

public class Identifier extends Expression {
	private final String name;

	public Identifier(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	@Override
	public <T> T accept(Visitor<T> syntaxVisitor, T param) {
		return syntaxVisitor.visit(this, param);
	}

	@Override
	public String toString() {
		return "" + name + "";
	}
}
