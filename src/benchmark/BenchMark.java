package benchmark;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.apache.commons.lang3.text.StrBuilder;

public class BenchMark {
	//private static final String abCmd = "ab -n 100 -c 10 -g {DATA_FILENAME}.txt -p post_data.txt -T application/json -H \"x-forwarded-for: 171.48.0.1\" -H \"x-bsy-did: {DEVICE_ID}\" -H \"x-bsy-utkn: {UID}:{TOKEN}\" -H \"x-client: map\" {API_URL}";
	private static final String gnuplotPath = "/opt/local/bin/gnuplot";
	private static final Integer TOTAL_REQUEST_COUNT = 50;
	private static final Integer CONCURRENT_REQUEST_COUNT = 10;
	private static Map<String, String> apiUrlMap = new HashMap<String, String>();
	
	static {
		
		apiUrlMap.put("products", "http://10.0.3.113/myairtelapp/v1/account/products?spoof=true");
		apiUrlMap.put("genericInfo", "http://10.0.3.113/myairtelapp/v1/account/genericInfo?spoof=true");
		apiUrlMap.put("customTiles", "http://10.0.3.113/myairtelapp/v1/account/customTiles?spoof=true");
		apiUrlMap.put("alerts", "http://10.0.3.113/myairtelapp/v1/account/alerts?spoof=true");
	}
	
	public static void main(String[] args) {
		URL url = BenchMark.class.getResource("user_info.txt");
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File(url.getPath())));
			br.readLine();
			String line = null;
			while ((line = br.readLine()) != null) {
				String[] infoArray = line.split(" ");
				String msisdn = infoArray[0];
				String deviceId = infoArray[1];
				String uId = infoArray[2];
				String token = infoArray[3];
				System.out.println(msisdn+" "+deviceId+" "+token +" "+ uId);
				
				for(Map.Entry<String, String> apiInfo : apiUrlMap.entrySet()) {
					String dataFileName = msisdn+"-"+apiInfo.getKey();
					dataFileName = dataFileName.trim();
					String apiUrl = apiInfo.getValue();
					String[] cmd = new String[11];
					cmd[0] = "ab";
					cmd[1] = "-n "+TOTAL_REQUEST_COUNT.toString();
					cmd[2] = "-c "+CONCURRENT_REQUEST_COUNT.toString();
					cmd[3] = "-T application/json";
					cmd[4] = "-g"+dataFileName+".txt";
					cmd[5] = "-H \"x-forwarded-for: 171.48.0.1\"";
					cmd[6] = "-H \"x-client: map\"";
					cmd[7] = "-H \"x-bsy-did: "+deviceId+"\"";
					cmd[8] = "-H \"x-bsy-utkn: "+uId+":"+token+"\"";
					cmd[9] = "-v 4";
					cmd[10] = apiUrl;
					
					Process p = Runtime.getRuntime().exec(cmd);
					System.out.println(apiUrl+"\nProcessing ...");
					
					BufferedReader outReader = 
					         new BufferedReader(new InputStreamReader(p.getInputStream()));
					BufferedReader errorReader = 
					         new BufferedReader(new InputStreamReader(p.getErrorStream()));
					String line1 = "";
				    while ((line1 = outReader.readLine())!= null) {
				    		System.out.println(line1);
				    }
				    
				    while ((line1 = errorReader.readLine())!= null) {
			    		System.out.println(line1);
				    }
				    p.waitFor();
				    
				    // Ploting graph
				    URL plot_url = BenchMark.class.getResource("benchmark.plot");
				    String script_text = new Scanner(new File(plot_url.getPath())).useDelimiter("\\Z").next();
				    StrBuilder script = new StrBuilder(script_text);
				    script.replaceAll("{DATAFILE}", dataFileName);
				    
				    FileWriter fw = new FileWriter( (new File("benchmark1.plot")).getAbsoluteFile( ) );
				    BufferedWriter bw = new BufferedWriter( fw );
				    bw.write( script.toString() );
				    bw.close();
				    
				    Process gnuplot = Runtime.getRuntime().exec(gnuplotPath+" benchmark1.plot");
				    gnuplot.waitFor();
				    
				    BufferedReader reader1 = 
					         new BufferedReader(new InputStreamReader(gnuplot.getErrorStream()));
					String line2 = "";
				    while ((line2 = reader1.readLine())!= null) {
				    		System.out.println(line2);
				    }
				    
				    System.out.println("Done.");
				}
			}
		 
			br.close();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
}
