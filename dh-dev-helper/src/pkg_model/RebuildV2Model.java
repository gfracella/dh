package pkg_model;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import oracle.jdbc.OracleConnection;

public class RebuildV2Model {

	public static String CreaRebuildProc() throws Exception {

		List<String> aTabs = Util.GetDailyTables();
		String sql = "";

		sql += String.format("-- Nota: le tabelle per le quali ci sono delle eccezioni sono:\n");
		sql += String.format("--      SD51T: non viene calcolata la data di ultimo full ma viene presa la DTV massima\n");
		sql += String.format("--      SD49T: Questa tabella viene spedita con i dati relativi a T-2\n");
		sql += String.format("--      SDE  : Ogni volta che faccio la rebuild su SDE lancio la rebuild su SD30T\n");
		sql += String.format("--      SD65T: Ogni volta che faccio la rebuild su SD65T lancio la rebuild su SDA65\n");
		sql += String.format("--      SD66T: Ogni volta che faccio la rebuild su SD66T lancio la rebuild su SDA66\n");
		sql += String.format("--      UPRCT: Controllare bene il popolamento della _BDT\n\n");

		sql += String.format("PROMPT--->'Creating Package REBUILD_BDT_V2';\n");
		sql += String.format("--------------------------------------------------------\n");
		sql += String.format("--  DDL for Package REBUILD_BDT_V2\n");
		sql += String.format("--------------------------------------------------------\n");
		sql += String.format(" \n");
		sql += String.format("  CREATE OR REPLACE EDITIONABLE PACKAGE \"S2A\".\"REBUILD_BDT_V2\" \n");
		sql += String.format("AS\n");
		sql += String.format("   -- Dichiarazione del package REBUILD_BDT_V2\n");
		sql += String.format("\n");
		sql += String.format("    PROCEDURE REBUILD_TABLE (aTableName IN VARCHAR2, anAbi IN VARCHAR2, aDateFrom IN DATE, aDateTo IN DATE, aDeleteExistingRecords IN VARCHAR2);\n");
		sql += String.format("    PROCEDURE REBUILD_ALL_BDT(anAbi IN VARCHAR2, aDateFrom IN DATE, aDateTo IN DATE, aDeleteExistingRecords IN VARCHAR2);\n");

		for (int i = 0; i < aTabs.size(); i++) {
			String tab = aTabs.get(i);
			sql += String.format("    PROCEDURE REBUILD_%s_BDT(anAbi IN VARCHAR2, aDateFrom IN DATE, aDateTo IN DATE, aDeleteExistingRecords IN VARCHAR2);\n", tab);
		}
		sql += String.format("END REBUILD_BDT_V2;\n");
		sql += String.format("\n");
		sql += String.format("/\n");
		sql += String.format("\n");
		sql += String.format("  GRANT EXECUTE ON \"S2A\".\"REBUILD_BDT_V2\" TO \"S2A_ROLE\";\n");
		sql += String.format(" \n");
		sql += String.format(" \n");

		sql += String.format("--------------------------------------------------------\n");
		sql += String.format("--  DDL for Package Body REBUILD_BDT_V2\n");
		sql += String.format("--------------------------------------------------------\n");
		sql += String.format("\n");
		sql += String.format("  CREATE OR REPLACE EDITIONABLE PACKAGE BODY \"S2A\".\"REBUILD_BDT_V2\" AS\n");
		sql += String.format("------------- INIZIO PACKAGE ----------------------\n");
		sql += String.format("\n");
		sql += String.format("\n");
				
		
		sql += String.format("PROCEDURE REBUILD_TABLE(aTableName IN VARCHAR2, anAbi IN VARCHAR2, aDateFrom IN DATE, aDateTo IN DATE, aDeleteExistingRecords IN VARCHAR2)\n");
		sql += String.format("IS\n");
		sql += String.format("    b_bdt_table_name VARCHAR2(128);\n");
		sql += String.format("    b_sql  VARCHAR2(128);\n");
		sql += String.format("    b_count int;\n");
		sql += String.format("BEGIN\n");
		sql += String.format("    S2A.LOG_MANAGER.LOG_INFO('REBUILD_TABLE...',  aTableName || ',' || anAbi || ',' || to_char(aDateFrom) || ',' || to_char(aDateTo) || ',' || aDeleteExistingRecords, null);\n");
		sql += String.format("    b_count := 0;\n");
		sql += String.format("    b_bdt_table_name := upper(aTableName) || '_BDT';\n");
		sql += String.format("    select count(*) into b_count from user_tables where table_name = b_bdt_table_name;\n");
		sql += String.format("    IF b_count>0 THEN\n");
		sql += String.format("        b_sql:='BEGIN S2A.REBUILD_BDT_V2.REBUILD_' || b_bdt_table_name || '(:anAbi,:aDateFrom,:aDateTo,:aDeleteExistingRecords) ; END;';\n");
		sql += String.format("        S2A.LOG_MANAGER.LOG_INFO('REBUILD_TABLE sqlcmd',  aTableName || ',' || anAbi || ',' || to_char(aDateFrom) || ',' || to_char(aDateTo) || ',' || aDeleteExistingRecords, b_sql);\n");
		sql += String.format("        execute immediate b_sql using anAbi, aDateFrom,aDateTo,aDeleteExistingRecords;\n");
		sql += String.format("    END IF;\n");
		sql += String.format("    S2A.LOG_MANAGER.LOG_INFO('REBUILD_TABLE OK',  aTableName || ',' || anAbi || ',' || to_char(aDateFrom) || ',' || to_char(aDateTo) || ',' || aDeleteExistingRecords, null);\n");
		sql += String.format("END REBUILD_TABLE;\n\n\n");

		
		
		sql += String.format("PROCEDURE REBUILD_ALL_BDT(anAbi IN VARCHAR2, aDateFrom IN DATE, aDateTo IN DATE, aDeleteExistingRecords IN VARCHAR2)\n");
		sql += String.format("IS\n");
		sql += String.format("    v_sql VARCHAR2(512);\n");
		sql += String.format("BEGIN\n");
		for (int i = 0; i < aTabs.size(); i++) {
			String tab = aTabs.get(i);
			sql += String.format("    REBUILD_%s_BDT(anAbi, aDateFrom, aDateTo, aDeleteExistingRecords);\n", tab);
		}
		sql += String.format("END;\n");
		sql += String.format("\n");

		for (int i = 0; i < aTabs.size(); i++) {
			String tab = aTabs.get(i);
			OracleCatalogTableModel tabObj = new OracleCatalogTableModel(tab);

			if (!tabObj.getName().equals("SD51T")) {
				sql += String.format("\n\n");
				sql += String.format("-- CALCULATE_LAST_FULL_IMPORT_DATE_%s - Calcola la data relativa all'ultima importazione FULL (%s).\n", tabObj.getName(), tabObj.getName());
				sql += String.format("FUNCTION CALCULATE_LAST_FULL_IMPORT_DATE_%s(anAbiIn VARCHAR2, aDatain DATE) RETURN DATE IS\n", tabObj.getName());
				sql += String.format("bLastFull DATE;\n");
				sql += String.format("BEGIN\n");
				sql += String.format("  bLastFull:=NULL;\n");
				sql += String.format("  IF TRUNC(aDatain) = TRUNC(LAST_DAY(aDatain))\n");
				sql += String.format("  THEN\n");
				sql += String.format("    --Se aDataIn e' un fine mese prendo come data di ultima importazione FULL la data massima del mese di aDatain\n");

				sql += String.format("    SELECT MAX(%s) INTO bLastFull FROM %s WHERE %s  BETWEEN TRUNC(aDatain, 'FMMM') AND LAST_DAY(aDatain) and %s=anAbiIn;\n", tabObj.getDTVField(), tabObj.getName(),
						tabObj.getDTVField(), tabObj.getAbiField());

				sql += String.format("  ELSE\n");
				sql += String.format("    --Se aDataIn NON un fine mese prendo come ultima importazione FULL la data massima del mese precente a quello passato come aDatain\n");

				sql += String.format("    SELECT MAX(%s) INTO bLastFull FROM %s WHERE %s  BETWEEN TRUNC(ADD_MONTHS(aDatain, -1), 'FMMM') AND LAST_DAY(ADD_MONTHS(aDatain, -1)) and %s=anAbiIn;\n", tabObj.getDTVField(),
						tabObj.getName(), tabObj.getDTVField(), tabObj.getAbiField());

				sql += String.format("  END IF;\n");
				sql += String.format("-- Se non esiste l'ultimo FULL del mese precente prendo come ultimmo FULL la data minima del mese corrente\n");
				sql += String.format("IF bLastFull IS NULL THEN\n");

				sql += String.format("    SELECT MIN(%s) INTO bLastFull FROM %s WHERE %s  BETWEEN TRUNC(aDatain, 'FMMM') AND LAST_DAY(aDatain) and %s=anAbiIn;\n", tabObj.getDTVField(), tabObj.getName(),
						tabObj.getDTVField(), tabObj.getAbiField());

				sql += String.format("END IF;\n");
				sql += String.format("RETURN bLastFull;\n");
				sql += String.format("END;\n\n");
			}

			sql += String.format("-- Ricostruzione della tabella %s_BDT (%s By Day Table)\n", tabObj.getName(), tabObj.getName());
			sql += String.format("PROCEDURE REBUILD_%s_BDT(anAbi IN VARCHAR2, aDateFrom IN DATE, aDateTo IN DATE, aDeleteExistingRecords IN VARCHAR2)\n", tabObj.getName());
			sql += String.format("IS\n");
			sql += String.format("b_abi %s.%s%%TYPE;\n", tabObj.getName(), tabObj.getAbiField());
			sql += String.format("b_date_from DATE; b_date_to DATE; b_curr_day DATE; b_last_full_import_date DATE;\n");
			sql += String.format("b_log_Id varchar2(32) := ' [' || to_char(current_timestamp, 'HH24') || to_char(current_timestamp, 'MM') || to_char(current_timestamp, 'FF') || '] ';\n");
			sql += String.format("b_existing_records INT;\n");
			sql += String.format("BEGIN\n");
			sql += String.format("    S2A.LOG_MANAGER.LOG_INFO('REBUILD_%s', b_log_id || 'Inizio');\n", tabObj.getName());
			sql += String.format("    b_date_from := trunc(aDateFrom);           \n");
			sql += String.format("    b_date_to   := trunc(aDateTo);             \n");
			sql += String.format("    FOR abi_row IN ( SELECT abi FROM get_abi())\n");
			sql += String.format("    LOOP                                       \n");
			sql += String.format("        b_abi := abi_row.abi;                  \n");
			sql += String.format("        IF b_abi=anAbi OR anAbi='*ALL'         \n");
			sql += String.format("        THEN                                   \n");
			sql += String.format(
					"            S2A.LOG_MANAGER.LOG_INFO('REBUILD_%s' || '-' || b_abi, b_log_id || 'Rebuilding %s_BDT. Data da ' || to_char(b_date_from) || ' a ' || to_char(b_date_to) || ' - Delete Existing=' || aDeleteExistingRecords);\n",
					tabObj.getName(), tabObj.getName());
			sql += String.format("            b_curr_day:=b_date_from;\n");
			sql += String.format("            WHILE b_curr_day <= b_date_to\n");
			sql += String.format("            LOOP            \n");
			sql += String.format("                -- Se richiesto (aDeleteExistingRecords='S'), cancello i dati presenti per la data di elaborazione \n");
			sql += String.format("                IF aDeleteExistingRecords='S' THEN            \n");
			sql += String.format(
					"                    S2A.LOG_MANAGER.LOG_INFO('REBUILD_%s' || '-' || b_abi, b_log_id || 'Rebuilding %s_BDT. Cancellazione record con data  ' || b_curr_day  || ' e ABI:' || b_abi || '...');\n",
					tabObj.getName(), tabObj.getName());
			sql += String.format("                    DELETE FROM %s_BDT WHERE %s = b_abi AND cal_date = b_curr_day ;\n", tabObj.getName(), tabObj.getAbiField());
			sql += String.format(
					"                    S2A.LOG_MANAGER.LOG_INFO('REBUILD_%s' || '-' || b_abi, b_log_id || 'Rebuilding %s_BDT. Cancellazione record con data  ' || b_curr_day  || ' e ABI:' || b_abi || '. Record cancellati=' || SQL%%rowcount);\n",
					tabObj.getName(), tabObj.getName());
			sql += String.format("                    COMMIT;\n");
			sql += String.format("                END IF;\n\n");
			sql += String.format("                -- Eseguo la insert solo se NON sono presenti record per cal_date e abi uguali a quelli che si stanno inserendo\n");
			sql += String.format("                SELECT COUNT(*) INTO b_existing_records FROM %s_BDT WHERE %s = b_abi AND cal_date = b_curr_day AND ROWNUM<2;\n", tabObj.getName(), tabObj.getAbiField());
			sql += String.format("                IF b_existing_records=0 THEN\n");
			if (!tabObj.getName().equals("SD51T"))
				sql += String.format("                    b_last_full_import_date:=CALCULATE_LAST_FULL_IMPORT_DATE_%s(b_abi, b_curr_day); -- Calcolo ultimo full\n", tabObj.getName());
			sql += String.format("                    S2A.LOG_MANAGER.LOG_INFO('REBUILD_%s' || '-' || b_abi, b_log_id || 'Rebuilding %s_BDT. bLastFull:' || b_last_full_import_date || '. Inserimento nuovi record...');\n",
					tabObj.getName(), tabObj.getName());

			sql += String.format("                    INSERT INTO %s_BDT (\n", tabObj.getName());
			sql += String.format("                        cal_date,\n");
			for (int j = 0; j < tabObj.getKeys().size(); j++) {
				String key = tabObj.getKeys().get(j);
				if (!key.equals(tabObj.getDTVField()))
					sql += String.format("                        %s,\n", key);
			}
			sql += String.format("                        %s )\n", tabObj.getDTVField());
			sql += String.format("                        ( \n");
			sql += String.format("                            SELECT b_curr_day,\n");

			for (int j = 0; j < tabObj.getKeys().size(); j++) {
				String key = tabObj.getKeys().get(j);
				if (!key.equals(tabObj.getDTVField()))
					sql += String.format("                                    %s,\n", key);
			}
			sql += String.format("                            MAX(%s)\n", tabObj.getDTVField());

			sql += String.format("                            FROM %s\n", tabObj.getName());

			if (tabObj.getName().equals("SD51T"))
				sql += String.format("                            WHERE %s = b_abi AND %s <= b_curr_day\n", tabObj.getAbiField(), tabObj.getDTVField());
			else
				sql += String.format("                            WHERE %s = b_abi AND %s >= b_last_full_import_date and %s <= b_curr_day\n", tabObj.getAbiField(), tabObj.getDTVField(), tabObj.getDTVField());

			sql += String.format("                            GROUP BY %s\n", tabObj.getAbiField());
			for (int j = 0; j < tabObj.getKeys().size(); j++) {
				String key = tabObj.getKeys().get(j);
				if (key.equals(tabObj.getAbiField()) || key.equals(tabObj.getDTVField()))
					continue;
				sql += String.format("                                    ,%s\n", key);
			}

			sql += String.format("                        );\n");
			sql += String.format(
					"                    S2A.LOG_MANAGER.LOG_INFO('REBUILD_%s' || '-' || b_abi, b_log_id || 'Rebuilding %s_BDT. Date:' || b_curr_day || ' ABI:' || b_abi || '. Nr. record inseriti=' || SQL%%rowcount);\n",
					tabObj.getName(), tabObj.getName());

			sql += String.format("                    COMMIT;\n");

			sql += String.format("                END IF;\n");
			sql += String.format("                b_curr_day:=trunc(b_curr_day+1);\n");
			sql += String.format("            END LOOP;\n");
			sql += String.format("        END IF;\n");
			sql += String.format("    END LOOP;\n");
			sql += String.format("S2A.LOG_MANAGER.LOG_INFO('REBUILD_%s', b_log_id || 'Fine');\n", tabObj.getName());

			if (tabObj.getName().equals("SDE")) {
				sql += String.format("--Ogni volta che faccio la rebuild su SDE lancio la rebuild su SD30T\n");
				sql += String.format("REBUILD_BDT_V2.REBUILD_SD30T_BDT(anAbi, aDateFrom, aDateTo, aDeleteExistingRecords);\n");
			}
			if (tabObj.getName().equals("SD65T")) {
				sql += String.format("--Ogni volta che faccio la rebuild su SD65T lancio la rebuild su SDA65\n");
				sql += String.format("REBUILD_BDT_V2.REBUILD_SDA65_BDT(anAbi, aDateFrom, aDateTo, aDeleteExistingRecords);\n");
			}
			if (tabObj.getName().equals("SD66T")) {
				sql += String.format("--Ogni volta che faccio la rebuild su SD66T lancio la rebuild su SDA66\n");
				sql += String.format("REBUILD_BDT_V2.REBUILD_SDA66_BDT(anAbi, aDateFrom, aDateTo, aDeleteExistingRecords);\n");
			}
			sql += String.format("END REBUILD_%s_BDT;\n", tabObj.getName());
		}

		sql += String.format("------------------------ FINE PACKAGE BOBY -----------------------\n");
		sql += String.format("END REBUILD_BDT_V2;\n");
		sql += String.format("/\n");
		sql += String.format("\n");
		sql += String.format("  GRANT EXECUTE ON \"S2A\".\"REBUILD_BDT_V2\" TO \"S2A_ROLE\";\n");
		sql += String.format("\n");
		sql += String.format("\n");
		sql += String.format("PROMPT '--->RICOMPILAZIONE DEL PACKAGE REBUILD_BDT_V2';\n");
		sql += String.format("BEGIN\n");
		sql += String.format("EXECUTE IMMEDIATE 'ALTER PACKAGE S2A.REBUILD_BDT_V2 COMPILE BODY';\n");
		sql += String.format("EXCEPTION\n");
		sql += String.format("    WHEN OTHERS THEN\n");
		sql += String.format("    RAISE_APPLICATION_ERROR (-20000,'PACKAGE REBUILD_BDT_V2 NON COMPILATO...');\n");
		sql += String.format("END;\n");
		sql += String.format("/\n");
		return sql;
	}
}
