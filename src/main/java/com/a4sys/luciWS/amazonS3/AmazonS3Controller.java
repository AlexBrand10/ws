package com.a4sys.luciWS.amazonS3;

import com.a4sys.luciWS.domain.AmazonRequest;
import com.a4sys.luciWS.util.Util;
import com.a4sys.luciWS.util.WsErrors;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;

/**
 * Created by mmorones on 3/22/18.
 */
@Path("/amazonS3")
public class AmazonS3Controller {

    @GET
    @Path("/getObject")
    public Response getFileFromS3Amazon(@QueryParam("idWebService") String idWebService, @QueryParam("key") String key) {

        Response.ResponseBuilder response = null;
        AmazonS3Service service = new AmazonS3Service();

        try {
            File file  = service.getFileFromS3Amazon(idWebService,key);
            response = Response.ok((Object) file);
            response.header("Content-Disposition", "attachment; filename="+file.getName());
        } catch (Exception e) {
            response = Response.ok(Util.setResponseJson("-1", e.getCause() == null ? e.getMessage() : e.getCause().toString(), null), MediaType.APPLICATION_JSON + ";charset=utf-8");
        } finally {
            service.closeService();
        }

        return response.build();

    }

    @POST
    @Path("/getMassiveImages")
    public Response getMassiveImages(AmazonRequest amazonRequest) {
        String respuesta;
        Response.ResponseBuilder response = null;
        AmazonS3Service service = new AmazonS3Service();

        try {

            respuesta=service.getMassiveImages(amazonRequest);
            response = Response.ok(Util.setResponseJson("-0","OK", null), MediaType.APPLICATION_JSON + ";charset=utf-8");
        } catch (Exception e) {
            response = Response.ok(Util.setResponseJson("-1", e.getCause() == null ? e.getMessage() : e.getCause().toString(), null), MediaType.APPLICATION_JSON + ";charset=utf-8");
        } finally {
            service.closeService();
        }

        return response.build();

    }

}
