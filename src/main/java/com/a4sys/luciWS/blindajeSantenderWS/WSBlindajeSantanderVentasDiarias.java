package com.a4sys.luciWS.blindajeSantenderWS;

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
@Path("/santander")
public class WSBlindajeSantanderVentasDiarias {
	
	
	/***
	 * METODO QUE RESIBE LA PETICION Y LA ENRUTA PARA SU PROCESAMIENTO
	 * @param method
	 * @param idCampana
	 * @param idUsuarioResponsable
	 * @return
	 */
	@GET
	@Path("/{method}") 
	public  Response blindajeSantanderGet(@PathParam("method") String method,
			@QueryParam("idCampana") String idCampana,
			@QueryParam("idUsuarioResponsable") Long idUsuarioResponsable,
			@QueryParam("fechaInicial") String fechaInicial,
			@QueryParam("fechaFinal") String fechaFinal){ 
	
		ResponseBuilder response = null;        
		
		try {
			if(method.equals("ventasDiariasBlindaje")){
				response = ventasDiarias(idCampana, idUsuarioResponsable,fechaInicial,fechaFinal);
			}else{
				response = Response.ok(Util.setResponseJson("9", WsErrors.WS_ERROR_9, null),MediaType.APPLICATION_JSON+ ";charset=utf-8");			
			}
		} catch (Exception e) {
			response = Response.ok(Util.setResponseJson("-1", e.getCause() == null ? e.getMessage() : e.getCause().toString(), null),MediaType.APPLICATION_JSON+ ";charset=utf-8");
		}	
		
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
	public  Response blindajeSantanderPost(@PathParam("method") String method,
			@QueryParam("idCampana") String idCampana,
			@QueryParam("idUsuarioResponsable") Long idUsuarioResponsable,
			@QueryParam("fechaInicial") String fechaInicial,
			@QueryParam("fechaFinal") String fechaFinal){  
	
		ResponseBuilder response = null;        
		
		try {
			if(method.equals("ventasDiariasBlindaje")){
				response = ventasDiarias(idCampana, idUsuarioResponsable,fechaInicial,fechaFinal);
			}else{
				response = Response.ok(Util.setResponseJson("9", WsErrors.WS_ERROR_9, null),MediaType.APPLICATION_JSON+ ";charset=utf-8");			
			}
		} catch (Exception e) {
			response = Response.ok(Util.setResponseJson("-1", e.getCause() == null ? e.getMessage() : e.getCause().toString(), null),MediaType.APPLICATION_JSON+ ";charset=utf-8");
		}	
		
		
		return response.build();
	}
	

	
	/****
	 * METODO QUE MANDA LLAMAR EL SERVICIO DE DONDE  SE CREA LA REMESA JUNTO CON EL ARCHIVO Y SE GUARDA EN EL SFTP
	 * @param idCampana
	 * @param idUsuarioResponsable
	 * @return
	 */
	private ResponseBuilder ventasDiarias(String idCampana,Long idUsuarioResponsable, String fechaInicial, String fechaFinal) {
		ResponseWS output = new ResponseWS();
		ResponseBuilder response = null;
		String entity = null;
		WSBlindajeSantanderVentasDiariasService service = null;

		//Incializamos variable con mensaje De error
		output = Util.setResponse("1", WsErrors.WS_ERROR_1, null);

		try {

			if(null != idCampana && null != idUsuarioResponsable ){

				service = new WSBlindajeSantanderVentasDiariasService();
				String respuesta = service.ventasDiarias(idCampana, idUsuarioResponsable,fechaInicial,fechaFinal);
				output = Util.setResponse(respuesta.split(",")[0], respuesta.split(",")[1] , null);
				service.closeService();
			}

		} catch (Exception e) {
			// TODO: handle exception
			output= Util.setResponse("-1", e.getCause().toString(), null);
			if(service != null)
				service.closeService();
		}

		entity = Util.toJson(output);
		response = Response.ok(entity).type(MediaType.APPLICATION_JSON+ ";charset=utf-8");
		response.header("Access-Control-Allow-Origin", "*");
		return response;
	}

}
