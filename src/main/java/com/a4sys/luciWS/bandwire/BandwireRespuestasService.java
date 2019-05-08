package com.a4sys.luciWS.bandwire;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.codec.binary.StringUtils;

import com.a4sys.core.dao.ALQueryResult;
import com.a4sys.core.dao.DaoService;
import com.a4sys.core.exceptions.SystemException;
import com.a4sys.luciWS.domain.ResponseWS;
import com.a4sys.luciWS.util.QuerysDB;
import com.a4sys.luciWS.util.SystemConstants;
import com.a4sys.luciWS.util.Util;
import com.a4sys.luciWS.util.WsErrors;
import com.google.gson.Gson;


/***
 * Clase servicio para web services de bandwire
 * @author Martin Morones
 *
 */
public class BandwireRespuestasService extends DaoService {

	
	
	public BandwireRespuestasService(){
		super(SystemConstants.JDNI);
	}

	/***
	 *  Metodo que guarda las respuestas de bandwire
	 * @param notificacion
	 * @return
	 */
	public ResponseWS saveResponse(NotificacionBanwire notificacion) {
				
		ResponseWS respuesta = new ResponseWS();
		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");			
		String inicioTransaccion = format.format(new Date());
		
		//guadamos la respuesta en nuestra base de datos
		String notificacionJson = new Gson().toJson(notificacion);
		sqlQueryExecutor.addParameter("notificacion",notificacionJson );
		
		
		ALQueryResult res = null;
		try {
			res = sqlQueryExecutor.executeQuery(QuerysDB.MAKEPAYMENT_LUCI);
			
			String result = res.get(0,"RES").toString(); 
			if(result.equalsIgnoreCase("Cobro Exitoso")){
				respuesta = Util.setResponse("0", result, null);
			}else{
				respuesta = Util.setResponse("-1", result, null);
			}
		} catch (SystemException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			respuesta = Util.setResponse("-1", "Error: Ocurrio un Error,"+e.getMessage(), null);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			respuesta = Util.setResponse("-1", "Error: Ocurrio un Error en La BD,"+e.getMessage(), null);		
		}
		
		
		//Insertamos el Low de Web service
		Long idLogWebService;
		try {
			//obtenemos el idLogWebService
			idLogWebService = (Long)sqlQueryExecutor.executeQuery(QuerysDB.GET_IDLODWEBSERVICE).get(0,"IDLOGWEBSERVICE");
			Util.insertLogWs(sqlQueryExecutor, idLogWebService,  49L, notificacionJson ,  new Gson().toJson(respuesta), new Long(notificacion.getReference()), inicioTransaccion, null);
		} catch (SystemException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
				
		
		return respuesta;
	}
	
	

	/***
	 * Clase que realiza un cargo a una tarjeta nueva usando openpay
	 * @param folio
	 * @param idClienteTdc
	 * @param idWebService 
	 * @param idUsuarioResponsable
	 * @param esPreAutorizacion 
	 * @return
	 * @throws SQLException 
	 * @throws SystemException 
	 */
	public ResponseWS addSubscription(Long folio, Long idClienteTdc, Long idWebService) throws SystemException, SQLException {
		ResponseWS respuesta = new ResponseWS();
		String entrada = "";
		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");			
		String inicioTransaccion = format.format(new Date());
		StringBuffer response  = null;
		
		InputStream inputStream  = null;
		OutputStream outputStream = null;
		
		//Obtenermos los datos del Cliente y de TDC				
		sqlQueryExecutor.addParameter("folio", folio);
		sqlQueryExecutor.addParameter("idClienteTdc", idClienteTdc);
		ALQueryResult dataCharge =  sqlQueryExecutor.executeQuery(QuerysDB.GET_BANDWIRE_CHARGE_DATA);
		
		//Obtenemos los datos del WS = 42
		sqlQueryExecutor.addParameter("idWebService", idWebService); //dicWebService = 42 
		ALQueryResult dataWs = sqlQueryExecutor.executeQuery(QuerysDB.GET_WS_DATA_CONSUME_CHARGE);
		
		//obtenemos el idLogWebService
		Long idLogWebService = (Long)sqlQueryExecutor.executeQuery(QuerysDB.GET_IDLODWEBSERVICE).get(0,"IDLOGWEBSERVICE");
		
		if(dataCharge.size() != 0){
			

			try{
					
				URL obj = new URL(dataWs.get(0,"URL").toString());
				HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

				//add reuqest header
				con.setRequestMethod("POST");
				con.setRequestProperty("User-Agent", "Mozilla/5.0");
				con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

				String urlParameters = "method=add"
									  +"&user="+dataWs.get(0,"LLAVE").toString() 
				                      +"&reference="+folio.toString()//idLogWebService.toString()
				                      +"&interval=month"
				                      +"&limit=0"
				                      +"&amount="+dataCharge.get(0,"AMOUNT").toString()
				                      +"&start="+dataCharge.get(0,"FECHASTART").toString()
				                      +"&url_notify="+dataWs.get(0,"ACTION").toString()
				                      +"&number="+dataCharge.get(0,"CARDNUMBER").toString()
				                      +"&exp_month="+dataCharge.get(0,"EXP_MONTH").toString()
				                      +"&exp_year="+dataCharge.get(0,"EXP_YEAR").toString()
				                      +"&cvv="+dataCharge.get(0,"CVV").toString()
				                      +"&name="+dataCharge.get(0,"NAME").toString()
				                      +"&address="+dataCharge.get(0,"ADDRESS").toString()
				                      +"&postal_code="+dataCharge.get(0,"POSTAL_CODE").toString()
				                      +"&email="+ (dataCharge.get(0,"EMAIL") == null ? "" : dataCharge.get(0,"EMAIL").toString());
				                       
				
				
				// Send post request
				con.setDoOutput(true);
				DataOutputStream wr = new DataOutputStream(con.getOutputStream());
				wr.writeBytes(urlParameters);
				wr.flush();
				wr.close();
				//para el log webService
				entrada = urlParameters;
				
				int responseCode = con.getResponseCode();
				
				if(responseCode == 200){

					BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
					String inputLine;
					response = new StringBuffer();

					while ((inputLine = in.readLine()) != null) {
						response.append(inputLine);
					}
					in.close(); 
					
					Gson gson = new Gson();
					BandwireResponse bwr =  gson.fromJson(response.toString(), BandwireResponse.class);				
					System.err.println(bwr.getError());

					//Si existe un error
					if(null != bwr.getError()){

						//Obtenmos el Codigo de Error
						String errorCode = "";
						try{
							errorCode = bwr.getError().substring(bwr.getError().lastIndexOf(":")+1, bwr.getError().indexOf("(")).trim();
						}catch(Exception e){}
						
						respuesta = Util.setResponse((errorCode.isEmpty() || errorCode.length() > 2 ? "-1" : errorCode ),bwr.getError(), null);
						
						/***
						 * Envia Alertqa si el Codigo el negativo , eso nos indica que el servicio esta caido
						 */
						if(!errorCode.isEmpty() ){
							
							Integer code = null;
							try {
								code = Integer.parseInt(errorCode);
								
								if(code < 0){
									Util.sendAlerta(sqlQueryExecutor, 40l, "");
								}
							} catch (Exception e) {
								// TODO: handle exception
							}
							
						}

					}else{//Si todo salido Ok

						Map<String, Object> success = new LinkedHashMap<String, Object>();

						success.put("autorizacion", bwr.getPayment().getAuth_code());
						success.put("Stoken", bwr.getToken());
						success.put("idTransaction", bwr.getPayment().getId_transaction());


						ALQueryResult data = new ALQueryResult();
						data.setMapa(success);

						respuesta = Util.setResponse("0","Suscripcion Agregada Correctamente", data);
					}
				}else{
					respuesta = Util.setResponse("-1","Error de Comunicacion:"+responseCode , null);
				}
				
				
			}catch(Exception e){
				e.printStackTrace();
				respuesta =  Util.setResponse("-1","Error de Comunicacion:"+e.getMessage() , null);				
			}finally{
				//if(null != sc){sc.close();}
				if(null != inputStream){
					try {
						inputStream.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						respuesta = Util.setResponse("-1","Error de Comunicacion:"+e.getMessage() , null);
					}
				}
				if(null != outputStream){
					try {
						outputStream.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						respuesta = Util.setResponse("-1","Error de Comunicacion:"+e.getMessage() , null);
					}
				}
			}
			
		}else{
			respuesta = Util.setResponse("-1", WsErrors.WS_ERROR_18, null);
		}
		
		//Insertamos el Low de Web service
		Util.insertLogWs(sqlQueryExecutor, idLogWebService, idWebService, entrada, (response == null ?  "" : response.toString() ) + new Gson().toJson(respuesta), folio, inicioTransaccion, null);
		
		
		return respuesta;
	}

	
	/***
	 * METODO QUE CANCELA UNA SUSCRIPTION EN BANWIRE
	 * @param folio
	 * @param idWebService
	 * @param token
	 * @return
	 */
	public ResponseWS cancelSubscription(Long folio, Long idWebService,
			String token)  throws SystemException, SQLException {
		ResponseWS respuesta = new ResponseWS();
		String entrada = "";
		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");			
		String inicioTransaccion = format.format(new Date());
		StringBuffer response  = null;

		InputStream inputStream  = null;
		OutputStream outputStream = null;


		//Obtenemos los datos del WS = 42
		sqlQueryExecutor.addParameter("idWebService", idWebService); //dicWebService = 42 
		ALQueryResult dataWs = sqlQueryExecutor.executeQuery(QuerysDB.GET_WS_DATA_CONSUME_CHARGE);

		//obtenemos el idLogWebService
		Long idLogWebService = (Long)sqlQueryExecutor.executeQuery(QuerysDB.GET_IDLODWEBSERVICE).get(0,"IDLOGWEBSERVICE");


		try{

			URL obj = new URL(dataWs.get(0,"URL").toString());
			HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

			//add reuqest header
			con.setRequestMethod("POST");
			con.setRequestProperty("User-Agent", "Mozilla/5.0");
			con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

			String urlParameters = "method=cancel"
					+"&user="+dataWs.get(0,"LLAVE").toString() 
					+"&token="+token;

			// Send post request
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(urlParameters);
			wr.flush();
			wr.close();
			//para el log webService
			entrada = urlParameters;

			int responseCode = con.getResponseCode();

			if(responseCode == 200){

				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String inputLine;
				response = new StringBuffer();

				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close(); 

				Gson gson = new Gson();
				BandwireResponse bwr =  gson.fromJson(response.toString(), BandwireResponse.class);				
				System.err.println(response.toString());

				//Si existe un error
				if(null != bwr.getError()){

					//Obtenmos el Codigo de Error
					String errorCode = "";
					try{
						errorCode = bwr.getError().substring(bwr.getError().lastIndexOf(":")+1, bwr.getError().indexOf("(")).trim();
					}catch(Exception e){}

					respuesta = Util.setResponse((errorCode.isEmpty() || errorCode.length() > 2 ? "-1" : errorCode ),bwr.getError(), null);

					/***
					 * Envia Alerta si el Codigo el negativo , eso nos indica que el servicio esta caido
					 */
					if(!errorCode.isEmpty() ){

						Integer code = null;
						try {
							code = Integer.parseInt(errorCode);

							if(code < 0){
								Util.sendAlerta(sqlQueryExecutor, 40l, "");
							}
						} catch (Exception e) {
							// TODO: handle exception
						}

					}

				}else{//Si todo salido Ok

				
					respuesta = Util.setResponse("0","Suscripcion Cancelada Correctamente", new ALQueryResult());
				}
			}else{
				respuesta = Util.setResponse("-1","Error de Comunicacion:"+responseCode , null);
			}


		}catch(Exception e){
			e.printStackTrace();
			respuesta =  Util.setResponse("-1","Error de Comunicacion:"+e.getMessage() , null);				
		}finally{
			//if(null != sc){sc.close();}
			if(null != inputStream){
				try {
					inputStream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					respuesta = Util.setResponse("-1","Error de Comunicacion:"+e.getMessage() , null);
				}
			}
			if(null != outputStream){
				try {
					outputStream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					respuesta = Util.setResponse("-1","Error de Comunicacion:"+e.getMessage() , null);
				}
			}
		}


		//Insertamos el Low de Web service
		Util.insertLogWs(sqlQueryExecutor, idLogWebService, idWebService, entrada, (response == null ?  "" : response.toString() ) + new Gson().toJson(respuesta), folio, inicioTransaccion, null);

		return respuesta;
	}

}