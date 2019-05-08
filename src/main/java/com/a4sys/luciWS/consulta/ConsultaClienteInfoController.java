package com.a4sys.luciWS.consulta;

import com.a4sys.core.dao.ALQueryResult;
import com.a4sys.luciWS.domain.ClientInfo;
import com.a4sys.luciWS.util.WsErrors;
import org.apache.commons.lang3.StringUtils;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/clients")
public class ConsultaClienteInfoController {


    /***
     * Método que obtiene la información del cliente
     * @return ClientInfo
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/getClientInfo")
    public Response getClientInfo(ClientInfo clientInfo){
        WsErrors error = new WsErrors();
        Response.ResponseBuilder response = null;
        ConsultaClienteInfoService service = new ConsultaClienteInfoService();


        try{
            // se registra un log con el JSON de entrada
            Long idLogWebService = service.insertLogWebService(clientInfo.toString());
            clientInfo.setIdLog(idLogWebService);


            String user = clientInfo.getUser().toString().trim();
            String password = clientInfo.getPassword().toString().trim();
            String folio = clientInfo.getFolio().toString().trim();

            boolean isValidUser = service.isValidUser(user, password);
            if (isValidUser) {

                if (!StringUtils.isNumeric(folio.toString())) {//folio no es un número.
                    clientInfo.setMessage(error.WS_S5_MSG_2);

                    service.updateLogWebService(clientInfo.getRespuesta(), clientInfo.getIdLog());
                    service.commit();


                    return Response.ok(clientInfo.getRespuesta(),MediaType.APPLICATION_JSON).build();

                }

                if (!service.folioExist(folio.toString())) { //folio no existe en el sistema
                    clientInfo.setMessage(error.WS_S5_MSG_3);

                    service.updateLogWebService(clientInfo.getRespuesta(), clientInfo.getIdLog());
                    service.commit();


                    return Response.ok(clientInfo.getRespuesta(),MediaType.APPLICATION_JSON).build();

                }

                //obtenemos la info del cliente by folio
                clientInfo.setMessage("OK");
                clientInfo.setDetail(service.getInfoClient(folio));
                service.updateLogWebService(clientInfo.getRespuesta(), clientInfo.getFolio(), clientInfo.getIdLog());
                service.commit();
            }else { //usuario y/o contraseña invalidos.
                clientInfo.setMessage(error.WS_S5_MSG_4);

                service.updateLogWebService(clientInfo.getRespuesta(), clientInfo.getIdLog());
                service.commit();


                return Response.ok(clientInfo.getRespuesta(),MediaType.APPLICATION_JSON).build();
            }

        } catch (Exception e) {
            e.printStackTrace();


            clientInfo.setMessage(error.WS_S5_MSG_1);

            service.updateLogWebService(clientInfo.getMessage(), clientInfo.getIdLog());
            service.commit();


            return Response.ok(clientInfo.getMessage(),MediaType.APPLICATION_JSON).build();

        }finally{
            service.closeService();
        }




        return Response.ok(clientInfo.getRespuesta(), MediaType.APPLICATION_JSON).build();


    }
}
