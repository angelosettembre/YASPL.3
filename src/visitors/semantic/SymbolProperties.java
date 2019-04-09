package visitors.semantic;

import java.util.ArrayList;

import nodes.ParDef;

public class SymbolProperties {
	private String symbolName;
	private String symbolKind;
	private String symbolType;
	private ArrayList<ParDef> listParameterDef;
	
	public SymbolProperties() {
		super();
		listParameterDef = new ArrayList<ParDef>();
	}

	public SymbolProperties(String symbolName) {
		super();
		this.symbolName = symbolName;
	}

	public SymbolProperties(String symbolName, String symbolKind, String symbolType) {
		super();
		this.symbolName = symbolName;
		this.symbolKind = symbolKind;
		this.symbolType = symbolType;
	}

	public String getSymbolName() {
		return symbolName;
	}

	public void setSymbolName(String symbolName) {
		this.symbolName = symbolName;
	}

	public String getSymbolKind() {
		return symbolKind;
	}

	public void setSymbolKind(String symbolKind) {
		this.symbolKind = symbolKind;
	}

	public String getSymbolType() {
		return symbolType;
	}

	public void setSymbolType(String symbolType) {
		this.symbolType = symbolType;
	}

	public ArrayList<ParDef> getListParameterDef() {
		return listParameterDef;
	}

	public void setListParameterDef(ArrayList<ParDef> listParameterDef) {
		this.listParameterDef = listParameterDef;
	}

	@Override
	public String toString() {
		return "SymbolProperties [symbolName=" + symbolName + ", symbolKind=" + symbolKind + ", symbolType="
				+ symbolType + ", listParameterDef=" + listParameterDef + "]";
	}
}
