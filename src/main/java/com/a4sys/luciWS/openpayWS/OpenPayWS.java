package com.a4sys.luciWS.openpayWS;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import com.a4sys.luciWS.domain.ResponseWS;
import com.a4sys.luciWS.util.Util;
import com.a4sys.luciWS.util.WsErrors;



/***
 * 
 * @author Martin Morones
 *
 */

@Path("/openPay")
public class OpenPayWS {
	

	/***
	 * METODO QUE RESIBE LA PETICION Y LA ENRUTA PARA SU PROCESAMIENTO
	 * @param method
	 * @param idCampana
	 * @param idUsuarioResponsable
	 * @return
	 */
	@GET
	@Path("/{method}") 
	public  Response manager(@PathParam("method") String method,
			@QueryParam("folio")Long folio,
			@QueryParam("idClienteTdc") Long idClienteTdc,			
			@QueryParam("esPreAutorizacion") String esPreAutorizacion,			
			@QueryParam("idPreAutorizacion") String idPreAutorizacion,
			@QueryParam("idWebService") Long idWebService
			){ 
	
		ResponseBuilder response = null;        
		
		try {
			if(method.equals("charge")){
				response = charge(folio,idClienteTdc,esPreAutorizacion,idWebService);
			}else if(method.equals("preCharge")){
				response = preCharge(folio,idPreAutorizacion);
			}else{
				response = Response.ok(Util.setResponseJson("9", WsErrors.WS_ERROR_9, null),MediaType.APPLICATION_JSON+ ";charset=utf-8");			
			}
		} catch (Exception e) {
			response = Response.ok(Util.setResponseJson("-1", e.getCause() == null ? e.getMessage() : e.getCause().toString(), null),MediaType.APPLICATION_JSON+ ";charset=utf-8");
		}	
		
		return response.build();
	}

	/****
	 * 
	 * @param folio
	 * @param idPreAutorizacion
	 * @return
	 */
	private ResponseBuilder preCharge(Long folio, String idPreAutorizacion) {
		ResponseWS output = new ResponseWS();
		ResponseBuilder response = null;
		String entity = null;
		OpenPayWSService service = null;

		//Incializamos variable con mensaje De error
		output = Util.setResponse("1", WsErrors.WS_ERROR_1, null);

		try {
			service = new OpenPayWSService();
			output = service.preCharge(folio,idPreAutorizacion);						
		} catch (Exception e) {			
			output= Util.setResponse("-1", e.getCause().toString(), null);			
		}finally{
			service.closeService();
		}

		entity = Util.toJson(output);
		response = Response.ok(entity).type(MediaType.APPLICATION_JSON+ ";charset=utf-8");
		response.header("Access-Control-Allow-Origin", "*");
		return response;
		
	}


	/***
	 * Metodo que manda consumir sevicion de cargo para una tarjeta de credito nueva
	 * @param folio
	 * @param idClienteTdc
	 * @param esPreAutorizacion 
	 * @param idWebService 
	 * @return
	 */
	private ResponseBuilder charge(Long folio, Long idClienteTdc, String esPreAutorizacion, Long idWebService) {
		ResponseWS output = new ResponseWS();
		ResponseBuilder response = null;
		String entity = null;
		OpenPayWSService service = null;

		//Incializamos variable con mensaje De error
		output = Util.setResponse("1", WsErrors.WS_ERROR_1, null);

		try {
			service = new OpenPayWSService();
			output = service.charge(folio,idClienteTdc,esPreAutorizacion,idWebService);						
		} catch (Exception e) {			
			output= Util.setResponse("-1", e.getCause().toString(), null);			
		}finally{
			service.closeService();
		}

		entity = Util.toJson(output);
		response = Response.ok(entity).type(MediaType.APPLICATION_JSON+ ";charset=utf-8");
		response.header("Access-Control-Allow-Origin", "*");
		return response;
	}
	
	
	
}
