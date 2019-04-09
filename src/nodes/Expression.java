package nodes;

import visitors.Visitor;

public abstract class Expression {

	public abstract <T> T accept(Visitor<T> syntaxVisitor, T param);
}
