package com.a4sys.luciWS.proasistWS;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import com.a4sys.core.dao.ALQueryResult;
import com.a4sys.luciWS.domain.ResponseWS;
import com.a4sys.luciWS.util.Util;
import com.a4sys.luciWS.util.WsErrors;

/***
 * 
 * @author Martin Morones
 *
 */
@Path("/proasist")
public class WSProasist {

	/***
	 * METODO QUE DIRECCIONA  LOS DISTINTOS METODOS QUE TIENE EL WEB SERVICE DE PROASIST POR GET
	 * @param method
	 * @param idConfirmacion
	 * @param claveAutenticacion
	 * @return
	 */
	@GET
	@Path("/{method}") 			
	public  Response ProasitGet(@PathParam("method") String method,
			@QueryParam("idConfirmacion") Long idConfirmacion,
			@QueryParam("claveAutenticacion") String claveAutenticacion,
			@QueryParam("data") String data){ 

		ResponseBuilder response = null;        

		try {
			if(method.equals("getDataAddress")){
				response = getDataAddress(idConfirmacion, claveAutenticacion);
			}else if(method.equals("saveDataAddress")){
				response = saveDataAddress(data);
			}else if(method.equals("confirmNotification")){
				response = confirmNotification(idConfirmacion, claveAutenticacion);
			}else{
				response = Response.ok(Util.setResponseJson("9", WsErrors.WS_ERROR_9, null),MediaType.APPLICATION_JSON+ ";charset=utf-8");			
			}
		} catch (Exception e) {
			response = Response.ok(Util.setResponseJson("-1", e.getCause() == null ? e.getMessage() : e.getCause().toString(), null),MediaType.APPLICATION_JSON+ ";charset=utf-8");
		}	



		return response.build();
	}
	
	/***
	 * METODO QUE DIRECCIONA  LOS DISTINTOS METODOS QUE TIENE EL WEB SERVICE DE PROASIST POR POST
	 * @param method
	 * @param idConfirmacion
	 * @param claveAutenticacion
	 * @return
	 */
	
	@POST
	@Path("/{method}") 			
	public  Response ProasitPost(@PathParam("method") String method,
			@QueryParam("idConfirmacion") Long idConfirmacion,
			@QueryParam("claveAutenticacion") String claveAutenticacion,
			@QueryParam("data") String data){ 

		ResponseBuilder response = null;        

		try {
			if(method.equals("getDataAddress")){
				response = getDataAddress(idConfirmacion, claveAutenticacion);
			}else if(method.equals("saveDataAddress")){
				response = saveDataAddress(data);
			}else if(method.equals("confirmNotification")){
				response = confirmNotification(idConfirmacion, claveAutenticacion);
			}else{
				response = Response.ok(Util.setResponseJson("9", WsErrors.WS_ERROR_9, null),MediaType.APPLICATION_JSON+ ";charset=utf-8");			
			}
		} catch (Exception e) {
			response = Response.ok(Util.setResponseJson("-1", e.getCause() == null ? e.getMessage() : e.getCause().toString(), null),MediaType.APPLICATION_JSON+ ";charset=utf-8");
		}	



		return response.build();
	}


	/***
	 * METODO QUE VERIFICA SI EL TOKEN ES VALIDO Y CONFIRMA 
	 * DEL TOKEN
	 * @param idConfirmacion
	 * @param claveAuteticacion
	 * @return
	 */
	private  ResponseBuilder confirmNotification(Long idConfirmacion ,String claveAutenticacion){

		ResponseWS output = new ResponseWS();
		ResponseBuilder response = null;
		String entity = null;

		//Incializamos variable con mensaje De error
		output = Util.setResponse("1", WsErrors.WS_ERROR_1, null);

		try {
			// solo si tiene todos los parametros necesarios continuamos con el proceso
			if(null != idConfirmacion && null != claveAutenticacion ){

				WSProasistService service = new WSProasistService();
				String respuesta = service.cofirmNotification(idConfirmacion, claveAutenticacion) ;
				output = Util.setResponse(respuesta.split(",")[0], respuesta.split(",")[1] , null);
				service.closeService();
			}

		} catch (Exception e) {
			// TODO: handle exception
			output= Util.setResponse("-1", e.getCause().toString(), null);
		}

		entity = Util.toJson(output);
		response = Response.ok(entity).type(MediaType.APPLICATION_JSON+ ";charset=utf-8");
		response.header("Access-Control-Allow-Origin", "*");
		return response;
	}

	/***
	 * METODO QUE ACTUALIZA EL DOMICILIO DE CORRESPONDENCIA
	 * @param data
	 * @return
	 */
	private  ResponseBuilder saveDataAddress(String json) {
		ResponseWS output = new ResponseWS();
		ResponseBuilder response = null;
		String entity = null;

		//Incializamos variable con mensaje De error
		output = Util.setResponse("1", WsErrors.WS_ERROR_1, null);
		try {
			// solo si tiene todos los parametros necesarios continuamos con el proceso
			if(null != json ){
				
				WSProasistService service = new WSProasistService();
				String isSaved = service.saveDataAddress(json);
				if( "".equals(isSaved) ){
					output = Util.setResponse("0", WsErrors.WS_ERROR_0, null);
				}else{
					// puede retornar un codigo de error 2 = para saber que el token no es valido  y  3 = para indicar que el token perdio vigencia
					output = Util.setResponse(isSaved.split(",")[0], isSaved.split(",")[1] , null);
				}
				service.closeService();
			}

		} catch (Exception e) {
			// TODO: handle exception
			output= Util.setResponse("-1", e.getCause() == null ? e.getMessage() : e.getCause().toString(), null);
		}

		entity = Util.toJson(output);
		response = Response.ok(entity).type(MediaType.APPLICATION_JSON+ ";charset=utf-8");
		response.header("Access-Control-Allow-Origin", "*");
		return response;
	}



	/***
	 * METODO QUE OBTIEN LA DIRECCION DE CORRESPONDENCIA AL MISMO TIEMPO QUE VALIDA VIGENCIA 
	 * DEL TOKEN
	 * @param idConfirmacion
	 * @param claveAuteticacion
	 * @return
	 */
	private  ResponseBuilder getDataAddress(Long idConfirmacion ,String claveAutenticacion){

		ResponseWS output = new ResponseWS();
		ResponseBuilder response = null;
		String entity = null;

		//Incializamos variable con mensaje De error
		output = Util.setResponse("1", WsErrors.WS_ERROR_1, null);

		try {
			// solo si tiene todos los parametros necesarios continuamos con el proceso
			if(null != idConfirmacion && null != claveAutenticacion ){

				WSProasistService service = new WSProasistService();
				String tokenValido = service.isValidToken(idConfirmacion, claveAutenticacion); 
				if( "".equals(tokenValido) ){
					ALQueryResult res =  service.getDataAddress(idConfirmacion);

					if( res.size()!= 0){
						//Existen Datos
						output = Util.setResponse("0", WsErrors.WS_ERROR_0, res);
					}else{
						//No existen datos para mostrar de domicilio de correspondecia
						output = Util.setResponse("4", WsErrors.WS_ERROR_4, null);
					}

				}else{
					// puede retornar un codigo de error 2 = para saber que el token no es valido  y  3 = para indicar que el token perdio vigencia
					output = Util.setResponse(tokenValido.split(",")[0], tokenValido.split(",")[1] , null);
				}
				service.closeService();
			}

		} catch (Exception e) {
			// TODO: handle exception
			output= Util.setResponse("-1", e.getCause().toString(), null);
		}

		entity = Util.toJson(output);
		response = Response.ok(entity).type(MediaType.APPLICATION_JSON+ ";charset=utf-8");
		response.header("Access-Control-Allow-Origin", "*");
		return response;
	}


	/*List<DataProasistAddress> list = new ArrayList<DataProasistAddress>();

	DataProasistAddress data = new DataProasistAddress();
	data.setFolio(1l);
	data.setCalle("Falsa 123");
	list.add(data);

	data = new DataProasistAddress();
	data.setFolio(2l);
	data.setCalle("NAda 123");
	list.add(data);


	//GenericEntity<List<DataProasistAddress>> entity = new GenericEntity<List<DataProasistAddress>>(list){};
	GenericEntity<DataProasistAddress> entity = new GenericEntity<DataProasistAddress>(data){};
	//GenericEntity<ALQueryResult> entity = new GenericEntity<ALQueryResult>(res){};
	 */

}