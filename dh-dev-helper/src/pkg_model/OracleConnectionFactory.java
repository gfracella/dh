package pkg_model;
import oracle.jdbc.OracleConnection;
import java.util.Properties;

import oracle.ucp.jdbc.PoolDataSource;
import oracle.ucp.jdbc.PoolDataSourceFactory;
import pkg_config.DHConfig;

public class OracleConnectionFactory {

//	private final static String DB_URL = "jdbc:oracle:thin:/@SRVTS2A_S2A_APP";
//	private final static String DB_PROXY_USER = "S2A";
//	private final static String WALLET_LOCATION = "C:\\Users\\fce458\\Documents\\Apps\\oracle\\OracleDeveloper\\instantclient\\network\\admin";
//	private final static String TNS_ADMIN_LOCATION = "C:\\Users\\fce458\\Documents\\Apps\\oracle\\OracleDeveloper\\instantclient\\network\\admin";

	public static OracleConnection getConnection() {

		OracleConnection conn = null;
		DHConfig cfg = new DHConfig();
		try {
//			System.setProperty("oracle.net.tns_admin",TNS_ADMIN_LOCATION);
//			System.setProperty("oracle.net.wallet_location", WALLET_LOCATION);

			System.setProperty("oracle.net.tns_admin",cfg.getTnsAdminLocation());
			System.setProperty("oracle.net.wallet_location", cfg.getWalletLocation());

			PoolDataSource pds = PoolDataSourceFactory.getPoolDataSource();
			pds.setConnectionFactoryClassName("oracle.jdbc.pool.OracleDataSource");
//			pds.setURL(DB_URL);
			pds.setURL("jdbc:oracle:thin:/@" + cfg.getTnsName());
			pds.setInitialPoolSize(10);
			pds.setMinPoolSize(1);
			pds.setMaxPoolSize(10);

			conn = (OracleConnection) pds.getConnection();

			// Imposto il proxy user
			if (!conn.isProxySession()) {
				Properties prop = new Properties();
				prop.put(OracleConnection.PROXY_USER_NAME, cfg.getProxy());
				conn.openProxySession(OracleConnection.PROXYTYPE_USER_NAME, prop);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return conn;

	}

}
