package com.a4sys.luciWS.banorteWS;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import com.a4sys.luciWS.domain.ResponseWS;
import com.a4sys.luciWS.util.Util;
import com.a4sys.luciWS.util.WsErrors;

@Path("/banorte")
public class BanorteWS {
	
	/***
	 * Método que ingresa el registro de llamadas para la venta de banorte
	 * @param folioFur
	 * @param telefonoContacto
	 * @param idLlamada
	 * @param gradoEnojo
	 * @param calificacion
	 * @param etiquetaGrabacion
	 * @param fechaLlamada
	 * @return response
	 */
	@GET
	@Path("/{method}") 
	public  Response BnaGet(@PathParam("method") String method,
			@QueryParam("folioFur") String folioFur,
			@QueryParam("telefonoContacto") String telefonoContacto,
			@QueryParam("idLlamada") String idLlamada,
			@QueryParam("gradoEnojo") String gradoEnojo,
			@QueryParam("calificacion") String calificacion,
			@QueryParam("etiquetaGrabacion") String etiquetaGrabacion,
			@QueryParam("fechaLlamada") String fechaLlamada
			){ 
	
		ResponseBuilder response = null;        
		
		try {
			if(method.equals("insertRecordCall")){
				response = insertRecordCall(folioFur, telefonoContacto, idLlamada, gradoEnojo, calificacion, etiquetaGrabacion, fechaLlamada);
								
			}else{
				response = Response.ok(Util.setResponseJson("9", WsErrors.WS_ERROR_9, null),MediaType.APPLICATION_JSON+ ";charset=utf-8");
				
			}
		} catch (Exception e) {
			response = Response.ok(Util.setResponseJson("-1", e.getCause() == null ? e.getMessage() : e.getCause().toString(), null),MediaType.APPLICATION_JSON+ ";charset=utf-8");
		}	
		
		return response.build();
	}
	
	
	/***
	 * Método que llama al service para el registro de llamadas para la venta de banorte
	 * @param folioFur
	 * @param telefonoContacto
	 * @param idLlamada
	 * @param gradoEnojo
	 * @param calificacion
	 * @param etiquetaGrabacion
	 * @param fechaLlamada
	 * @return response
	 */
	private ResponseBuilder insertRecordCall(String folioFur, String telefonoContacto, String idLlamada, String gradoEnojo, String calificacion, 
			String etiquetaGrabacion, String fechaLlamada){
		ResponseWS output = new ResponseWS();
		ResponseBuilder response = null;
		String entity = null;
		BanorteWSService service = null;
		
		//Incializamos variable con mensaje De error
		output = Util.setResponse("1", WsErrors.WS_ERROR_1, null);
		
		try{
			if(folioFur.length() <= 11){
		
				if(folioFur != null && telefonoContacto != null && idLlamada != null && calificacion != null && fechaLlamada != null ){
					service = new BanorteWSService();
					String folioLuci= service.getFolioByIdExternoSolicitud(folioFur);
					if(!folioLuci.equals("0")){
						String respuesta = service.insertRecorCall(folioLuci, folioFur, telefonoContacto, idLlamada, gradoEnojo, calificacion, etiquetaGrabacion, fechaLlamada);
						output = Util.setResponse(respuesta.split(",")[0], respuesta.split(",")[1] , null);
						service.closeService();
					}else{
						output = Util.setResponse("17", WsErrors.WS_ERROR_17, null);
					}
					
				}
			}else{
				output = Util.setResponse("16", WsErrors.WS_ERROR_16, null);
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
