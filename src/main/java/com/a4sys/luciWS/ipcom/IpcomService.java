package com.a4sys.luciWS.ipcom;

import com.a4sys.core.dao.ALQueryResult;
import com.a4sys.core.dao.DaoService;
import com.a4sys.core.exceptions.SystemException;
import com.a4sys.luciWS.domain.IpcomRequest;
import com.a4sys.luciWS.domain.RequestGenericWs;
import com.a4sys.luciWS.domain.ResponseWS;
import com.a4sys.luciWS.util.QuerysDB;
import com.a4sys.luciWS.util.SystemConstants;
import com.a4sys.luciWS.util.Util;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileFilter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javax.ws.rs.core.MultivaluedMap;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import static com.a4sys.luciWS.util.Util.getValueFromOject;

public class IpcomService extends DaoService {

    private int CANTIDAD = 2000;
    private int FULLDETAIL = 5000;

    public IpcomService() {
        super(SystemConstants.JDNI);
    }


    /***
     * Consume un webservice  del api de reportes de ipcom
     * @param ipcomRequest
     * @Comment: Se modifico servicio para que acepatara la etiqueta action para los reportes de IPCOM
     * @return
     */
    public ResponseWS consumeWsIpcom(IpcomRequest ipcomRequest) {
        ResponseWS responseWS = null;
        String output = "";
        DateFormat inFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        inFormat.setTimeZone(TimeZone.getTimeZone("America/Mexico_City"));
        Date fechaHora = new Date();
        String inicioTransaccion = inFormat.format(fechaHora);
        Long idLogWebService = null;
        JSONParser parser = new JSONParser();
        try {
            MultivaluedMap params =  new MultivaluedMapImpl();
            if(null != ipcomRequest.getAction()){
                params.add("action",ipcomRequest.getAction());
            }else{
                params.add("report",ipcomRequest.getReport());
            }
            params.add("token",ipcomRequest.getToken());
            if(!Util.isEmpty(ipcomRequest.getDateIni())) {
                params.add("date_ini", ipcomRequest.getDateIni());
                params.add("date_end", ipcomRequest.getDateEnd());
            }

            long startTime = System.currentTimeMillis();
            Client client = Client.create();
            WebResource webResource = client.resource(ipcomRequest.getUrl()).queryParams(params);
            ClientResponse response = webResource.get(ClientResponse.class);
            output = response.getEntity(String.class);
            long endTime = System.currentTimeMillis();
            double tiempo = (double) ((endTime - startTime)/1000);
            System.out.println(tiempo +" tiempo en segundos en que tarda en consumir el api de ipcom...");
            System.out.println(tiempo +" tiempo en segundos en que tarda en consumir el api de ipcom...");
            //Revisamos si es JSon
            try{

                Object obj = parser.parse(output);
                JSONArray arregloJson =  (JSONArray) obj;
                idLogWebService = (Long)sqlQueryExecutor.executeQuery(QuerysDB.GET_IDLODWEBSERVICE).get(0,"IDLOGWEBSERVICE");
                saveJsonToBD(arregloJson,ipcomRequest.getIdWebService(),idLogWebService, ipcomRequest.getIdProveedor(),ipcomRequest.getIdTelefoniaCampanaCallCenter());
                output = "OK";

            }catch (Exception e ){
                // De otro modo es sepadado por algun carctes en este caso | pipe  y saldos de linea para cada registro
                String [] records = output.split("\\n");
                if(records.length > 0 ){
                    idLogWebService = (Long)sqlQueryExecutor.executeQuery(QuerysDB.GET_IDLODWEBSERVICE).get(0,"IDLOGWEBSERVICE");
                    saveCsvToBD(records,ipcomRequest.getIdWebService(),idLogWebService, ipcomRequest.getIdProveedor(),ipcomRequest.getIdTelefoniaCampanaCallCenter());
                    output = "OK";
                }

            }
            responseWS = Util.setResponse("0",output,null);
        } catch (Exception e) {
            e.printStackTrace();
            output = e.getCause().toString();
            responseWS = Util.setResponse("-1",output,null);

        }

        try {
            //obtenemos el idLogWebService
            Util.insertLogWs(sqlQueryExecutor, idLogWebService,  new Long(ipcomRequest.getIdWebService()), ipcomRequest.toString(), output, null, inicioTransaccion, null);
        } catch (SystemException e) {
            e.printStackTrace();
        }

        return responseWS;
    }

    /***
     * guarda en la base de datos los resultados que venga separados por algun caracter
     * @param records
     * @param idWebService
     * @param idProveedor
     */
    private void saveCsvToBD(String[] records, String idWebService, Long idLogWebService, String idProveedor,String idTelefoniaCampanaCallCenter) {
        if (idWebService.equals("89")) {// agent_events
            boolean flag=true;
            for (int i = 0; i < records.length; i++){
                String [] record = records[i].split("\\|");
                if (record.length > 1) {
                    try {
                        if(record[4].equals("TALK")) {
                            sqlQueryExecutor.addParameter("idLogWebService", idLogWebService);
                            sqlQueryExecutor.addParameter("idUser", record[1]);
                            sqlQueryExecutor.addParameter("nombre", record[2]);
                            sqlQueryExecutor.addParameter("estado", record[4]);
                            sqlQueryExecutor.addParameter("tiempo", record[5]);
                            sqlQueryExecutor.addParameter("fechaInicio", record[6]);
                            sqlQueryExecutor.addParameter("fechaFin", record[7]);
                            sqlQueryExecutor.addParameter("idRegistro", record[8]);
                            sqlQueryExecutor.addParameter("idProveedor", idProveedor);
                            sqlQueryExecutor.addParameter("idTelefoniaCampanaCallCenter", idTelefoniaCampanaCallCenter);

                            sqlQueryExecutor.executeUpdateUsingString("insert into TBLTELEFONIAREPAGENTEVENTS " +
                                    "(IDTELEFONIAREPAGENTEVENTS,IDLOGWEBSERVICE,IDUSER, NOMBRE, ESTADO , TIEMPO, FECHAINICIO, FECHAFIN, IDREGISTRO , IDPROVEEDOR,idTelefoniaCampanaCallCenter) " +
                                    "values (SECIDTELEFONIAREPAGENTEVENTS.nextval,{idLogWebService},{idUser},{nombre},{estado},{tiempo},to_date({fechaInicio},'yyyy-MM-dd HH24:mi:ss'),to_date({fechaFin},'yyyy-MM-dd HH24:mi:ss'),{idRegistro},{idProveedor},{idTelefoniaCampanaCallCenter})");
                            if(i%CANTIDAD==0) {
                                sqlQueryExecutor.commit();
                                flag=true;
                            }else{
                                flag=false;
                            }
                        }
                    } catch (SQLException e) {
                        // System.out.println("Error: Valor duplicado, existe registro con los mismos datos a ingresar. ---> " + e.getMessage());
                    }
                }
            }
            if(!flag){
                try {
                    sqlQueryExecutor.commit();
                } catch (SQLException e) {
                    System.out.println("Error: Ocurrio un problema al ejecutar commit. ---> " + e.getMessage());
                }
            }
        }

        if (idWebService.equals("105")) {// full detail Report
            long startTime = System.currentTimeMillis();
            boolean flag=true;
            for (int i = 0; i < records.length; i++){
                String [] record = records[i].split("\\|");
                if (1 < record.length) {
                    try {
                            sqlQueryExecutor.addParameter("idLogWebService", idLogWebService);
                            sqlQueryExecutor.addParameter("reg", record[0]);
                            sqlQueryExecutor.addParameter("idCampana", record[1]);
                            sqlQueryExecutor.addParameter("nombreCliente", record[2]);
                            sqlQueryExecutor.addParameter("apellidoCliente", record[3]);
                            sqlQueryExecutor.addParameter("tipoDocumento", record[4]);
                            sqlQueryExecutor.addParameter("idCliente", record[5]);
                            sqlQueryExecutor.addParameter("resultadoMaquina", record[6]);
                            sqlQueryExecutor.addParameter("telefono", record[7]);
                            sqlQueryExecutor.addParameter("fechaLlamada", record[8]);
                            sqlQueryExecutor.addParameter("idProveedor", idProveedor);
                            sqlQueryExecutor.addParameter("idTelefoniaCampanaCallCenter", idTelefoniaCampanaCallCenter);

                            sqlQueryExecutor.executeUpdateUsingString("insert into TBLTELEFONIAREPFULLDETAIL " +
                                    "(IDTELEFONIAREPFULLDETAIL,IDLOGWEBSERVICE,REG, IDCAMPANA, NOMBRE, APELLIDO, TIPODOC, IDCLIENTE, RESULTADOMAQUINA, TELEFONO, FECHALLAMADA , IDPROVEEDOR, PROCESADO, IDTELEFONIACAMPANACALLCENTER) " +
                                    "values (SECIDTELEFONIAREPFULLDETAIL.nextval,{idLogWebService},{reg},{idCampana},{nombreCliente},{apellidoCliente},{tipoDocumento},{idCliente},{resultadoMaquina},{telefono},to_date({fechaLlamada},'yyyy-MM-dd HH24:mi:ss'),{idProveedor},'N', {idTelefoniaCampanaCallCenter})");
                        if(i%FULLDETAIL==0) {
                            sqlQueryExecutor.commit();
                            flag=true;
                        }else{
                            flag=false;
                        }
                    } catch (SQLException e) {
                      // System.out.println("Error: Valor duplicado, existe registro con los mismos datos a ingresar. ---> " + e.getMessage());
                    }
                }
            }
            if(!flag){
                try {
                    sqlQueryExecutor.commit();
                } catch (SQLException e) {
                    System.out.println("Error: Ocurrio un problema al ejecutar commit. ---> " + e.getMessage());
                }
            }
            long endTime = System.currentTimeMillis();
            double tiempo = (double) ((endTime - startTime)/1000);
            System.out.println(tiempo +" tiempo en segundos en que tarda en inserta los registros los registros consumidos");
            System.out.println(tiempo +" tiempo en segundos en que tarda en inserta los registros los registros consumidos");
        }

    }

    /***
     * guarda en la base de datos los resultados que vengan el Json
     * @param arregloJson
     * @param idWebService
     * @param idProveedor
     */
    private void saveJsonToBD(JSONArray arregloJson, String idWebService, Long idLogWebService, String idProveedor,String idTelefoniaCampanaCallCenter) {
        if(idWebService.equals("92")) {// Login time
            boolean flag=true;
            for (int i = 0 ; i  < arregloJson.size(); i++){
                JSONObject record = (JSONObject)arregloJson.get(i);
                try {
                    sqlQueryExecutor.addParameter("idLogWebService", idLogWebService);
                    sqlQueryExecutor.addParameter("fechaRegistro", record.get("date"));
                    sqlQueryExecutor.addParameter("idUser", record.get("agent"));
                    sqlQueryExecutor.addParameter("loginDate", record.get("login_date"));
                    sqlQueryExecutor.addParameter("logoutDate", record.get("logout_date"));
                    sqlQueryExecutor.addParameter("loginTime", record.get("login_time"));
                    sqlQueryExecutor.addParameter("idProveedor", idProveedor);
                    sqlQueryExecutor.addParameter("idTelefoniaCampanaCallCenter", idTelefoniaCampanaCallCenter);


                    sqlQueryExecutor.executeUpdateUsingString("insert into TBLTELEFONIAREPLOGINTIME " +
                            "(IDTELEFONIAREPLOGINTIME,IDLOGWEBSERVICE,FECHAREGISTRO, IDUSER, LOGINDATE , LOGOUTDATE, LOGINTIME, IDPROVEEDOR,idTelefoniaCampanaCallCenter) " +
                            "values (SECIDTELEFONIAREPLOGINTIME.nextval,{idLogWebService},to_date({fechaRegistro},'YYYY-MM-dd'),{idUser},to_date({loginDate},'yyyy-MM-dd HH24:mi:ss'),to_date({logoutDate},'yyyy-MM-dd HH24:mi:ss'),{loginTime},{idProveedor},{idTelefoniaCampanaCallCenter})");
                    if(i%CANTIDAD==0) {
                        sqlQueryExecutor.commit();
                        flag=true;
                    }else{
                        flag=false;
                    }
                } catch (SQLException e) {
                    // System.out.println("Error: Valor duplicado, existe registro con los mismos datos a ingresar. ---> " + e.getMessage());
                }
            }
            if(!flag){
                try {
                    sqlQueryExecutor.commit();
                } catch (SQLException e) {
                    System.out.println("Error: Ocurrio un problema al ejecutar commit. ---> " + e.getMessage());
                }
            }
        }

        if(idWebService.equals("91")) {// Login time
            boolean flag=true;
            for (int i = 0 ; i  < arregloJson.size(); i++){
                JSONObject record = (JSONObject)arregloJson.get(i);
                try {

                    sqlQueryExecutor.addParameter("idLogWebService", idLogWebService);
                    sqlQueryExecutor.addParameter("fecharegistro", record.get("date"));
                    sqlQueryExecutor.addParameter("iduser", record.get("id_agent"));
                    sqlQueryExecutor.addParameter("nombre", record.get("name"));
                    sqlQueryExecutor.addParameter("idqueue", record.get("id_queue"));
                    sqlQueryExecutor.addParameter("typecall", record.get("type_call"));
                    sqlQueryExecutor.addParameter("telefono", record.get("tel_number"));
                    sqlQueryExecutor.addParameter("codact", record.get("cod_act"));
                    sqlQueryExecutor.addParameter("folio", record.get("id_customer"));
                    sqlQueryExecutor.addParameter("duracion", record.get("duration"));
                    sqlQueryExecutor.addParameter("hungup", record.get("hung_up"));
                    sqlQueryExecutor.addParameter("idcampaing", record.get("id_campaing"));
                    sqlQueryExecutor.addParameter("idProveedor", idProveedor);
                    sqlQueryExecutor.addParameter("idTelefoniaCampanaCallCenter", idTelefoniaCampanaCallCenter);

                    sqlQueryExecutor.executeUpdateUsingString("insert into TBLTELEFONIAREPZOOMCALLS " +
                            " (idtelefoniarepzoomcalls, idlogwebservice, fecharegistro, iduser, nombre, idqueue, typecall, telefono, codact, folio, duracion, hungup, idcampaing, idproveedor, procesado, fechaevento,idTelefoniaCampanaCallCenter) " +
                            " values (SECIDTELEFONIAREPZOOMCALLS.nextval,{idLogWebService},to_date({fecharegistro},'yyyy-MM-dd HH24:mi:ss'), {iduser}, {nombre}, {idqueue}, {typecall}, {telefono}, {codact}, {folio}, {duracion}, {hungup}, {idcampaing}, {idProveedor}, 'N', sysdate,{idTelefoniaCampanaCallCenter}) ");
                    if(i%CANTIDAD==0) {
                        sqlQueryExecutor.commit();
                        flag=true;
                    }else{
                        flag=false;
                    }
                } catch (SQLException e) {
                    // System.out.println("Error: Valor duplicado, existe registro con los mismos datos a ingresar. ---> " + e.getMessage());
                }
            }
            if(!flag){
                try {
                    sqlQueryExecutor.commit();
                } catch (SQLException e) {
                    System.out.println("Error: Ocurrio un problema al ejecutar commit. ---> " + e.getMessage());
                }
            }
        }


        if(idWebService.equals("96")) {// registros de llamada
            boolean flag=true;
            for (int i = 0 ; i  < arregloJson.size(); i++){
                JSONObject record = (JSONObject)arregloJson.get(i);
                try {
                    if (record.get("customer_identification").toString().length() > 0 ){
                        sqlQueryExecutor.addParameter("idLogWebService", idLogWebService);
                        sqlQueryExecutor.addParameter("idLlamada", record.get("id_call"));
                        sqlQueryExecutor.addParameter("typeCall", record.get("type_call"));
                        sqlQueryExecutor.addParameter("talkTime", record.get("talk_time"));
                        sqlQueryExecutor.addParameter("idUser", record.get("id_agent"));
                        sqlQueryExecutor.addParameter("nombre", record.get("agent_name"));
                        sqlQueryExecutor.addParameter("fechaLlamada", record.get("date"));
                        sqlQueryExecutor.addParameter("typingCode", record.get("typing_code"));
                        sqlQueryExecutor.addParameter("descriTypingCode", record.get("descri_typing_code"));
                        sqlQueryExecutor.addParameter("telefono", record.get("telephone_number"));
                        sqlQueryExecutor.addParameter("whoHangsUp", record.get("who_hangs_up"));
                        sqlQueryExecutor.addParameter("folio", record.get("customer_identification"));
                        sqlQueryExecutor.addParameter("idProveedor", idProveedor);
                        sqlQueryExecutor.addParameter("idTelefoniaCampanaCallCenter", idTelefoniaCampanaCallCenter);

                        sqlQueryExecutor.executeUpdateUsingString("insert into TBLTELEFONIAREPREGLLAMADAS " +
                                " (IDTELEFONIAREPREGLLAMADAS, IDLOGWEBSERVICE, IDLLAMADA, TYPECALL, TALKTIME, IDUSER, NOMBRE, FECHALLAMADA, TYPINGCODE, DESCRITYPINGCODE, TELEFONO, WHOHANGSUP, FOLIO, FECHAEVENTO, IDPROVEEDOR, PROCESADO,idTelefoniaCampanaCallCenter) " +
                                " values (SECIDTELEFONIAREPZOOMCALLS.nextval,{idLogWebService},{idLlamada}, {typeCall}, {talkTime}, {idUser}, {nombre}, to_date({fechaLlamada},'yyyy-MM-dd HH24:mi:ss'), {typingCode}, {descriTypingCode}, {telefono}, {whoHangsUp},{folio},sysdate, {idProveedor}, 'N',{idTelefoniaCampanaCallCenter} ) ");
                        if(i%CANTIDAD==0) {
                            sqlQueryExecutor.commit();
                            flag=true;
                        }else{
                            flag=false;
                        }
                    }
                }  catch (SQLException e) {
                    // System.out.println("Error: Valor duplicado, existe registro con los mismos datos a ingresar. ---> " + e.getMessage());
                }
            }
            if(!flag){
                try {
                    sqlQueryExecutor.commit();
                } catch (SQLException e) {
                    System.out.println("Error: Ocurrio un problema al ejecutar commit. ---> " + e.getMessage());
                }
            }
        }

    }

    public ResponseWS procesaAudio(IpcomRequest ipcomRequest) {
        ResponseWS responseWS=new ResponseWS();
      responseWS=solicitaAudio(ipcomRequest);
      //todo: si el responseWs da error entonces no maneja el audio, en caso contrario continua
        if(responseWS.getStatus().equals("0")){
            responseWS=ProcesaAudioFtp(ipcomRequest);
        }

      return responseWS;
    }

    private ResponseWS ProcesaAudioFtp(final IpcomRequest ipcomRequest) {
        ResponseWS responseWS=new ResponseWS();
        FTPClient ftpClient = new FTPClient();
        try {

            ftpClient.connect(ipcomRequest.getFtp_server());
            ftpClient.login(ipcomRequest.getFtp_user(),ipcomRequest.getFtp_pass());
            ftpClient.enterLocalPassiveMode();
            String path="/AUDIOS";

            for(int i=0;i<ipcomRequest.getRemote_path().split("/").length;i++){
                path=path+"/"+ipcomRequest.getRemote_path().split("/")[i];
                ftpClient.makeDirectory(path+"/");

            }

            FTPFileFilter filter = new FTPFileFilter() {
                @Override
                public boolean accept(FTPFile ftpFile) {
                    return (ftpFile.isFile() && ftpFile.getName().toUpperCase().contains( ipcomRequest.getNew_name_audio()));
                }
            };

            DateFormat dateFormater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            // lists files and directories in the current working directory
            FTPFile[] files = ftpClient.listFiles("/AUDIOS/"+ipcomRequest.getRemote_path(),filter);
            // iterates over the files and prints details for each
            for (FTPFile file : files) {
                String details = file.getName();
                if (file.isDirectory()) {
                    details = "[" + details + "]";
                }
                details += "\t\t" + file.getSize();
                details += "\t\t" + dateFormater.format(file.getTimestamp().getTime());
            }



            if(files.length>0){
                ftpClient.rename("/AUDIOS/ipComV2Tmp/"+ipcomRequest.getId_call()+".mp3","/AUDIOS/"+ipcomRequest.getRemote_path()+"/"+ipcomRequest.getNew_name_audio()+"_"+files.length+".mp3");
            }else{
                ftpClient.rename("/AUDIOS/ipComV2Tmp/"+ipcomRequest.getId_call()+".mp3","/AUDIOS/"+ipcomRequest.getRemote_path()+"/"+ipcomRequest.getNew_name_audio()+".mp3");
            }



            ftpClient.logout();
            ftpClient.disconnect();

            responseWS.setStatus("OK");


        } catch (IOException ex) {
            ex.printStackTrace();
            responseWS.setStatus("-1");
            responseWS.setErrorMessage("ex.getMessage()");
            return responseWS;

        } finally {
            try {
                if (ftpClient.isConnected()) {
                    ftpClient.logout();
                    ftpClient.disconnect();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                responseWS.setStatus("-1");
                responseWS.setErrorMessage("ex.getMessage()");
                return responseWS;
            }
        }

        return responseWS;
    }

    public ResponseWS solicitaAudio(IpcomRequest ipcomRequest){

        ResponseWS responseWS=new ResponseWS();

        String output = "";
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String inicioTransaccion = format.format(new Date());
        Long idLogWebService = null;
        JSONParser parser = new JSONParser();
        ALQueryResult dataParams = new ALQueryResult();
        ALQueryResult wsData = new ALQueryResult();
        ALQueryResult dataUrlParams = new ALQueryResult();

        try {


            sqlQueryExecutor.addParameter("idWebService", ipcomRequest.getIdWebService());
            sqlQueryExecutor.addParameter("tipo", "P");

            dataParams = sqlQueryExecutor.executeQuery(QuerysDB.GET_ELEMENTS_WS);
            wsData = sqlQueryExecutor.executeQuery(QuerysDB.GET_BASIC_DATA_WS);

            sqlQueryExecutor.addParameter("tipo", "U");
            dataUrlParams = sqlQueryExecutor.executeQuery(QuerysDB.GET_ELEMENTS_WS);

              /*Establecemos la ruta*/
            String url= wsData.get(0,"URL").toString();

            MultivaluedMap<String, String> params =  new MultivaluedMapImpl();
            if(dataParams.size() != 0){
                for (int i = 0; i < dataParams.size(); i++) {
                    String nameParameter = dataParams.get(i,"NOMBRE").toString();
                    String  valueParameter = dataParams.get(i,"VALOR") != null ? dataParams.get(i,"VALOR").toString() : null;
                    if(Util.isEmpty(valueParameter )){
                        valueParameter = getValueFromOject(ipcomRequest, nameParameter);
                    }
                    if(Util.isEmpty(valueParameter )){
                        responseWS.setErrorMessage("El parametro "+nameParameter+" No Tiene valor");
                        return responseWS;
                    }

                    params.add(nameParameter ,valueParameter );
                }
            }


            StringBuffer paths = new StringBuffer();
            if(dataUrlParams.size() != 0){
                for (int i = 0; i < dataUrlParams.size(); i++) {
                    String nameParameter = dataUrlParams.get(i,"NOMBRE").toString();
                    String valueParameter = getValueFromOject(ipcomRequest, nameParameter);
                    if(url.contains("{"+nameParameter+"}")){
                        url=url.replace("{"+nameParameter+"}",valueParameter);
                    }else {
                        paths.append("/").append(valueParameter);
                    }
                }
            }

            Client client = Client.create();
            WebResource webResource = client.resource(url).queryParams(params);
            ClientResponse response = webResource.get(ClientResponse.class);
            output = response.getEntity(String.class);

            try{

                Object obj = parser.parse(output);
                if(((JSONObject) obj).get("message").equals("successfully uploaded")){
                    output = "OK";
                    responseWS = Util.setResponse("0",output,null);
                }else{
                    responseWS = Util.setResponse("-1",output,null);
                }
                //obtenemos el idLogWebService
                idLogWebService = (Long)sqlQueryExecutor.executeQuery(QuerysDB.GET_IDLODWEBSERVICE).get(0,"IDLOGWEBSERVICE");
            }catch (Exception e ){
                responseWS = Util.setResponse("-1",output,null);
            }

        } catch (Exception e) {
            e.printStackTrace();
            output = e.getCause().toString();
            responseWS = Util.setResponse("-1",output,null);
        }

        try {
            Util.insertLogWs(sqlQueryExecutor, idLogWebService,  new Long(ipcomRequest.getIdWebService()), ipcomRequest.toString(), output, null, inicioTransaccion, null);
        } catch (SystemException e) {
            e.printStackTrace();
        }

        return responseWS;
    }

    /***
     * metodo que consume el ws de insertar masivamente telefonos a listas negras para ipcom
     * @param ipcomRequest
     * @return
     */
    public ResponseWS massiveDnc(IpcomRequest ipcomRequest){

        String output = "";
        ResponseWS responseWS = null;
        JSONParser parser = new JSONParser();
        String telefonos = "";
        try {
            MultivaluedMap params =  new MultivaluedMapImpl();
            params.add("function","massive_mounting_dnc");
            params.add("token",ipcomRequest.getToken());
            params.add("type","outbound");


            Client client = Client.create();
            WebResource webResource = client.resource("http://"+ipcomRequest.getIp()+"/ipdialbox/api_configuration.php").queryParams(params);
            WebResource.Builder buider = webResource.accept("");

            MultivaluedMap formData = new MultivaluedMapImpl();
            telefonos = getListaTelefonos();
            formData.add("massive_dnc", telefonos);

                ClientResponse response = buider
                    .header("Content-Type","application/x-www-form-urlencoded")
                    .post(ClientResponse.class,formData);

            output = response.getEntity(String.class);


            Object obj = parser.parse(output);
            if(((JSONObject) obj).get("result").equals("OK")){
                responseWS = Util.setResponse("0","OK",null);
            }else{
                responseWS = Util.setResponse("-1",output,null);
            }

        } catch (Exception e) {
            e.printStackTrace();
            output = e.getCause().toString();
            responseWS = Util.setResponse("-1",output,null);
        }
        return responseWS;

    }

    public String getListaTelefonos(){
        ALQueryResult result = new ALQueryResult();
        String listaTelefonos = "";
        try{

            result = sqlQueryExecutor.executeQuery(QuerysDB.GET_ALL_PHONES_BY_IDTELEFONIA_PROVEEDOR);

            if(result.size() > 0){
                for (int i = 0; i < result.size(); i++) {
                    if(i == 0){
                        listaTelefonos = result.get(i, "TELEFONO").toString();
                    }else{
                        listaTelefonos += "\r\n"+result.get(i, "TELEFONO").toString();
                    }
                }
            }

        }catch(Exception e) {
            e.printStackTrace();
        }
        return listaTelefonos;
    }

    public ResponseWS renameAndRelocateAudios(RequestGenericWs requestGenericWs){
        ResponseWS responseWS=new ResponseWS();
        FTPClient ftpClient = new FTPClient();
        boolean succes;
        //todo: loggin ftp

        sqlQueryExecutor.addParameter("idSftps",requestGenericWs.getIdSftp().toString());

        ALQueryResult alQueryResultFtp= null;
        try {
            alQueryResultFtp = sqlQueryExecutor.executeQuery(QuerysDB.GET_FTP_PARAMS);


            String ftpServer=alQueryResultFtp.get(0,"IP").toString();
            String ftpUser=alQueryResultFtp.get(0,"USUARIO").toString();
            String ftpPass=alQueryResultFtp.get(0,"CONTRASENIA").toString();


            ftpClient.connect(ftpServer);
            ftpClient.login(ftpUser,ftpPass);
            ftpClient.enterLocalPassiveMode();
            String pathOrigen="/AUDIOS/IPComAudiosToWork/";

            String anio=requestGenericWs.getFechaInicio().split("/")[2];
            String mes=requestGenericWs.getFechaInicio().split("/")[1];
            String diaInicio=requestGenericWs.getFechaInicio().split("/")[0];

            String diaFin=requestGenericWs.getFechaFin().split("/")[0];

            FTPFile[] files = ftpClient.listFiles(pathOrigen);
            for(FTPFile ftpFile:files) { //bloque de agencia
                int contadorErrores=0;
                String codigo="";
                String descripcion="";

                for (int d = Integer.parseInt(diaInicio); d <= Integer.parseInt(diaFin); d++) {//analizar por dia
                    String dia = "" + d;
                    System.out.println("path Audios: " + pathOrigen + ftpFile.getName() + "/" + anio + "/" + mes + "/" + anio + "-" + mes + "-" + (dia.length() < 2 ? "0" + dia : dia));
                    String pathAudios = pathOrigen  + ftpFile.getName() + "/" + anio + "/" + mes + "/" + anio + "-" + mes + "-" + (dia.length() < 2 ? "0" + dia : dia);
                    FTPFile[] dirAudios = ftpClient.listFiles(pathAudios);

                    for (FTPFile audio : dirAudios) {
                        try{
                            System.out.println("Audio: " + audio.getName());
                            //todo:procesar el audio, sacar el id y consultar en bd
                            String idLlamada = getIdLlamada(audio.getName());
                            System.out.println("idLlamada: " + idLlamada);

                            if (!idLlamada.equals("")) {
                                ALQueryResult alQueryResult;

                                sqlQueryExecutor.addParameter("idLlamada", idLlamada);
                                sqlQueryExecutor.addParameter("idEstadoSolicitud",(!requestGenericWs.getIdsEstadoSolicitud().equals(""))?requestGenericWs.getIdsEstadoSolicitud():"2,3,6");
                                alQueryResult = sqlQueryExecutor.executeQuery(QuerysDB.GET_DATA_ID_LLAMADA);

                                if (alQueryResult.size() > 0) {
                                    String pathDestino = alQueryResult.get(0, "RUTADESTINO").toString();
                                    final String nuevoNombreAudio = alQueryResult.get(0, "NEWNAME").toString();
                                    String folio = alQueryResult.get(0, "FOLIO").toString();
                                    String idExternoSolicitud = (alQueryResult.get(0, "IDEXTERNOSOLICITUD") != null) ? alQueryResult.get(0, "IDEXTERNOSOLICITUD").toString() : "";
                                    String tipificacion=(alQueryResult.get(0, "TYPINGCODE") != null) ? alQueryResult.get(0, "TYPINGCODE").toString() : "";
                                    String idUser=(alQueryResult.get(0, "IDUSER") != null) ? alQueryResult.get(0, "IDUSER").toString() : "";
                                    String path = "/AUDIOS";//destino de los audios

                                    //crea el directorio que le corresponde al archivo
                                    for (int i = 0; i < pathDestino.split("/").length; i++) {
                                        path = path + "/" + pathDestino.split("/")[i];
                                        ftpClient.makeDirectory(path + "/");
                                    }

                                    String extension = audio.getName().toString().split("\\.")[audio.getName().toString().split("\\.").length - 1];

                                    //obtener el numero de registros q tengo de ese nombre de file en tblgrabaciones
                                    sqlQueryExecutor.addParameter("archivo",nuevoNombreAudio+"%");
                                    sqlQueryExecutor.addParameter("folio",folio);
                                    ALQueryResult numeroReg=sqlQueryExecutor.executeQueryUsingQueryString("select count(*) cont from tblgrabaciones where upper(archivo) like upper({archivo}) and folio = {folio} ");
                                    int numeroRegistros=Integer.parseInt(numeroReg.get(0,"CONT").toString());
                                    String nuevoAudio="";
    

                                    if (numeroRegistros > 0) {
                                        nuevoAudio=nuevoNombreAudio + "_" +numeroRegistros+ "." + extension;
                                    } else {
                                        nuevoAudio=nuevoNombreAudio + "." + extension;
                                    }

                                    //Obtenemos el Archivo
                                    String remoteFile = pathAudios + "/" + audio.getName();
                                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                                    succes = ftpClient.retrieveFile(remoteFile, outputStream);


                                    //todo: recupero codigo y descripcion del problema
                                    codigo = "" + ftpClient.getReplyCode();
                                    descripcion = ftpClient.getReplyString();

                                    if(succes) {

                                        InputStream is = new ByteArrayInputStream(outputStream.toByteArray());

                                        // assuming backup directory is with in current working directory
                                        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);//binary files
                                        ftpClient.changeWorkingDirectory(path);
                                        //this overwrites the existing file
                                        succes = ftpClient.storeFile(nuevoAudio, is);
                                        //if you don't want to overwrite it use storeUniqueFile

                                        sqlQueryExecutor.addParameter("registro", nuevoAudio);

                                        //todo: recupero codigo y descripcion del problema
                                        codigo = "" + ftpClient.getReplyCode();
                                        descripcion = ftpClient.getReplyString();

                                        if (succes) {

                                            sqlQueryExecutor.addParameter("folio", folio);
                                            sqlQueryExecutor.addParameter("idExternoSolicitud", idExternoSolicitud);
                                            sqlQueryExecutor.addParameter("rutaArchivo", path + "/");
                                            sqlQueryExecutor.addParameter("rutaArchivoOriginal", pathAudios + "/" + audio.getName());
                                            sqlQueryExecutor.addParameter("idLlamada", idLlamada);
                                            sqlQueryExecutor.addParameter("tipificacion", tipificacion);
                                            sqlQueryExecutor.addParameter("iduser", idUser);

                                            sqlQueryExecutor.executeUpdate(QuerysDB.INSERT_TBLGRABACIONES);
                                            sqlQueryExecutor.commit();

                                            //eliminar el file original: se quita ya que se solicito se mantenga el archivo original
                                            if (requestGenericWs.getBorraArchivo().equals("S")) {
                                                ftpClient.deleteFile(pathAudios + "/" + audio.getName());
                                            }

                                        }
                                    }

                                    almacenaLog(codigo,descripcion,pathAudios + "/" + audio.getName(),path + "/"+ nuevoAudio,folio);

                                }
                            }
                        }catch(Exception e1){
                            almacenaLog("",e1.getMessage(),"","","0");
                            e1.printStackTrace();

                            contadorErrores++;
                            if(contadorErrores>=50){
                                Util.sendAlerta(sqlQueryExecutor, (long) 48,"");
                                contadorErrores=0;
                            }
                        }
                    }
                }
            }

            ftpClient.logout();
            ftpClient.disconnect();
        } catch (Exception e) {
            almacenaLog("",e.getMessage(),"","","0");
            e.printStackTrace();
        }



        return responseWS;
    }

    private String getIdLlamada(String cadenaEvaluar){
        String res="";
        int posicion=0;
        try {
            String seccionDeInteres = cadenaEvaluar.split("\\.id\\.")[1];
            //todo: evaluar el string para determinar la posicion en la cual partirla
            for(int i=0;i<seccionDeInteres.length();i++){
                char ch=seccionDeInteres.charAt(i);
                if(((ch>='a'&&ch<='z')||(ch>='A'&&ch<='Z')) && posicion==0){
                    posicion=i;
                }
            }
            res=seccionDeInteres.substring(0,posicion-1);
        }catch (Exception e){
            e.printStackTrace();
        }
        return res;
    }

    private void almacenaLog(String estatus,String trazaError,String rutaArchivoOrigen,String rutaArchivoDestino,String folio){
        sqlQueryExecutor.addParameter("rutaArchivoOrigen",rutaArchivoOrigen);
        sqlQueryExecutor.addParameter("rutaArchivoDestino",rutaArchivoDestino);
        sqlQueryExecutor.addParameter("estatus",estatus);
        sqlQueryExecutor.addParameter("trazaError",trazaError);
        sqlQueryExecutor.addParameter("folio",folio);

        try {
            sqlQueryExecutor.executeUpdate(QuerysDB.INSERT_TBLLOGGRABACIONES);
            sqlQueryExecutor.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
}