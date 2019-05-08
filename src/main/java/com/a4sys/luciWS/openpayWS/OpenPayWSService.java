package com.a4sys.luciWS.openpayWS;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
/*
import mx.openpay.client.Card;
import mx.openpay.client.Charge;
import mx.openpay.client.Customer;
import mx.openpay.client.core.OpenpayAPI;
import mx.openpay.client.core.requests.transactions.ConfirmCaptureParams;
import mx.openpay.client.core.requests.transactions.CreateCardChargeParams;
import mx.openpay.client.enums.Currency;
import mx.openpay.client.exceptions.OpenpayServiceException;
import mx.openpay.client.exceptions.ServiceUnavailableException;
*/
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
 * Clase servicion para consumir los web service de OpenPay
 * @author Martin Morones
 *
 */

public class OpenPayWSService extends DaoService{
	
	public OpenPayWSService(){
		super(SystemConstants.JDNI);
	}
	
	
	/***
	 * Clase que realiza un cargo a una tarjeta nueva usando openpay
	 * @param folio
	 * @param idClienteTdc
	 * @param idUsuarioResponsable
	 * @param esPreAutorizacion 
	 * @param idWebService 
	 * @return
	 * @throws SQLException 
	 * @throws SystemException 
	 */
	public ResponseWS charge(Long folio, Long idClienteTdc, String esPreAutorizacion, Long idWebService) throws SystemException, SQLException {
		ResponseWS respuesta = new ResponseWS();
		/*String entrada = "";
		
		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");			
		String inicioTransaccion = format.format(new Date());
		
		//Obtenermos los datos del Cliente y de TDC				
		sqlQueryExecutor.addParameter("folio", folio);
		sqlQueryExecutor.addParameter("idClienteTdc", idClienteTdc);
		ALQueryResult dataCharge =  sqlQueryExecutor.executeQuery(QuerysDB.GET_OPENPAY_CHARGE_DATA);
		
		//Obtenemos los datos del 
		sqlQueryExecutor.addParameter("idWebService", idWebService); 
		ALQueryResult dataWs = sqlQueryExecutor.executeQuery(QuerysDB.GET_WS_DATA_CONSUME_CHARGE);
		
		//obtenemos el idLogWebService
		Long idLogWebService = (Long)sqlQueryExecutor.executeQuery(QuerysDB.GET_IDLODWEBSERVICE).get(0,"IDLOGWEBSERVICE");
		
		if(dataCharge.size() != 0){
			
			OpenpayAPI api = new OpenpayAPI(dataWs.get(0,"URL").toString(), dataWs.get(0,"LLAVE").toString(), dataWs.get(0,"ID").toString());
			CreateCardChargeParams request = new CreateCardChargeParams();
			Customer customer = new Customer();
			customer.setName(dataCharge.get(0,"NAME").toString());
			customer.setLastName(dataCharge.get(0,"LASTNAME").toString());
			customer.setPhoneNumber(dataCharge.get(0,"PHONENUMBER").toString());
			customer.setEmail(dataCharge.get(0,"EMAIL").toString());

			Card card = new Card();
			card.holderName(dataCharge.get(0,"HOLDERNAME").toString());
			card.cardNumber(dataCharge.get(0,"CARDNUMBER").toString());  //Para ambiente de pruebas se puede utilizar estos numeros http://www.openpay.mx/docs/testing.html
			//card.cvv2(dataCharge.get(0,"CVV2").toString());
			card.expirationMonth(Integer.parseInt(dataCharge.get(0,"EXPIRATIONMONTH").toString()));
			card.expirationYear(Integer.parseInt(dataCharge.get(0,"EXPIRATIONYEAR").toString()));
			
			if (esPreAutorizacion.toUpperCase().equals("S")){
				request.capture(false);
			}
			
			request.amount(new BigDecimal(dataCharge.get(0,"AMOUNT").toString()));
			request.currency(Currency.MXN);
			request.description("Cargo a TDC via Luci CallCenter");

			request.card(card);
			request.orderId(idLogWebService.toString()); //campo opcional para identificar el numero de orden usualmente el id en tu sistema
			//request.deviceSessionId("kR1MiQhz2otdIuUlQkbEyitIqVMiI16f"); // no se necesario para nuestro usuario
			request.customer(customer);
			
			entrada = new Gson().toJson(request);
			
			try {
				Charge charge = api.charges().create(request);
				Map<String, Object> success = new LinkedHashMap<String, Object>();
				
				success.put("idPreAutorizacion", charge.getId());
				success.put("autorizacion", charge.getAuthorization());
				
				ALQueryResult data = new ALQueryResult();
				data.setMapa(success);
				respuesta = Util.setResponse("0", "Cobro Exitoso", data);
			} catch (OpenpayServiceException e) {
				//Error procesando la transaccion
				//Puedes consultar los codigos de error en http://www.openpay.mx/docs/errors.html
				e.printStackTrace();
				respuesta = Util.setResponse(e.getErrorCode().toString(), e.getDescription(), null);
			} catch (ServiceUnavailableException e) {
				//Servicio no disponble
				e.printStackTrace();
				respuesta = Util.setResponse("-1", "El Serviion de OpenPay no esta Online", null);
			}
			
					
		}else{
			respuesta = Util.setResponse("-1", WsErrors.WS_ERROR_18, null);
		}
		
		//Insertamos el Low de Web service
		Util.insertLogWs(sqlQueryExecutor, idLogWebService, 30l, entrada, new Gson().toJson(respuesta), folio, inicioTransaccion);
		*/
		
		return respuesta;
	}


	/***
	 * 	
	 * @param folio
	 * @param idPreAutorizacion
	 * @return
	 * @throws SQLException 
	 * @throws SystemException 
	 */
	public ResponseWS preCharge(Long folio, String idPreAutorizacion) throws SystemException, SQLException {

		ResponseWS respuesta = new ResponseWS();
		String entrada = "";

		/*SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		String inicioTransaccion = format.format(new Date());

		//Obtenemos los datos del WS = 30
		sqlQueryExecutor.addParameter("idWebService", 30l); //dicWebService = 30
		ALQueryResult dataWs = sqlQueryExecutor.executeQuery(QuerysDB.GET_WS_DATA_CONSUME_CHARGE);

		//obtenemos el idLogWebService
		Long idLogWebService = (Long)sqlQueryExecutor.executeQuery(QuerysDB.GET_IDLODWEBSERVICE).get(0,"IDLOGWEBSERVICE");
		
		//Confirmación del cargo
		OpenpayAPI api = new OpenpayAPI(dataWs.get(0,"URL").toString(), dataWs.get(0,"LLAVE").toString(), dataWs.get(0,"ID").toString());				
		ConfirmCaptureParams confirmCaptureParams = new ConfirmCaptureParams();
		confirmCaptureParams.chargeId(idPreAutorizacion);
		//confirmCaptureParams.amount(amount); cantidad si es diferente a la original 
		
		entrada = new Gson().toJson(confirmCaptureParams);
		
		try {
			Charge charge = api.charges().confirmCapture(confirmCaptureParams);
			Map<String, Object> success = new LinkedHashMap<String, Object>();
			
			success.put("idPreAutorizacion", charge.getId());
			success.put("autorizacion", charge.getAuthorization());
			
			ALQueryResult data = new ALQueryResult();
			data.setMapa(success);
			respuesta = Util.setResponse("0", "Cobro Exitoso", data);
			
		} catch (OpenpayServiceException e) {
			//Error procesando la confirmación 
			//Con esta accion se pueden lanzar los errores generales que se ncuentran en: http://www.openpay.mx/docs/errors.html y, 
			//1005, "The requested resource doesn't exist"
			//3002, "The card has expired"
			e.printStackTrace();			
			respuesta = Util.setResponse(e.getErrorCode().toString(), e.getDescription(), null);
		} catch (ServiceUnavailableException e) {
			//Servicio no disponble
			e.printStackTrace();
			respuesta = Util.setResponse("-1", "El Serviion de OpenPay no esta Online", null);

		}	

		//Insertamos el Low de Web service
		Util.insertLogWs(sqlQueryExecutor, idLogWebService, 30l, entrada, new Gson().toJson(respuesta), folio, inicioTransaccion);

	   */
		return respuesta;

	}

}
