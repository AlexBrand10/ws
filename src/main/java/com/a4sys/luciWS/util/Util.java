package com.a4sys.luciWS.util;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.a4sys.luciWS.domain.*;
import com.a4sys.luciWS.enviarAdjuntoWS.PDFResult;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import oracle.jdbc.OracleTypes;

import com.a4sys.core.dao.ALQueryResult;
import com.a4sys.core.dao.SQLQueryExecutor;
import com.a4sys.core.exceptions.SystemException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.LongSerializationPolicy;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.ws.rs.core.MultivaluedMap;

public class Util {


	/***
	 * METODO QUE UTILIZA GSON PARA CONVERTIR A JSON FORMAT UN OBJETO
	 * @param object
	 * @return
	 */
	public static String toJson(Object object){
		Gson gson = new GsonBuilder()
				.setPrettyPrinting()
				.disableHtmlEscaping()
				.setLongSerializationPolicy(LongSerializationPolicy.STRING)
				.serializeNulls()
				.setDateFormat("dd/MM/yyyy hh:mm:ss").create();
		return gson.toJson(object);
	}

	/***
	 *
	 * @param json
	 * @param classOfT
	 * @param <T>
	 * @return
	 */
	public static <T> Object fromJson(String json,Class<T> classOfT){
		Gson gson = new GsonBuilder()
				.setPrettyPrinting()
				.setLongSerializationPolicy(LongSerializationPolicy.STRING)
				.serializeNulls()
				.setDateFormat("dd/MM/yyyy hh:mm:ss").create();
		return gson.fromJson(json, classOfT);
	}

	/***
	 * METODO QUE ASIGNA SETTEA UN RESPONSE
	 * @param idError
	 * @param messageError
	 * @param data
	 * @return
	 */
	public static ResponseWS setResponse(String idError,String messageError, ALQueryResult data){
		ResponseWS output = new ResponseWS();
		output.setStatus(idError.toString());
		output.setErrorMessage(messageError);
		output.setData(data == null ? new ALQueryResult() : data);
		return output;
	}

	/***
	 * METOOD QUE REGRESA UNA RESPUESTA DE WEB SERVICE EN FORMATO JSON
	 * @param idError
	 * @param messageError
	 * @param data
	 * @return
	 */
	public static String setResponseJson(String idError,String messageError, ALQueryResult data){
		ResponseWS output = new ResponseWS();
		output.setStatus(idError.toString());
		output.setErrorMessage(messageError);
		output.setData(data == null ? new ALQueryResult() : data);
		return toJson(output);

	}

	/***
	 * METODO QUE CONVIERTE A STRING UNA TRAZA DE EXCEPCION
	 * @param aThrowable
	 * @return
	 */
	public static String getStackTrace(Throwable aThrowable) {
		Writer result = new StringWriter();
		PrintWriter printWriter = new PrintWriter(result);
		aThrowable.printStackTrace(printWriter);
		return result.toString();
	}

	/**
	 * Obtiene un scriopt con sus variables remplazadas por los valores
	 * @param exe
	 * @param idScript
	 * @param folio
	 * @param idUsuario
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static String getScriptFromDB(SQLQueryExecutor exe, Long idScript, Long folio, Long idUsuario) {
		CallableStatement cs = null;
		String query = null;
		java.sql.Clob script = null;
		StringBuilder scriptListo = new StringBuilder();
		try {

			query = "{call ? := EmailEngine.ReplaceTags(?,?,?)}";

			cs = exe.getConnection().prepareCall(query);

			cs.setLong(2, folio);
			cs.setLong(3, idScript);
			cs.setLong(4, idUsuario);

			cs.registerOutParameter(1, OracleTypes.CLOB);

			cs.execute();

			script = cs.getClob(1);
			InputStream in = script.getAsciiStream();
			int caracter = 0;
			while ((caracter = in.read()) != -1) {
				scriptListo.append((char) caracter);
			}
			cs.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return scriptListo.toString();
	}

	/***
	 * Genera archivo txt en SFTP
	 * @param idRemesa
	 * @param Encabezado
	 * @param idftp
	 * @param idLayout
	 * @param fileName
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static String getFileGeneric(SQLQueryExecutor sqlQueryExecutor, Long idRemesa,Boolean Encabezado,Boolean pieDePagina,Long idftp, Long idLayout, String fileName){


		OutputStream os = null;
		PrintStream printStream = null;

		//Escritura de archivo en ftp
		Session     session     = null;
		Channel     channel     = null;
		ChannelSftp channelSftp = null;

		String respuesta = "";

		ALQueryResult r;
		try {
			sqlQueryExecutor.addParameter("idsftp", idftp);
			r = sqlQueryExecutor.executeQuery(QuerysDB.GET_FTP);
			if(r.size() > 0){
				String SFTPHOST = r.get(0,"IP")!=null?r.get(0,"IP").toString():"";
				int    SFTPPORT = r.get(0,"PUERTO")!=null?Integer.parseInt(r.get(0,"PUERTO").toString()):0;
				String SFTPUSER = r.get(0,"USUARIO")!=null?r.get(0,"USUARIO").toString():"";
				String SFTPPASS = r.get(0,"CONTRASENIA")!=null?r.get(0,"CONTRASENIA").toString():"";
				String SFTPWORKINGDIR = r.get(0,"RUTA")!=null?r.get(0,"RUTA").toString():"";

				Long idLayOut = idLayout;
				sqlQueryExecutor.addParameter("idRemesaExport", idRemesa);
				sqlQueryExecutor.addParameter("idLayOut", idLayOut);
				sqlQueryExecutor.addParameter("idRemesa", idRemesa);

				ALQueryResult res = sqlQueryExecutor.executeQuery(QuerysDB.GET_LAYOUTS);
				if(res.size() > 0 ){

					if(null != fileName){

						try{
							/* Conexion con sftp */
							JSch jsch = new JSch();
							session = jsch.getSession(SFTPUSER,SFTPHOST,SFTPPORT);
							session.setPassword(SFTPPASS);
							java.util.Properties config = new java.util.Properties();
							config.put("StrictHostKeyChecking", "no");
							session.setConfig(config);
							session.connect();
							channel = session.openChannel("sftp");
							channel.connect();
							channelSftp = (ChannelSftp)channel;
							channelSftp.cd(SFTPWORKINGDIR);

							try {	

								/*Obtencion de cabeceras y datos para archivo*/

								os = channelSftp.put(SFTPWORKINGDIR+"/"+fileName+(idftp.equals(2l) || idLayOut.equals(17l) || idLayOut.equals(18l)  ? ".csv" :".txt"));
								printStream = new PrintStream(os);

								//Se llena el Ecabezado si lo tiene
								if(Encabezado){
									ALQueryResult encabezado =  sqlQueryExecutor.executeQuery(QuerysDB.GET_ENCABEZADO_LAYOUT);
									for (int i = 0; i < encabezado.size(); i++) {
										if(i == 0){
											printStream.print(encabezado.get(i,"TITULO").toString());
										}else{
											printStream.print(","+encabezado.get(i,"TITULO").toString());
										}
									}
									printStream.print("\n");
								}


								//FOR QUE LLENA EL ARCHIVO
								for (int i = 0; i < res.size(); i++) {
									printStream.println(res.get(i,"LAYOUT"));
								}


								//Se llena el Pie de Pagina si lo tiene
								if(pieDePagina){
									printStream.println("*");
									printStream.println("TOTALDEREGISTROS,"+res.size());
								}

								printStream.close();
								os.close();

								//Genera el bat
								//si la ruta de grabaciones es distinta de null generamos bat
								if(r.get(0,"RUTAGRABACIONES") != null){
									generaBat(sqlQueryExecutor,channelSftp,r.get(0,"RUTAGRABACIONES").toString(),idRemesa,SFTPWORKINGDIR+"/"+ fileName +".bat");
								}

								//Codigo quemada para la genracion del LayOut de los beneficiarios , para el seguro de desempleo de RSA
								if(idLayOut.equals(8l)){
									generBeneficiarios(sqlQueryExecutor,channelSftp,idRemesa,SFTPWORKINGDIR+"/"+"BENEFICIARIOS_"+ fileName +".txt");
								}


							} catch (Exception e) {
								//e.printStackTrace();
								respuesta = "Ocurrio un Error al Crear El Archivo. :"+Util.getStackTrace(e);
							} finally {
								try {
									// Nuevamente aprovechamos el finally para
									// asegurarnos que se cierra el fichero.
									if(printStream!=null)
										printStream.close();
									if(os!=null)
										os.close();

									if(channelSftp.isConnected()){
										channelSftp.exit();
										channel.disconnect();
									}
									if(channel.isConnected())
										channel.disconnect();
									if(session.isConnected())
										session.disconnect();



								} catch (Exception e2) {
									//e2.printStackTrace();
									respuesta = "Ocurrio un Error al Cerrar el Archivo.: "+Util.getStackTrace(e2);
								}
							}


						}catch(Exception ex){
							ex.printStackTrace();
							respuesta = "Ocurrio un Al Conectarse con el SFTP. "+ idftp +":"+Util.getStackTrace(ex);
						}

					}else{
						respuesta = "No Fue Posible Obtener el Nombre del Archivo";
					}
				}else{
					//respuesta = "El Layout No existe en la Base de Datos.";
					respuesta = "No existen Folios Generados para la Fecha Indicada.";
				}
			}else{
				respuesta = "No existe Configuracion en SFTPS para enviar el Archivo";
			}

		} catch (SystemException e1) {
			// TODO Auto-generated catch block
			//e1.printStackTrace();
			respuesta = "Ocurrio un Error al Enviar El Archivo. :"+Util.getStackTrace(e1);
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			//e1.printStackTrace();
			respuesta = "Ocurrio un Error de Base de Datos. :"+Util.getStackTrace(e1);
		}

		return respuesta;
	}


	/***
	 * MEOTOD QUE OBTIENE EL NOMBRE PARA EL ARCHIVO APARTIR DEL QUE TIENE CONFIGURADON EL LAYOUT
	 * @param sqlQueryExecutor
	 * @param idLayOut
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static String getFileNameByLayout(SQLQueryExecutor sqlQueryExecutor, Long idLayOut) {
		String fileName = null;

		sqlQueryExecutor.addParameter("idLayOut", idLayOut);
		ALQueryResult res = new ALQueryResult();

		try {
			res = sqlQueryExecutor.executeQuery(QuerysDB.GET_FILE_NAME_BY_IDLAYOUT);
			fileName = (String) res.get(0,"NAME");
		} catch (SystemException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return fileName;
	}

	/***
	 * METODO QUE GENERA EL LAY OUT DE BENEFICIARIOS
	 * @param sqlQueryExecutor
	 * @param channelSftp
	 * @param idRemesa
	 * @param pathAndNameFile
	 * @throws SystemException
	 * @throws SQLException
	 */
	@SuppressWarnings("rawtypes")
	private static void generBeneficiarios(SQLQueryExecutor sqlQueryExecutor,ChannelSftp channelSftp, Long idRemesa, String pathAndNameFile) throws SystemException, SQLException {

		sqlQueryExecutor.addParameter("idRemesa", idRemesa);

		ALQueryResult folios = sqlQueryExecutor.executeQuery(QuerysDB.GET_BENEFICIARIOS);
		ALQueryResult maxBeneficiarios = sqlQueryExecutor.executeQuery(QuerysDB.GET_MAX_BENEFICIARIOS);

		if(folios.size() != 0 && maxBeneficiarios.size() != 0){



			OutputStream os2 = null;
			PrintStream printStream2  = null;

			try{
				os2 = channelSftp.put(pathAndNameFile);
				printStream2 = new PrintStream(os2);

				//Header
				StringBuffer header = new StringBuffer();
				header.append("Numero de Transacción Partner,");
				for(int i = 0; i < ((Long)maxBeneficiarios.get(0,"MAX")).intValue() ; i++ ){

					header.append("Nombre completo beneficiario ").append(i+1).append(",")
							.append("Parentesco Beneficiario ").append(i+1).append(",")
							.append("Email Beneficiario ").append(i+1).append(",");

				}
				//Escribimos el hearder								
				printStream2.println(header.toString().substring(0,header.toString().length()-1));


				//FOR QUE LLENA GENERA UNA LISTA DE FOLIOS QUE SE PONDRA EN EL BAT
				Long folioAnterior = 0l;
				StringBuffer record = null;

				for (int i = 0; i < folios.size(); i++) {

					Long folio = (Long)folios.get(i,"FOLIO");
					if(!folioAnterior.equals(folio)){

						if( i != 0 && null != record ){
							printStream2.println( record.toString().substring(0,record.toString().length()-1));
						}

						record = new StringBuffer();
						record.append(folio).append(",")
								.append(null == folios.get(i,"NOMBRE") ? "" : folios.get(i,"NOMBRE").toString()).append(",")
								.append(null == folios.get(i,"PARENTESCO") ? "" : folios.get(i,"PARENTESCO").toString()).append(",")
								.append(null == folios.get(i,"EMAIL") ? "" : folios.get(i,"EMAIL").toString()).append(",");
					}else{

						record.append(null == folios.get(i,"NOMBRE") ? "" : folios.get(i,"NOMBRE").toString()).append(",")
								.append(null == folios.get(i,"PARENTESCO") ? "" : folios.get(i,"PARENTESCO").toString()).append(",")
								.append(null == folios.get(i,"EMAIL") ? "" : folios.get(i,"EMAIL").toString()).append(",");
					}
					folioAnterior =  folio;

				}
				///Para llenar el ultimo registro
				printStream2.println(record.toString().substring(0,record.toString().length()-1));

				printStream2.close();
				os2.close();
			}catch (Exception e) {
				e.printStackTrace();
			}finally{
				if(printStream2!=null)
					printStream2.close();
				if(os2!=null)
					try {
						os2.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

			}
		}



	}
	/****
	 * 	MEOTODO QUE GENERA UN BAT
	 * @param sqlQueryExecutor
	 * @param channelSftp
	 * @param rutaGrabaciones
	 * @param idRemesa
	 * @param pathAndNameFile
	 * @throws SQLException
	 * @throws SystemException
	 */
	@SuppressWarnings("rawtypes")
	public static void generaBat(SQLQueryExecutor sqlQueryExecutor,ChannelSftp channelSftp,String rutaGrabaciones,Long idRemesa,String pathAndNameFile) throws SystemException, SQLException{


		StringBuffer listaFolios = new StringBuffer();
		//StringBuffer listaNombres = new StringBuffer();  //Esto era para RSA


		sqlQueryExecutor.addParameter("idRemesa", idRemesa);
		ALQueryResult folios = new ALQueryResult();
		folios = sqlQueryExecutor.executeQuery(QuerysDB.GET_FOLIOS_AND_NAMES_FOR_BAT);


		if(folios.size() != 0){

			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DAY_OF_MONTH, -1);
			String fecha = sdf.format(cal.getTime()).toString();

			//FOR QUE LLENA GENERA UNA LISTA DE FOLIOS QUE SE PONDRA EN EL BAT
			for (int i = 0; i < folios.size(); i++) {
				//Esto era para RSA
				/*if (i  == folios.size()-1 ){
					listaFolios.append(folios.get(i,"IDEXTERNOSOLICITUD"));
					listaNombres.append(folios.get(i,"NOMBRECOMPLETO"));
				}else{
					listaFolios.append(folios.get(i,"IDEXTERNOSOLICITUD")).append(" ");
					listaNombres.append(folios.get(i,"NOMBRECOMPLETO")).append(" ");
				}*/

				listaFolios.append(folios.get(i,"IDEXTERNOSOLICITUD")).append(" ");

			}

			OutputStream os2 = null;
			PrintStream printStream2  = null;
			try{

				os2 = channelSftp.put(pathAndNameFile);
				printStream2 = new PrintStream(os2);
				//Esto era para RSA
				/*printStream2.print( "@ECHO OFF \n"+
						"cd C:\\ \n"+
						"mkdir "+fecha+" \n"+
						"cd "+fecha+" \n"+													
						"setlocal enableextensions enabledelayedexpansion \n"+
						"set listado="+listaFolios.toString()+" \n"+
						"set listado2="+listaNombres.toString()+" \n"+
						"set /A contador=0 \n"+
						"set /A contador2=0	 \n"+													
						"for %%i in (%listado%) do ( \n"+										
						"for %%j in (%listado2%) do ( \n"+
						"if !contador! == !contador2! ( \n"+
						"copy \""+rutaGrabaciones+"%%i.mp3\" \"%%i_%%j.mp3\" /y \n"+
						"set /A contador2+=1 \n"+
						") \n"+
						") \n"+
						"set /A contador+=1 \n"+	
						") \n"+
						"endlocal \n"+
						"echo \"Copia Terminada\" \n"+
						"msg * /w \"Copia Terminada\" \n");

				 */
				printStream2.print("@ECHO OFF\n" + "cd C:\\" + "\nmkdir " + fecha
						+ "\n" + "cd " + fecha + "\n" + "set listado="
						+ listaFolios.toString().substring(0,listaFolios.toString().length()-1) + "\n"
						+ "for %%i in (%listado%) do xcopy \"" + rutaGrabaciones
						+ "%%i.mp3\" \".\" /y\n"
						+"echo \"Copia Terminada\"\n"
						+ "msg * /w \"Copia Terminada\"");

				printStream2.close();
				os2.close();
			}catch (Exception e) {
				e.printStackTrace();
			}finally{
				if(printStream2!=null)
					printStream2.close();
				if(os2!=null)
					try {
						os2.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

			}
		}



	}

	/***
	 * metodo que desahace la remesa ante un erro
	 * @param sqlQueryExecutor
	 * @param idRemesaExport
	 */
	public static void rollbackRemesa(SQLQueryExecutor sqlQueryExecutor,Long idRemesaExport) {
		//RollBackRemesa				
		try {
			sqlQueryExecutor.addParameter("idRemesaExport", idRemesaExport);
			sqlQueryExecutor.executeUpdateUsingString("update tblsolicitudes set idremesaexport = ''  where idremesaexport = {idRemesaExport}");
			sqlQueryExecutor.executeUpdateUsingString("delete from tblremesasexportsolicitudes where idremesaexport = {idRemesaExport}");
			sqlQueryExecutor.executeUpdateUsingString("delete from tblremesasexport where idremesaexport = {idRemesaExport}");
			sqlQueryExecutor.commit();
		} catch (SQLException e) {
			//e.printStackTrace();
		}

	}
	/***
	 * metodo que regresa el directorio maestro de luci
	 * @param sqlQueryExecutor
	 * @return url del archivo maestro 
	 */
	public static String loadDirectorioMaestroSIC(SQLQueryExecutor sqlQueryExecutor){
		String url = "";
		ALQueryResult res = new ALQueryResult();
		try {
			res = sqlQueryExecutor.executeQueryUsingQueryString("select distinct directoriomaestrosic from tblcfgsistema");
			if(res != null && res.size() > 0){
				url = (String) res.get(0, "DIRECTORIOMAESTROSIC");
				if(url != null){
					int size = url.length();
					if(size > 0){
						char ultimoCaracter = url.charAt(size -1);
						if('/' != ultimoCaracter){
							url += "/";
						}
					}
				}
			}
		} catch (SystemException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return url;
	}


	/***
	 *
	 * @param sqlQueryExecutor
	 * @param idsftp
	 * @param fileName
	 * @param file
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static String writeFileInSFTP(SQLQueryExecutor sqlQueryExecutor,Long idsftp,String fileName,ByteArrayOutputStream file){


		OutputStream os = null;
		PrintStream printStream = null;

		//Escritura de archivo en ftp
		Session     session     = null;
		Channel     channel     = null;
		ChannelSftp channelSftp = null;

		String respuesta = "";

		ALQueryResult r;
		try {
			sqlQueryExecutor.addParameter("idsftp", idsftp);
			r = sqlQueryExecutor.executeQuery(QuerysDB.GET_FTP);
			if(r.size() > 0){
				String SFTPHOST = r.get(0,"IP")!=null?r.get(0,"IP").toString():"";
				int    SFTPPORT = r.get(0,"PUERTO")!=null?Integer.parseInt(r.get(0,"PUERTO").toString()):0;
				String SFTPUSER = r.get(0,"USUARIO")!=null?r.get(0,"USUARIO").toString():"";
				String SFTPPASS = r.get(0,"CONTRASENIA")!=null?r.get(0,"CONTRASENIA").toString():"";
				String SFTPWORKINGDIR = r.get(0,"RUTA")!=null?r.get(0,"RUTA").toString():"";


				try {	
					/* Conexion con sftp */
					JSch jsch = new JSch();
					session = jsch.getSession(SFTPUSER,SFTPHOST,SFTPPORT);
					session.setPassword(SFTPPASS);
					java.util.Properties config = new java.util.Properties();
					config.put("StrictHostKeyChecking", "no");
					session.setConfig(config);
					session.connect();
					channel = session.openChannel("sftp");
					channel.connect();
					channelSftp = (ChannelSftp)channel;
					channelSftp.cd(SFTPWORKINGDIR);

					if(null != fileName){
						os = channelSftp.put(SFTPWORKINGDIR+"/"+fileName);
						printStream = new PrintStream(os);
						printStream.write(file.toByteArray());
						printStream.close();
						os.close();
					}

				} catch (Exception e) {
					//e.printStackTrace();
					respuesta = "Ocurrio un Error al Crear El Archivo "+fileName+":"+Util.getStackTrace(e);
				} finally {
					try {
						// Nuevamente aprovechamos el finally para
						// asegurarnos que se cierra el fichero.
						if(printStream!=null)
							printStream.close();
						if(os!=null)
							os.close();

						if(channelSftp.isConnected()){
							channelSftp.exit();
							channel.disconnect();
						}
						if(channel.isConnected())
							channel.disconnect();
						if(session.isConnected())
							session.disconnect();



					} catch (Exception e2) {
						//e2.printStackTrace();
						respuesta = "Ocurrio un Error al Cerrar el Archivo "+fileName+": "+Util.getStackTrace(e2);
					}
				}

			}else{
				respuesta = "No existe Configuracion en SFTPS para enviar el Archivo "+fileName;
			}

		} catch (SystemException e1) {
			// TODO Auto-generated catch block
			//e1.printStackTrace();
			respuesta = "Ocurrio un Error al Enviar El Archivo "+fileName+":"+Util.getStackTrace(e1);
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			//e1.printStackTrace();
			respuesta = "Ocurrio un Error de Base de Datos al momento de entregar el archivo "+fileName+" :"+Util.getStackTrace(e1);
		}

		return respuesta;
	}


	/***
	 * metodo que inserta un log de un web service consumido
	 * @param exe
	 * @param idLogWebService
	 * @param idWebService
	 * @param xmlEntrada
	 * @param xmlSalida
	 * @param folio
	 * @param inicioTransaccion
	 */
	@SuppressWarnings("rawtypes")
	public static void insertLogWs(SQLQueryExecutor exe, Long idLogWebService,Long idWebService,String xmlEntrada, String xmlSalida, Long folio, String inicioTransaccion, String idError) {
		CallableStatement cs = null;
		String query = null;
		try {
			DateFormat inFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			inFormat.setTimeZone(TimeZone.getTimeZone("America/Mexico_City"));
			Date fechaHora = new Date();
			String finTransaccion = inFormat.format(fechaHora);

			query = "insert into TBLLOGWEBSERVICES (" +
					"IDLOGWEBSERVICE, IDWEBSERVICE, XMLENTRADA, XMLSALIDA, FECHAEVENTO, FOLIO, FECHAINICIOTRANSACCION, " +
					"FECHAFINTRANSACCION, IDERROR) values (?, ?, ?, ?, to_date(?,'dd/mm/yyyy HH24:mi:ss'), ?, to_date(?,'dd/mm/yyyy HH24:mi:ss'), to_date(?,'dd/mm/yyyy HH24:mi:ss'), ?)";

			cs = exe.getConnection().prepareCall(query);

			cs.setLong(1, idLogWebService);
			cs.setLong(2, idWebService);
			cs.setString(3, xmlEntrada);
			cs.setString(4, xmlSalida);
			cs.setString(5, finTransaccion);
			if(folio == null){
				cs.setString(6,"");
			}else{
				cs.setLong(6, folio);
			}
			cs.setString(7, inicioTransaccion);
			cs.setString(8, finTransaccion);
			cs.setString(9, idError);


			cs.executeUpdate();
			exe.commit();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}




	/***
	 * METODO QUE ENVIA UNA ALERTA CLASICA DE LUCI
	 * @param idAlerta
	 * @param variables
	 */
	public static void sendAlerta(SQLQueryExecutor sqlQueryExecutor,Long idAlerta, String variables){
		CallableStatement cs = null;
		String query;
		try {

			query = "{call GpEngine.MailAlert(?,?)}";

			cs = sqlQueryExecutor.getConnection().prepareCall(query);
			cs.setLong(1, idAlerta);
			cs.setString(2, variables);;
			cs.execute();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			try {
				cs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}


	/***
	 * Obtiene el folio de una cadena ejemplo "Su tramite tiene el Folio no.1235632."  por me dio de una regex obtiene
	 * el folio  que esta entre puntos "."
	 * @param respuesta
	 * @return
	 */
	public static String getFolioFromResponse(String respuesta){


		String folio = null;
		Pattern pattern = Pattern.compile("\\.(.*?)\\.");
		Matcher matcher = pattern.matcher(respuesta);

		if (matcher.find()){
			folio = matcher.group(1);
		}

		return folio;
	}

	public static void sendPushNotificationGSM(SQLQueryExecutor sqlQueryExecutor,Long idWebService, NotificacionGSM notificacionGSM){

		String output = "";
		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		String inicioTransaccion = format.format(new Date());

		try {

			//Obtenemos la Url para hacer el consumo del Ws
			sqlQueryExecutor.addParameter("idWebService",idWebService);
			ALQueryResult dataWs = sqlQueryExecutor.executeQuery(QuerysDB.GET_WS_DATA);

			Client client = Client.create();

			WebResource webResource = client
					.resource(dataWs.get(0,"URL").toString());

			WebResource.Builder buider = webResource.accept("");

			ClientResponse response = buider
					.header("Content-Type","application/json")
					.header("Authorization",dataWs.get(0,"USUARIO").toString())
					.post(ClientResponse.class,new Gson().toJson(notificacionGSM) );

			output = response.getEntity(String.class);
			System.out.println(output);

		} catch (Exception e) {

			e.printStackTrace();
			output = e.getCause().toString();

		}

		//Insertamos el Low de Web service
		Long idLogWebService;
		try {
			//obtenemos el idLogWebService
			idLogWebService = (Long)sqlQueryExecutor.executeQuery(QuerysDB.GET_IDLODWEBSERVICE).get(0,"IDLOGWEBSERVICE");
			Util.insertLogWs(sqlQueryExecutor, idLogWebService,  idWebService, notificacionGSM.toString() , output, new Long(notificacionGSM.getData().getFolio()), inicioTransaccion, null);
		} catch (SystemException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}

	/***
	 * metodo que actuliza el campo en tblsolciitudesMensajeriasApp confirma recoleccion , para saber que el cliente ya
	 * confirmo via sms que esta listo para su recoleccion de documentos
	 * @param sqlQueryExecutor
	 * @param folio
	 * @param confirmaRecolecion
	 */
	public static void insertConfirmacionRecoleccion(SQLQueryExecutor sqlQueryExecutor, String folio, String confirmaRecolecion) throws Exception {

		try {
			sqlQueryExecutor.addParameter("folio",folio);
			sqlQueryExecutor.addParameter("confirmaRecoleccion", confirmaRecolecion);
			sqlQueryExecutor.executeUpdate(QuerysDB.UPDATE_CONFIRMACION_RECOLECCION);
			sqlQueryExecutor.commit();
		}catch (Exception e){
			throw e;
		}

	}


	/***
	 * La siguiente funcion elimina los acentos de las letras
	 * @param str
	 * @return
	 */
	public static String eliminarAcentos(String str) {

		final String ORIGINAL = "ÁáÉéÍíÓóÚúÑñÜü";
		final String REEMPLAZO = "AaEeIiOoUuNnUu";

		if (str == null) {
			return null;
		}
		char[] array = str.toCharArray();
		for (int indice = 0; indice < array.length; indice++) {
			int pos = ORIGINAL.indexOf(array[indice]);
			if (pos > -1) {
				array[indice] = REEMPLAZO.charAt(pos);
			}
		}


		str = new String(array);
		str = str.toUpperCase();
		if(str.contains("STATE OF")){
			str = str.replace("STATE OF","ESTADO DE");
		}
		if(str.contains("MEXICO CITY")){
			str = "CIUDAD DE MEXICO";
		}
		if(str.contains("NEZAHUALCOYOTL")){
			str = "CIUDAD "+str;
		}
		if(str.contains("CIUDAD LOPEZ MATEOS")){
			str = "CIUDAD ADOLFO LOPEZ MATEOS";
		}

		return str;

	}

    public static boolean continueFromHere(SQLQueryExecutor sqlQueryExecutor, Long folio) {

        String respuesta = "";
        String query = "";
        CallableStatement cs = null;
        Long idRemesaExport = 0L;


        //query = "{call ? := ExportEngine.RSASales(?,?,?,?)}";
        query = "BEGIN FlowEngine.ContinueFromHere(?, 0, 1); END;";

        try {

            cs = sqlQueryExecutor.getConnection().prepareCall(query);

            cs.setLong(1, folio);
            cs.execute();      //ejecucion de funcion RSSALES

            return true;

        } catch (SQLException e) {
            return false;
        } catch (Exception e) {
            return false;
        } finally {
            try {
                cs.close();
            } catch (SQLException e) {
                return false;
            }
        }

    }


	/****
	 * metodo que obtiene un archivo apartir de una url
	 * @param urlFile
	 * @param extension
	 * @return
	 */
	public static File getFileFromUrl(String urlFile,String extension,String prefijo) throws Exception {

		URL url;

		File file = null;
		try {
			// get URL content
			url = new URL(urlFile);
			URLConnection conn = url.openConnection();

			file = File.createTempFile(prefijo,"."+extension );
			FileUtils.copyToFile(conn.getInputStream(), file );

		} catch (Exception e) {
			throw e;
		}

		return file;

	}

	/***
	 * metodo que obtinee un archivo pdf desde el  servidor de jasper
	 * @param sqlQueryExecutor
	 * @param folio
	 * @param idReporte
	 * @return
	 */
	public static File getPdfFromJasperServer(SQLQueryExecutor sqlQueryExecutor,Long folio, Long idReporte,String prefijo,String extension) throws  Exception{

		File file = null;
		ALQueryResult res = new ALQueryResult();

		try {
			sqlQueryExecutor.addParameter("idReporte", idReporte);
			res = sqlQueryExecutor.executeQuery(QuerysDB.GET_REPORT_PATH);
			String rutaReporte = res.get(0, "RUTAREPORTE") == null ? "" : res.get(0, "RUTAREPORTE").toString();

			/***
			 * SI el reporte tiene path se asume que se generara en Jasperserver
			 */
			if (!"".equals(rutaReporte)) {

				//Obtener etiqueta tipo ambiente
				int AMBIENTE_ETIQUETA_ID = -1;
				ALQueryResult ambiente = sqlQueryExecutor.executeQuery(QuerysDB.GET_AMBIENTE_ETIQUETA_ID);
				AMBIENTE_ETIQUETA_ID = Integer.parseInt(ambiente.get(0, "ESTILO").toString());

				//get properties from configuration file
				Properties prop = new Properties();
				prop.load(new FileInputStream("/jasperServer.properties"));
				String ip = prop.getProperty("ip").toString();
				String puerto = prop.getProperty("puerto").toString();
				String contexto = prop.getProperty("contexto").toString();
				String protocolo = "http://";

				StringBuffer url = new StringBuffer();

				if (AMBIENTE_ETIQUETA_ID == 0) {
					//ip = "192.168.100.27";
					//puerto = "8080";
					protocolo = "https://";
				}

				url.append(protocolo).append(ip).append(":").append(puerto).append("/")
						.append(contexto).append("/flow.html?_flowId=viewReportFlow&standAlone=true&Decorate=no&theme=embed&reportUnit=")
						.append(rutaReporte).append(AMBIENTE_ETIQUETA_ID == 3 ? "&j_username=jasperadmin&j_password=jasperadmin" : "" ).append("&folio=").append(folio).append("&output="+((extension!=null && extension!="")?extension:"pdf"));

				file = getFileFromUrl(url.toString(),((extension!=null && extension!="")?extension:"pdf"),prefijo);

			}
		}catch (Exception e){
		 	throw  e;
		}

		return  file;

	}

	public static boolean isEmpty(String s) {
		return ((s == null) || s.equals(""));
	}


	public static ResponseGenericWs consumeGenericWs(SQLQueryExecutor sqlQueryExecutor, RequestGenericWs requestGenericWs, Long idWebService){
		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		//String inicioTransaccion = format.format(new Date());

		ResponseGenericWs responseGenericWs = new ResponseGenericWs();

		/***
		 * Obtemos los datos de WS a consumir
		 */
		sqlQueryExecutor.addParameter("idWebService",idWebService);

		ALQueryResult wsData = new ALQueryResult();

		try {
			wsData = sqlQueryExecutor.executeQuery(QuerysDB.GET_BASIC_DATA_WS);
		} catch (SystemException e) {
			e.printStackTrace();
			responseGenericWs.setDescription("Error:"+e.getMessage() );
		} catch (SQLException e) {
			e.printStackTrace();
			responseGenericWs.setDescription("Error:"+e.getMessage() );
		}

		if(wsData.size() > 0){

			/*Establecemos la ruta*/
			String url= wsData.get(0,"URL").toString();

			/***
			 *  Procedemos a Consumir el WS usando un cliente REST
			 *	Obtenemos los headers y parametros asociados
			 */
			ALQueryResult dataHeaders = new ALQueryResult();
			ALQueryResult dataParams = new ALQueryResult();
			ALQueryResult dataResponses = new ALQueryResult();
			ALQueryResult dataJsonParams = new ALQueryResult();
			ALQueryResult dataUrlParams = new ALQueryResult();

			String accept = "";

			try {
				sqlQueryExecutor.addParameter("idWebService", idWebService);
				sqlQueryExecutor.addParameter("tipo", "H");
				dataHeaders = sqlQueryExecutor.executeQuery(QuerysDB.GET_ELEMENTS_WS);

				sqlQueryExecutor.addParameter("tipo", "P");
				dataParams = sqlQueryExecutor.executeQuery(QuerysDB.GET_ELEMENTS_WS);

				sqlQueryExecutor.addParameter("tipo", "J");
				dataJsonParams = sqlQueryExecutor.executeQuery(QuerysDB.GET_ELEMENTS_WS);

				sqlQueryExecutor.addParameter("tipo", "U");
				dataUrlParams = sqlQueryExecutor.executeQuery(QuerysDB.GET_ELEMENTS_WS);

				sqlQueryExecutor.addParameter("tipo", "R");
				dataResponses = sqlQueryExecutor.executeQuery(QuerysDB.GET_ELEMENTS_WS);


				/***
				 * Para Los parametros convencionales
				 */
				MultivaluedMap<String, String> params =  new MultivaluedMapImpl();
				if(dataParams.size() != 0){
					for (int i = 0; i < dataParams.size(); i++) {
						String nameParameter = dataParams.get(i,"NOMBRE").toString();
						String valueParameter = getValueFromOject(requestGenericWs, nameParameter);
						if(Util.isEmpty(valueParameter )){
							responseGenericWs.setDescription("El parametro "+nameParameter+" No Tiene valor");
							return responseGenericWs;
						}
						params.add(nameParameter ,valueParameter );
					}
					//accept="application/json";
				}

				/***
				 * Creamos un Json Con los Parametros
				 */
				StringBuffer jsonParam = new StringBuffer();
				jsonParam.append("{");
				if(dataJsonParams.size() != 0){
					for (int i = 0; i < dataJsonParams.size(); i++) {
						String nameParameter = dataJsonParams.get(i,"NOMBRE").toString();
						String valueParameter = getValueFromOject(requestGenericWs, nameParameter);
						if(Util.isEmpty(valueParameter )){
							responseGenericWs.setDescription("El parametro "+nameParameter+" No Tiene valor");
							return responseGenericWs;
						}
						jsonParam.append("\"").append(nameParameter).append("\":").append("\"").append(valueParameter).append("\"");
						if( i+1 < dataJsonParams.size()){
							jsonParam.append(",");
						}
					}
					//accept="application/json";
				}
				jsonParam.append("}");


				/***
				 * Asignamos los parametros que van en el path
				 */
				StringBuffer paths = new StringBuffer();
				if(dataUrlParams.size() != 0){
					for (int i = 0; i < dataUrlParams.size(); i++) {
						String nameParameter = dataUrlParams.get(i,"NOMBRE").toString();
						String valueParameter = getValueFromOject(requestGenericWs, nameParameter);
						if(url.contains("{"+nameParameter+"}")){
							url=url.replace("{"+nameParameter+"}",valueParameter);
						}else {
							paths.append("/").append(valueParameter);
						}
					}
					//accept="application/json";
				}


				/***
				 * Asigamos el metodo de envio
				 */
				Client client = Client.create();



				String method = wsData.get(0,"METODO").toString();


				WebResource webResource = client.resource(url.concat(paths.toString())).queryParams(params);
				if(method.equalsIgnoreCase("POST")){
					webResource = client.resource(url);
				}else if(method.equalsIgnoreCase("PUT")){
					webResource = client.resource(url);
				}


				/***
				 * Asignamos los headers de la petion
				 */
				WebResource.Builder buider = webResource.getRequestBuilder();
				for (int i = 0; i < dataHeaders.size(); i++) {
					buider.header(dataHeaders.get(i,"NOMBRE").toString(), replaceMetaData(dataHeaders.get(i,"VALOR").toString(),requestGenericWs));
				}

				/***
				 * /Ejecutamos el Consumo
				 */
				ClientResponse response ;
				if(method.equalsIgnoreCase("POST")){
					response = buider.post(ClientResponse.class,params.size() == 0 ? jsonParam.toString() : params.size());
				}else if(method.equalsIgnoreCase("PUT")){
					response = buider.put(ClientResponse.class,params);
				}else{
					response = buider.get(ClientResponse.class);

				}

				/***
				 * Revisamos la respuesta
				 */
				if (response.getStatus() == 200 && idWebService == 81) {
					responseGenericWs.setSuccess(true);

					File file = null;
					try{
						file = File.createTempFile("formaPago_",".pdf");
						InputStream input = (InputStream)response.getEntity(InputStream.class);

						byte[] byteArray = IOUtils.toByteArray(input);

						FileOutputStream fos = new FileOutputStream(file);
						fos.write(byteArray);
						fos.flush();
						fos.close();
						responseGenericWs.setDescription(file.getPath());
					}catch (Exception e) {
						e.printStackTrace();
						responseGenericWs.setSuccess(false);
					}
				}else if(response.getStatus() == 200 && idWebService != 81){
                    responseGenericWs.setSuccess(true);

                    File file = null;
                    try{
                        file = File.createTempFile("archivo_",".pdf");
                        InputStream input = (InputStream)response.getEntity(InputStream.class);

                        byte[] byteArray = IOUtils.toByteArray(input);

                        FileOutputStream fos = new FileOutputStream(file);
                        fos.write(byteArray);
                        fos.flush();
                        fos.close();
                        responseGenericWs.setDescription(file.getPath());
                    }catch (Exception e) {
                        e.printStackTrace();
                        responseGenericWs.setSuccess(false);
                    }
				}else{
					responseGenericWs.setDescription("Error: Http code "+response.getStatus());
				}

			} catch (SystemException e) {
				e.printStackTrace();
				responseGenericWs.setDescription("Error: "+e.getMessage() );
			} catch (SQLException e) {
				e.printStackTrace();
				responseGenericWs.setDescription("Error: "+e.getMessage() );
			}catch (Exception e) {
				e.printStackTrace();
				responseGenericWs.setDescription("Error: "+e.getMessage() );
			}


		}
		
		return responseGenericWs;
	}

	/****
	 * MEtodo que Remplaza de un metada
	 * @param param
	 * @param requestGenericWs
	 * @return
	 */
	private static String replaceMetaData(String param,Object requestGenericWs) {
		List<String> tagValues = new ArrayList<String>();

		Pattern TAG_REGEX = Pattern.compile("\\{(.+?)\\}");
		Matcher matcher = TAG_REGEX.matcher(param);
		while (matcher.find()) {
			tagValues.add(matcher.group(1));
		}

		String nombreCampo = "";
		if(tagValues.size() != 0){
			String name  =  tagValues.get(0);
			String value = getValueFromOject(requestGenericWs,tagValues.get(0));
			nombreCampo  = param.replace("{"+name+"}", value);
		}else{
			nombreCampo = param;
		}

		return nombreCampo;
	}

	/*****
	 * METODO QUE USANDO REFLEXION OBTINE EL VALOR DEL PARAMETRO
	 * @param requestGenericWs
	 * @param nameParameter
	 * @return
	 */
	public static String getValueFromOject(Object requestGenericWs, String nameParameter){

		String value ="";

		Class<?> objetoDeClassConInfoDeMiClase = requestGenericWs.getClass();

		Method metodo;
		try {

			String methodName = "get"+(nameParameter.charAt(0)+"").toUpperCase()+nameParameter.substring(1);
			metodo = objetoDeClassConInfoDeMiClase.getDeclaredMethod( methodName);
			value = (String)metodo.invoke(requestGenericWs);

		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}

		return value;
	}

	/***
	 *
	 * @param url
	 * @return
	 * @throws IOException
	 */
	public static File readPdfFromURL(String url) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		boolean allOK = false;
		InputStream is1 = null;
		File file = null;
		try {


			try {
				// Read the PDF from the URL and save to a local file
				file = new File(url);
				is1 = new FileInputStream(file);
				int read;
				byte[] buff = new byte[256];
				while((read = is1.read(buff)) != -1) {
					baos.write(buff, 0, read);
				}
				baos.flush();
				// Load the PDF document and display its page count
				//System.out.print("DONE.\nProcessing the PDF ... ");
				allOK = true;
				//file.delete();
			} catch (ConnectException ce) {
				System.out.println("FAILED.\n[" + ce.getMessage() + "]\n");
			} catch (IOException ioe) {
				System.out.println("FAILED.\n[" + ioe.getMessage() + "]\n");
			} finally {
				if (null != baos) {
					baos.close();
				}
				if (null != is1) {
					is1.close();
				}
			}

		} catch(MalformedURLException me) {
			System.out.println("FAILED.\n[" + me.getMessage() + "]\n");
		} catch (NullPointerException npe) {
			System.out.println("FAILED.\n[" + npe.getMessage() + "]\n");
		}

		return allOK ? file : null;
	}

	public static Long getIdExternoSolicitud(SQLQueryExecutor sqlQueryExecutor, Long folio){
		Long idExternoSolicitud = 0L;
		ALQueryResult resultado = null;
		try{
			sqlQueryExecutor.addParameter("folio", folio);
			resultado = sqlQueryExecutor.executeQueryUsingQueryString("select DECODE(idexternosolicitud, null, '0', idexternosolicitud) as ID from tblsolicitudes where folio = {folio} ");

			if(resultado.size() > 0){
				if(!resultado.get(0, "ID").equals("0")) {
					idExternoSolicitud = new Long(resultado.get(0, "ID").toString());
				}
			}


		}catch(SQLException e) {
			return 0L;
		}

		return  idExternoSolicitud;
	}

	/***
	 * metodo que envia un email con adjuntos
	 * @param idNotificacionEmail
	 * @param folio
	 * @param adjuntos
	 */
	public static void sendEmailWihAttachments(SQLQueryExecutor sqlQueryExecutor, Long idNotificacionEmail,String email, Long folio ,Long idUsuario,String rfc,ArrayList<File> adjuntos,boolean sendCCO) throws Exception{


		//Obtenemos los datos de la cuenta con la que se envia el correo
		sqlQueryExecutor.addParameter("idNotificacionEmail",idNotificacionEmail);
		ALQueryResult dataEmail = sqlQueryExecutor.executeQuery(QuerysDB.GET_DATA_FOR_EMAIL);

		if(dataEmail.size() > 0) {
			Properties props = new Properties();
			props.put("mail.smtp.host", dataEmail.get(0, "HOST").toString());
			props.put("mail.smtp.socketFactory.port", dataEmail.get(0, "PUERTO").toString());
			props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
			props.put("mail.smtp.auth", "true");
			props.put("mail.smtp.port", dataEmail.get(0, "PUERTO").toString());

			final String login = dataEmail.get(0, "LOGIN").toString();
			final String password = dataEmail.get(0, "PASSWORD").toString();
			javax.mail.Session session = javax.mail.Session.getInstance(props,
					new javax.mail.Authenticator() {
						protected PasswordAuthentication getPasswordAuthentication() {
							return new PasswordAuthentication(login, password);
						}
					});

			//Obtenemos el Cuerpo del Correo
			Long idScript = (Long) dataEmail.get(0, "IDSCRIPT");
			String body = Util.getScriptFromDB(sqlQueryExecutor, idScript, folio, idUsuario);

			try {
				// datos del mail
				Message message = new MimeMessage(session);
				message.setFrom(new InternetAddress(dataEmail.get(0, "EMAIL").toString()));

				///Enviamos al CLiente y ala responsable de facturacion
				if(sendCCO) {
					message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
					message.setRecipients(Message.RecipientType.BCC, InternetAddress.parse(dataEmail.get(0, "EMAIL").toString()));
				}else{
					//Enviamos solo a responsable de facturacion
					message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
				}
				message.setSubject(dataEmail.get(0, "ASUNTO").toString() +" "+ rfc);

				// cuerpo del mail
				Multipart multipart = new MimeMultipart("mixed");
				BodyPart htmlText = new MimeBodyPart();

				htmlText.setContent(body, "text/html; charset=\"ISO-8859-1\"");
				//2) create new MimeBodyPart object and set DataHandler object to this object
				multipart.addBodyPart(htmlText);


				//adjuntamos los archivod que se encentran en /sicimporter/ mas la ruta definida en tbladjuntosreporte
				if (adjuntos.size() > 0) {

					for (int i = 0; i < adjuntos.size(); i++) {
						MimeBodyPart attachementAdjunto = new MimeBodyPart();
						attachementAdjunto.setHeader("Content-Transfer-Encoding", "base64");
						DataSource sourceAdjunto = new FileDataSource(adjuntos.get(i).getPath());
						attachementAdjunto.setDataHandler(new DataHandler(sourceAdjunto));
						attachementAdjunto.setFileName(adjuntos.get(i).getName());
						multipart.addBodyPart(attachementAdjunto);
					}
				}

				//MimeBodyPart attachement = new MimeBodyPart();
				message.setHeader("Content-Type", "multipart/mixed");
				message.setContent(multipart);

				// enviar mail
				Transport.send(message);


			} catch (Exception e) {
				throw e;
			}finally {
				///Borramos los archivos
				for (int i = 0; i < adjuntos.size() ; i++) {
					adjuntos.get(i).delete();
				}
			}
		}
	}

	public static Long getFolioByIdCorporation(SQLQueryExecutor sqlQueryExecutor, Long idCorporation){
		Long folio = 0L;
		ALQueryResult resultado = null;
		try{
			sqlQueryExecutor.addParameter("idCorporation", idCorporation);
			resultado = sqlQueryExecutor.executeQueryUsingQueryString("select count(*) as EXISTE from tblsolicitudes where idexternosolicitud = '"+idCorporation+"' ");

			if(resultado.size() > 0){
				if(!resultado.get(0, "EXISTE").toString().equals("0")) {
					resultado = sqlQueryExecutor.executeQueryUsingQueryString("select folio as FOLIO from tblsolicitudes where idexternosolicitud = '"+idCorporation+"' ");
					if(resultado.size() > 0){
						folio = new Long(resultado.get(0, "FOLIO").toString());
					}

				}
			}


		}catch(SQLException e) {
			return 0L;
		}

		return  folio;
	}


	/**
	 *
	 * @param idAdjunto
	 * @param folio
	 * @return
	 */
	public static PDFResult generatePDF(SQLQueryExecutor sqlQueryExecutor, Long idAdjunto, Long folio) {
		PDFResult result = new PDFResult();
		if (null != idAdjunto && null != folio) {
			try {


				ALQueryResult res = new ALQueryResult();

				sqlQueryExecutor.addParameter("idReporte", idAdjunto);
				res = sqlQueryExecutor.executeQuery(QuerysDB.GET_REPORT_PATH);
				String rutaReporte = res.get(0, "RUTAREPORTE") == null ? "" : res.get(0, "RUTAREPORTE").toString();

				/***
				 * SI el reporte tiene path se asume que se generara en Jasperserver
				 */
				if( !"".equals(rutaReporte) ){
					//Obtener etiqueta tipo ambiente
					int AMBIENTE_ETIQUETA_ID = -1;
					ALQueryResult ambiente = sqlQueryExecutor.executeQuery(QuerysDB.GET_AMBIENTE_ETIQUETA_ID);

					if (ambiente.size() != 1) {
						result.setMessage("Ocurrió un error al recuperar el TIPO DE AMBIENTE del sistema.");
					} else {

						AMBIENTE_ETIQUETA_ID = Integer.parseInt(ambiente.get(0, "ESTILO").toString());

						//obtemos el numero de poliza lo necesitarremos para guardar el pdf con ese nombre en el sfpt
						ALQueryResult noPoliza = sqlQueryExecutor.executeQuery(QuerysDB.GET_NUMERO_POLIZA);
						if(noPoliza.size() != 0){
							result.setIdExternoSolitud(noPoliza.get(0,"IDEXTERNOSOLICITUD") == null ? null : noPoliza.get(0,"IDEXTERNOSOLICITUD").toString());
						}

						//get properties from configuration file
						Properties prop = new Properties();
						prop.load(new FileInputStream("/jasperServer.properties"));
						String ip = prop.getProperty("ip").toString();
						String puerto = prop.getProperty("puerto").toString();
						String contexto = prop.getProperty("contexto").toString();

						StringBuffer url = new StringBuffer();
						System.out.println("Generacion de URL para reporte...");
						System.out.println("Concatenacion...");

						if(AMBIENTE_ETIQUETA_ID == 0){
							ip = "192.168.100.27";
							puerto = "8080";
						}
						url.append("http://").append(ip).append(":").append(puerto).append("/")
								.append(contexto).append("/flow.html?_flowId=viewReportFlow&standAlone=true&Decorate=no&theme=embed&reportUnit=")
								.append(rutaReporte).append("&folio=").append(folio).append("&output=pdf");

						result.setUrl(url.toString());
						result.setNombreReporte(res.get(0, "REPORTE").toString());

					}
				}

				/***
				 * Obtnemos la inforamcion para los adjuntos estaticos
				 */
				//seteamos el idsfpt si el reporte tiene
				result.setIdSftp(res.get(0,"IDSFTP") == null ? "" : res.get(0,"IDSFTP").toString());

				//seteamos el tipo
				result.setTipo(res.get(0,"TIPO") == null ? "" : res.get(0,"TIPO").toString());

				//seteamos el idAdjunto que es el idReporte
				result.setIdAdjunto(idAdjunto.toString());

				//verificamos si existen más archivos adjuntos
				res = sqlQueryExecutor.executeQuery(QuerysDB.GET_RUTA_ADJUNTA_REPORTE);
				if (res.size() != 0) {
					StringBuffer urlAdjunto = new StringBuffer();
					String ruta = res.get(0, "RUTA").toString();
					String urlMaestro = com.a4sys.luciWS.util.Util.loadDirectorioMaestroSIC(sqlQueryExecutor);
					String nombresArchivos = "";
					urlAdjunto.append(urlMaestro).append(ruta);
					result.setRutaAdjuntoReporte(""+urlAdjunto.toString());

					for(int i=0; i<res.size(); i++){
						if(i == 0){
							nombresArchivos = res.get(0, "NOMBREARCHIVOADJUNTO").toString();
						}else{
							nombresArchivos = nombresArchivos +","+res.get(i, "NOMBREARCHIVOADJUNTO").toString();
						}
					}
					result.setNombreArchivos(""+nombresArchivos.toString());

				}

			} catch (SQLException e0) {
				e0.printStackTrace();
				result.setMessage("Error: " + e0.getMessage());

			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
				result.setMessage("Error: " + e1.getMessage());

			} catch (IOException e2) {
				e2.printStackTrace();
				result.setMessage("Error: " + e2.getMessage());

			} catch (Exception e3) {
				e3.printStackTrace();
				result.setMessage("Error: " + e3.getMessage());
			}
		} else {
			result.setMessage(WsErrors.WS_ERROR_1);
		}
		return result;
	}

	public static Long getCodigoPais(SQLQueryExecutor sqlQueryExecutor, String telefono){
		Long codigoPais = 0L;
		ALQueryResult resultado = null;
		try{
			sqlQueryExecutor.addParameter("telefono", telefono);
			resultado = sqlQueryExecutor.executeQuery(QuerysDB.IS_TELEPHONE_VALID_IN_COFETEL);

			if(resultado.size() > 0){
				if(resultado.get(0, "RES").toString().equals("0")) {
					codigoPais = 1L;
				}else{
					codigoPais = 52L;
				}
			}


		}catch(SQLException e) {
			return 0L;
		}

		return  codigoPais;
	}

}

