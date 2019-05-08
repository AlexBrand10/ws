package com.a4sys.luciWS.test;
import java.util.ArrayList;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import com.google.gson.Gson;

/***
 * 
 * @author Martin Morones
 *
 */
@Path("/test")
public class Test {
	
	
	@GET
	@Path("/get")
	//@Produces("application/json")
	public Response getProductInJSON() {

		
		ResponseBuilder response = null;
		Solicitud product = new Solicitud();
		product.setUsuario("iPad 3");
		product.setContrasena("999");
		ArrayList<Campo> campo = new ArrayList<Campo>();
		Campo c = new Campo();
		c.setNombre("sadas");
		c.setId(1);
		campo.add(c);
		Campo c2 = new Campo();
		c2.setNombre("sadas");
		c2.setId(2);
		campo.add(c2);
		product.setCampos(campo);
		
		String entity = new Gson().toJson(product);
		response = Response.ok(entity).type(MediaType.APPLICATION_JSON+ ";charset=utf-8");
		//response.header("Access-Control-Allow-Origin", "*");
		
		return response.build();
	}
	
	
	@POST
	@Path("/post")
	//@Consumes("application/json")
	public Response createProductInJSON(String sol) {

		Solicitud soli = new Gson().fromJson(sol, Solicitud.class);
		String result = "Product created : " + soli.getContrasena();
		return Response.status(201).entity(result).build();
		
	}
		
	
	
	
}