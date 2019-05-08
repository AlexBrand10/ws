package com.a4sys.luciWS.ikeWS;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

import com.a4sys.core.dao.ALQueryResult;
import com.a4sys.core.dao.DaoService;
import com.a4sys.luciWS.util.QuerysDB;
import com.a4sys.luciWS.util.SystemConstants;

/***
 * Clase servicio 
 * @author Martin Morones
 *
 */
public class WSIkeEmisionPolizaService extends DaoService{
	
	public WSIkeEmisionPolizaService(){
		super(SystemConstants.JDNI);
	}
	
	/***
	 * metodo que consume el WS de la Latino y obtiene un numero de Poliza
	 * @param xml
	 * @param soapAction
	 * @return
	 */
	public String getPoliza(String xml, Long idWebService) {
		String respuesta = "";
		InputStream inputStream  = null;
		OutputStream outputStream = null;
		Scanner sc = null;
		
		try{
				//otenemos la url y el action
				sqlQueryExecutor.addParameter("idWebService", idWebService);
				ALQueryResult ws  =  sqlQueryExecutor.executeQuery(QuerysDB.GET_WS_DATA);
				
			    URL url = new URL(ws.get(0,"URL").toString());
			    HttpsURLConnection  conn =  (HttpsURLConnection)url.openConnection();
			    conn.setDoOutput(true);
			    //conn.setRequestMethod("POST");		        
			    conn.setRequestProperty("Content-Type", "text/xml; charset=charset=utf-8");
			    //conn.setRequestProperty("Transfer-Encoding", "chunked");
			    conn.setRequestProperty("SOAPAction", ws.get(0,"ACTION").toString());
		
			    // Send the request XML
			    outputStream = conn.getOutputStream();
			    outputStream.write(xml.getBytes());			    
		
			    // Read the response XML
			    inputStream = conn.getInputStream();			    
			    
			    InputStreamReader inputstreamreader = new InputStreamReader(inputStream);
				BufferedReader bufferedreader = new BufferedReader(inputstreamreader);
				
			    String linea = bufferedreader.readLine();
			    String xmlResponse = "";
				while (linea != null) {
				 xmlResponse +=linea;   
				 linea = bufferedreader.readLine();
				}
				
			    
			    respuesta = "0,"+xmlResponse;
			    System.out.println(respuesta);
			    
		}catch(Exception e){
			respuesta = "-1,Error No Esperado:"+e.getMessage();
			e.printStackTrace();
		}finally{
			//if(null != sc){sc.close();}
			if(null != inputStream){
				try {
					inputStream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if(null != outputStream){
				try {
					outputStream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		return respuesta;
	}
	
	
	
}
