package com.a4sys.luciWS.amazonSMS;

import com.a4sys.luciWS.domain.ResponseWS;
import com.a4sys.luciWS.util.Util;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by mmorones on 4/18/18.
 */
@Path("/amazonSMS")
public class AmazonSMSController {

    @POST
    @Path("/send")
    public Response sendSMS(AmazonSMSRequest amazonSMSRequest) {

        Response.ResponseBuilder response = null;
        AmazonSMSService service = new AmazonSMSService();

        try {
            ResponseWS output = new ResponseWS();
            output = service.send(amazonSMSRequest);
            response = Response.ok(output).type(MediaType.APPLICATION_JSON + ";charset=utf-8");
        } catch (Exception e) {
            response = Response.ok(Util.setResponseJson("-1", e.getCause() == null ? e.getMessage() : e.getCause().toString(), null), MediaType.APPLICATION_JSON + ";charset=utf-8");
        } finally {
            service.closeService();
        }

        return response.build();

    }
}
