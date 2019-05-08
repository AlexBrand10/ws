package com.a4sys.luciWS.consulta;

import com.a4sys.core.dao.ALQueryResult;
import com.a4sys.core.dao.DaoService;
import com.a4sys.core.exceptions.SystemException;
import com.a4sys.luciWS.domain.ClientValue;
import com.a4sys.luciWS.util.QuerysDB;
import com.a4sys.luciWS.util.SystemConstants;
import com.a4sys.luciWS.util.WsErrors;

import java.sql.SQLException;

public class ConsultaClienteInfoService extends DaoService{

    public ConsultaClienteInfoService(){
        super(SystemConstants.JDNI);
    }


    /*
     * Método que autentica a un usuario en LUCI
     *
     * @param user
     *
     * @param password
     *
     * @return
     */
    public boolean isValidUser(String user, String password) {
        boolean isValid = true;
        WsErrors error = new WsErrors();

        Long idUser = 0L;

        ALQueryResult res = new ALQueryResult();

        try {

            sqlQueryExecutor.addParameter("user", user);
            sqlQueryExecutor.addParameter("password", password);
            res = sqlQueryExecutor.executeQuery(QuerysDB.IS_VALID_USER);
            idUser = Long.parseLong(res.get(0, "IDUSUARIO").toString());

        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (idUser == 0L) {
            isValid = false;
        }

        return isValid;

    }



    /*
     * Método que devuelve informacion de un cliente
     *
     * @param folio
     *
     * @return
     */
    public String getInfoClient(String folio) {

        String respuesta = "";

        ALQueryResult res = new ALQueryResult();

        try {
            sqlQueryExecutor.autocommit(false);
            sqlQueryExecutor.addParameter("folio", folio);
            res = sqlQueryExecutor.executeQueryUsingQueryString(
                    "select s.folio as FOLIO,\n" +
                            "cli.nombre||' '||cli.apaterno||' '||cli.amaterno as NOMBRE\n" +
                            "from tblsolicitudes s\n" +
                            "join tblclientes cli on (cli.idcliente = s.idcliente)\n" +
                            "where folio = {folio} ");

            for (int i = 0; i <= 2; i++) {
                ClientValue client = new ClientValue();
                if (i == 0) {
                    client.setField("FOLIO");
                    client.setValue(!res.get(0, "FOLIO").equals("0")
                            ? res.get(0, "FOLIO").toString() : " ");
                    respuesta += client.toString() + ",";
                }
                if (i == 1) {
                    client.setField("NOMBRE");
                    client.setValue(
                            !res.get(0, "NOMBRE").equals("0") ? res.get(0, "NOMBRE").toString() : " ");
                    respuesta += client.toString() + ",";
                }
            }


            res = sqlQueryExecutor.executeQueryUsingQueryString("select idformalizacionllamada\n" +
                    "from (\n" +
                    "select idformalizacionllamada,\n" +
                    "RANK() OVER (PARTITION BY folio ORDER BY fecha desc ) as num \n" +
                    "from tblformalizacionllamada \n" +
                    "where folio = {folio}\n" +
                    ")\n" +
                    "where num = 1");

            if(res.size() > 0){
                String idFormalizacionLlamada = res.get(0, "IDFORMALIZACIONLLAMADA").toString();

                sqlQueryExecutor.addParameter("idFormalizacionLlamada", idFormalizacionLlamada);
                res = sqlQueryExecutor.executeQueryUsingQueryString("select fr.respuesta as FECHACITA,\n" +
                        "pv.elementoprefvisita as VISITASUCURSAL\n" +
                        "from tblformalizacionrespuestas fr\n" +
                        "join dicelementosprefvisita pv on (pv.idelementoprefvisita = fr.idelementoprefvisita)\n" +
                        "where fr.idformalizacionllamada = {idFormalizacionLlamada} and fr.idcampodu = 14 and fr.respuesta is not null");

                ClientValue client = new ClientValue();
                client.setField("FECHACITA");
                client.setValue(res.get(0, "FECHACITA").toString());
                respuesta += client.toString() + ",";


                ClientValue clientpref = new ClientValue();
                clientpref.setField("VISITASUCURSAL");
                clientpref.setValue(res.get(0, "VISITASUCURSAL").toString());
                respuesta += clientpref.toString();

            }else{
                ClientValue client = new ClientValue();
                client.setField("FECHACITA");
                client.setValue("");
                respuesta += client.toString() + ",";


                ClientValue clientpref = new ClientValue();
                clientpref.setField("VISITASUCURSAL");
                clientpref.setValue("");
                respuesta += clientpref.toString();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return respuesta;

    }


    /*
     * Método que valida que un folio exista en el sistema LUCI
     *
     * @param folio
     *
     * @return
     */
    public boolean folioExist(String folio) {

        boolean existe = false;

        ALQueryResult res = new ALQueryResult();

        try {
            sqlQueryExecutor.addParameter("folio", folio);
            res = sqlQueryExecutor.executeQueryUsingQueryString(
                    "select case when count(*) >= 1 then 'S' else 'N' end EXIST from tblsolicitudes where folio = {folio} ");

            if (res.get(0, "EXIST").toString().equals("S")) {
                existe = true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return existe;

    }

    /***
     * Método que inserta un log de un web service consumido
     *
     * @param idLogWebService
     * @param jsonSalida
     * @param folio
     */

    public Integer updateLogWebService(String jsonSalida, String folio, Long idLogWebService) {
        int respuestaWS = 0;

        ALQueryResult res = new ALQueryResult();

        try {

            sqlQueryExecutor.addParameter("jsonSalida", jsonSalida.toString());
            sqlQueryExecutor.addParameter("folio", (folio.length() > 0L) ? folio.toString() : null);
            sqlQueryExecutor.addParameter("idLogWebService", idLogWebService.toString());
            respuestaWS = sqlQueryExecutor.executeUpdate(QuerysDB.UPDATE_LOG_WS);
            sqlQueryExecutor.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return respuestaWS;
    }


    /***
     * Método que inserta un log de un web service consumido
     *
     * @param idLogWebService
     * @param jsonSalida
     */

    public Integer updateLogWebService(String jsonSalida, Long idLogWebService) {
        int respuestaWS = 0;
        ALQueryResult res = new ALQueryResult();

        try {

            sqlQueryExecutor.addParameter("jsonSalida", jsonSalida.toString());
            sqlQueryExecutor.addParameter("idLogWebService", idLogWebService.toString());
            respuestaWS = sqlQueryExecutor.executeUpdate(QuerysDB.UPDATE_LOG_GENERIC_WS);
            sqlQueryExecutor.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return respuestaWS;
    }

    /***
     * Método que inserta un log de un web service consumido
     *
     * @param jsonEntrada
     * @throws SQLException
     * @throws SystemException
     */

    public Long insertLogWebService(String jsonEntrada) throws SystemException, SQLException {
        Long idLogWebService = 0L;
        ALQueryResult getIdLogWebService = sqlQueryExecutor.executeQuery(QuerysDB.GET_ID_LOG_WS);

        if (getIdLogWebService.size() > 0 && getIdLogWebService != null) {
            idLogWebService = Long.parseLong(getIdLogWebService.get(0, "IDLOGWEBSERVICE").toString());

            ALQueryResult res = new ALQueryResult();

            try {

                sqlQueryExecutor.addParameter("jsonEntrada", jsonEntrada.toString());
                sqlQueryExecutor.addParameter("idLogWebService", idLogWebService.toString());
                sqlQueryExecutor.executeUpdate(QuerysDB.INSERT_LOG_WS_CLIENTINFO);
                sqlQueryExecutor.commit();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return idLogWebService;
    }

    public void commit() {
        try {
            sqlQueryExecutor.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

}
