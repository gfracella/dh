package pkg_model;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import oracle.jdbc.OracleConnection;

// Legge la struttura della tabella dal catalogo Oracle
public class OracleCatalogTableModel {

	static Map<String, List<String>> AllTables;
	private String Name;
	private List<String> Keys;

	private void InitAllTables() {
		AllTables = new Hashtable<String, List<String>>();

		try {
			String sql = "SELECT cols.table_name, cols.column_name, cols.position, cons.status";
			sql += ",row_number() OVER (partition by cols.table_name ORDER BY cols.table_name, cols.position) rn ";
			sql += " FROM user_constraints cons, all_cons_columns cols";
			sql += " WHERE ";
			sql += " cols.table_name not like 'BIN%' AND ";
			sql += " cols.table_name not like '%_BDT' AND ";
			sql += " cols.table_name not like 'ACT%' AND ";
			sql += " cols.table_name not like 'DTO%' AND ";
			sql += " cols.table_name not like '_DEV%' AND ";
			sql += " cols.column_name not IN ('MESE', 'ANNO') AND ";
			sql += " cons.constraint_type = 'P'";
			sql += " AND cons.constraint_name = cols.constraint_name";
			sql += " AND cons.owner = cols.owner";
			sql += " ORDER BY cols.table_name, cols.position	   ";

			OracleConnection conn = OracleConnectionFactory.getConnection();
			Statement stmt = conn.createStatement();
			ResultSet rset = stmt.executeQuery(sql);

			String tableName = null;
			List<String> tableKeys = null;

			while (rset.next()) {
				String tname = rset.getString(1);
				String cname = rset.getString(2);
				int rn = rset.getInt(5);
				if (rn == 1) {
					tableName = tname;
					tableKeys = new ArrayList<String>();
					AllTables.put(tableName, tableKeys);
				}
				tableKeys.add(cname);
			}

			rset.close();
			stmt.close();
			conn.close();
			conn = null;
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public OracleCatalogTableModel(String name) {

		if (AllTables == null) {
			InitAllTables();
		}

		Name = name;
		Keys = AllTables.get(name);

	}

	public String getName() {
		return Name;
	}

	public List<String> getKeys() {
		return Keys;
	}

	public String getDTVField() throws Exception {
		if (Keys != null)
			for (String s : Keys) {
				if (s.endsWith("DTV"))
					return s;
			}
		throw new Exception("Attenzione: la tabella non ha il campo DTV");
	}

	public String getAbiField() throws Exception {
		if (Keys != null)
			for (String s : Keys) {
				if (s.endsWith("ABI"))
					return s;
			}
		throw new Exception("Attenzione: la tabella non ha il campo ABI");
	}

}
