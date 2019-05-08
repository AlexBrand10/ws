package com.a4sys.luciWS.smsMasivos;

import com.a4sys.core.dao.ALQueryResult;
import com.a4sys.core.dao.DaoService;
import com.a4sys.core.exceptions.SystemException;
import com.a4sys.luciWS.domain.DataGSM;
import com.a4sys.luciWS.domain.NotificacionGSM;
import com.a4sys.luciWS.domain.RespuestaSMSMasivos;
import com.a4sys.luciWS.util.QuerysDB;
import com.a4sys.luciWS.util.SystemConstants;
import com.a4sys.luciWS.util.Util;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by mmorones on 5/17/17.
 */
public class SmsMasivosService extends DaoService{

    public SmsMasivosService(){
        super(SystemConstants.JDNI);
    }

    /***
     * Meotodo que Interpreta la Respuesta del SMS de dos Vias
     * @param respuestaSMSMasivos
     * @return
     */
    public String respuestaSMSDosVias( RespuestaSMSMasivos respuestaSMSMasivos ) {

        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String inicioTransaccion = format.format(new Date());
        String output = "";

        //Obtenemos el folio de la respuesa
        String folio = Util.getFolioFromResponse(respuestaSMSMasivos.getMensaje());



        //Guardamos la Repuesta
        try {
            sqlQueryExecutor.autocommit(false);
            sqlQueryExecutor.addParameter("folio",folio);
            sqlQueryExecutor.addParameter("respuesta",respuestaSMSMasivos.getRespuesta());
            sqlQueryExecutor.addParameter("fechaRespuesta",respuestaSMSMasivos.getFechaRespuesta());
            sqlQueryExecutor.addParameter("idProveedorSms", 4L); //SMSMASIVOS

            sqlQueryExecutor.executeUpdate(QuerysDB.INSERT_TBLLOGRESPUESTASSMS);
            sqlQueryExecutor.commit();


            //Si obtenemos folio procedemos a envia el push a la app
            if (null != folio && !"".equals(respuestaSMSMasivos.getRespuesta()) ) {

                //Enviamos la Notificacion a la App
                ALQueryResult datosMensajero = new ALQueryResult();

                int resultadoRespueta =  analizarRespuesta(respuestaSMSMasivos.getRespuesta());

                if(resultadoRespueta == 1 || resultadoRespueta == 2) {

                    datosMensajero = sqlQueryExecutor.executeQuery(QuerysDB.GET_TOKENPUSHAPP);

                    if (datosMensajero.size() > 0) {
                        //obtemos el Token Push App apartir del folio

                        DataGSM dataGSM = new DataGSM();
                        dataGSM.setFolio(new Long(folio));
                        dataGSM.setIdTypeMessage(resultadoRespueta == 1 ? 1L : 2l); // tipo 1 = Notificacion de que cliente confirmo que le recojan doctos. 2= No Dispoinible
                        dataGSM.setMessage(resultadoRespueta == 1 ? "El Cliente con No. de Folio :" + folio + " Esta Listo para efectuar la recolección" : "");
                        NotificacionGSM notificacionGSM = new NotificacionGSM();
                        notificacionGSM.setData(dataGSM);
                        notificacionGSM.setTo(datosMensajero.get(0, "TOKENPUSHAPP").toString());

                        // Mandamos una notificacion al app de mensajeria
                        Util.sendPushNotificationGSM(sqlQueryExecutor, 71l, notificacionGSM);

                        //Se actualiza campo en tblSolcitiudesMensajeriaApp donde se confirma que el cliente esta listo para
                        //su recoleccion
                        String confirmaRecolecion = resultadoRespueta == 1 ? "S" : "N";
                        Util.insertConfirmacionRecoleccion(sqlQueryExecutor, folio,confirmaRecolecion);
                    }
                }

            }
        }catch (Exception e){
            e.printStackTrace();
            output = e.getCause().toString();
        }


        //Insertamos el Low de Web service
        Long idLogWebService;
        Long idWebService = 71l;
        try {
            //obtenemos el idLogWebService
            idLogWebService = (Long)sqlQueryExecutor.executeQuery(QuerysDB.GET_IDLODWEBSERVICE).get(0,"IDLOGWEBSERVICE");
            Util.insertLogWs(sqlQueryExecutor, idLogWebService,  idWebService, respuestaSMSMasivos.toString() , output, (null == folio ? null : new Long(folio)), inicioTransaccion, null);
        } catch (SystemException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


        return "OK";
    }

    private int analizarRespuesta(String respuesta) {

        int resultado = 0;
        respuesta = " ".concat(respuesta).concat(" ").toUpperCase();

        if(respuesta.contains(" 1 ") || respuesta.contains(" SI ")){
            resultado = 1;
        }else if(respuesta.contains(" 2 ") || respuesta.contains(" NO ")){
            resultado = 2;
        }

        return  resultado;

    }


}
