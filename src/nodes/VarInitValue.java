package nodes;

import visitors.Visitor;

public class VarInitValue extends Decl{
	private final AssignOP assign;

	public VarInitValue(AssignOP assign) {
		this.assign = assign;
	}

	public AssignOP getAssign() {
		return assign;
	}

	@Override
	public <T> T accept(Visitor<T> syntaxVisitor) {
		return syntaxVisitor.visit(this);
	}
}
