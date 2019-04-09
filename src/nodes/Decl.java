package nodes;

import visitors.*;

public abstract class Decl {

	public abstract <T> T accept(Visitor<T> syntaxVisitor);	
}
