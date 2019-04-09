package nodes;


import visitors.Visitor;

public class IfThenElseOP extends Statement{
	private final Expression expressionList;
	private final CompStatement compStmt;
	private final CompStatement compStmtElse;
	
	public IfThenElseOP(Expression expressionList, CompStatement compStmt, CompStatement compStmtElse) {
		this.expressionList = expressionList;
		this.compStmt = compStmt;
		this.compStmtElse = compStmtElse;
	}

	public Expression getExpressionList() {
		return expressionList;
	}

	public CompStatement getCompStmt() {
		return compStmt;
	}

	public CompStatement getCompStmtElse() {
		return compStmtElse;
	}

	@Override
	public <T> T accept(Visitor<T> syntaxVisitor) {
		return syntaxVisitor.visit(this);
	}

}
