package com.a4sys.luciWS.ipcom;


import ch.qos.logback.core.net.SyslogOutputStream;
import com.a4sys.luciWS.domain.IpcomRequest;
import com.a4sys.luciWS.domain.RequestGenericWs;
import com.a4sys.luciWS.domain.ResponseGenericWs;
import com.a4sys.luciWS.domain.ResponseWS;
import com.a4sys.luciWS.util.Util;
import com.a4sys.luciWS.utilWS.WSUtilService;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/***
 * Martin Morones
 * 14/08/2018
 */
@Path("/ipcom")
public class IpcomController {


    /***
     *  Metodo que consume el Web service de  agent_event del API de IPCOM de reportes
      * @param ipcomRequest
     * @return
     */
    @POST
    @Path("/reports")
    public Response agentEvents(IpcomRequest ipcomRequest) {
        Response.ResponseBuilder response = null;
        IpcomService service  = new IpcomService();
        ResponseWS output = null;
        try{
            output =  service.consumeWsIpcom(ipcomRequest);
        }catch (Exception e){
            output= Util.setResponse("-1", e.getCause().toString(), null);
        }finally {
            service.closeService();
        }
        response = Response.ok(Util.toJson(output)).type(MediaType.APPLICATION_JSON+ ";charset=utf-8");
        return response.build();
    }

    @POST
    @Path("/loadRecordToFtp")
    public Response loadRecordToFtp(IpcomRequest ipcomRequest){
        Response.ResponseBuilder response = null;
        IpcomService ipcomService  = new IpcomService();
        ResponseWS output = null;
         try{
             output=ipcomService.procesaAudio(ipcomRequest);
         }catch (Exception e){

         }finally {
             ipcomService.closeService();
         }
        response = Response.ok(Util.toJson(output), MediaType.APPLICATION_JSON + ";charset=utf-8");
        return response.build();
    }


    @POST
    @Path("/massiveDnc")
    public Response massiveDnc(IpcomRequest ipcomRequest) {
        Response.ResponseBuilder response = null;
        IpcomService service  = new IpcomService();
        ResponseWS output = null;
        try{
            output =  service.massiveDnc(ipcomRequest);
        }catch (Exception e){
            output= Util.setResponse("-1", e.getCause().toString(), null);
        }finally {
            service.closeService();
        }
        response = Response.ok(Util.toJson(output)).type(MediaType.APPLICATION_JSON+ ";charset=utf-8");
        return response.build();
    }


    @POST
    @Path("/renameAndRelocateAudios")
    public Response renameAndRelocateAudios(RequestGenericWs requestGenericWs){
        Response.ResponseBuilder response = null;
        IpcomService ipcomService  = new IpcomService();
        ResponseWS output = null;
        try{
            output=ipcomService.renameAndRelocateAudios(requestGenericWs);
        }catch (Exception e){
            System.out.println(e.getMessage());
        }finally {
            ipcomService.closeService();
        }
        response = Response.ok(Util.toJson(output), MediaType.APPLICATION_JSON + ";charset=utf-8");
        return response.build();
    }


}
