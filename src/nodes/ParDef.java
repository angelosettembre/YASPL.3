package nodes;

public class ParDef {
	private String symbolName;
	private String typeSymbol;
	private String typeParam;
	
	public ParDef(String symbolName, String typeSymbol, String typeParam) {
		super();
		this.symbolName = symbolName;
		this.typeSymbol = typeSymbol;
		this.typeParam = typeParam;
	}

	public String getSymbolName() {
		return symbolName;
	}

	public void setSymbolName(String symbolName) {
		this.symbolName = symbolName;
	}

	public String getTypeSymbol() {
		return typeSymbol;
	}

	public void setTypeSymbol(String typeSymbol) {
		this.typeSymbol = typeSymbol;
	}

	public String getTypeParam() {
		return typeParam;
	}

	public void setTypeParam(String typeParam) {
		this.typeParam = typeParam;
	}

	@Override
	public String toString() {
		return "ParDef [symbolName=" + symbolName + ", typeSymbol=" + typeSymbol + ", typeParam=" + typeParam + "]";
	}
}
