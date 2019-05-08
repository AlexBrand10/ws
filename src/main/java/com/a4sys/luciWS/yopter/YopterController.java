package com.a4sys.luciWS.yopter;

import com.a4sys.luciWS.domain.ResponseWS;
import com.a4sys.luciWS.util.Util;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


/**
 * Created by mmorones on 5/14/18.
 */
@Path("/yopter")
public class YopterController {


    /***
     * metodo que verifica si un usuario esta en luci
     * @param yopterRequest
     * @return
     */
    @POST
    @Path("/authLuci")
    public Response login(YopterRequest yopterRequest) {

        Response.ResponseBuilder response = null;
        YopterService service = new YopterService();

        YopterResponse yopterResponse = new YopterResponse();
        try {
            yopterResponse = service.login(yopterRequest);
        } catch (Exception e) {
            yopterResponse.setIdUser(-1);
            yopterResponse.setMessage("Error: " + e.getMessage());
        } finally {
            service.closeService();
        }
        response = Response.ok(yopterResponse, MediaType.APPLICATION_JSON + ";charset=utf-8");
        return response.build();
    }


    /***
     * metodo que guarda o actualiza un comercio
     * @param commerceRequest
     * @return
     */
    @POST
    @Path("/addCommerce")
    public Response addCommerce(CommerceRequest commerceRequest, @HeaderParam("key") String key) {
        Response.ResponseBuilder response = null;
        YopterService service = new YopterService();
        if (service.isKeyValid(key)) {
            if (service.addOrUpdateCommerce(commerceRequest)) {
                response = Response.ok();
            } else {
                response = Response.status(Response.Status.NOT_ACCEPTABLE);
            }
        } else {
            response = Response.status(Response.Status.UNAUTHORIZED);
        }
        service.closeService();
        return response.build();


    }

    /**
     * Registra el pago en caso de existir el comercio
     *
     * @param yopterRequest
     * @param key
     * @return response
     */
    @POST
    @Path("/confirmPayment")
    public Response confirmPayment(YopterRequest yopterRequest, @HeaderParam("key") String key) {
        Response.ResponseBuilder response = null;
        YopterService service = new YopterService();
        if (service.isKeyValid(key)) {
            if (service.registerPayment(yopterRequest)) {
                response = Response.ok();
            } else {
                response = Response.status(Response.Status.NOT_ACCEPTABLE);
            }
        } else {
            response = Response.status(Response.Status.UNAUTHORIZED);
        }
        service.closeService();
        return response.build();
    }

    /**
     * metodo que obtiene los comercios por medio del id usuario Luci
     * @param idLuciUser
     * @return
     */
    @GET
    @Path("/getProspects")
    public Response get(@QueryParam("idLuciUser") String idLuciUser) {
        Response.ResponseBuilder response = null;
        ResponseWS output = new ResponseWS();
        YopterService service = new YopterService();

        if(service.existCommerceByIdUser(idLuciUser)){
                output = Util.setResponse("1", "", service.getCommerceByIdUser(idLuciUser));
        }else{
                output = Util.setResponse("0", "El dato idLuciUser no tiene asociado comercios.", null);
        }
        service.closeService();
        response = Response.ok(Util.toJson(output), MediaType.APPLICATION_JSON + ";charset=utf-8");
        return response.build();
    }
}
