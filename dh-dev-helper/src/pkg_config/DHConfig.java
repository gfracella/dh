package pkg_config;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class DHConfig {

	String _tnsName;
	String _proxy;
	String _walletLocation;
	String _tnsAdminLocation;
	
	public DHConfig() {
		JSONParser jsonParser = new JSONParser();
		
		try (FileReader reader = new FileReader("dh-dev.json"))
        {
            //Read JSON file
			JSONObject obj = (JSONObject)jsonParser.parse(reader);
			_tnsName = (String)obj.get("TNS_NAME");
			_proxy = (String)obj.get("PROXY_USER");
			_walletLocation = (String)obj.get("WALLET_LOCATION");
			_tnsAdminLocation = (String)obj.get("TNS_ADMIN_LOCATION");			
 
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
	}

	public String getTnsName() {
		return _tnsName;
	}

	public String getProxy() {
		return _proxy;
	}

	public String getWalletLocation() {
		return _walletLocation;
	}

	public String getTnsAdminLocation() {
		return _tnsAdminLocation;
	}
	
	
	
}
