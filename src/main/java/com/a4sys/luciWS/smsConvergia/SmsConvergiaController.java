package com.a4sys.luciWS.smsConvergia;

import com.a4sys.luciWS.util.Util;
import com.a4sys.luciWS.util.WsErrors;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/send")
public class SmsConvergiaController {

    @GET
    @Path("/sms")
    public Response smsConvergia(@QueryParam("cellPhone") String cellPhone, @QueryParam("message") String message, @QueryParam("idWebService") String idWebServices){
        Response.ResponseBuilder response = null;
        SmsConvergiaService service = new SmsConvergiaService();
        try {
            String respuestaService = service.sendMessage(cellPhone, message, idWebServices);
            response = Response.ok(respuestaService).type(MediaType.TEXT_PLAIN + ";charset=utf-8");
        }catch (Exception e){
        }finally {
            service.closeService();
        }

        return response.build();
    }
}
