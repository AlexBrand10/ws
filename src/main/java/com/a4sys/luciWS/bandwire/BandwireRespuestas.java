package com.a4sys.luciWS.bandwire;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;

import com.a4sys.luciWS.domain.ResponseWS;
import com.a4sys.luciWS.util.Util;
import com.a4sys.luciWS.util.WsErrors;
import com.google.gson.Gson;


/****
 * 
 * @author Martin Morones 
 *
 */


@Path("/bandwire")
public class BandwireRespuestas {

	
	@Context
    Request request;
	
	/***
	 * METODO QUE RESIBE LA PETICION Y LA ENRUTA PARA SU PROCESAMIENTO
	 * @param method
	 * @param idCampana
	 * @param idUsuarioResponsable
	 * @return
	 */
	@POST
	@Path("/responses")
	@Consumes("application/x-www-form-urlencoded")
	public  Response respuesta(
			@FormParam("payment[id]") String paymentId,
			@FormParam("payment[auth_code]") String paymentAuthCode,
			@FormParam("payment[reference]") String paymentReference,
			@FormParam("payment[description]") String paymentDescription,
			@FormParam("payment[amount]") String paymentAmount,
			@FormParam("error[code]") String errorCode,
			@FormParam("error[message]") String errorMessage,
			@FormParam("error[detail]") String errorDetail,			
			@FormParam("status") String status,
			@FormParam("date") String date,
			@FormParam("id") String id,
			@FormParam("id_card") String idCard,
			@FormParam("auth_code") String authCode,
			@FormParam("last_digits") String lastDigits,
			@FormParam("token") String token,
			@FormParam("amount") String amount,
			@FormParam("reference") String reference,
			@FormParam("concept") String concept,
			@FormParam("email") String email,
			@FormParam("card_owner") String cardOwner,
			@FormParam("remaining_payments") String remainingPayments,
			@FormParam("next_payment") String nextPayment,
			@FormParam("token_plan") String tokenPlan
			) {
	     
		
		NotificacionBanwire notificacion = new NotificacionBanwire(paymentId, paymentAuthCode, paymentReference, paymentDescription, paymentAmount, errorCode, errorMessage, errorDetail, status, date, id, idCard, authCode, lastDigits, token, amount, reference, concept, email, cardOwner, remainingPayments, nextPayment, tokenPlan);
		
		ResponseBuilder response = null;        
		
		try {
			response = respuestas(notificacion);
		} catch (Exception e) {
			response = Response.ok(Util.setResponseJson("-1", e.getCause() == null ? e.getMessage() : e.getCause().toString(), null),MediaType.APPLICATION_JSON+ ";charset=utf-8");
		}
		response = Response.ok(new Gson().toJson(notificacion) ,MediaType.APPLICATION_JSON+ ";charset=utf-8");
		
		return response.build();
		
		
		
	}
	/***
	 * METODO QUE RESIBE LA PETICION Y LA ENRUTA PARA SU PROCESAMIENTO
	 * @param method
	 * @param idCampana
	 * @param idUsuarioResponsable
	 * @return
	 */
	@POST
	@Path("/{method}") 
	public  Response manager(
			@PathParam("method") String method,			
			@QueryParam("folio") Long folio,
			@QueryParam("idClienteTdc") Long idClienteTdc,
			@QueryParam("idWebService") Long idWebService,
			@QueryParam("token") String token
			){ 
	
		ResponseBuilder response = null;        
		
		try {
		    if(method.equals("subscription")){
				response = addSubscription(folio,idClienteTdc,idWebService);
			}else if(method.equals("cancelSubscription")){
				response = cancelSubscription(folio,idWebService,token);
			}else{
				response = Response.ok(Util.setResponseJson("9", WsErrors.WS_ERROR_9, null),MediaType.APPLICATION_JSON+ ";charset=utf-8");			
			}
		} catch (Exception e) {
			response = Response.ok(Util.setResponseJson("-1", e.getCause() == null ? e.getMessage() : e.getCause().toString(), null),MediaType.APPLICATION_JSON+ ";charset=utf-8");
		}	
		
		return response.build();
	}
	
	

	/***
	 * Metodo que guada en la base da datos las respuestas de los cobros hechos en via bandwire	
	 * @param event
	 * @param status
	 * @param authCode
	 * @param reference
	 * @param id
	 * @param hash
	 * @param total
	 * @param token
	 * @param cancelUrl
	 * @return
	 */
	private ResponseBuilder respuestas(NotificacionBanwire notificacion) {
		
		ResponseWS output = new ResponseWS();
		ResponseBuilder response = null;
		String entity = null;
		BandwireRespuestasService service = null;

		//Incializamos variable con mensaje De error
		output = Util.setResponse("1", WsErrors.WS_ERROR_1, null);

		try {
			service = new BandwireRespuestasService();
			output = service.saveResponse(notificacion);						
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
	 * Metodo que consume el apli de bandwiere , el metodo de Agregar Suscripción en particular
	 * @param folio
	 * @param idClienteTdc
	 * @param idWebService 
	 * @return
	 */
	private ResponseBuilder addSubscription(Long folio, Long idClienteTdc, Long idWebService) {
		
		ResponseWS output = new ResponseWS();
		ResponseBuilder response = null;
		String entity = null;
		BandwireRespuestasService service = null;

		//Incializamos variable con mensaje De error
		output = Util.setResponse("1", WsErrors.WS_ERROR_1, null);

		try {
			service = new BandwireRespuestasService();
			output = service.addSubscription(folio, idClienteTdc,idWebService);						
		} catch (Exception e) {		
			e.printStackTrace();
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
	 * METODO QUE MANDA CANCELAR UNA SUSCRIPTION EN BANWIRE
	 * @param folio
	 * @param idWebService
	 * @param token
	 * @return
	 */
	private ResponseBuilder cancelSubscription(Long folio, Long idWebService,
			String token) {
		ResponseWS output = new ResponseWS();
		ResponseBuilder response = null;
		String entity = null;
		BandwireRespuestasService service = null;

		//Incializamos variable con mensaje De error
		output = Util.setResponse("1", WsErrors.WS_ERROR_1, null);

		try {
			service = new BandwireRespuestasService();
			output = service.cancelSubscription(folio,idWebService,token);						
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
