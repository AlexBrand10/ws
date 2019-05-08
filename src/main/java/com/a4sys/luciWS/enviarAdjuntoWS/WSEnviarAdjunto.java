package com.a4sys.luciWS.enviarAdjuntoWS;

//import java.sql.SQLException;
import com.a4sys.luciWS.domain.ResponseWS;
import com.a4sys.luciWS.util.Util;
import com.a4sys.luciWS.util.WsErrors;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;

@Path("/adjunto")
public class WSEnviarAdjunto {

	/***
	 * /***
	 * METODO QUE RESIBE LA PETICION Y LA ENRUTA PARA SU PROCESAMIENTO
	 * @param method
	 * @param subject
	 * @param host
	 * @param port
	 * @param fromEmail
	 * @param theUser
	 * @param thePass
	 * @param toEmail
	 * @param idAdjunto
	 * @param isSecure
	 * @param folio
	 * @param idScript
	 * @param idUsuario
	 * @return
	 */
	@GET
	@Path("/rsa/{method}")
	public  Response adjuntoGet(
			@PathParam("method") String method, 	@QueryParam("subject") String subject, 		@QueryParam("host") String host,
			@QueryParam("port") String port, 		@QueryParam("fromEmail") String fromEmail, 	@QueryParam("theUser") String theUser,
			@QueryParam("thePass") String thePass,	@QueryParam("toEmail") String toEmail, 		@QueryParam("idAdjunto") Long idAdjunto,
			@QueryParam("isSecure") Integer isSecure, @QueryParam("folio") Long folio, 		@QueryParam("idScript") Long idScript,
			@QueryParam("idUsuario") Long idUsuario) { 

		ResponseBuilder response = null;        

		WSEnviarAdjuntoService service = new WSEnviarAdjuntoService();
		try {

			if(method.equals("enviarAdjunto")) {
				PDFResult result = service.generatePDF(idAdjunto, folio);
				String body = service.getBodyPart(folio, idScript, idUsuario);
				response = sendMail(subject, host, port, fromEmail, theUser, thePass, toEmail, body, isSecure, result,idAdjunto);				
			}else if(method.equals("enviarEmail")){
				String body = service.getBodyPart(folio, idScript, idUsuario);
				response = sendMail(subject, host, port, fromEmail, theUser, thePass, toEmail, body, isSecure);
			}else {
				response = Response.ok(Util.setResponseJson("9", WsErrors.WS_ERROR_9, null), MediaType.APPLICATION_JSON + ";charset=utf-8");			
			}
		} catch (Exception e) {
			e.printStackTrace();
			response = Response.ok(Util.setResponseJson("-1", e.getCause() == null ? e.getMessage() : e.getCause().toString(), null), MediaType.APPLICATION_JSON + ";charset=utf-8");
		}finally{
			service.closeService();
		}
		return response.build();
	}

	/****
	 * metodo que envia un mail sin adjunto
	 * @param subject
	 * @param host
	 * @param port
	 * @param fromEmail
	 * @param theUser
	 * @param thePass
	 * @param toEmail
	 * @param body
	 * @param isSecure
	 * @return
	 */
	private ResponseBuilder sendMail(String subject, String host, String port,
			String fromEmail, final String theUser, final String thePass, String toEmail,
			String body, Integer isSecure) {
		ResponseWS output = new ResponseWS();
		ResponseBuilder response = null;
		String entity = null;

		//Incializamos variable con mensaje De error
		output = Util.setResponse("1", WsErrors.WS_ERROR_1, null);

		boolean dataIncomplete = null == subject || null == host || null == port || null == fromEmail || null == theUser  || null == thePass || null == toEmail || null == body ||
				subject.equals("") || host.equals("") || port.equals("") || fromEmail.equals("") || theUser.equals("") || thePass.equals("") || toEmail.equals("") ||  body.equals("");

		if (null == isSecure || (!isSecure.equals(1) && !isSecure.equals(0))) {
			isSecure = 0;
		}

		if (!dataIncomplete) {

			Properties props = new Properties();
			props.put("mail.smtp.host", host);
			props.put("mail.smtp.socketFactory.port", port);
			if (isSecure.equals(1)) {
				props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
			}
			props.put("mail.smtp.auth", "true");
			props.put("mail.smtp.port", port);

			Session session = Session.getInstance(props,
					new javax.mail.Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(theUser,
							thePass);
				}
			});



			try {
				// datos del mail
				Message message = new MimeMessage(session);
				message.setFrom(new InternetAddress(fromEmail));
				message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
				message.setSubject(subject);

				// cuerpo del mail
				Multipart multipart = new MimeMultipart("related");
				BodyPart htmlText = new MimeBodyPart();
				//2) create new MimeBodyPart object and set DataHandler object to this object				

				htmlText.setContent(body,"text/html; charset=\"ISO-8859-1\"");
				multipart.addBodyPart(htmlText);
				message.setContent(multipart);


				// enviar mail
				Transport.send(message);
				output = Util.setResponse("0", "El e-mail ha sido enviado correctamente.", null);


			} catch (Exception e) {
				e.printStackTrace();
				// TODO: handle exception
				output = Util.setResponse("-1", e.getMessage(), null);
			}
		} else {
			output = Util.setResponse("13", WsErrors.WS_ERROR_13, null);
		}

		//Return response
		entity = Util.toJson(output);
		response = Response.ok(entity).type(MediaType.APPLICATION_JSON + ";charset=utf-8");
		response.header("Access-Control-Allow-Origin", "*");
		return response;
	}

	/****
	 *
 	 * @param subject
	 * @param host
	 * @param port
	 * @param fromEmail
	 * @param theUser
	 * @param thePass
	 * @param toEmail
	 * @param body
	 * @param isSecure
	 * @param pdfResult
	 * @param idAdjunto
	 * @return
	 */
	public ResponseBuilder sendMail(String subject, String host, String port,
			String fromEmail, final String theUser, final String thePass, String toEmail, String body, Integer isSecure,PDFResult pdfResult, Long idAdjunto)  {

		ResponseWS output = new ResponseWS();
		ResponseBuilder response = null;
		String entity = null;

		//Incializamos variable con mensaje De error
		output = Util.setResponse("1", WsErrors.WS_ERROR_1, null);

		boolean dataIncomplete = null == subject || null == host || null == port || null == fromEmail || null == theUser  || null == thePass || null == toEmail ||  null == body ||
				subject.equals("") || host.equals("") || port.equals("") || fromEmail.equals("") || theUser.equals("") || thePass.equals("") || toEmail.equals("")  || body.equals("");

		if (null == isSecure || (!isSecure.equals(1) && !isSecure.equals(0))) {
			isSecure = 0;
		}

		if (!dataIncomplete) {

			Properties props = new Properties();
			props.put("mail.smtp.host", host);
			props.put("mail.smtp.socketFactory.port", port);
			if (isSecure.equals(1)) {
				props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
			}
			props.put("mail.smtp.auth", "true");
			props.put("mail.smtp.port", port);

			Session session = Session.getInstance(props,
					new javax.mail.Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(theUser,
							thePass);
				}
			});

			try {
				// datos del mail
				Message message = new MimeMessage(session);
				message.setFrom(new InternetAddress(fromEmail));
				message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
				message.setSubject(subject);

				// cuerpo del mail
				Multipart multipart = new MimeMultipart("mixed");
				BodyPart htmlText = new MimeBodyPart();

				htmlText.setContent(body, "text/html; charset=\"ISO-8859-1\"");
				//2) create new MimeBodyPart object and set DataHandler object to this object												
				multipart.addBodyPart(htmlText);
				
				
				/***
				 * Entra a Esta parte si el Tipo es 3 genera un archivo con JasperServer
				 */
				ByteArrayOutputStream baos = null;
				if(pdfResult.getTipo().equals("3")){
					
					System.out.println("Exact path--->" + pdfResult.getUrl());					
					try {
						baos = readPdfFromURL(pdfResult.getUrl());
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					if (baos != null) {
						MimeBodyPart attachement = new MimeBodyPart();
						attachement.setHeader("Content-Transfer-Encoding", "base64");
						DataSource ds = new ByteArrayDataSource(baos.toByteArray(), "application/pdf");
						attachement.setDataHandler(new DataHandler(ds));
						attachement.setFileName(pdfResult.getNombreReporte()+".pdf");
						multipart.addBodyPart(attachement);
					} else {
						  throw new Exception();
					}				
				}


				//adjuntamos los archivod que se encentran en /sicimporter/ mas la ruta definida en tbladjuntosreporte
				if(pdfResult.getRutaAdjuntoReporte().length() > 0){

					String[] archivosAdjuntos = pdfResult.getNombreArchivos().split(",");
					for (int i=0; i<archivosAdjuntos.length; i++){

						MimeBodyPart attachementAdjunto = new MimeBodyPart();
						attachementAdjunto.setHeader("Content-Transfer-Encoding", "base64");
						DataSource sourceAdjunto = new FileDataSource(pdfResult.getRutaAdjuntoReporte()+""+archivosAdjuntos[i]);
						attachementAdjunto.setDataHandler(new DataHandler(sourceAdjunto));
						attachementAdjunto.setFileName(archivosAdjuntos[i]);
						multipart.addBodyPart(attachementAdjunto);
					}
				}
				
				//MimeBodyPart attachement = new MimeBodyPart();
				message.setHeader("Content-Type", "multipart/mixed");
				message.setContent(multipart);

				// enviar mail
				Transport.send(message);
				output = Util.setResponse("0", "El e-mail ha sido enviado correctamente.", null);

				//Si se evio correctamente lo guardamos en el sftp que tenga configurado el reporte
				if(!"".equals(pdfResult.getIdSftp()) && null != pdfResult.getIdExternoSolitud()  && baos != null){												
					WSEnviarAdjuntoService service =  new WSEnviarAdjuntoService();
					String res =service.writeFileSFTP(pdfResult.getIdSftp(), pdfResult.getIdExternoSolitud()+".pdf", baos);
					if(!"".equals(res)){
						service.sendAlerta(38L, res);
					}
					service.closeService();
				}


			} catch (Exception e) {
				e.printStackTrace();
				// TODO: handle exception
				output = Util.setResponse("-1", e.getMessage(), null);
			}

		}
		//Return response
		entity = Util.toJson(output);
		response = Response.ok(entity).type(MediaType.APPLICATION_JSON + ";charset=utf-8");
		response.header("Access-Control-Allow-Origin", "*");
		return response;
	}

	/***
	 * 
	 * @param url
	 * @return
	 * @throws IOException
	 */
	private ByteArrayOutputStream readPdfFromURL(String url) throws IOException {
		URL url1;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		boolean allOK = false;
		InputStream is1 = null;
		try {
			url1 = new URL(url);
			//Contacting the URL
			System.out.print("Connecting to " + url1.toString() + " ... ");
			URLConnection urlConn = url1.openConnection();

			// Checking whether the URL contains a PDF
			if (!urlConn.getContentType().equalsIgnoreCase("application/pdf")) {
				System.out.println("FAILED.\n[Sorry. This is not a PDF.]");
			} else {
				try {
					// Read the PDF from the URL and save to a local file
					is1 = url1.openStream();
					int read;
					byte[] buff = new byte[256];
					while((read = is1.read(buff)) != -1) {
						baos.write(buff, 0, read);
					}
					baos.flush();
					// Load the PDF document and display its page count
					System.out.print("DONE.\nProcessing the PDF ... ");
					allOK = true;
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
			}
		} catch(MalformedURLException me) {
			System.out.println("FAILED.\n[" + me.getMessage() + "]\n");
		} catch (NullPointerException npe) {
			System.out.println("FAILED.\n[" + npe.getMessage() + "]\n");
		}
		return allOK ? baos : null;
	}

	public static File[] getPdfsFromUrl(String fileSource) throws IOException {
		File pathFolderCopy = null;

		pathFolderCopy = new File(fileSource);
		File[] archivosCopy = null;


		archivosCopy = pathFolderCopy.listFiles();
		System.out.println("archivosCopy: "+archivosCopy.length);
		return archivosCopy;
	}
}
