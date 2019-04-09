package nodes;

import java.util.ArrayList;


import visitors.Visitor;

public class CallOP extends Statement {
	private Identifier identifier;
	private ArrayList<Expression> exprList;
	
	public CallOP(Identifier identifier, ArrayList<Expression> exprList) {
		this.identifier = identifier;
		this.exprList = exprList;
	}
	
	public CallOP(Identifier identifier) {
		this.identifier = identifier;
	}

	public Identifier getIdentifier() {
		return identifier;
	}
	
	public ArrayList<Expression> getExprList() {
		return exprList;
	}

	@Override
	public <T> T accept(Visitor<T> syntaxVisitor) {
		return syntaxVisitor.visit(this);
	}
}
