package com.a4sys.luciWS.smsConvergia;

import com.a4sys.core.dao.ALQueryResult;
import com.a4sys.core.dao.DaoService;
import com.a4sys.luciWS.util.SystemConstants;
import com.a4sys.luciWS.util.Util;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.sql.SQLException;

public class SmsConvergiaService extends DaoService{

    private String error;

    public SmsConvergiaService(){
        super(SystemConstants.JDNI);
    }

    public boolean validData(String cellPhone, String message, String idWebServices){
        boolean exito = true;

        if (cellPhone == null || message == null || idWebServices == null || cellPhone.equals("") || message.equals("") || idWebServices.equals("")) {
            exito = false;
        }

        return exito;
    }

    public String sendMessage(String cellPhone, String message, String idWebServices){
        String respuestaService = "El mensaje se envió con éxito.";
        String url = "";
        String rutaServidor = "";
        String usuario = "";
        String contrasenia = "";
        String datos = "";
        Long codigoPais = 0L;
        error = "0";
        boolean isDataComplet = validData(cellPhone, message, idWebServices);


        if(!isDataComplet){
            return "No se recibieron los parámetros necesarios.";
        }


        try {
            sqlQueryExecutor.addParameter("idWebServices", idWebServices);
            ALQueryResult res = sqlQueryExecutor.executeQueryUsingQueryString("select url,usuario,contrasenia from dicWebServices where idWebService = {idWebServices}");


            rutaServidor = res.get(0, "URL").toString();
            usuario = res.get(0, "USUARIO").toString();
            contrasenia = res.get(0, "CONTRASENIA").toString();

            //obtenemos el codigo del pais 52 Mexico - 1 usa

            codigoPais = Util.getCodigoPais(sqlQueryExecutor, cellPhone);

            datos = "username="+usuario+
                    "&password="+contrasenia+
                    "&number="+codigoPais+""+cellPhone+
                    "&message="+ URLEncoder.encode(message, "UTF-8");

            url = rutaServidor +"?"+ datos;
            HttpClient client = HttpClientBuilder.create().build();
            HttpGet httpGet = new HttpGet(url);
            /*Base64 b = new Base64();
            String encoding = b.encodeAsString(new String(usuario+":"+contrasenia).getBytes("UTF-8"));
            httpGet.setHeader("Authorization", "Basic " + encoding);*/

            HttpResponse response = client.execute(httpGet);

            //System.out.println("response = " + response);

            BufferedReader breader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            StringBuilder responseString = new StringBuilder();
            String line = "";
            while ((line = breader.readLine()) != null) {
                responseString.append(line);
            }
            breader.close();
            String repsonseStr = responseString.toString();

            //System.out.println("repsonseStr = " + repsonseStr);

            respuestaService = repsonseStr;



        }catch(SQLException e){
            error = "1";
            respuestaService = "Ocurrió un error al obtener los datos del Web Services.";

        }catch (Exception e) {
            error = "1";
            respuestaService = "Ocurrió un error al crear la url del Web Services."+e;
        }

        return respuestaService;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}

