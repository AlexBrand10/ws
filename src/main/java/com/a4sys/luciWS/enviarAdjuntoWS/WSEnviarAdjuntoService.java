package com.a4sys.luciWS.enviarAdjuntoWS;

//import java.sql.SQLException;
import java.util.Properties;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.CallableStatement;
import java.sql.SQLException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import com.a4sys.core.dao.SQLQueryExecutor;
import com.a4sys.luciWS.domain.ResponseWS;
import com.a4sys.luciWS.util.SystemConstants;
import com.a4sys.luciWS.util.Util;
import com.a4sys.luciWS.util.WsErrors;
import com.a4sys.luciWS.util.Util;
import com.a4sys.core.dao.ALQueryResult;
import com.a4sys.core.dao.DaoService;
import com.a4sys.luciWS.util.QuerysDB;


public class WSEnviarAdjuntoService extends DaoService {

	/***
	 * Constructor de la Clase
	 */
	public WSEnviarAdjuntoService() {
		super(SystemConstants.JDNI);

	}

	/**
	 *
	 * @param idAdjunto
	 * @param folio
	 * @return
	 */
	public PDFResult generatePDF( Long idAdjunto, Long folio) {
		return Util.generatePDF(sqlQueryExecutor, idAdjunto, folio);
	}

	/**
	 * 
	 * @param folio
	 * @param idScript
	 * @param idUser
	 * @return
	 */
	public String getBodyPart(Long folio, Long idScript, Long idUser) {
		return Util.getScriptFromDB(sqlQueryExecutor, idScript, folio, idUser);
	}


	/***
	 * metodo que escribe el un archvio en un sftp
	 * @param idSftp
	 * @param nombreReporte
	 * @param baos
	 * @return
	 */
	public String writeFileSFTP(String idSftp, String nombreReporte ,ByteArrayOutputStream file) {	
		return Util.writeFileInSFTP(sqlQueryExecutor, Long.parseLong(idSftp), nombreReporte, file);
	}

	/***
	 * METODO QUE ENVIA UNA ALERTA CLASICA DE LUCI
	 * @param idAlerta
	 * @param variables
	 */
	public void sendAlerta(Long idAlerta, String variables){
		CallableStatement cs = null;
		String query;
		try {

			query = "{call GpEngine.MailAlert(?,?)}";

			cs = sqlQueryExecutor.getConnection().prepareCall(query);
			cs.setLong(1, idAlerta); // Alerta de Aviso de Resmas RSA			
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



}