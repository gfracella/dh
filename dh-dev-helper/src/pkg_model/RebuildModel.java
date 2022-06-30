package pkg_model;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import oracle.jdbc.OracleConnection;

public class RebuildModel {

	public static String CreaRebuildProc() throws Exception {

		List<String> aTabs = Util.GetDailyTables();
		String sql = "";
		sql += String.format("PROMPT--->'Creating Package REBUILD_BDT';\n");
		sql += String.format("--------------------------------------------------------\n");
		sql += String.format("--  DDL for Package REBUILD_BDT\n");
		sql += String.format("--------------------------------------------------------\n");
		sql += String.format(" \n");
		sql += String.format("  CREATE OR REPLACE EDITIONABLE PACKAGE \"S2A\".\"REBUILD_BDT\" \n");
		sql += String.format("AS\n");
		sql += String.format("   -- Dichiarazione del package REBUILD_BDT\n");
		sql += String.format("\n");
		sql += String.format("    FUNCTION GET_DEFAULT_REBUILD_DAYS(nrofdays IN INT) RETURN INT;\n");		
		sql += String.format("    PROCEDURE REBUILD_TABLE (aTableName IN VARCHAR2, nrofdays IN INT);\n");		
		sql += String.format("    PROCEDURE REBUILD_ALL_BDT (nrofdays IN INT);\n");
		sql += String.format("\n");

		for (int i = 0; i < aTabs.size(); i++) {
			String tab = aTabs.get(i);
			sql += String.format("    PROCEDURE REBUILD_%s_BDT(nrofdays IN INT);\n", tab);
		}
		sql += String.format("END REBUILD_BDT;\n");
		sql += String.format("\n");
		sql += String.format("/\n");
		sql += String.format("\n");
		sql += String.format("  GRANT EXECUTE ON \"S2A\".\"REBUILD_BDT\" TO \"S2A_ROLE\";\n");
		sql += String.format(" \n");
		sql += String.format(" \n");
		sql += String.format("--------------------------------------------------------\n");
		sql += String.format("--  DDL for Package Body REBUILD_BDT\n");
		sql += String.format("--------------------------------------------------------\n");
		sql += String.format("\n");
		sql += String.format("  CREATE OR REPLACE EDITIONABLE PACKAGE BODY \"S2A\".\"REBUILD_BDT\" AS\n");
		sql += String.format("------------- INIZIO PACKAGE ----------------------\n");
		sql += String.format("\n");
		sql += String.format("\n");
		sql += String.format("------------------------------------------------------------------------------------------------------------\n");
		sql += String.format("--- FUNZIONE get_Default_rebuild_days(123): calcola il numero di giorni di default se non specificato ----\n");
		sql += String.format("------------------------------------------------------------------------------------------------------------\n");
		sql += String.format("\n");
		sql += String.format("FUNCTION get_Default_rebuild_days (nrofdays IN INT) RETURN INT\n");
		sql += String.format("AS\n");
		sql += String.format("    b_nrOfDays INT;\n");
		sql += String.format("BEGIN\n");
		sql += String.format("    IF nrofdays IS NULL OR nrofdays < 0\n");
		sql += String.format("    THEN b_nrOfDays := 1; -- Default 1 giorni\n");
		sql += String.format("    ELSE b_nrOfDays := nrofdays;\n");
		sql += String.format("    END IF;\n");
		sql += String.format("    RETURN b_nrOfDays;\n");
		sql += String.format("END;\n");
		sql += String.format("\n");
		
		sql += String.format("PROCEDURE REBUILD_TABLE (aTableName IN VARCHAR2, nrofdays IN INT)\n");
		sql += String.format("AS\n");
		sql += String.format("    b_bdt_table_name VARCHAR2(128);\n");
		sql += String.format("    b_sql  VARCHAR2(128);\n");
		sql += String.format("    b_count int;\n");
		sql += String.format("BEGIN\n");
		sql += String.format("    S2A.LOG_MANAGER.LOG_INFO('REBUILD_TABLE...[1]',  aTableName || ',' || nvl(to_char(nrofdays), 'null'), null);\n");
		sql += String.format("    b_count := 0;\n");
		sql += String.format("    b_bdt_table_name := upper(aTableName) || '_BDT';\n");
		sql += String.format("    select count(*) into b_count from user_tables where table_name = b_bdt_table_name;\n");
		sql += String.format("    IF b_count>0 THEN\n");
		sql += String.format("        S2A.LOG_MANAGER.LOG_INFO('REBUILD_TABLE...[2]',  aTableName || ',' || nvl(to_char(nrofdays),'null'), b_sql);\n");
		sql += String.format("        b_sql:='BEGIN S2A.REBUILD_BDT.REBUILD_' || b_bdt_table_name || '(:nroddays) ; END;';\n");
		sql += String.format("        execute immediate b_sql using nrofdays;\n");
		sql += String.format("        S2A.LOG_MANAGER.LOG_INFO('REBUILD_TABLE OK[2]',  aTableName || ',' || nvl(to_char(nrofdays),'null'), b_sql);\n");
		sql += String.format("    END IF;\n");
		sql += String.format("    S2A.LOG_MANAGER.LOG_INFO('REBUILD_TABLE OK[1]',  aTableName || ',' || nvl(to_char(nrofdays), 'null'), null);\n");
		sql += String.format("END REBUILD_TABLE;\n");

		
		
		sql += String.format("\n");
		sql += String.format("/************************************************************\n");
		sql += String.format("CALCULATE_LAST_FULL_IMPORT_DATE: E' una procedura comune a tutte.\n");
		sql += String.format("\n");
		sql += String.format("Calcola la data relativa all'ultima importazione FULL.\n");
		sql += String.format("Viene presa come data di ultima importazione FULL quella relativa\n");
		sql += String.format("alla data MASSIMA del mese precedente rispetto alla data passata\n");
		sql += String.format("in input. Se non esiste una importazione FULL per il mese\n");
		sql += String.format("precedente viene presa come data di ultima importazione FULL\n");
		sql += String.format("quella relativa alla data MINIMA del mese delle data passata\n");
		sql += String.format("in input.\n");
		sql += String.format("************************************************************/\n");
		sql += String.format("\n");
		sql += String.format("\n");
		sql += String.format("PROCEDURE REBUILD_ALL_BDT (nrofdays IN INT)\n");
		sql += String.format("IS\n");
		sql += String.format("    v_sql VARCHAR2(512);\n");
		sql += String.format("BEGIN\n");
		sql += String.format("    dbms_output.put_line('Rebuilding ALL BDT tables. Date:' || nrofdays);\n");
		sql += String.format("    -- FOR bRow IN\n");
		sql += String.format("    -- ( SELECT owner, object_name, procedure_name FROM all_procedures\n");
		sql += String.format("    --   WHERE owner = 'S2A'  and object_name = 'REBUILD_BDT' AND object_name <> 'REBUILD_ALL_BDT' and procedure_name like 'REBUILD*BDT')\n");
		sql += String.format("    --  LOOP\n");
		sql += String.format("    --     v_sql := 'BEGIN ' || bRow.owner || '.' || bRow.object_name || '.' || bRow.procedure_name || '(' || nrofdays || '); END;';\n");
		sql += String.format("    --     dbms_output.put_line(v_sql);\n");
		sql += String.format("    --     EXECUTE IMMEDIATE v_sql;\n");
		sql += String.format("    -- END LOOP;\n");
		for (int i = 0; i < aTabs.size(); i++) {
			String tab = aTabs.get(i);
			sql += String.format("    REBUILD_%s_BDT(nrofdays);\n", tab);
		}
		sql += String.format("END;\n");
		sql += String.format("\n");

		for (int i = 0; i < aTabs.size(); i++) {
			String tab = aTabs.get(i);
			OracleCatalogTableModel tabObj = new OracleCatalogTableModel(tab);
			System.out.println(String.format("%d - %s ", i, tab));
			sql += String.format("\n------------------------------------------------------------------------------------------\n");
			sql += String.format("  -- Ricostruzione della tabella %s_BDT (%s By Day Table)\n", tab, tab);
			sql += String.format("------------------------------------------------------------------------------------------\n");

			sql += String.format("PROCEDURE REBUILD_%s_BDT(nrofdays IN INT)\n", tabObj.getName());
			sql += String.format("IS\n");
			sql += String.format("    b_ela_date       DATE;\n");

			sql += String.format("    b_current_date   DATE;\n");

			sql += String.format("    b_abi        %s.%s%%TYPE;\n", tabObj.getName(), tabObj.getAbiField());
			sql += String.format("    b_nrOfDays   INT;\n");
			sql += String.format("    b_last_full_import_date DATE;  -- Data relativa all'ultima importazione FULL;\n");
			sql += String.format("    b_log_Id varchar2(32) := ' [' || to_char(current_timestamp, 'HH24') || to_char(current_timestamp, 'MM') || to_char(current_timestamp, 'FF') || '] '; \n\n");

			if (!tabObj.getName().equals("SD51T")) {
				sql += String.format("/**************************************************************************************************************************************\n");
				sql += String.format(" CALCULATE_LAST_FULL_IMPORT_DATE\n");
				sql += String.format("  Calcola la data relativa all'ultima importazione FULL. Viene presa come data di ultima importazione FULL quella relativa\n");
				sql += String.format("  alla data MASSIMA del mese precedente rispetto alla data passata in input. Se non esiste una importazione FULL per il mese\n");
				sql += String.format("  precedente viene presa come data di ultima importazione FULL quella relativa alla data MINIMA del mese delle data passata in input.\n");
				sql += String.format("***************************************************************************************************************************************/\n");

				sql += String.format("FUNCTION CALCULATE_LAST_FULL_IMPORT_DATE(anAbiIn VARCHAR2, aDatain DATE) RETURN DATE IS\n");
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
				sql += String.format("dbms_output.put_line('REBUILD_%s_BDT.CALCULATE_LAST_FULL_IMPORT_DATE. ABI: ' || anAbiIn ||  ' aDataIn:' || aDatain || '. bLastFull is ' || bLastFull);\n", tabObj.getName());

				sql += String.format(
						"S2A.LOG_MANAGER.LOG_INFO('REBUILD_%s' || '-' || anAbiIn, b_log_id || 'REBUILD_%s_BDT.CALCULATE_LAST_FULL_IMPORT_DATE. ABI: ' || anAbiIn ||  ' aDataIn:' || aDatain || '. bLastFull is ' || bLastFull);\n",
						tabObj.getName(), tabObj.getName());

				sql += String.format("RETURN bLastFull;\n");
				sql += String.format("END;\n\n");
			}

			sql += String.format("/**************************************************************************************************************************************\n");
			sql += String.format(" GET_DAYS_TO_REBUILD\n");
			sql += String.format("  Calcola il numero di giorni di cui fare la rebuild: ultimo giorno della _BDT o ultimo giorno della tabella o il default \n");
			sql += String.format("***************************************************************************************************************************************/\n");
			sql += String.format("FUNCTION GET_DAYS_TO_REBUILD(anAbiIn VARCHAR2, aNrOfDays INT) RETURN INT AS\n");
			sql += String.format("b_startDate   DATE;\n");
			sql += String.format("b_days   INT;\n");
			sql += String.format("BEGIN\n");
			sql += String.format("    IF aNrOfDays IS NULL THEN\n");
			sql += String.format("        SELECT MAX(CAL_DATE) into b_startDate FROM S2A.%s_BDT WHERE %s = anAbiIn ;\n", tabObj.getName(), tabObj.getAbiField());
			sql += String.format("        IF b_startDate IS NULL THEN\n");
			sql += String.format("            SELECT MAX(%s)-30 into b_startDate FROM S2A.%s WHERE %s = anAbiIn ;\n", tabObj.getDTVField(), tabObj.getName(), tabObj.getAbiField());
			sql += String.format("        END IF;\n");
			sql += String.format("        SELECT ABS(NVL(TRUNC(b_current_date-1) - TRUNC(b_startDate) , 0)) into b_days from dual; -- rebuild fino al giorno precedente\n");
			sql += String.format("        RETURN b_days;\n");
			sql += String.format("    ELSE\n");
			sql += String.format("        RETURN GET_DEFAULT_REBUILD_DAYS(nrofdays);\n");
			sql += String.format("    END IF;\n");
			sql += String.format("END;\n\n");
			sql += String.format("/************************************************************\n");
			sql += String.format("    MAIN    \n");
			sql += String.format("*************************************************************/\n");
			sql += String.format("BEGIN\n");
			if (!tabObj.getName().equals("SD51T"))
				sql += String.format("b_last_full_import_date:=NULL;\n");

			sql += String
					.format("b_current_date:=current_date + interval '5' hour;  -- Aggiunto 5 ore alla current_date (per fare in modo che le rebuild cominciate prima di mezzanotte elaborino il giorno successivo;\n");
			if (tabObj.getName().equals("SD49T"))
				sql += String.format("b_current_date:=b_current_date-1;  -- Questa tabella viene spedita con i dati relativi a T-2\n");
			sql += String.format("--SELECT GET_DEFAULT_REBUILD_DAYS(nrofdays) INTO b_nrOfDays FROM DUAL;\n");
			sql += String.format("S2A.LOG_MANAGER.LOG_INFO('REBUILD_%s', b_log_id || 'Starting...');\n", tabObj.getName());
			sql += String.format("\n");
			sql += String.format("-- Per tutti gli abi a cui sono abilitato\n");
			sql += String.format("FOR abi_row IN ( SELECT abi FROM get_abi())\n");
			sql += String.format("LOOP\n");
			sql += String.format("    b_abi := abi_row.abi;\n");
			sql += String.format("    b_nrOfDays:=GET_DAYS_TO_REBUILD(b_abi, nrofdays);\n");
			sql += String.format("    dbms_output.put_line('Rebuilding  %s_BDT. Numero giorni da ricostruire:' || b_nrOfDays );\n", tabObj.getName());

			sql += String.format("    S2A.LOG_MANAGER.LOG_INFO('REBUILD_%s' || '-' || b_abi, b_log_id || 'Rebuilding  %s_BDT. Numero giorni da ricostruire:' || b_nrOfDays );\n", tabObj.getName(), tabObj.getName());

			sql += String.format("\n");
			sql += String.format("-- A partire da b_nrOfDays giorni prima di oggi calcolo il record da inserire in %s_BDT\n", tabObj.getName());
			sql += String.format("    FOR idx IN REVERSE 1..b_nrOfDays LOOP\n");
			sql += String.format("        b_ela_date := trunc(b_current_date) - idx; -- rebuild fino al giorno precedente\n");

			sql += String.format("\n        -- Cancello i dati presenti per la data di elaborazione \n");
			sql += String.format("        DELETE FROM %s_bdt WHERE %s = b_abi AND cal_date = b_ela_date ; \n", tabObj.getName(), tabObj.getAbiField());
			sql += String.format("        dbms_output.put_line('Rebuilding %s_BDT. Cancellazione dei record con data  ' || b_ela_date || ' e ABI:' || b_abi || '. Nr. record cancellati=' || SQL%%rowcount);\n",
					tabObj.getName());
			sql += String.format(
					"        S2A.LOG_MANAGER.LOG_INFO('REBUILD_%s' || '-' || b_abi, b_log_id || 'Rebuilding %s_BDT. Cancellazione dei record con data  ' || b_ela_date  || ' e ABI:' || b_abi || '. Nr. record cancellati=' || SQL%%rowcount);\n",
					tabObj.getName(), tabObj.getName());
			sql += String.format("        COMMIT;\n");

			if (!tabObj.getName().equals("SD51T")) {
				sql += String.format("\n        -- Calcolo ultimo full \n");
				sql += String.format("        b_last_full_import_date:=CALCULATE_LAST_FULL_IMPORT_DATE(b_abi, b_ela_date);\n");
			}
			sql += String.format("        INSERT INTO %s_bdt (\n", tabObj.getName());
			sql += String.format("            cal_date,\n");
			for (int j = 0; j < tabObj.getKeys().size(); j++) {
				String key = tabObj.getKeys().get(j);
				if (!key.equals(tabObj.getDTVField()))
					sql += String.format("            %s,\n", key);
			}
			sql += String.format("            %s )\n", tabObj.getDTVField());
			sql += String.format("            ( SELECT b_ela_date,\n");
			
			for (int j = 0; j < tabObj.getKeys().size(); j++) {
				String key = tabObj.getKeys().get(j);
				if (!key.equals(tabObj.getDTVField()))
					sql += String.format("                     %s,\n", key);
			}
			sql += String.format("                     MAX(%s)\n", tabObj.getDTVField());

			sql += String.format("            FROM %s\n", tabObj.getName());

			if (tabObj.getName().equals("SD51T"))
				sql += String.format("            WHERE %s = b_abi AND %s <= b_ela_date\n", tabObj.getAbiField(), tabObj.getDTVField());
			else
				sql += String.format("            WHERE %s = b_abi AND %s >= b_last_full_import_date and %s <= b_ela_date\n", tabObj.getAbiField(), tabObj.getDTVField(), tabObj.getDTVField());

			sql += String.format("            GROUP BY %s\n", tabObj.getAbiField());
			for (int j = 0; j < tabObj.getKeys().size(); j++) {
				String key = tabObj.getKeys().get(j);
				if (key.equals(tabObj.getAbiField()) || key.equals(tabObj.getDTVField()))
					continue;
				sql += String.format("                     ,%s\n", key);
			}

			sql += String.format("            );\n");
			sql += String.format("    dbms_output.put_line('Rebuilding %s_BDT. Date:' || b_ela_date || ' ABI:' || b_abi || '. Nr. record inseriti=' || SQL%%rowcount);\n", tabObj.getName());

			sql += String.format("    S2A.LOG_MANAGER.LOG_INFO('REBUILD_%s' || '-' || b_abi, b_log_id || 'Rebuilding %s_BDT. Date:' || b_ela_date || ' ABI:' || b_abi || '. Nr. record inseriti=' || SQL%%rowcount);\n",
					tabObj.getName(), tabObj.getName());

			sql += String.format("    COMMIT;\n");
			sql += String.format("    END LOOP;\n");
			sql += String.format("END LOOP;\n");
			sql += String.format("S2A.LOG_MANAGER.LOG_INFO('REBUILD_%s', b_log_id || 'End...');\n", tabObj.getName());
			if (tabObj.getName().equals("SDE")) {
				sql += String.format("--Ogni volta che faccio la rebuild su SDE lancio la rebuild su SD30T\n");
				sql += String.format("REBUILD_BDT.REBUILD_SD30T_BDT(nrofdays);\n");
			}
			if (tabObj.getName().equals("SD65T")) {
				sql += String.format("--Ogni volta che faccio la rebuild su SD65T lancio la rebuild su SDA65\n");
				sql += String.format("REBUILD_BDT.REBUILD_SDA65_BDT(nrofdays);\n");
			}
			if (tabObj.getName().equals("SD66T")) {
				sql += String.format("--Ogni volta che faccio la rebuild su SD66T lancio la rebuild su SDA66\n");
				sql += String.format("REBUILD_BDT.REBUILD_SDA66_BDT(nrofdays);\n");
			}
			sql += String.format("END;\n");
		}
		sql += String.format("\n");
		sql += String.format("\n");
		sql += String.format("------------------------ FINE PACKAGE BOBY -----------------------\n");
		sql += String.format("END REBUILD_BDT;\n");
		sql += String.format("\n");
		sql += String.format("/\n");
		sql += String.format("\n");
		sql += String.format("  GRANT EXECUTE ON \"S2A\".\"REBUILD_BDT\" TO \"S2A_ROLE\";\n");
		sql += String.format("\n");
		sql += String.format("\n");
		sql += String.format("PROMPT '--->RICOMPILAZIONE DEL PACKAGE REBUILD_BDT';\n");
		sql += String.format("BEGIN\n");
		sql += String.format("EXECUTE IMMEDIATE 'ALTER PACKAGE S2A.REBUILD_BDT COMPILE BODY';\n");
		sql += String.format("EXCEPTION\n");
		sql += String.format("    WHEN OTHERS THEN\n");
		sql += String.format("    RAISE_APPLICATION_ERROR (-20000,'PACKAGE REBUILD_BDT NON COMPILATO...');\n");
		sql += String.format("END;\n");
		sql += String.format("/\n");

		return sql;
	}
}
