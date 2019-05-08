package com.a4sys.luciWS.facturaDigital;

import com.a4sys.luciWS.domain.ResponseWS;
import com.a4sys.luciWS.facturaDigital.model.DatosFactura;
import com.a4sys.luciWS.util.Util;
import com.google.gson.Gson;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by mmorones on 5/29/18.
 */

@Path("/facturaDigital")
public class FacturaDigitalController {


    @POST
    @Path("/generarFactura")
    public Response generarFactura(@FormParam("folio") Long folio,@FormParam("idUsuario") Long idUsuario){
        Response.ResponseBuilder response = null;
        FacturaDigitalService service  = new FacturaDigitalService();
        ResponseWS output ;

        try{
             output =  service.generarFacturaOrNotaVenta(folio,idUsuario);
            //output =  service.generaFacturaPublico(folio,idUsuario);
        }catch (Exception e){
            output= Util.setResponse("-1", e.getCause().toString(), null);
        }finally {
            service.closeService();
        }

        response = Response.ok(Util.toJson(output)).type(MediaType.APPLICATION_JSON+ ";charset=utf-8");
        return response.build();
    }

}
