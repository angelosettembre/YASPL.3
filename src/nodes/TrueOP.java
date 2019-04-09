package nodes;


import visitors.Visitor;

public class TrueOP extends Expression {
	
	public TrueOP() {
        super();
    }
	
	public boolean getValue() {
        return true;
    }

	@Override
	public <T> T accept(Visitor<T> syntaxVisitor, T param) {
		return syntaxVisitor.visit(this, param);
	}
}
