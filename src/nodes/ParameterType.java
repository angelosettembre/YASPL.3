package nodes;

import visitors.Visitor;

public class ParameterType extends Decl {
	private String typeParFunc;

	public ParameterType(String typeParFunc) {
		this.typeParFunc = typeParFunc;
	}

	public String getTypeParFunc() {
		return typeParFunc;
	}

	public void setTypeParFunc(String typeParFunc) {
		this.typeParFunc = typeParFunc;
	}

	@Override
	public <T> T accept(Visitor<T> syntaxVisitor) {
		return syntaxVisitor.visit(this);
	}
}
