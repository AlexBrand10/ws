package com.a4sys.luciWS.utilWS;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.a4sys.core.dao.ALQueryResult;
import com.a4sys.core.dao.DaoService;
import com.a4sys.core.dao.domain.Consulta;
import com.a4sys.core.dao.domain.ParametroConsulta;
import com.a4sys.luciWS.domain.Adjuntos;
import com.a4sys.luciWS.domain.RequestGenericEmail;
import com.a4sys.luciWS.domain.RequestGenericWs;
import com.a4sys.luciWS.domain.ResponseGenericWs;
import com.a4sys.luciWS.util.QuerysDB;
import com.a4sys.luciWS.util.SystemConstants;
import com.a4sys.luciWS.util.Util;
import com.amazonaws.util.StringUtils;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.Session;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;


/***
 * 
 * @author Marti Morones 
 *
 */
public class WSUtilService extends DaoService {

	
	
	public WSUtilService() {
		super(SystemConstants.JDNI);		
	}

	
	/****
	 * METODO QUE REGRESA UNA LISTA DE COLONIAS CON SUS MUNICIPIO Y ESTADO APARTIR DE UN cp
	 * @param cp
	 * @return
	 */
	public ALQueryResult getSuburbs(String cp){
		List<ParametroConsulta> parametros = new ArrayList<ParametroConsulta>();
		parametros.add(new ParametroConsulta("cp", cp));
		Consulta consulta = new Consulta(QuerysDB.GET_SUBURBS_BY_CP, parametros);
		ALQueryResult res = new ALQueryResult();
		try {
			res = ejecutarConsulta(consulta);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return res;
		
	}

	public boolean sendGenericMail(RequestGenericEmail requestGenericEmail){
		boolean enviadoExitoso = true;
		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		String inicioTransaccion = format.format(new Date());
		ArrayList<File> files = new ArrayList<File>();
		//System.err.println(requestGenericEmail.toString());
		Long idWebService = 83L;
		Long idLogWebService = 0L;
		String xmlEntrada = "";
		String xmlSalida = "";
		String urlArchivoAdjunto = "";
		try {
			xmlEntrada = requestGenericEmail.toString();
			idLogWebService = (Long) sqlQueryExecutor.executeQuery(QuerysDB.GET_IDLODWEBSERVICE).get(0, "IDLOGWEBSERVICE");

			if (isValidData(requestGenericEmail)) {

                List<Adjuntos> listAdjuntos = requestGenericEmail.getAdjuntos();
                if(listAdjuntos != null){
                    for (int i = 0; i < listAdjuntos.size(); i++) {
                    Adjuntos adjunto = listAdjuntos.get(i);

                    if (adjunto.getTipo().toString().equals("1")) { // 1 = id del webservice (dicwebservices)
                        RequestGenericWs requestGenericWs = new RequestGenericWs();

                        if (adjunto.getIdAdjunto().toString().equals("81")) { // generacion de referencia bancaria
                            if (!StringUtils.isNullOrEmpty(requestGenericEmail.getFolio())) {
                                Long idExternoSolicitud = Util.getIdExternoSolicitud(sqlQueryExecutor, new Long(requestGenericEmail.getFolio()));
                                if (idExternoSolicitud > 0) {
                                    requestGenericWs.setIdCorporation("" + idExternoSolicitud);
                                } else {
                                    xmlSalida += "Acción no realizada con éxito: Folio no tiene un Id Corporation. ";
                                }
                            } else if (!StringUtils.isNullOrEmpty(requestGenericEmail.getIdCorporation())) {
                                Long folio = Util.getFolioByIdCorporation(sqlQueryExecutor, new Long(requestGenericEmail.getIdCorporation()));
                                requestGenericWs.setIdCorporation(requestGenericEmail.getIdCorporation());
                                requestGenericEmail.setFolio("" + folio);
                            } else {
                                xmlSalida += "El dato folio y/o idCorporation no deben de venir vacíos. ";
                            }

                        }

                        ResponseGenericWs responseGenericWs = Util.consumeGenericWs(sqlQueryExecutor, requestGenericWs, new Long(adjunto.getIdAdjunto()));
                        if (responseGenericWs.getSuccess()) {

                            urlArchivoAdjunto = responseGenericWs.getDescription();
                            if (urlArchivoAdjunto != null) {
                                File file = Util.readPdfFromURL(urlArchivoAdjunto);

                                if (file != null) {
                                    xmlSalida += "Se generó el pdf (" + urlArchivoAdjunto + "). ";
                                    files.add(file);
                                } else {
                                    xmlSalida += "No fue posible generar el pdf (el archivo no existe: " + urlArchivoAdjunto + "). ";
                                }

                            } else {
                                xmlSalida += "No fue posible generar el pdf (la url del archivo es null). ";
                            }

                        } else {
                            xmlSalida += "No fue posible generar el adjunto con id : " + adjunto.getIdAdjunto() + " Error: " + responseGenericWs.getDescription() + " ";
                        }


                    } else if (adjunto.getTipo().toString().equals("2")) { // 2 = id del reporte (dicreportes)
                        File file = null;
                        if (!StringUtils.isNullOrEmpty(requestGenericEmail.getFolio()) && !StringUtils.isNullOrEmpty(adjunto.getIdAdjunto())) {

                            file = Util.getPdfFromJasperServer(sqlQueryExecutor, new Long(requestGenericEmail.getFolio()), new Long(adjunto.getIdAdjunto()), "ws_",adjunto.getExtension());

                        } else if (!StringUtils.isNullOrEmpty(requestGenericEmail.getIdCorporation()) && !StringUtils.isNullOrEmpty(adjunto.getIdAdjunto())) {

                            Long folio = Util.getFolioByIdCorporation(sqlQueryExecutor, new Long(requestGenericEmail.getIdCorporation()));
                            requestGenericEmail.setFolio("" + folio);

                            file = Util.getPdfFromJasperServer(sqlQueryExecutor, new Long(requestGenericEmail.getFolio()), new Long(adjunto.getIdAdjunto()), "ws_",adjunto.getExtension());

                        } else {
                            xmlSalida += "El dato folio y/o IdAdjunto no deben de venir vacíos. ";
                        }

                        if (file != null) {
                            xmlSalida += "Se generó el archivo adjunto (" + file.getPath() + "). ";
                            files.add(file);
                        } else {
                            xmlSalida += "No fue posible generar el archivo adjunto (el archivo no existe: " + file.getPath() + "). ";
                        }

                    } else if (adjunto.getTipo().toString().equals("3")) { // 3 = ruta de archivo en físico
                        if (!StringUtils.isNullOrEmpty(adjunto.getRuta())) {
                            urlArchivoAdjunto = adjunto.getRuta().toString();
                            File file = Util.readPdfFromURL(urlArchivoAdjunto);

                            if (file != null) {
                                xmlSalida += "Se generó el pdf (" + urlArchivoAdjunto + "). ";
                                files.add(file);
                            } else {
                                xmlSalida += "No fue posible generar el pdf (el archivo no existe: " + urlArchivoAdjunto + "). ";
                            }
                        } else {
                            xmlSalida += "El dato ruta no debe de venir vacío. ";
                        }
                    } else {
                        xmlSalida += "El tipo del elemto adjunto es desconocido (" + adjunto.getTipo() + "). ";
                    }

                }
            }

				Util.sendEmailWihAttachments(sqlQueryExecutor, new Long(requestGenericEmail.getIdNotificacionEmail()), requestGenericEmail.getEmail().toString(), new Long(requestGenericEmail.getFolio()), new Long(requestGenericEmail.getIdUsuario()), "", files, false);
				xmlSalida += "Se envió con éxito el IdNotificacionEmail: " + requestGenericEmail.getIdNotificacionEmail() + " al correo: " + requestGenericEmail.getEmail() + " ";

			}else{
				xmlSalida += "El folio, idNotificacionEmail, email y/o idUsuario  vienen vacíos. ";
				enviadoExitoso = false;
			}

			}catch(Exception e){
				e.printStackTrace();
				xmlSalida += "Error al consumir el web service id: " + requestGenericEmail.getAdjuntos() + " Error: " + e.getMessage()+" ";
			}


		Util.insertLogWs(sqlQueryExecutor, idLogWebService, idWebService, xmlEntrada, xmlSalida, (Util.isEmpty(requestGenericEmail.getFolio().toString()) ? null : new Long(requestGenericEmail.getFolio().toString())), inicioTransaccion, null);

		return enviadoExitoso;
	}

	public boolean isValidData(RequestGenericEmail requestGenericEmail) {
		//String folio = (null == requestGenericEmail.getFolio() ? "" : (requestGenericEmail.getFolio().equals("null") ? "" : requestGenericEmail.getFolio()));
		String idNotificacionEmail = (null == requestGenericEmail.getIdNotificacionEmail() ? "" : (requestGenericEmail.getIdNotificacionEmail().equals("null") ? "" : requestGenericEmail.getIdNotificacionEmail()));
		String idUsuario = (null == requestGenericEmail.getIdUsuario() ? "" : (requestGenericEmail.getIdUsuario().equals("null") ? "" : requestGenericEmail.getIdUsuario()));
		String email = (null == requestGenericEmail.getEmail() ? "" : (requestGenericEmail.getEmail().equals("null") ? "" : requestGenericEmail.getEmail()));
		if(StringUtils.isNullOrEmpty(idNotificacionEmail) || StringUtils.isNullOrEmpty(idUsuario) || StringUtils.isNullOrEmpty(email)){
			return false;
		}
		return true;
	}

	public String sendFileExitus(){
        SimpleDateFormat format = new SimpleDateFormat("YYYYMMddHHmm");
        String date = format.format(new Date());
        String fileName = "-1";
        String idsftp = "10";

        OutputStream os = null;
        PrintStream printStream = null;

        //Escritura de archivo en ftp
        Session session     = null;
        Channel channel     = null;
        ChannelSftp channelSftp = null;

        try {
            ALQueryResult resultData = sqlQueryExecutor.executeQuery(QuerysDB.GET_DATOS_EXITUS);
            if(resultData.size() > 0){
                sqlQueryExecutor.addParameter("idsftp", idsftp);
                ALQueryResult resultFTP = sqlQueryExecutor.executeQuery(QuerysDB.GET_FTP);
                if(resultFTP.size() > 0){
                    String SFTPHOST = resultFTP.get(0,"IP")!=null?resultFTP.get(0,"IP").toString():"";
                    int    SFTPPORT = resultFTP.get(0,"PUERTO")!=null?Integer.parseInt(resultFTP.get(0,"PUERTO").toString()):0;
                    String SFTPUSER = resultFTP.get(0,"USUARIO")!=null?resultFTP.get(0,"USUARIO").toString():"";
                    String SFTPPASS = resultFTP.get(0,"CONTRASENIA")!=null?resultFTP.get(0,"CONTRASENIA").toString():"";
                    String SFTPWORKINGDIR = resultFTP.get(0,"RUTA")!=null?resultFTP.get(0,"RUTA").toString():"";

                    FTPClient ftpClient = new FTPClient();
                    try{
                        /*Conexion FTP*/
                        ftpClient.connect(SFTPHOST, SFTPPORT);
                        ftpClient.login(SFTPUSER, SFTPPASS);
                        ftpClient.enterLocalPassiveMode();


                        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);


                        try{

                            fileName = "VentasTCI-"+date+".csv";
                            /*Obtencion de cabeceras y datos para archivo*/

                            os = ftpClient.storeFileStream(SFTPWORKINGDIR+""+fileName);
                            printStream = new PrintStream(os);

                            printStream.print("FOLIO LUCI, CALLE, NUMERO INTERIOR, NUMERO EXTERIOR, COLONIA VIVIENDA, CODIGO POSTAL, ESTADO VIVIENDA, MUNICIPIO VIVIENDA, MATERNO CLIENTE, NOMBRE CLIENTE, PARTENO CLIENTE, RFC, TELEFONO CASA, TELEFONO CELULAR");
                            printStream.print("\n");

                            for(int i=0; i < resultData.size(); i++){

                                printStream.print(resultData.get(i,"FOLIO"));
                                printStream.print(","+resultData.get(i,"CALLE"));
                                printStream.print(","+resultData.get(i,"NOINTERIOR"));
                                printStream.print(","+resultData.get(i,"NOEXTERIOR"));
                                printStream.print(","+resultData.get(i,"COLONIA"));
                                printStream.print(","+resultData.get(i,"CPOSTAL"));
                                printStream.print(","+resultData.get(i,"ESTADO"));
                                printStream.print(","+resultData.get(i,"MUNICIPIO"));
                                printStream.print(","+resultData.get(i,"AMATERNO"));
                                printStream.print(","+resultData.get(i,"NOMBRE"));
                                printStream.print(","+resultData.get(i,"APATERNO"));
                                printStream.print(","+resultData.get(i,"RFC"));
                                printStream.print(","+resultData.get(i,"TELEFONO"));
                                printStream.print(","+resultData.get(i,"TELEFONOCELULAR"));
                                printStream.print("\n");
                            }


                            printStream.close();
                            os.close();

                        } catch (Exception e) {
                            e.printStackTrace();
                            fileName = "~Ocurrio un error al crear el Archivo.";
                        } finally {
                            try {
                                // Nuevamente aprovechamos el finally para
                                // asegurarnos que se cierra el fichero.
                                if(printStream!=null)
                                    printStream.close();
                                if(os!=null)
                                    os.close();

                            } catch (Exception e2) {
                                e2.printStackTrace();
                                fileName = "~Ocurrio un error al cerrar el archivo.";
                            }
                        }

                    }catch(Exception ex){
                        ex.printStackTrace();
                        fileName = "~Ocurrio un error al conectarse con el FTP. (idsftp: "+ idsftp +").";
                    } finally {
                        try {
                            if (ftpClient.isConnected()) {
                                ftpClient.logout();
                                ftpClient.disconnect();
                            }
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                }

            }else{
               fileName = "~No se genero archivo para Exitus.";
            }
        }catch (SQLException e) {
            e.printStackTrace();
            fileName = "~Ocurrio un error de Base de Datos.";
        }
        return fileName;
    }

    public ResponseGenericWs consumeGenericWs(RequestGenericWs requestGenericWs) {
	    return Util.consumeGenericWs(sqlQueryExecutor,requestGenericWs,Long.parseLong(requestGenericWs.getIdWebService()));
    }
}
