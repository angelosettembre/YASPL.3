package nodes;


import visitors.Visitor;

public class RelOP extends Expression {
	private final Expression expr1;
	private final Expression expr2;
	private final String operation;
	
	public RelOP(Expression expr1, Expression expr2, String operation) {
		this.expr1 = expr1;
		this.expr2 = expr2;
		this.operation = operation;
	}
	
	public Expression getExpr1() {
		return expr1;
	}
	
	public Expression getExpr2() {
		return expr2;
	}
	
	public String getOperation() {
		return operation;
	}

	@Override
	public <T> T accept(Visitor<T> syntaxVisitor, T param) {
		return syntaxVisitor.visit(this, param);
	}
}
