package nodes;


import visitors.Visitor;

public class NotOP extends Expression {
	private final Expression expr;
	
	public NotOP(Expression expr) {
		this.expr = expr;
	}

	public Expression getExpr() {
		return expr;
	}
	
	@Override
	public <T> T accept(Visitor<T> syntaxVisitor, T param) {
		return syntaxVisitor.visit(this, param);
	}
}
