package nodes;


import visitors.Visitor;

public class AssignOP extends Statement{
	private Identifier identifier;
	private Expression expr;
	
	public AssignOP(Expression expr) {
		this.expr = expr;
	}

	public AssignOP(Identifier identifier, Expression expr) {
		this.identifier = identifier;
		this.expr = expr;
	}

	public Expression getExpr() {
		return expr;
	}

	public Identifier getIdentifier() {
		return identifier;
	}

	@Override
	public <T> T accept(Visitor<T> syntaxVisitor) {
		return syntaxVisitor.visit(this);
	}
}
