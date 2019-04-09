package nodes;


import visitors.Visitor;

public class FalseOP extends Expression {
	
	public FalseOP() {
        super();
    }
	
	public boolean getValue() {
        return false;
    }

	@Override
	public <T> T accept(Visitor<T> syntaxVisitor, T param) {
		return syntaxVisitor.visit(this, param);
	}
}
