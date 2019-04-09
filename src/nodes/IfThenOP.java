package nodes;


import visitors.Visitor;

public class IfThenOP extends Statement {
	private final Expression expressionList;
	private final CompStatement compStmt;
	
	public IfThenOP(Expression expressionList, CompStatement compStmt) {
		this.expressionList = expressionList;
		this.compStmt = compStmt;
	}

	public Expression getExpressionList() {
		return expressionList;
	}

	public CompStatement getCompStmt() {
		return compStmt;
	}

	@Override
	public <T> T accept(Visitor<T> syntaxVisitor) {
		return syntaxVisitor.visit(this);
	}
}
