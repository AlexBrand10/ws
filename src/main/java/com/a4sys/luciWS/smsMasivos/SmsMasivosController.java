package com.a4sys.luciWS.smsMasivos;

import com.a4sys.luciWS.domain.RespuestaSMSMasivos;
import com.a4sys.luciWS.util.Util;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by mmorones on 5/17/17.
 */

@Path("/smsmasivos")
public class SmsMasivosController {


    @GET
    @Path("/callback")
    public Response respuestaSMSDosVias(
            @QueryParam("referenciaid") String referenciaId,
            @QueryParam("destinatario") String destinatario,
            @QueryParam("mensaje") String mensaje   ,
            @QueryParam("respuesta") String respuesta,
            @QueryParam("fechaenvio") String fechaEnvio,
            @QueryParam("fecharespuesta") String fechaRespuesta

    ) {

        System.out.println("SMSMASIVOS_RESPUSTA:"+ referenciaId + "_"+destinatario +"_"+mensaje+"_"+respuesta+"_"+fechaEnvio+"_"+fechaRespuesta);
        System.err.println("SMSMASIVOS_RESPUSTA:"+ referenciaId + "_"+destinatario +"_"+mensaje+"_"+respuesta+"_"+fechaEnvio+"_"+fechaRespuesta);

        String respuestaService = "";
        Response.ResponseBuilder response = null;
        RespuestaSMSMasivos respuestaSMSMasivos =
                new RespuestaSMSMasivos(referenciaId, destinatario, mensaje, respuesta, fechaEnvio,fechaRespuesta );
        if(null != respuestaSMSMasivos.getReferenciaId()) {

            SmsMasivosService service = new SmsMasivosService();
            try {
                respuestaService = service.respuestaSMSDosVias(respuestaSMSMasivos);
            } catch (Exception e) {

            } finally {
                service.closeService();
            }

        }else{
            respuestaService = "OK";
        }

        response = Response.ok(respuestaService).type(MediaType.TEXT_PLAIN+ ";charset=utf-8");
        return response.build();
    }

}
