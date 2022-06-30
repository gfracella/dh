package pkg_model;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import oracle.jdbc.OracleConnection;

public class Util {

	public static String newLine = System.getProperty("line.separator");

	public static String getTableSpaceName(String aTableName) {
		String res = "";
		String sql = String.format("SELECT ' S2A_DATA_' || LPAD(ORA_HASH(UPPER('%s'), 19) + 1, 2, '0') FROM DUAL",
				aTableName);
		try {
			OracleConnection conn = OracleConnectionFactory.getConnection();
			Statement stmt = conn.createStatement();
			ResultSet rset = stmt.executeQuery(sql);

			while (rset.next()) {
				res = rset.getString(1);
			}

			rset.close();
			stmt.close();
			conn.close();
			conn = null;
		} catch (Exception e) {
			e.printStackTrace();

		}
		return res;

	}

	public static String myTrim(String aValue) {
		byte[] bytes = aValue.getBytes();
		for (int i = 0; i < bytes.length; i++)
			if (bytes[i] < 0)
				bytes[i] = ' ';
		String res = (new String(bytes)).trim();
		return res;
	}

	// Grant standard sulle viste
	static String getViewGrantString(String name, Boolean isVisibileToBank) {
		String sql = "";
		sql += "\n\n";
		if (isVisibileToBank) {
			sql += "-- S2A_ROLE: Utente Banca\n";
			sql += String.format("GRANT SELECT ON S2A.%s TO S2A_ROLE;" + newLine, name);
		}
		sql += "-- S2A_DWH_AP: Utente applicativo DWH\n";
		sql += String.format("GRANT SELECT ON S2A.%s TO S2A_DWH_AP;" + newLine, name);
		sql += "-- S2A_GRACE_AP: Utente applicativo GRACE\n";
		sql += String.format("GRANT SELECT ON S2A.%s TO S2A_GRACE_AP; " + newLine, name);
		sql += "-- S2A_READ_ROLE: Utente EUS (dominio FCDB)\n";
		sql += String.format("GRANT SELECT ON S2A.%s TO S2A_READ_ROLE;" + newLine, name);
		sql += "-- EDCDISCOVERY: Utente EDC discovery per EDQ \n";
		sql += String.format("GRANT SELECT ON S2A.%s TO EDCDISCOVERY;" + newLine, name);
		sql += "-- S2A_ERMAS_AP: Utente per ERMAS \n";
		sql += String.format("GRANT SELECT ON S2A.%s TO S2A_ERMAS_AP;" + newLine, name);
		sql += "-- DATALAYER: Utente per DATALAYER \n";
		sql += String.format("GRANT SELECT ON S2A.%s TO DATALAYER WITH GRANT OPTION;" + newLine, name);
		return sql;
	}

	// Grant standard sulle tabelle: solo utenti EUR (utenti di dominio FCDB
	// allitude)
	static String getTableGrantString(String name) {
		String sql = "";
		sql += String.format("\n\n");
		sql += "-- S2A_READ_ROLE:-- Utente EUS (dominio FCDB) \n";
		sql += String.format("GRANT SELECT ON S2A.%s TO S2A_READ_ROLE  ;  " + newLine, name);
		sql += "-- S2A_WRITE_ROLE:-- Utente EUS (dominio FCDB) \n";
		sql += String.format("GRANT DELETE ON S2A.%s TO S2A_WRITE_ROLE ;  " + newLine, name);
		sql += String.format("GRANT INSERT ON S2A.%s TO S2A_WRITE_ROLE ;  " + newLine, name);
		sql += String.format("GRANT UPDATE ON S2A.%s TO S2A_WRITE_ROLE ;  " + newLine, name);
		return sql;
	}
	
	public static List<String> GetDailyTables() throws Exception {

		List<String> bRes = new ArrayList<String>();
		try {
			OracleConnection conn = OracleConnectionFactory.getConnection();
			Statement stmt = conn.createStatement();
			ResultSet rset = stmt.executeQuery(
					"SELECT REPLACE(REPLACE(PROCEDURE_NAME, 'REBUILD_',''), '_BDT') TAB FROM USER_PROCEDURES WHERE OBJECT_NAME = 'REBUILD_BDT' AND PROCEDURE_NAME LIKE 'REBUILD_%' AND PROCEDURE_NAME <> 'REBUILD_ALL_BDT' AND PROCEDURE_NAME <> 'REBUILD_TABLE' ORDER BY SUBPROGRAM_ID");
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
}
