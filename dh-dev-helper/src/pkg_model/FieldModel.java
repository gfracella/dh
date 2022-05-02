package pkg_model;

public class FieldModel {

	private String Name;
	private String Description;
	private Boolean IsKey;
	private Boolean IsAnalitycs;
	private String Type;
	private int Length;
	private int Scale;
	private int Nro;
	private String NameS2A;

	public FieldModel() {
		Scale = 0;
	}

	public String getName() {
		String val = Name.trim();
		return val;
	}

	public void setName(String name) {
		Name = Util.myTrim(name);
	}

	public String getNameS2A() {
		String val = NameS2A.trim();
		if (val.length() == 0 || val == null)
			val = Name.trim();
		return val;
	}

	public void setNameS2A(String name) {
		NameS2A = Util.myTrim(name);;
	}

	public String getDescription() {
		String val = Description.replace("'", "''").trim();
		return val;
	}

	public void setDescription(String description) {	
		Description =  Util.myTrim(description);
	}

	public Boolean getIsKey() {
		return IsKey;
	}

	public void setIsKey(Boolean isKey) {
		IsKey = isKey;
	}

	public Boolean getIsAnalitycs() {
		return IsAnalitycs;
	}

	public void setIsAnalitycs(Boolean isAnalitycs) {
		IsAnalitycs = isAnalitycs;
	}

	public String getType() {
		String val = Type.trim();
		return val;
	}

	public void setType(String type) {
		Type = Util.myTrim(type);
	}

	public int getLength() {
		return Length;
	}

	public void setLength(int length) {
		Length = length;
	}

	public int getScale() {
		return Scale;
	}

	public void setScale(int scale) {
		Scale = scale;
	}

	public int getNro() {
		return Nro;
	}

	public void setNro(int nro) {
		Nro = nro;
	}

	public String NameQuoted() {
		return "\"" + getName() + "\"";
	}

	private String OracleType() throws Exception {
		String bType = getType(); 
		if (bType.equals("S") || getType().equals("P"))
			return "NUMBER";
		if (bType.equals("L"))
			return "DATE";
		if (bType.equals("Z"))
			return "TIMESTAMP";
		if (bType.equals("A")) {
			if (getLength() < 15)
				return "VARCHAR2(" + getLength() + ")";
			else
				return "VARCHAR2(" + getLength() + " CHAR)";
		} else {

			throw new Exception("Tipo " +  bType + " non ammesso !!!");
		}
	}

	public String OraFieldDefinition() throws Exception {
		String ret = String.format("%-15s %-20s", NameQuoted(), OracleType());
		if (getIsKey())
			ret = String.format("%-35s NOT NULL", ret);
		return ret;
	}

	public String OraFieldDefinitionNullable() throws Exception {
		String ret;
		ret = String.format("%-15s %-20s", NameQuoted(), OracleType());
		return ret;
	}

}
