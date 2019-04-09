package nodes;

import visitors.Visitor;

public abstract class Statement {

	public abstract <T> T accept(Visitor<T> syntaxVisitor);
}
