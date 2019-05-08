package com.a4sys.luciWS.amazonS3;


import com.a4sys.core.dao.ALQueryResult;
import com.a4sys.core.dao.DaoService;
import com.a4sys.luciWS.domain.AmazonRequest;
import com.a4sys.luciWS.util.QuerysDB;
import com.a4sys.luciWS.util.SystemConstants;
import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by mmorones on 3/22/18.
 */
public class AmazonS3Service extends DaoService {

    public AmazonS3Service(){
        super(SystemConstants.JDNI);
    }

    public File getFileFromS3Amazon(String idWebService, String key) {

        File file = null;

        try {


            String postFijo = key.split("_")[1];
            String folio = key.split("_")[0];
            String orden="";
            Pattern p = Pattern.compile("-?\\d+");
            Matcher m = p.matcher(postFijo);
            while (m.find()) {
               orden = m.group();
            }
            postFijo = postFijo.replaceAll("[0-9]", "");

            sqlQueryExecutor.addParameter("idWebService",idWebService);
            ALQueryResult result = sqlQueryExecutor.executeQueryUsingQueryString("select usuario, contrasenia, LLAVEENCRIPTAR from dicwebservices where idWebService = {idWebService} ");


            sqlQueryExecutor.cleanParameter();

            String bucketName  = result.get(0,"USUARIO").toString();
            String acccessKey  = result.get(0,"CONTRASENIA").toString();
            String secretKey   = result.get(0,"LLAVEENCRIPTAR").toString();
			/*Nos conectamos al servidor*/
            BasicAWSCredentials creds = new BasicAWSCredentials(acccessKey, secretKey);
            AmazonS3 s3Client = AmazonS3ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(creds)).withRegion(Regions.US_EAST_1).build();

			/*Obtenemos el objeto que es el pdf*/
            S3Object object = s3Client.getObject(new GetObjectRequest(bucketName, key));


            sqlQueryExecutor.addParameter("posfijo",postFijo);
            sqlQueryExecutor.addParameter("orden",orden);
            sqlQueryExecutor.addParameter("folio",folio);


            result = sqlQueryExecutor.executeQueryUsingQueryString("" +
                    " select extension from tblSolicitudesDocumentos d\n" +
                    " join Dictiposdocumentos td on Td.Idtipodocumentos = D.Idtipodocumento\n" +
                    " where folio = {folio} and td.posfijo = {posfijo} and d.orden = {orden} ");

            String extension = "jpeg";
            if( null != result.get(0,"EXTENSION") ){
                extension = result.get(0,"EXTENSION").toString();
            }


            file = File.createTempFile("temp","."+extension );
            FileUtils.copyToFile(object.getObjectContent(), file );

        }catch (Exception e) {
            e.printStackTrace();;
        }

        return file;
    }



    /***
     * Metodo que guarda en un s3 de amazon
     * @param filePath
     * @param idWebService
     * @return
     */
    public void saveFileToS3Amazon(String  filePath, int idWebService,String key)  throws Exception {


        ALQueryResult result = new ALQueryResult();

        try{

            sqlQueryExecutor.autocommit(false);
            sqlQueryExecutor.addParameter("idWebService",idWebService);
            result = sqlQueryExecutor.executeQueryUsingQueryString("select usuario, contrasenia, LLAVEENCRIPTAR from dicwebservices where idWebService = {idWebService} ");

            sqlQueryExecutor.cleanParameter();

            String bucketName  = result.get(0,"USUARIO").toString();
            String acccessKey  = result.get(0,"CONTRASENIA").toString();
            String secretKey   = result.get(0,"LLAVEENCRIPTAR").toString();

            BasicAWSCredentials creds = new BasicAWSCredentials(acccessKey, secretKey);
            AmazonS3 s3Client = AmazonS3ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(creds)).withRegion(Regions.US_EAST_1).build();

            File file = new File(filePath);
            s3Client.putObject(new PutObjectRequest( bucketName, key, file));


        } catch (AmazonServiceException ase) {
            throw ase;
        } catch (AmazonClientException ace) {
            throw ace;
        } catch (SQLException e) {
            throw e;
        } catch (Exception e) {
            throw e;
        }

    }

    public String getMassiveImages(AmazonRequest amazonRequest) {
        FTPClient ftpClient = new FTPClient();
        File file = null;
        try {
            //todo:get bucket and ftp.

            sqlQueryExecutor.addParameter("idSftps",amazonRequest.getIdSftp().toString());
            sqlQueryExecutor.addParameter("idWebService",amazonRequest.getIdWebService().toString());

            ALQueryResult alQueryResultFtp=sqlQueryExecutor.executeQuery(QuerysDB.GET_FTP_PARAMS);

            String ftpServer=alQueryResultFtp.get(0,"IP").toString();
            String ftpUser=alQueryResultFtp.get(0,"USUARIO").toString();
            String ftpPass=alQueryResultFtp.get(0,"CONTRASENIA").toString();

            ALQueryResult alQueryResultBucket = sqlQueryExecutor.executeQuery(QuerysDB.GET_BUCKET_PARAMS);
            sqlQueryExecutor.cleanParameter();

            String bucketName  = alQueryResultBucket.get(0,"USUARIO").toString();
            String acccessKey  = alQueryResultBucket.get(0,"CONTRASENIA").toString();
            String secretKey   = alQueryResultBucket.get(0,"LLAVEENCRIPTAR").toString();

            sqlQueryExecutor.addParameter("idCampana",amazonRequest.getIdCampana().toString());
            sqlQueryExecutor.addParameter("fechaInicio",amazonRequest.getFechaInicio().toString());
            sqlQueryExecutor.addParameter("fechaFin",amazonRequest.getFechaFin().toString());

            //aqui tenemos que modificar para que la consulta se comporte de dos formas distintas
            //1 por fecha
            //2 por folios
            String query="select distinct s.folio, m.folioMensajeria,s.folio||'_'||td.posfijo||d.orden key,to_char(d.fechaevento,'yyyymmdd') fecha,\n" +
                    "replace(translate(initcap(dc.campana), 'áéíóúÁÉÍÓÚ', 'aeiouAEIOU'),' ','_') campana \n" +
                    "from tblSolicitudes s \n" +
                    "join tblsolicitudesmensajerias m on m.folio = s.folio \n" +
                    "join tblSolicitudesDocumentos d on d.folio = s.folio \n" +
                    "join dictiposdocumentos td on td.idtipodocumentos = d.idtipodocumento \n" +
                    "join diccampanas dc on dc.idcampana=s.idcampana \n" +
                    "where s.idCampana = {idCampana} \n";

            if(amazonRequest.getFolios()==null || amazonRequest.getFolios().isEmpty()){
                query+="and d.fechaevento BETWEEN to_date( {fechaInicio} ||'00:00:00' ,'DD/MM/YYYY HH24:mi:ss') and to_date( {fechaFin}||'23:59:59' ,'DD/MM/YYYY HH24:mi:ss')";
            }else{
                query+="and s.folio in ("+amazonRequest.getFolios().toString()+")";
            }






            ALQueryResult alQueryResultKeys=sqlQueryExecutor.executeQueryUsingQueryString(query);

            //abrimos una conexion con el ftp
            ftpClient.connect(ftpServer);
            ftpClient.login(ftpUser,ftpPass);
            ftpClient.enterLocalPassiveMode();

            Date fechaInicio = new Date();

            int numeroKeys=alQueryResultKeys.size();

            for(int i = 0 ; i  < numeroKeys ; i++) {
                //nos conectamos con el servidor de amazon
                BasicAWSCredentials creds = new BasicAWSCredentials(acccessKey, secretKey);
                AmazonS3 s3Client = AmazonS3ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(creds)).withRegion(Regions.US_EAST_1).build();
                String path="";

                String pathDestino=alQueryResultKeys.get(i,"CAMPANA").toString()+"/luci_img_"+alQueryResultKeys.get(i,"FECHA").toString();

                //crea el directorio que le corresponde al archivo
                for (int j = 0; j < pathDestino.split("/").length; j++) {
                    path = path + "/" + pathDestino.split("/")[j];
                    ftpClient.makeDirectory(path + "/");
                }



                //ftpClient.makeDirectory(path+"/");

                //obtengo el objeto
                S3Object object = s3Client.getObject(new GetObjectRequest(bucketName, alQueryResultKeys.get(i, "KEY").toString()));

                //creo el file temp
                //file = new File("/home/pvargas/Pictures/MassiveImages/" + object.getKey() + ".jpeg");
                //FileUtils.copyToFile(object.getObjectContent(), file);
                //System.out.println((i + 1) + "/" + numeroKeys + " folio: " + alQueryResultKeys.get(i, "FOLIO").toString() + " nombreImagen: " + file.getName());

                file = File.createTempFile( object.getKey(),".jpeg" );
                FileUtils.copyToFile(object.getObjectContent(), file );
    
                InputStream in =new FileInputStream(file);
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

                ftpClient.changeWorkingDirectory(path);
                ftpClient.storeFile( object.getKey() + ".jpeg", in);

                if(file.exists()) {
                    file.delete();
                }

                System.out.println("---->"+i+"/"+numeroKeys+"  ---> "+path+"/" + object.getKey() + ".jpeg ---->"+fechaInicio + " / "+ new Date());
            }

            ftpClient.logout();
            ftpClient.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
        return "";
    }
}
