package nodes;

import java.util.ArrayList;


import visitors.Visitor;

public class WriteOP extends Statement {
	private final ArrayList<Expression> exprList;

	public WriteOP(ArrayList<Expression> exprList) {
		this.exprList = exprList;
	}

	public ArrayList<Expression> getExprList() {
		return exprList;
	}

	@Override
	public <T> T accept(Visitor<T> syntaxVisitor) {
		return syntaxVisitor.visit(this);
	}
}
