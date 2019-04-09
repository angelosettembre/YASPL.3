package nodes;

import visitors.Visitor;

public class Type extends Decl{
	private final String varType;

	public Type(String varType) {
		this.varType = varType;
	}

	public String getVarType() {
		return varType;
	}

	@Override
	public <T> T accept(Visitor<T> syntaxVisitor) {
		return syntaxVisitor.visit(this);
	}
}
