package com.a4sys.luciWS.utilWS;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import com.a4sys.core.dao.ALQueryResult;
import com.a4sys.core.dao.DaoService;
import com.a4sys.core.dao.SQLQueryExecutor;
import com.a4sys.luciWS.domain.RequestGenericEmail;
import com.a4sys.luciWS.domain.RequestGenericWs;
import com.a4sys.luciWS.domain.ResponseGenericWs;
import com.a4sys.luciWS.domain.ResponseWS;
import com.a4sys.luciWS.util.Util;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

/***
 * 
 * @author Martin Morones
 *
 */
@Path("/util")
public class WSUitl {


	/***
	 * METODO QUE OBTIENE LAS COLONIAS APARTIR DE UN CODIGO POSTAL
	 * @param cp
	 * @return
	 */
	@GET	
	@Path("/getSuburbs")	
	public Response Proasit(@QueryParam("cp") String cp){ 
		ResponseBuilder response = null ;        	

		try {
			if(null != cp){

				String entity = null;		
				ResponseWS output = new ResponseWS();
				WSUtilService service = new WSUtilService();
				ALQueryResult res =  service.getSuburbs(cp);


				if( res.size()!= 0){
					//Existen Datos
					output = Util.setResponse("0", "Ok.", res);
				}else{
					//No existen datos para mostrar de domicilio de correspondecia
					output = Util.setResponse("4", "No se Encontran Datos", null);
				}

				entity = Util.toJson(output);				
				response = Response.ok(entity).type(MediaType.APPLICATION_JSON+ ";charset=utf-8");				
			}else{
				response = Response.ok(Util.setResponseJson("1", "No se recibieron todos los parámetros completos para la petición", null),MediaType.APPLICATION_JSON + ";charset=utf-8");			
			}
		} catch (Exception e) {
			response = Response.ok(Util.setResponseJson("-1", e.getCause().toString(), null),MediaType.APPLICATION_JSON+ ";charset=utf-8");
		}
		
		response.header("Access-Control-Allow-Origin", "*");		
		return response.build();
	}

	@POST
	@Path("/sendGenericMail")
	public Response sendGenericMail(RequestGenericEmail requestGenericEmail){
		Response.ResponseBuilder response = null;
		ResponseWS output = new ResponseWS();
		WSUtilService service = new WSUtilService();

		if(service.sendGenericMail(requestGenericEmail)){
			output = Util.setResponse("0", "El correo se envió con éxito", null);
		}else{
			output = Util.setResponse("1", "No se pudo enviar el correo.", null);
		}

		service.closeService();
		response = Response.ok(Util.toJson(output), MediaType.APPLICATION_JSON + ";charset=utf-8");
		return response.build();
	}


	@POST
	@Path("/sendFileExitus")
	public Response sendFileExitus(){
		Response.ResponseBuilder response = null;
		ResponseWS output = new ResponseWS();
		WSUtilService service = new WSUtilService();
		String respuesta = service.	sendFileExitus();

		if(!respuesta.contains("~")){
			output = Util.setResponse("0", respuesta, null);
		}else{
			output = Util.setResponse("1", respuesta, null);
		}

		service.closeService();
		response = Response.ok(Util.toJson(output), MediaType.APPLICATION_JSON + ";charset=utf-8");
		return response.build();
	}


	@POST
	@Path("/consumeGenericWs")
	public Response consumeGenericWs(RequestGenericWs requestGenericWs){
		Response.ResponseBuilder response = null;
		ResponseGenericWs responseGenericWs=new ResponseGenericWs();
		WSUtilService service = new WSUtilService();
		responseGenericWs=service.consumeGenericWs(requestGenericWs);
		service.closeService();
		response = Response.ok(Util.toJson(responseGenericWs), MediaType.APPLICATION_JSON + ";charset=utf-8");
		return response.build();
	}

}
