package pkg_model;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import oracle.jdbc.OracleConnection;

public class TableModel {

	private List<FieldModel> _Fields;
	private String _Name;
	private String _Description;
	private String _ViewType;
	private String _ViewName;
	private String _Frequenza;
	private Boolean _IsPartizionamentoMensile;

	public List<FieldModel> getFields() {
		return _Fields;
	}

	public void setFields(List<FieldModel> _Fields) {
		this._Fields = _Fields;
	}

	public String getName() {
		return _Name.trim();
	}

	public String getDTOName() {
		return this.getName().replace('-', '_');
	}

	public void setName(String _Name) {
		this._Name = Util.myTrim(_Name);
		;
	}

	public String getDescription() {
		return _Description.trim();
	}

	public void setDescription(String _Description) {
		this._Description = Util.myTrim(_Description);
		;
	}

	public String getViewType() {
		return _ViewType.trim();
	}

	public void setViewType(String _ViewType) {
		this._ViewType = Util.myTrim(_ViewType);
	}

	public String getViewName() {
		return _ViewName.trim();
	}

	public String getViewName_ALL() {
		return _ViewName.trim() + "_ALL";
	}

	public void setViewName(String _ViewName) {
		this._ViewName = Util.myTrim(_ViewName);
	}

	public String getFrequenza() {
		return _Frequenza.trim();
	}

	public void setFrequenza(String _Frequenza) {
		this._Frequenza = Util.myTrim(_Frequenza);
	}

	public Boolean getIsPartizionamentoMensile() {
		return _IsPartizionamentoMensile;
	}

	public void setIsPartizionamentoMensile(Boolean _IsPartizionamentoMensile) {
		this._IsPartizionamentoMensile = _IsPartizionamentoMensile;
	}

	public String Quote(String aVal) {
		return "\"" + aVal + "\"";
	}

	protected TableModel() {
	}

	public TableModel(Boolean anIsPartizionamentoMensile) {
		_Fields = new ArrayList<FieldModel>();
		setIsPartizionamentoMensile(anIsPartizionamentoMensile);
	}

	public String FullTableName() {
		return Quote("S2A") + "." + TableNameQuoted();
	}

	public String TableNameQuoted() {
		return Quote(getName());
	}

	public String ViewNameQuoted() {
		return Quote(getViewName());
	}

	public String ViewName_ALLQuoted() {
		return Quote(getViewName_ALL());
	}

	public List<FieldModel> Fields_NoABI_NoDTV() throws Exception {
		List<FieldModel> ret = new ArrayList<FieldModel>();
		for (FieldModel f : getFields()) {
			if (f != Field_ABI() && f != Field_DTV())
				ret.add(f);
		}
		return ret;

	}

	public FieldModel Field_ABI() throws Exception {
		for (FieldModel f : getFields()) {
			if (f.getName().endsWith("ABI"))
				return f;
		}
		throw new Exception("Attenzione: la tabella non ha il campo ABI");
	}

	public FieldModel Field_DTV() throws Exception {
		for (FieldModel f : getFields()) {
			if (f.getName().endsWith("DTV"))
				return f;
		}
		throw new Exception("Attenzione: la tabella non ha il campo ABI");
	}

	public String PrimaryKey() throws Exception {

		String s = "";

		if (getIsPartizionamentoMensile())
			s = "MESE,";
		else
			s = "ANNO,";
		s += Field_DTV().getName();

		for (FieldModel f : getFields()) {
			if (f.getIsKey()) {
				if (f.getName().endsWith("DTV")) {
					continue;
				}
				s += "," + f.NameQuoted();
			}
		}

		return s;
	}

	public Boolean isGiornaliera() {
		return getFrequenza().equals("GIORNALIERA");
	}

	public Boolean isMensile() {
		return getFrequenza().equals("MENSILE");
	}

	public Boolean isSemplice() {
		return getViewType().equals("SEMPLICE");
	}

	public String PrimaryKeyName() {
		return getName() + "_PK";
	}

	public TableBDTModel getDBTTable(Boolean isPArtizionamentoMensile) {
		TableBDTModel t = new TableBDTModel(isPArtizionamentoMensile);
		t.setName(this.getName() + "_BDT");

		FieldModel calF = new FieldModel();
		calF.setName("CAL_DATE");
		calF.setIsKey(true);
		calF.setType("L");
		calF.setLength(10);
		calF.setScale(0);
		calF.setNameS2A("DATA_RIFERIMENTO");
		t.getFields().add(calF);

		FieldModel dtvF = null;
		for (FieldModel f : getFields()) {
			if (f.getIsKey()) {
				FieldModel newF = new FieldModel();
				newF.setName(f.getName().trim());
				newF.setNameS2A(f.getNameS2A().trim());
				newF.setIsKey(f.getIsKey());
				newF.setType(f.getType().trim());
				newF.setLength(f.getLength());
				newF.setScale(f.getScale());
				if (!f.getName().endsWith("DTV"))
					t.getFields().add(newF);
				else
					dtvF = newF;
			}
		}
		t.getFields().add(dtvF);
		return t;
	}

	public static List<String> AllTables() {
		List<String> bRes = new ArrayList<String>();
		try {
			OracleConnection conn = OracleConnectionFactory.getConnection();
			Statement stmt = conn.createStatement();
			ResultSet rset = stmt.executeQuery("SELECT NAME FROM \"_DEV_TAB_DEFS\" ORDER BY NAME");

			while (rset.next()) {
				String t = rset.getString(1);
				bRes.add(t);
			}

			rset.close();
			stmt.close();
			conn.close();
			conn = null;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return bRes;
	}

	public static TableModel GetTable(String aTableName, Boolean isPartizionamentoMensile) {

		TableModel t = new TableModel(isPartizionamentoMensile);
		try {
			OracleConnection conn = OracleConnectionFactory.getConnection();
			Statement stmt = conn.createStatement();
			String sql = String.format(
					"SELECT A.NAME, A.DES, A.VIEW_TYPE, A.VIEW_NAME, A.FREQUENZA, B.NAME, B.DES, B.ISKEY, B.IS_SA, B.NAME_S2A, B.TYPE,B.LEN, B.SCALE, B.NRO FROM \"_DEV_TAB_DEFS\" A, \"_DEV_FLD_DEFS\" B WHERE A.NAME = B.TABLE_NAME AND A.NAME = '%s' ORDER BY B.NRO",
					aTableName);

			ResultSet rset = stmt.executeQuery(sql);

			while (rset.next()) {
				t.setName(rset.getString(1).trim().toUpperCase());
				t.setDescription(rset.getString(2).trim().replace('à', 'a'));
				t.setViewType(rset.getString(3).trim().toUpperCase());
				t.setViewName(rset.getString(4).trim().toUpperCase());
				t.setFrequenza(rset.getString(5).trim().toUpperCase());

				FieldModel fld = new FieldModel();
				fld.setName(rset.getString(6).toUpperCase().trim());
				fld.setDescription(rset.getString(7).trim().replace('à', 'a'));

				String tmpString = rset.getString(8);
				if (!rset.wasNull() && tmpString.toUpperCase().trim().equals("K"))
					fld.setIsKey(true);
				else
					fld.setIsKey(false);

				tmpString = rset.getString(9);
				if (!rset.wasNull() && tmpString.toUpperCase().trim().equals("S"))
					fld.setIsAnalitycs(true);
				else
					fld.setIsAnalitycs(false);

				tmpString = rset.getString(10);
				if (rset.wasNull())
					tmpString = "";
				fld.setNameS2A(tmpString.toUpperCase().trim());

				fld.setType(rset.getString(11).toUpperCase().trim());

				int tmpInt = rset.getInt(12);
				if (rset.wasNull())
					tmpInt = 0;
				fld.setLength(tmpInt);

				tmpInt = rset.getInt(13);
				if (rset.wasNull())
					tmpInt = 0;
				fld.setScale(tmpInt);

				tmpInt = rset.getInt(14);
				if (rset.wasNull())
					tmpInt = 0;
				fld.setNro(tmpInt);
				t.getFields().add(fld);

			}

			rset.close();
			stmt.close();
			conn.close();
			conn = null;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return t;
	}

	public static String CreatabellaODS(String aTableName, Boolean isPartizionamentoPerMeseParam, Boolean createALLView) throws Exception {
		Boolean isPartizionamentoPerMESE = isPartizionamentoPerMeseParam;
		String tName = aTableName;
		pkg_model.TableModel t = pkg_model.TableModel.GetTable(tName, isPartizionamentoPerMESE);

		String sql = "";
		sql += Util.newLine + "------------------------------------------------------------------------------------------";
		sql += Util.newLine + "-- " + t.getName() + " - " + t.getDescription() + " (" + t.getFrequenza() + ")";
		sql += Util.newLine + "------------------------------------------------------------------------------------------";

		sql += Util.newLine + String.format("PROMPT '---> Creazione tabella %s [%s]';\n", t.getName(), t.getDescription());
		sql += Util.newLine + String.format("CREATE TABLE %s ", t.getName());
		sql += Util.newLine + "(";

		String bComma = "";
		for (FieldModel f : t.getFields()) {
			if (f.getIsAnalitycs()) {
				sql += String.format(Util.newLine + "   %s%s  ", bComma, f.OraFieldDefinition());
				bComma = ",";
			}
		}
		sql += Util.newLine + "   , DATA_INSERIMENTO TIMESTAMP(6) DEFAULT SYSTIMESTAMP";
		if (isPartizionamentoPerMESE)
			sql += Util.newLine + String.format("   , MESE DATE GENERATED ALWAYS AS (TRUNC(%s,'FMMM')) VIRTUAL", t.Field_DTV().getName());
		else
			sql += Util.newLine + String.format("   , ANNO DATE GENERATED ALWAYS AS (TRUNC(%s,'YYYY')) VIRTUAL", t.Field_DTV().getName());

		sql += Util.newLine + String.format("   , CONSTRAINT %-20s  FOREIGN KEY(%s) REFERENCES \"ACT_ABI\"(\"CODICE_ABI\")", t.getName() + "_ACT_ABI_FK", t.Field_ABI().NameQuoted());
		sql += Util.newLine + ") ";
		sql += Util.newLine + " SEGMENT CREATION DEFERRED ROW STORE COMPRESS ADVANCED";
		if (isPartizionamentoPerMESE)
			sql += String.format(Util.newLine + "PARTITION BY LIST(\"MESE\", \"%s\") AUTOMATIC ", t.Field_ABI().getName());
		else
			sql += String.format(Util.newLine + "PARTITION BY LIST(\"ANNO\", \"%s\") AUTOMATIC ", t.Field_ABI().getName());

		sql += String.format(" (PARTITION \"%s_P1\" VALUES((TO_DATE('01/01/2021', 'DD/MM/YYYY'), '03599')))", t.getName());

		String tableSpaceName = Util.getTableSpaceName(t.getName());

		sql += String.format(" TABLESPACE %s;", tableSpaceName);

		sql += String.format(Util.newLine + "CREATE UNIQUE INDEX S2A.%s ON %s (%s) COMPRESS ADVANCED HIGH LOCAL", t.PrimaryKeyName(), t.getName(), t.PrimaryKey());
		sql += String.format(" TABLESPACE %s;", tableSpaceName);

		sql += String.format(Util.newLine + "ALTER TABLE S2A.%s ADD CONSTRAINT %s PRIMARY KEY (%s) USING INDEX %s  ENABLE;", t.getName(), t.PrimaryKeyName(), t.PrimaryKey(), t.PrimaryKeyName());

		if (t.isGiornaliera()) // Creazione tabella BDT
		{
			TableBDTModel bdt = t.getDBTTable(isPartizionamentoPerMESE);
			sql += Util.newLine + Util.newLine;
			sql += Util.newLine + "-------------------------------";
			sql += Util.newLine + " -- " + bdt.getName();
			sql += Util.newLine + "-------------------------------";

			sql += Util.newLine + String.format("DROP TABLE %s;", bdt.getName());
			sql += Util.newLine + String.format("CREATE TABLE %s", bdt.getName());
			sql += Util.newLine + "(";
			bComma = "";
			for (FieldModel f : bdt.getFields()) {
				sql += String.format(Util.newLine + "    %s%s ", bComma, f.OraFieldDefinitionNullable());
				bComma = ",";
			}
			if (isPartizionamentoPerMESE)
				sql += Util.newLine + String.format("    , MESE DATE GENERATED ALWAYS AS (TRUNC(CAL_DATE,'FMMM')) VIRTUAL ");
			else
				sql += Util.newLine + String.format("    , ANNO DATE GENERATED ALWAYS AS (TRUNC(CAL_DATE,'YYYY')) VIRTUAL ");
			sql += Util.newLine + ")";
			sql += Util.newLine + " SEGMENT CREATION DEFERRED ROW STORE COMPRESS ADVANCED";
			if (isPartizionamentoPerMESE)
				sql += String.format(Util.newLine + "PARTITION BY LIST(\"MESE\", \"%s\") AUTOMATIC ", bdt.Field_ABI().getName());
			else
				sql += String.format(Util.newLine + "PARTITION BY LIST(\"ANNO\", \"%s\") AUTOMATIC ", bdt.Field_ABI().getName());

			sql += String.format("(PARTITION \"%s_P1\" VALUES((TO_DATE('01/01/2021', 'DD/MM/YYYY'), '03599')))", bdt.getName());
			sql += String.format(" TABLESPACE %s;", tableSpaceName);
			sql += String.format(Util.newLine + "CREATE UNIQUE INDEX S2A.%s ON %s (%s) COMPRESS ADVANCED HIGH LOCAL", bdt.PrimaryKeyName(), bdt.getName(), bdt.PrimaryKey());
			sql += String.format(" TABLESPACE %s;", tableSpaceName);
			sql += String.format(Util.newLine + "ALTER TABLE S2A.%s ADD CONSTRAINT %s PRIMARY KEY (%s) USING INDEX %s  ENABLE;", bdt.getName(), bdt.PrimaryKeyName(), bdt.PrimaryKey(), bdt.PrimaryKeyName());

			sql += Util.getTableGrantString(bdt.getName());
		}

		// Commenti sulla tabella
		sql += Util.newLine;
		sql += String.format(Util.newLine + "COMMENT ON TABLE %s IS '%s';", t.getName(), t.getDescription());
		for (FieldModel f : t.getFields())
			if (f.getIsAnalitycs())
				sql += String.format(Util.newLine + "COMMENT ON COLUMN %s.%s  IS '%s'; ", t.TableNameQuoted(), f.NameQuoted(), f.getDescription());

		sql += Util.getTableGrantString(t.getName());

		sql += Util.newLine + Util.newLine + Util.newLine;
		sql += Util.newLine + " ---CREAZIONE VISTE >>>>>>>>>>>>>>>>>>>>>>>>>>>";
		sql += Util.newLine + " ---CREAZIONE VISTE >>>>>>>>>>>>>>>>>>>>>>>>>>>";
		sql += Util.newLine + " ---CREAZIONE VISTE >>>>>>>>>>>>>>>>>>>>>>>>>>>";

		// Viste
		if (t.isGiornaliera()) {
			if (!t.isSemplice()) {
				sql += Util.newLine + "=====================================";
				sql += Util.newLine + "=====================================";
				sql += Util.newLine;
				sql += Util.newLine + " TODO GREG: è una vista complessa: da fare a mano";
				sql += Util.newLine;
				sql += Util.newLine;
				sql += Util.newLine + "=====================================";
				sql += Util.newLine + "=====================================";

			} else {
				TableBDTModel bdt = t.getDBTTable(t._IsPartizionamentoMensile);
				sql += Util.newLine + "------------------------------------------------------------------------------------------";

				if (!createALLView) {
					sql += Util.newLine + " -- " + t.getViewName() + " - " + t.getDescription() + " (Giornaliero)";
					sql += Util.newLine + "------------------------------------------------------------------------------------------";
					sql += Util.newLine + String.format("CREATE OR REPLACE VIEW %s BEQUEATH DEFINER AS ", t.getViewName());
					sql += Util.newLine + "(";
					sql += Util.newLine + " SELECT";
					bComma = "";
					for (FieldModel f : bdt.getFields())
						if (f != bdt.Field_DTV()) {
							sql += String.format(Util.newLine + " %s%s.\"%s\" %s ", bComma, bdt.getName(), f.getName(), f.getNameS2A());
							bComma = ",";
						}
					for (FieldModel f : t.getFields())
						if (!f.getIsKey() && f.getIsAnalitycs())
							sql += String.format(Util.newLine + " ,%s.\"%s\" %s ", t.getName(), f.getName(), f.getNameS2A());
					sql += String.format(Util.newLine + " FROM %s INNER JOIN %s ON ", bdt.getName(), t.getName());
					sql += String.format(Util.newLine + "      %s.%s  IN (SELECT ABI FROM GET_ABI())", bdt.getName(), bdt.Field_ABI().getName());
					for (FieldModel f : bdt.getFields())
						if (f != bdt.Field_CAL_DATE())
							sql += String.format(Util.newLine + " AND %s.%s = %s.%s", bdt.getName(), f.getName(), t.getName(), f.getName());

					sql += Util.newLine + ") WITH CHECK OPTION;" + Util.newLine;
					sql += String.format(Util.newLine + "COMMENT ON TABLE %s IS '%s';", t.ViewNameQuoted(), t.getDescription());

					for (FieldModel f : t.getFields())
						if (f.getIsAnalitycs())
							sql += String.format(Util.newLine + "COMMENT ON COLUMN %s.%s  IS '%s'; ", t.ViewNameQuoted(), f.getNameS2A(), f.getDescription());
					sql += Util.newLine;

					sql += Util.getViewGrantString(t.getViewName(), true);
				} else {
					// Vengono create 2 viste.
					// La prima con suffisso ALL senza segregazione NON visibile alle banche.
					// La seconda senza suffisso ALL che punta alla sua corrispondente con suffisso
					// ALL e visibile alle banche

					// Vista con suffisso _ALL
					sql += Util.newLine + " -- Vista con suffisso _ALL NON visibile alle banche";
					sql += Util.newLine + " -- " + t.getViewName_ALL() + " - " + t.getDescription() + " (Giornaliero)";
					sql += Util.newLine + "------------------------------------------------------------------------------------------";
					sql += Util.newLine + String.format("CREATE OR REPLACE VIEW %s BEQUEATH DEFINER AS ", t.getViewName_ALL());
					sql += Util.newLine + "(";
					sql += Util.newLine + " SELECT";
					bComma = "";
					for (FieldModel f : bdt.getFields())
						if (f != bdt.Field_DTV()) {
							sql += String.format(Util.newLine + " %s%s.\"%s\" %s ", bComma, bdt.getName(), f.getName(), f.getNameS2A());
							bComma = ",";
						}
					for (FieldModel f : t.getFields())
						if (!f.getIsKey() && f.getIsAnalitycs())
							sql += String.format(Util.newLine + " ,%s.\"%s\" %s ", t.getName(), f.getName(), f.getNameS2A());
					sql += String.format(Util.newLine + " FROM %s INNER JOIN %s ON ", bdt.getName(), t.getName());
					// sql += String.format(Util.newLine + " %s.%s IN (SELECT ABI FROM GET_ABI())",
					// bdt.getName(), bdt.Field_ABI().getName());
					String bAND = " ";
					for (FieldModel f : bdt.getFields())
						if (f != bdt.Field_CAL_DATE()) {
							sql += String.format(Util.newLine + " %s %s.%s = %s.%s", bAND, bdt.getName(), f.getName(), t.getName(), f.getName());
							bAND = "AND";
						}

					sql += Util.newLine + ") WITH CHECK OPTION;" + Util.newLine;
					sql += String.format(Util.newLine + "COMMENT ON TABLE %s IS '%s';", t.ViewName_ALLQuoted(), t.getDescription());

					for (FieldModel f : t.getFields())
						if (f.getIsAnalitycs())
							sql += String.format(Util.newLine + "COMMENT ON COLUMN %s.%s  IS '%s'; ", t.ViewName_ALLQuoted(), f.getNameS2A(), f.getDescription());
					sql += Util.newLine;

					sql += Util.getViewGrantString(t.getViewName_ALL(), false);

					// Vista senza suffisso _ALL
					sql += Util.newLine + " --Vista senza suffisso _ALL visibile alle banche";
					sql += Util.newLine + " -- " + t.getViewName() + " - " + t.getDescription() + " (Giornaliero)";
					sql += Util.newLine + "------------------------------------------------------------------------------------------";
					sql += Util.newLine + String.format("CREATE OR REPLACE VIEW %s BEQUEATH DEFINER AS ", t.getViewName());
					sql += Util.newLine + "(";
					sql += Util.newLine + String.format(" SELECT * FROM %s WHERE ABI_BANCA IN (SELECT ABI FROM GET_ABI()) ", t.getViewName_ALL());
					sql += Util.newLine + ") WITH CHECK OPTION;" + Util.newLine;
					sql += String.format(Util.newLine + "COMMENT ON TABLE %s IS '%s';", t.ViewNameQuoted(), t.getDescription());

					for (FieldModel f : t.getFields())
						if (f.getIsAnalitycs())
							sql += String.format(Util.newLine + "COMMENT ON COLUMN %s.%s  IS '%s'; ", t.ViewNameQuoted(), f.getNameS2A(), f.getDescription());
					sql += Util.newLine;

					sql += Util.getViewGrantString(t.getViewName(), true);

				}
				sql += Util.newLine;

			}

		} else if (t.isMensile()) {
			sql += Util.newLine + "------------------------------------------------------------------------------------------";
			if (!createALLView) {
				sql += Util.newLine + " --" + t.getViewName() + " - " + t.getDescription() + " (Mensile)";
				sql += Util.newLine + "------------------------------------------------------------------------------------------";
				sql += Util.newLine + String.format("CREATE OR REPLACE VIEW %s BEQUEATH DEFINER AS ", t.getViewName());
				sql += Util.newLine + "(";
				sql += Util.newLine + " SELECT";
				bComma = "";
				for (FieldModel f : t.getFields())
					if (f.getIsAnalitycs()) {
						sql += String.format(Util.newLine + " %s%s %s ", bComma, f.getName(), f.getNameS2A());
						bComma = ",";
					}
				sql += String.format(Util.newLine + " FROM  %s WHERE %s IN (SELECT ABI FROM GET_ABI())", t.getName(), t.Field_ABI().getName());

				sql += Util.newLine + ") WITH CHECK OPTION;" + Util.newLine;

				sql += String.format(Util.newLine + "COMMENT ON TABLE %s IS '%s';\r" + Util.newLine, t.ViewNameQuoted(), t.getDescription());

				for (FieldModel f : t.getFields())
					if (f.getIsAnalitycs())
						sql += String.format(Util.newLine + "COMMENT ON COLUMN %s.%s  IS '%s'; ", t.ViewNameQuoted(), f.getNameS2A(), f.getDescription());
				sql += Util.newLine;
				sql += Util.getViewGrantString(t.getViewName(), true);
			} else {
				sql += Util.newLine + " -- Vista con suffisso _ALL NON visibile alle banche";
				sql += Util.newLine + " --" + t.getViewName_ALL() + " - " + t.getDescription() + " (Mensile)";
				sql += Util.newLine + "------------------------------------------------------------------------------------------";
				sql += Util.newLine + String.format("CREATE OR REPLACE VIEW %s BEQUEATH DEFINER AS ", t.getViewName_ALL());
				sql += Util.newLine + "(";
				sql += Util.newLine + " SELECT";
				bComma = "";
				for (FieldModel f : t.getFields())
					if (f.getIsAnalitycs()) {
						sql += String.format(Util.newLine + " %s%s %s ", bComma, f.getName(), f.getNameS2A());
						bComma = ",";
					}
				// sql += String.format(Util.newLine + " FROM %s WHERE %s IN (SELECT ABI FROM
				// GET_ABI())", t.getName(), t.Field_ABI().getName());
				sql += String.format(Util.newLine + " FROM  %s ", t.getName());

				sql += Util.newLine + ") WITH CHECK OPTION;" + Util.newLine;

				sql += String.format(Util.newLine + "COMMENT ON TABLE %s IS '%s';\r" + Util.newLine, t.ViewName_ALLQuoted(), t.getDescription());

				for (FieldModel f : t.getFields())
					if (f.getIsAnalitycs())
						sql += String.format(Util.newLine + "COMMENT ON COLUMN %s.%s  IS '%s'; ", t.ViewName_ALLQuoted(), f.getNameS2A(), f.getDescription());
				sql += Util.newLine;
				sql += Util.getViewGrantString(t.getViewName_ALL(), false);

				sql += Util.newLine + " --Vista senza suffisso _ALL visibile alle banche";
				sql += Util.newLine + " --" + t.getViewName() + " - " + t.getDescription() + " (Mensile)";
				sql += Util.newLine + "------------------------------------------------------------------------------------------";
				sql += Util.newLine + String.format("CREATE OR REPLACE VIEW %s BEQUEATH DEFINER AS ", t.getViewName());
				sql += Util.newLine + "(";
				sql += Util.newLine + String.format(" SELECT * FROM %s WHERE ABI_BANCA IN (SELECT ABI FROM GET_ABI()) ", t.getViewName_ALL());
				sql += Util.newLine + ") WITH CHECK OPTION;" + Util.newLine;

				sql += String.format(Util.newLine + "COMMENT ON TABLE %s IS '%s';\r" + Util.newLine, t.ViewNameQuoted(), t.getDescription());

				for (FieldModel f : t.getFields())
					if (f.getIsAnalitycs())
						sql += String.format(Util.newLine + "COMMENT ON COLUMN %s.%s  IS '%s'; ", t.ViewNameQuoted(), f.getNameS2A(), f.getDescription());
				sql += Util.newLine;
				sql += Util.getViewGrantString(t.getViewName(), true);

			}

			sql += Util.newLine;
		}

		sql += Util.newLine + " ---CANCELLAZIONE: FILE di ROLLBACK >>>>>>>>>>>>>>>>>>>>>>>>>>>";
		sql += Util.newLine + String.format("PROMPT '---> Cancellazione tabella %s [%s]';\n", t.getName(), t.getDescription());
		sql += Util.newLine + String.format("DROP TABLE %s CASCADE CONSTRAINTS PURGE;", t.getName());
		if (createALLView)
			sql += Util.newLine + String.format("DROP VIEW %s;", t.getViewName_ALL());
		sql += Util.newLine + String.format("DROP VIEW %s;", t.getViewName());

		if (t.isGiornaliera()) {
			// Stored procedure di creazione tabelle BDT
			sql += Util.newLine + Util.newLine + Util.newLine;
			sql += Util.newLine + " ---BDT STORED PROCEDURE >>>>>>>>>>>>>>>>>>>>>>>>>>>";
			sql += Util.newLine + " ---BDT STORED PROCEDURE >>>>>>>>>>>>>>>>>>>>>>>>>>>";
			sql += Util.newLine + " ---BDT STORED PROCEDURE >>>>>>>>>>>>>>>>>>>>>>>>>>>";
			sql += t.CreateRebuildProcedure(t);

			// Stored procedure di creazione tabelle BDT (versione V2)
			sql += Util.newLine + Util.newLine + Util.newLine;
			sql += Util.newLine + " ---BDT STORED PROCEDURE V2 >>>>>>>>>>>>>>>>>>>>>>>>>>>";
			sql += Util.newLine + " ---BDT STORED PROCEDURE V2 >>>>>>>>>>>>>>>>>>>>>>>>>>>";
			sql += Util.newLine + " ---BDT STORED PROCEDURE V2 >>>>>>>>>>>>>>>>>>>>>>>>>>>";
			sql += t.CreateRebuildProcedureV2(t);

		}

		return sql;
	}

	private String CreateRebuildProcedure(TableModel t) {
		String sql = "";

		TableBDTModel bdt = t.getDBTTable(t.getIsPartizionamentoMensile());

		sql += Util.newLine + "------HEADER -->";
		sql += String.format(Util.newLine + "PROCEDURE REBUILD_%s(nrofdays IN INT); ", bdt.getName());

		sql += Util.newLine + "-";
		sql += Util.newLine + "-";
		sql += Util.newLine + "------BODY -->";

		sql += Util.newLine + "------------------------------------------------------------------------------------------";
		sql += String.format(Util.newLine + "  -- Ricostruzione della tabella %s (%s By Day Table)", bdt.getName(), t.getName());
		sql += Util.newLine + "------------------------------------------------------------------------------------------";

		sql += String.format(Util.newLine + "PROCEDURE REBUILD_%s(nrofdays IN INT) ", bdt.getName());
		sql += String.format(Util.newLine + "IS");
		sql += String.format(Util.newLine + "BEGIN");
		sql += String.format(Util.newLine + "    dbms_output.put_line('todo...');");
		sql += String.format(Util.newLine + "END;");
		return sql;
	}

	private String CreateRebuildProcedureV2(TableModel t) {
		String sql = "";

		TableBDTModel bdt = t.getDBTTable(t.getIsPartizionamentoMensile());

		sql += Util.newLine + "------HEADER -->";				
		sql += String.format(Util.newLine + "PROCEDURE REBUILD_%s_BDT(anAbi IN VARCHAR2, aDateFrom IN DATE, aDateTo IN DATE, aDeleteExistingRecords IN VARCHAR2);", bdt.getName());
		
		sql += Util.newLine + "-";
		sql += Util.newLine + "-";
		sql += Util.newLine + "------BODY -->";

		sql += Util.newLine + "------------------------------------------------------------------------------------------";
		sql += String.format(Util.newLine + "  -- Ricostruzione della tabella %s (%s By Day Table)", bdt.getName(), t.getName());
		sql += Util.newLine + "------------------------------------------------------------------------------------------";

		sql += String.format(Util.newLine + "PROCEDURE REBUILD_%s_BDT(anAbi IN VARCHAR2, aDateFrom IN DATE, aDateTo IN DATE, aDeleteExistingRecords IN VARCHAR2) ", bdt.getName());		
		sql += String.format(Util.newLine + "IS");
		sql += String.format(Util.newLine + "BEGIN");
		sql += String.format(Util.newLine + "    dbms_output.put_line('todo...');");
		sql += String.format(Util.newLine + "END;");
		return sql;
	}
	public static String CreatabellaDTO(String aTableName, Boolean isPartizionamentoPerMESEParam, boolean aCreateALLView) throws Exception {
		String tName = aTableName;
		pkg_model.TableModel t = pkg_model.TableModel.GetTable(tName, false);

		String sql = "";

		// Se esistono i campi SERVIZIO_SEGNALAZIONE O RAPPORTO_SEGNALAZIONE aggiunto i
		// campi calcolati SERVIZIO e RAPPORTO
		Boolean contains_SERVIZIO_SEGNALAZIONE = false;
		for (FieldModel f : t.getFields()) {
			if (f.getName().equals("SERVIZIO_SEGNALAZIONE") || f.getName().equals("RAPPORTO_SEGNALAZIONE"))
				contains_SERVIZIO_SEGNALAZIONE = true;
		}

		sql += String.format("PROMPT '---> Definizione del flusso FTNSR001-%s';" + Util.newLine + Util.newLine, t.getName());
		sql += String.format("--Gestione flusso rischio di FTNSR001-%s - Il file su cui va ad insistere e' DTO_FTNSR001_%s" + Util.newLine, t.getName(), t.getDTOName());
		sql += String.format("INSERT INTO S2A.ACT_DTO_FILES VALUES ('FTNSR001-%s', 'CSV', 'DTO_FTNSR001_%s', 'S');" + Util.newLine, t.getName(), t.getDTOName());
		sql += String.format("INSERT INTO S2A.ACT_DTO_FILES_CSV VALUES ('FTNSR001-%s',  'S', ';',  NULL);" + Util.newLine, t.getName());

		for (int i = 0; i < t.getFields().size(); i++) {
			FieldModel f = t.getFields().get(i);
			if (f.getNameS2A().equals("ABI_BANCA")) {
				sql += String.format("INSERT INTO S2A.ACT_DTO_FIELDS_CSV VALUES ('FTNSR001-%s','%s' ,'%s','SUBSTR(''000000'' || ''###@@@[FLD_VALUE]@@@###'', -5, 5)', 'N', 'N', 'N');", t.getName(), f.getNameS2A(), i);
				sql += Util.newLine;
			} else if (f.getType().equals("L")) {
				sql += String.format("INSERT INTO S2A.ACT_DTO_FIELDS_CSV VALUES ('FTNSR001-%s','%s' ,'%s' ,'TO_DATE(''###@@@[FLD_VALUE]@@@###'', ''YYYY/MM/DD'')', 'N', 'N', 'N');", t.getName(), f.getNameS2A(), i);
				sql += Util.newLine;
			} else if (f.getType().equals("A")) {
				sql += String.format("INSERT INTO S2A.ACT_DTO_FIELDS_CSV VALUES ('FTNSR001-%s','%s','%s'  ,NULL, 'S', 'S', 'N');", t.getName(), f.getNameS2A(), i);
				sql += Util.newLine;
			} else if (f.getType().equals("S") || f.getType().equals("P")) {
				sql += String.format("INSERT INTO S2A.ACT_DTO_FIELDS_CSV VALUES ('FTNSR001-%s','%s','%s'  ,NULL, 'N', 'N', 'N');", t.getName(), f.getNameS2A(), i);
				sql += Util.newLine;
			} else
				throw new Exception("Tipo non previsto....");
		}

		sql += String.format("\n\nPROMPT '--->Aggiunta dei tipo evento FTNSR001-%s_RECEIVED';" + Util.newLine, t.getName());

		sql += String.format("INSERT INTO ACT_EVENT_LOG_TYPES(EVENT_TYPE, DESCRIPTION) VALUES ('FTNSR001-%s_RECEIVED', '%s');" + Util.newLine, t.getName(), t.getDescription());
		sql += "COMMIT;" + Util.newLine + Util.newLine;

		sql += String.format("PROMPT '--->Creazione tabella DTO_FTNSR001_%s';" + Util.newLine, t.getDTOName());
		sql += "--------------------------------------------------------" + Util.newLine;
		sql += String.format("--DDL for Table DTO_FTNSR001_%s" + Util.newLine, t.getDTOName());
		sql += "--------------------------------------------------------" + Util.newLine;

		sql += String.format("CREATE TABLE S2A.DTO_FTNSR001_%s" + Util.newLine, t.getDTOName());
		sql += "(" + Util.newLine;

		sql += " REMOTE_FILE_NAME  VARCHAR2(512)," + Util.newLine;
		sql += " LOCAL_FILE_NAME   VARCHAR2(512)," + Util.newLine;
		sql += " ROW_ID            INT," + Util.newLine;
		sql += " DATA_INSERIMENTO  TIMESTAMP(6) DEFAULT SYSTIMESTAMP," + Util.newLine;
		sql += " STATUS            VARCHAR2(32)," + Util.newLine;
		sql += " ABI_BANCA         VARCHAR2(5)," + Util.newLine;
		sql += " DATA_RIFERIMENTO  DATE," + Util.newLine;
		for (FieldModel f : t.Fields_NoABI_NoDTV()) {
			if (f.getType().equals("L")) {
				sql += String.format(" %s DATE,", f.getNameS2A());
			} else if (f.getType().equals("A")) {
				sql += String.format(" %s VARCHAR2(%s),", f.getNameS2A(), f.getLength());
			} else if (f.getType().equals("S") || f.getType().equals("P")) {
				sql += String.format(" %s NUMBER,", f.getNameS2A());
			}
			sql += Util.newLine;
		}

		String tableSpaceName = Util.getTableSpaceName(t.getName());

		if (contains_SERVIZIO_SEGNALAZIONE) {
			sql += " SERVIZIO VARCHAR2(3) ," + Util.newLine;
			sql += " RAPPORTO VARCHAR2(11)," + Util.newLine;
		}

		sql += " ANNO DATE GENERATED ALWAYS AS (TRUNC(DATA_RIFERIMENTO,'YYYY')) VIRTUAL" + Util.newLine;
		sql += ")" + Util.newLine;
		sql += "SEGMENT CREATION DEFERRED ROW STORE COMPRESS ADVANCED" + Util.newLine;
		sql += String.format("PARTITION BY LIST(ANNO, ABI_BANCA) AUTOMATIC (PARTITION DTO_FTNSR001_%s_P1 VALUES((TO_DATE('01/01/2021', 'DD/MM/YYYY'), '03599'))) TABLESPACE %s;" + Util.newLine + Util.newLine,
				t.getDTOName(), tableSpaceName);

		sql += Util.newLine + "--------------------------------------------------------" + Util.newLine;
		sql += String.format("--DDL for Index DTO_FTNSR001_%s_PK" + Util.newLine, t.getDTOName());
		sql += "--------------------------------------------------------" + Util.newLine;

		sql += String.format("CREATE UNIQUE INDEX S2A.DTO_FTNSR001_%s_PK ON S2A.DTO_FTNSR001_%s (REMOTE_FILE_NAME, LOCAL_FILE_NAME,ROW_ID,ANNO, ABI_BANCA) LOCAL TABLESPACE %s;" + Util.newLine, t.getDTOName(),
				t.getDTOName(), tableSpaceName);
		sql += String.format(
				"ALTER TABLE S2A.DTO_FTNSR001_%s ADD CONSTRAINT DTO_FTNSR001_%s_PK PRIMARY KEY (REMOTE_FILE_NAME, LOCAL_FILE_NAME, ROW_ID, ANNO, ABI_BANCA) USING INDEX DTO_FTNSR001_%s_PK ENABLE;" + Util.newLine,
				t.getDTOName(), t.getDTOName(), t.getDTOName());
		sql += String.format("CREATE INDEX  S2A.%s_IDX100 ON DTO_FTNSR001_%s (ANNO, DATA_RIFERIMENTO, ABI_BANCA) COMPRESS ADVANCED HIGH LOCAL TABLESPACE %s;" + Util.newLine, t.getDTOName(), t.getDTOName(),
				tableSpaceName);

		sql += Util.newLine + "--------------------------------------------------------" + Util.newLine;
		sql += String.format("--Constraints for Table DTO_FTNSR001_%s  " + Util.newLine, t.getDTOName());
		sql += "--------------------------------------------------------" + Util.newLine;

		sql += String.format("ALTER TABLE S2A.DTO_FTNSR001_%s MODIFY (REMOTE_FILE_NAME NOT NULL ENABLE);" + Util.newLine, t.getDTOName());
		sql += String.format("ALTER TABLE S2A.DTO_FTNSR001_%s MODIFY (LOCAL_FILE_NAME NOT NULL ENABLE);" + Util.newLine, t.getDTOName());
		sql += String.format("ALTER TABLE S2A.DTO_FTNSR001_%s MODIFY (ROW_ID NOT NULL ENABLE);" + Util.newLine, t.getDTOName());
		sql += String.format("ALTER TABLE S2A.DTO_FTNSR001_%s MODIFY (ABI_BANCA NOT NULL ENABLE);" + Util.newLine, t.getDTOName());
		sql += String.format("ALTER TABLE S2A.DTO_FTNSR001_%s MODIFY (DATA_RIFERIMENTO NOT NULL ENABLE);" + Util.newLine, t.getDTOName());

		sql += Util.newLine + "--------------------------------------------------------" + Util.newLine;
		sql += String.format("--Comments for Table DTO_FTNSR001_%s  " + Util.newLine, t.getDTOName());
		sql += "--------------------------------------------------------" + Util.newLine;

		sql += String.format("COMMENT ON TABLE  S2A.DTO_FTNSR001_%s IS '%s' ;" + Util.newLine, t.getDTOName(), t.getDescription());

		sql += String.format("COMMENT ON COLUMN S2A.DTO_FTNSR001_%s.REMOTE_FILE_NAME IS 'Nome del flusso ricevuto da Host' ;" + Util.newLine, t.getDTOName());
		sql += String.format("COMMENT ON COLUMN S2A.DTO_FTNSR001_%s.LOCAL_FILE_NAME IS 'Nome locale di ricezione' ;" + Util.newLine, t.getDTOName());
		sql += String.format("COMMENT ON COLUMN S2A.DTO_FTNSR001_%s.ROW_ID IS 'Numero Riga' ;" + Util.newLine, t.getDTOName());
		sql += String.format("COMMENT ON COLUMN S2A.DTO_FTNSR001_%s.DATA_INSERIMENTO IS 'Data inserimento record' ;" + Util.newLine, t.getDTOName());
		sql += String.format("COMMENT ON COLUMN S2A.DTO_FTNSR001_%s.STATUS IS 'Status Record' ;" + Util.newLine, t.getDTOName());

		for (FieldModel f : t.getFields())
			sql += String.format("COMMENT ON COLUMN S2A.DTO_FTNSR001_%s.%s IS '%s' ;" + Util.newLine, t.getDTOName(), f.getNameS2A(), f.getDescription());
		if (contains_SERVIZIO_SEGNALAZIONE) {
			sql += String.format("COMMENT ON COLUMN S2A.DTO_FTNSR001_%s.SERVIZIO IS 'Servizio SIB' ;" + Util.newLine, t.getDTOName());
			sql += String.format("COMMENT ON COLUMN S2A.DTO_FTNSR001_%s.RAPPORTO IS 'Rapporto SIB' ;" + Util.newLine, t.getDTOName());
		}

		sql += Util.getTableGrantString("DTO_FTNSR001_" + t.getDTOName());

		sql += Util.newLine + "--------------------------------------------------------" + Util.newLine;
		if (aCreateALLView)
			sql += String.format("--DDL for View %s" + Util.newLine, t.getViewName_ALL());
		else
			sql += String.format("--DDL for View %s" + Util.newLine, t.getViewName());

		sql += "--------------------------------------------------------" + Util.newLine + Util.newLine;

		// Creazione della vista
		String bViewName = "?????";
		if (aCreateALLView)
			bViewName = t.getViewName_ALL();
		else
			bViewName = t.getViewName();

		sql += String.format("CREATE OR REPLACE VIEW S2A.%s BEQUEATH DEFINER AS" + Util.newLine, bViewName);
		sql += "(\nSELECT" + Util.newLine;
		sql += "   ABI_BANCA" + Util.newLine;
		sql += " ,DATA_RIFERIMENTO" + Util.newLine;
		for (FieldModel f : t.Fields_NoABI_NoDTV()) {
			if (f.getName().equals("REMOTE_FILE_NAME") || f.getName().equals("LOCAL_FILE_NAME") || f.getName().equals("ROW_ID") || f.getName().equals("DATA_INSERIMENTO") || f.getName().equals("STATUS")
					|| f.getName().equals("ANNO"))
				continue;
			if (contains_SERVIZIO_SEGNALAZIONE) {
				if (f.getName().equals("SERVIZIO_SEGNALAZIONE")) {
					sql += " ,SERVIZIO" + Util.newLine;
					sql += " ,RAPPORTO" + Util.newLine;

				}
			}
			sql += String.format(" ,%s" + Util.newLine, f.getNameS2A());
		}
		sql += " ,REMOTE_FILE_NAME" + Util.newLine;
		sql += " ,LOCAL_FILE_NAME" + Util.newLine;
		sql += " ,ROW_ID" + Util.newLine;
		sql += " ,DATA_INSERIMENTO" + Util.newLine;
		sql += " ,STATUS" + Util.newLine;
		sql += " ,ANNO" + Util.newLine;

		sql += String.format(" FROM\n   DTO_FTNSR001_%s" + Util.newLine, t.getDTOName());
		sql += " WHERE" + Util.newLine;

		if (aCreateALLView)
			sql += "       STATUS = 'PUBLISHED'" + Util.newLine;
		else
			sql += "       ABI_BANCA IN (SELECT * FROM GET_ABI()) AND STATUS = 'PUBLISHED'" + Util.newLine;

		sql += ") WITH CHECK OPTION;" + Util.newLine;
		sql += String.format("\nCOMMENT ON TABLE  S2A.%s IS '%s' ;" + Util.newLine, bViewName, t.getDescription());
		sql += String.format("COMMENT ON COLUMN S2A.%s.REMOTE_FILE_NAME IS 'Nome del flusso ricevuto da Host' ;" + Util.newLine, bViewName);
		sql += String.format("COMMENT ON COLUMN S2A.%s.LOCAL_FILE_NAME IS 'Nome locale di ricezione' ;" + Util.newLine, bViewName);
		sql += String.format("COMMENT ON COLUMN S2A.%s.ROW_ID IS 'Numero Riga' ;" + Util.newLine, bViewName);
		sql += String.format("COMMENT ON COLUMN S2A.%s.DATA_INSERIMENTO IS 'Data inserimento record' ;" + Util.newLine, bViewName);
		sql += String.format("COMMENT ON COLUMN S2A.%s.STATUS IS 'Status Record' ;" + Util.newLine, bViewName);
		for (FieldModel f : t.getFields())
			sql += String.format("COMMENT ON COLUMN S2A.%s.%s IS '%s' ;" + Util.newLine, bViewName, f.getNameS2A(), f.getDescription());
		if (contains_SERVIZIO_SEGNALAZIONE) {
			sql += String.format("COMMENT ON COLUMN S2A.%s.SERVIZIO IS 'Servizio SIB' ;" + Util.newLine, bViewName);
			sql += String.format("COMMENT ON COLUMN S2A.%s.RAPPORTO IS 'Rapporto SIB' ;" + Util.newLine, bViewName);
		}

		if (aCreateALLView)
			sql += Util.getViewGrantString(bViewName, false);
		else
			sql += Util.getViewGrantString(bViewName, true);

		if (aCreateALLView) {
			// Vista senza suffisso _ALL
			sql += Util.newLine + " --Vista senza suffisso _ALL visibile alle banche";
			sql += Util.newLine + " -- " + t.getViewName() + " - " + t.getDescription();
			sql += Util.newLine + "------------------------------------------------------------------------------------------";
			sql += Util.newLine + String.format("CREATE OR REPLACE VIEW %s BEQUEATH DEFINER AS ", t.getViewName());
			sql += Util.newLine + "(";
			sql += Util.newLine + String.format(" SELECT * FROM %s WHERE ABI_BANCA IN (SELECT ABI FROM GET_ABI()) ", t.getViewName_ALL());
			sql += Util.newLine + ") WITH CHECK OPTION;" + Util.newLine;
			sql += String.format(Util.newLine + "COMMENT ON TABLE %s IS '%s';", t.ViewNameQuoted(), t.getDescription());

			for (FieldModel f : t.getFields())
				if (f.getIsAnalitycs())
					sql += String.format(Util.newLine + "COMMENT ON COLUMN %s.%s  IS '%s'; ", t.ViewNameQuoted(), f.getNameS2A(), f.getDescription());
			sql += Util.newLine;

			sql += Util.getViewGrantString(t.getViewName(), true);
		}

		// file di rollback
		sql += Util.newLine + "----------------------------------------------------------" + Util.newLine;
		sql += "----------------FILE DI ROLLBACK--------------------------" + Util.newLine;
		sql += "----------------------------------------------------------" + Util.newLine;
		sql += "----------------------------------------------------------" + Util.newLine;

		sql += String.format("\nPROMPT '--->Cancellazione della definizione del flusso FTNSR001-%s';" + Util.newLine, t.getName());

		sql += String.format("DELETE FROM S2A.ACT_DTO_FILES WHERE FILE_NAME = 'FTNSR001-%s';" + Util.newLine, t.getName());
		sql += String.format("DELETE FROM S2A.ACT_DTO_FILES_CSV WHERE FILE_NAME = 'FTNSR001-%s';" + Util.newLine, t.getName());
		sql += String.format("DELETE FROM S2A.ACT_DTO_FIELDS_CSV WHERE FILE_NAME = 'FTNSR001-%s';" + Util.newLine, t.getName());
		sql += String.format("\nPROMPT '--->Cancellazione dei due nuovi eventi %s_RECEIVED';" + Util.newLine, t.getName());
		sql += String.format("DELETE FROM S2A.ACT_EVENT_LOG_PRODUCED WHERE EVENT_TYPE = 'FTNSR001-%s_RECEIVED';" + Util.newLine, t.getName());
		sql += String.format("DELETE FROM S2A.ACT_EVENT_LOG_PRODUCERS WHERE EVENT_TYPE = 'FTNSR001-%s_RECEIVED';" + Util.newLine, t.getName());
		sql += String.format("DELETE FROM S2A.ACT_EVENT_LOG_TYPES WHERE EVENT_TYPE = 'FTNSR001-%s_RECEIVED';" + Util.newLine, t.getName());
		sql += "COMMIT;" + Util.newLine;

		sql += String.format("\nDROP TABLE DTO_FTNSR001_%s CASCADE CONSTRAINTS PURGE;" + Util.newLine, t.getDTOName());
		sql += String.format("DROP VIEW  %s;" + Util.newLine, t.getViewName());
		if (aCreateALLView)
			sql += String.format("DROP VIEW  %s;" + Util.newLine, t.getViewName_ALL());

		// header del package
		sql += Util.newLine + "----------------------------------------------------------" + Util.newLine;
		sql += "----------------Header del package: aggiungere--------------------------" + Util.newLine;
		sql += "----------------------------------------------------------" + Util.newLine;
		sql += "----------------------------------------------------------" + Util.newLine;
		sql += String.format("PROCEDURE DTO_FTNSR001_%s_ON_RECEIVED(aREMOTE_FILE_NAME IN VARCHAR2);" + Util.newLine, t.getDTOName());

		// body del package
		sql += Util.newLine + "----------------------------------------------------------" + Util.newLine;
		sql += "----------------Body del package: aggiungere--------------------------" + Util.newLine;
		sql += "----------------------------------------------------------" + Util.newLine;
		sql += "----------------------------------------------------------" + Util.newLine;

		sql += String.format("   PROCEDURE DTO_FTNSR001_%s_ON_RECEIVED    (aREMOTE_FILE_NAME IN VARCHAR2)" + Util.newLine, t.getDTOName());
		sql += String.format("    IS" + Util.newLine);
		sql += String.format("    -- La procedura consente viene eseguita al termine dell'importazione dalla DTO del flusso" + Util.newLine);
		sql += String.format("    BEGIN" + Util.newLine);
		sql += String.format("        S2A.LOG_MANAGER.LOG_INFO ('Ricezione del flusso %s...', aREMOTE_FILE_NAME) ;" + Util.newLine, t.getDTOName());
		sql += String.format("        DBMS_OUTPUT.PUT_LINE('Received from DTO File Name :' || aREMOTE_FILE_NAME);" + Util.newLine);
		sql += String
				.format("        -- Cancello tutti i record ricevuti in precedenza a fronte dello stesso ABI_BANCA,DATA_RIFERIMENTO ma con aREMOTE_FILE_NAME DIVERSO da quello che ho appena ricevuto." + Util.newLine);
		sql += String.format("        DELETE FROM S2A.DTO_FTNSR001_%s" + Util.newLine, t.getDTOName());
		sql += String.format("            WHERE     (ABI_BANCA,DATA_RIFERIMENTO) IN" + Util.newLine);
		sql += String.format("                      (SELECT ABI_BANCA,DATA_RIFERIMENTO FROM S2A.DTO_FTNSR001_%s WHERE REMOTE_FILE_NAME=aREMOTE_FILE_NAME )" + Util.newLine, t.getDTOName());
		sql += String.format("            AND REMOTE_FILE_NAME <> aREMOTE_FILE_NAME;" + Util.newLine);
		sql += String.format("" + Util.newLine);
		sql += String.format("        -- Aggiornamento SERVIZIO, RAPPORTO a partire da SERVIZIO_SEGNALAZIONE, RAPPORTO_SEGNALAZIONE. SOLO se serve !!!!" + Util.newLine);
		sql += String.format("        -- UPDATE_SERVIZIO_RAPPORTO('DTO_FTNSR001_%s', aREMOTE_FILE_NAME);  --- Se serve aggiornare il servizio !!! " + Util.newLine + Util.newLine, t.getDTOName());

		sql += String.format("        -- Aggiorno lo status per rendere visibili i record nella vista" + Util.newLine);
		sql += String.format("        S2A.LOG_MANAGER.LOG_INFO ('Aggiornamento dello stato a PUBLISHED per il flusso %s...', aREMOTE_FILE_NAME);" + Util.newLine, t.getDTOName());
		sql += String.format("        UPDATE S2A.DTO_FTNSR001_%s SET STATUS='PUBLISHED' WHERE STATUS='RECEIVED' AND REMOTE_FILE_NAME=aREMOTE_FILE_NAME;" + Util.newLine, t.getDTOName());
		sql += String.format("        COMMIT;" + Util.newLine);
		sql += String.format("        S2A.LOG_MANAGER.LOG_INFO ('Aggiornamento dello stato a PUBLISHED per il flusso %s OK', aREMOTE_FILE_NAME);" + Util.newLine + Util.newLine, t.getDTOName());

		sql += String.format("        S2A.LOG_MANAGER.LOG_INFO ('Ricezione del flusso %s OK', aREMOTE_FILE_NAME) ;" + Util.newLine, t.getDTOName());
		sql += String.format("        EXCEPTION WHEN OTHERS THEN" + Util.newLine);
		sql += String.format("            S2A.LOG_MANAGER.LOG_ERROR ('Ricezione del flusso %s KO', aREMOTE_FILE_NAME) ;" + Util.newLine, t.getDTOName());
		sql += String.format("            RAISE;" + Util.newLine + Util.newLine);
		sql += String.format("    END DTO_FTNSR001_%s_ON_RECEIVED;" + Util.newLine + Util.newLine, t.getDTOName());

		// procedura UPDATE_SERVIZIO_RAPPORTO: modifiche nel commento
		sql += Util.newLine + "----------------------------------------------------------" + Util.newLine;
		sql += "----------------procedura UPDATE_SERVIZIO_RAPPORTO: modifiche nel commento --------------------------" + Util.newLine;
		sql += "----------------------------------------------------------" + Util.newLine;
		sql += "----------------------------------------------------------" + Util.newLine;
		sql += String.format("        - %s" + Util.newLine, t.getDTOName());
		// procedura UPDATE_SERVIZIO_RAPPORTO: modifiche nel sourgente
		sql += Util.newLine + "----------------------------------------------------------" + Util.newLine;
		sql += "----------------procedura UPDATE_SERVIZIO_RAPPORTO: modifiche nel sorgente--------------------------" + Util.newLine;
		sql += "----------------------------------------------------------" + Util.newLine;
		sql += "----------------------------------------------------------" + Util.newLine;
		sql += String.format("            OR aTableName='DTO_FTNSR001_%s'" + Util.newLine, t.getDTOName());

		return sql;
	}
}
