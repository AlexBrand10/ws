package com.a4sys.luciWS.facturaDigital;

import com.a4sys.core.dao.ALQueryResult;
import com.a4sys.core.dao.DaoService;
import com.a4sys.luciWS.amazonS3.AmazonS3Service;
import com.a4sys.luciWS.domain.ResponseGenericWs;
import com.a4sys.luciWS.domain.RequestGenericWs;
import com.a4sys.luciWS.domain.ResponseWS;
import com.a4sys.luciWS.facturaDigital.model.*;
import com.a4sys.luciWS.util.QuerysDB;
import com.a4sys.luciWS.util.SystemConstants;
import com.a4sys.luciWS.util.Util;
import com.google.gson.Gson;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import javax.ws.rs.core.MultivaluedMap;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * Created by mmorones on 5/30/18.
 */
public class FacturaDigitalService extends DaoService {

    public FacturaDigitalService() {
        super(SystemConstants.JDNI);
    }


    /***
     * metodo que genera una factura de venta al publico
     * @param folio
     * @param idUsuario
     * @return
     */
    public ResponseWS generaFacturaPublico(Long folio,Long idUsuario){
        ResponseWS finalResponse  = new ResponseWS();
        finalResponse.setStatus("-1");
        Long idWebService = 80L;
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String inicioTransaccion = format.format(new Date());
        Gson gson = new Gson();
        try {

            //Obtenemos los datos del cliente para facturar
            sqlQueryExecutor.addParameter("folio",folio);
            ALQueryResult datosPersonales = sqlQueryExecutor.executeQuery(QuerysDB.GET_DATOS_PARA_FACTURAR);
            if(datosPersonales.size() > 0) {

                String requiereFactura = datosPersonales.get(0,"REQUIEREFACTURA").toString();
                ArrayList<File> files = new ArrayList<File>();
                if( requiereFactura.equals("N")){

                    String rfc = "XAXX010101000";
                    //Obtenemos el Pdf de la factura
                    files = getFactura(datosPersonales,folio, idUsuario);

                    //Enviamos el Correo con el pdf y el xml
                    if(files.size() > 0 ) {

                        sqlQueryExecutor.addParameter("idNotificacionEmail",1L);
                        ALQueryResult dataEmail = sqlQueryExecutor.executeQuery(QuerysDB.GET_DATA_FOR_EMAIL);
                        Util.sendEmailWihAttachments(sqlQueryExecutor,1L, dataEmail.get(0, "EMAIL").toString(), folio, idUsuario, rfc, files, false);
                        finalResponse.setStatus("0");
                        finalResponse.setErrorMessage("Generación de Factura Exitosa");

                    }else {
                        finalResponse.setErrorMessage("Ocurrio un error al Generar la Factura , favor de revisar el log de consumo.");
                    }

                }else{
                    finalResponse.setErrorMessage("Ya se genero  una factura para este folio : "+folio);
                }

            }else{
                finalResponse.setErrorMessage("No existen Datos Para Facturar para el Folio : "+ folio);
            }

        }catch (Exception e) {
            try {
                sqlQueryExecutor.rollBack();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
            finalResponse.setErrorMessage(e.getMessage());
        }
        try {
            //obtenemos el idLogWebService
            Long idLogWebService = (Long) sqlQueryExecutor.executeQuery(QuerysDB.GET_IDLODWEBSERVICE).get(0, "IDLOGWEBSERVICE");
            Util.insertLogWs(sqlQueryExecutor, idLogWebService, idWebService, "folio: "+folio+", idUsuario:"+idUsuario, finalResponse.toString(), folio, inicioTransaccion,null);

        }catch (Exception e){
            e.printStackTrace();
        }


        return finalResponse;
    }


    /***
     * metodo que genera  a una RFC valido o una nota de venta segun sea el caso
     * @param folio
     * @return
     */
    public ResponseWS generarFacturaOrNotaVenta(Long folio,Long idUsuario) {

        ResponseWS finalResponse  = new ResponseWS();
        finalResponse.setStatus("-1");
        Long idWebService = 80L;
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String inicioTransaccion = format.format(new Date());
        Gson gson = new Gson();

        try {

            //Obtenemos los datos del cliente para facturar
            sqlQueryExecutor.addParameter("folio",folio);
            ALQueryResult datosPersonales = sqlQueryExecutor.executeQuery(QuerysDB.GET_DATOS_PARA_FACTURAR);
            if(datosPersonales.size() > 0){

                String email = datosPersonales.get(0,"EMAIL").toString();
                String requiereFactura = datosPersonales.get(0,"REQUIEREFACTURA").toString();
                ArrayList<File> files = new ArrayList<File>();

                if( requiereFactura.equals("S")){

                    String rfc = datosPersonales.get(0,"RFC") == null ?  "" : datosPersonales.get(0,"RFC").toString();
                    //Obtenemos el Pdf de la factura
                    files = getFactura(datosPersonales,folio, idUsuario);
                    //Enviamos el Correo con el pdf y el xml
                    if(files.size() > 0) {
                        Util.sendEmailWihAttachments(sqlQueryExecutor,1L, email, folio, idUsuario, rfc, files, true);
                    }

                }else{

                    Long idFormaPago = (Long)datosPersonales.get(0,"IDFORMAPAGO");
                    //Generamos la Nota de Venta
                    File notaVenta = getNotaVenta(folio);
                    files.add(notaVenta);

                    if(idFormaPago == 2) {//Generamos el pdf de la referencia bancaria
                        File pdfReferenciaBancaria = getPdfReferenciaBancaria(folio);
                        if(pdfReferenciaBancaria != null) {
                            files.add(pdfReferenciaBancaria);
                        }
                    }

                    //Enviamos el Correo con el pdf y el xml
                    if(files.size() > 0) {
                        Util.sendEmailWihAttachments(sqlQueryExecutor,2L, email, folio, idUsuario, "", files, true);
                    }

                }

                if(files.size() > 0 ) {
                    finalResponse.setStatus("0");
                    finalResponse.setErrorMessage("Generación de Factura Exitosa");
                }else{
                    finalResponse.setErrorMessage("Ocurrio un error al Generar la Factura , favor de revisar el log de consumo.");
                }

            }else{
                finalResponse.setErrorMessage("No existen Datos Para Facturar para el Folio : "+ folio);
            }

        }catch (Exception e) {
            try {
                sqlQueryExecutor.rollBack();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
            finalResponse.setErrorMessage(e.getMessage());
        }
        try {
            //obtenemos el idLogWebService
            Long idLogWebService = (Long) sqlQueryExecutor.executeQuery(QuerysDB.GET_IDLODWEBSERVICE).get(0, "IDLOGWEBSERVICE");
            Util.insertLogWs(sqlQueryExecutor, idLogWebService, idWebService, "folio: "+folio+", idUsuario:"+idUsuario, finalResponse.toString(), folio, inicioTransaccion,null);

        }catch (Exception e){
            e.printStackTrace();
        }

        return finalResponse;
    }


    /***
     * metodoue genera una factura
     * @param datosPersonales
     */
    private ArrayList<File> getFactura(ALQueryResult datosPersonales,Long folio, Long idUsuario) throws  Exception{

        ArrayList<File> files = new ArrayList<File>();

        DatosFactura datosFactura = new DatosFactura();
        FacturaDigitalResponse facturaDigitalResponse = new FacturaDigitalResponse();
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String inicioTransaccion = format.format(new Date());
        Long idWebService = 79L;
        Gson gson = new Gson();


        try {

            String requiereFactura = datosPersonales.get(0,"REQUIEREFACTURA").toString();
            String razonSocial = datosPersonales.get(0,"RAZONSOCIAL") == null ? "" : datosPersonales.get(0,"RAZONSOCIAL").toString();
            String rfc = (requiereFactura.equals("S")? datosPersonales.get(0,"RFC").toString() : "XAXX010101000" );
            Long idFormaPago = (Long)datosPersonales.get(0,"IDFORMAPAGO");
            Double precioProducto = ((Long)datosPersonales.get(0,"PRECIOPRODUCTO")).doubleValue();
            Double iva = (Double) datosPersonales.get(0,"IVA");
            Long noSucursal=  datosPersonales.get(0,"NOSUCURSAL") == null ? 0L : (Long)datosPersonales.get(0,"NOSUCURSAL");
            String producto = datosPersonales.get(0,"PRODUCTO").toString() +" "+noSucursal + " Sucursal";
            String folioFactura = sqlQueryExecutor.executeQuery(QuerysDB.GET_SECFACTURASYOPTER).get(0,"SEC").toString();

            DecimalFormat df = new DecimalFormat("#0.00");

            //obtenemos el subtotal , el importe ,el valor unitario  y el total
            Double importe = (precioProducto * noSucursal);
            Double ivaImporte   = importe * iva;
            Double total = ivaImporte + importe;

            //Nombre receptor
            String nombreReceptor = requiereFactura.equals("S") ? razonSocial : "PUBICO EN GENERAL";

            //Forma de pago y metodo pago
            String formaDePago = "04";
            String metodoPago = "PUE";
            if(!idFormaPago.equals(1L)){
                formaDePago = "99";
                metodoPago = "PPD";
            }

            datosFactura.setSerie("G");
            datosFactura.setFolio(folioFactura);
            datosFactura.setFecha("AUTO");
            datosFactura.setSubTotal(df.format(importe));
            datosFactura.setDescuento(null);
            datosFactura.setMoneda("MXN");
            datosFactura.setTotal(df.format(total));
            datosFactura.setTipoDeComprobante("I");
            datosFactura.setFormaPago(formaDePago);
            datosFactura.setMetodoPago(metodoPago);
            datosFactura.setLugarExpedicion("05000");
            datosFactura.setNombreLugarExpedicion("ALVARO OBREGON");
            datosFactura.setTipoCambio("1");
            datosFactura.setCondicionesDePago("Credito");

            Emisor emisor = new Emisor();
            emisor.setRfc("GMO120508SE8");
            emisor.setNombre("GENOMA MOBIL SA DE CV");
            emisor.setRegimenFiscal("601"); //el que se debe usar :601 , demo: 612
            datosFactura.setEmisor(emisor);

            Receptor receptor = new Receptor();
            receptor.setRfc(rfc);
            receptor.setNombre(nombreReceptor);
            receptor.setUsoCFDI("G03");
            datosFactura.setReceptor(receptor);

            Concepto concepto = new Concepto();
            concepto.setClaveProdServ("80141601");
            concepto.setCantidad("1");
            concepto.setClaveUnidad("E48");
            concepto.setDescripcion(producto);
            concepto.setValorUnitario(df.format(importe));
            concepto.setImporte(df.format(importe));

            Impuestos impuestos = new Impuestos();
            List<Traslado> traslados = new ArrayList<Traslado>();
            Traslado traslado = new Traslado();
            traslado.setBase(df.format(importe));
            traslado.setImpuesto("002");
            traslado.setTipoFactor("Tasa");
            traslado.setTasaOCuota("0.160000");
            traslado.setImporte(df.format(ivaImporte));
            traslados.add(traslado);
            impuestos.setTraslados(traslados);
            concepto.setImpuestos(impuestos);

            List<Concepto> conceptos = new ArrayList<Concepto>();
            conceptos.add(concepto);
            datosFactura.setConceptos(conceptos);

            Impuestos impuestos2 = new Impuestos();
            List<Traslado> traslados2 = new ArrayList<Traslado>();
            Traslado traslado2 = new Traslado();
            traslado2.setImpuesto("002");
            traslado2.setTipoFactor("Tasa");
            traslado2.setTasaOCuota("0.160000");
            traslado2.setImporte(df.format(ivaImporte));
            traslados2.add(traslado2);
            impuestos2.setTraslados(traslados2);
            impuestos2.setTotalImpuestosTrasladados(df.format(ivaImporte));
            datosFactura.setImpuestos(impuestos2);

            //Convetimos el objeto a Json
            String datosFacturaJson = gson.toJson(datosFactura);

            //Obtemos los datos para consumir el ws
            sqlQueryExecutor.addParameter("idWebService",idWebService);
            ALQueryResult dataWs = sqlQueryExecutor.executeQuery(QuerysDB.GET_WS_DATA);


            //Se consume el WS de Generacion
            String urlWs = dataWs.get(0,"URL").toString();
            String usuario = dataWs.get(0,"USUARIO").toString();
            String contrasenia = dataWs.get(0,"CONTRASENIA").toString();
            Client client = Client.create();

            WebResource webResource = client .resource(urlWs);
            WebResource.Builder buider = webResource.accept("");

            MultivaluedMap formData = new MultivaluedMapImpl();
            formData.add("jsoncfdi", datosFacturaJson);

            ClientResponse response = buider
                    .header("Content-Type","application/x-www-form-urlencoded")
                    .header("accept","application/json")
                    .header("Accept","application/json")
                    .header("api-usuario",usuario)
                    .header("api-password",contrasenia)
                    .post(ClientResponse.class,formData);

            String output = response.getEntity(String.class);

            facturaDigitalResponse = (FacturaDigitalResponse)Util.fromJson(output,FacturaDigitalResponse.class);

            //si se genero la factura correctamente
            if(facturaDigitalResponse.getCodigo().equals("200")) {

                //Guadamos los datos de la factura en nueva tabla
                sqlQueryExecutor.addParameter("NoCertificado", facturaDigitalResponse.getCfdi().getNoCertificado());
                sqlQueryExecutor.addParameter("UUID", facturaDigitalResponse.getCfdi().getUUID());

                format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                Date fechaTimbrado = format.parse(facturaDigitalResponse.getCfdi().getFechaTimbrado());
                format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                sqlQueryExecutor.addParameter("FechaTimbrado", format.format(fechaTimbrado));

                sqlQueryExecutor.addParameter("RfcProvCertif", facturaDigitalResponse.getCfdi().getRfcProvCertif());
                sqlQueryExecutor.addParameter("SelloCFD", facturaDigitalResponse.getCfdi().getSelloCFD());
                sqlQueryExecutor.addParameter("NoCertificadoSAT", facturaDigitalResponse.getCfdi().getNoCertificadoSAT());
                sqlQueryExecutor.addParameter("SelloSAT", facturaDigitalResponse.getCfdi().getSelloCFD());
                sqlQueryExecutor.addParameter("CadenaOrigTFD", facturaDigitalResponse.getCfdi().getCadenaOrigTFD());
                sqlQueryExecutor.addParameter("CadenaQR", facturaDigitalResponse.getCfdi().getCadenaQR());
                sqlQueryExecutor.addParameter("XmlBase64", facturaDigitalResponse.getCfdi().getXmlBase64());
                sqlQueryExecutor.addParameter("PDF", facturaDigitalResponse.getCfdi().getPDF());


                sqlQueryExecutor.executeUpdate(QuerysDB.INSERT_TBLSOLICITUDESFACTURAS);
                sqlQueryExecutor.commit();

                //ejecutamos metodo que obtenga el pdf y lo guarde en el S3 de amazon y obtenemos el mismo
                File pdf = getPdfAndSaveInS3Amazon(folio, facturaDigitalResponse.getCfdi().getPDF(), idUsuario);

                //Obtemos el xml
                File xml = getXMl(facturaDigitalResponse.getCfdi().getXmlBase64());

                files.add(pdf);
                files.add(xml);

                if(idFormaPago == 2) {//Generamos el pdf de la referencia bancaria
                    File pdfReferenciaBancaria = getPdfReferenciaBancaria(folio);
                    if(pdfReferenciaBancaria != null) {
                        files.add(pdfReferenciaBancaria);
                    }
                }



            }

        }catch (Exception e) {
            throw  e;
        }

        //obtenemos el idLogWebService
        Long idLogWebService = (Long) sqlQueryExecutor.executeQuery(QuerysDB.GET_IDLODWEBSERVICE).get(0, "IDLOGWEBSERVICE");
        Util.insertLogWs(sqlQueryExecutor, idLogWebService, idWebService, gson.toJson(datosFactura), facturaDigitalResponse.getMensaje(), folio, inicioTransaccion,null);

        return files;
    }


    /***
     * meotodo que genera una nota de venta
     * @param folio
     * @return
     */
    private File getNotaVenta(Long folio) throws Exception{

        File notaVenta = null;
        Long idReporte = 81L;
        notaVenta = Util.getPdfFromJasperServer(sqlQueryExecutor,folio,idReporte,"yop_  ","");

        return notaVenta;
    }

    /***
     * obtenemos el xml para enviarlo por correo
     * @param xmlEncoded
     * @return
     * @throws Exception
     */
    private File getXMl(String xmlEncoded) throws Exception{
        File xml = null;
        try {

            xml = File.createTempFile("yop_",".xml" );
            FileUtils.copyToFile(new ByteArrayInputStream(Base64.decodeBase64(xmlEncoded)), xml );

        } catch (Exception e) {
            throw e;
        }

        return  xml;
    }


    /***
     * meotodo que obtiene el un pdf apartir de una url y lo envia a guaardar en el S3 de amazon
     * @param folio
     * @param urlPdf
     */
    private File getPdfAndSaveInS3Amazon(Long folio, String urlPdf, Long idUsuario)  throws  Exception{


        //Obtenemos el archivo
        String extension = "pdf";
        File file = Util.getFileFromUrl(urlPdf,extension,"yop_");

        //Obtenemos el orden y el posfijo para el tipo de doucmento de FACTURA
        sqlQueryExecutor.addParameter("folio",folio);
        sqlQueryExecutor.addParameter("idTipoDocumento",9); //9=Factura
        Long   orden   = ((Long)sqlQueryExecutor.executeQuery(QuerysDB.GET_ORDEN_DOCUMENTO).get(0,"ORDEN")) + 1;
        String posfijo = sqlQueryExecutor.executeQuery(QuerysDB.GET_POSFIJO_DOCUMENTO).get(0,"POSFIJO").toString() ;

        //Guardamos en S3
        AmazonS3Service amazonS3Service = new AmazonS3Service();
        amazonS3Service.saveFileToS3Amazon(file.getPath(),73,folio + "_" + posfijo + orden);
        amazonS3Service.closeService();

        // Borramos el Archivo
        //file.delete();

        //Creaamos registro en TblSolicitudes Documentos
        sqlQueryExecutor.addParameter("folio",folio);
        sqlQueryExecutor.addParameter("idUsuario",idUsuario);
        sqlQueryExecutor.addParameter("latitud",0L);
        sqlQueryExecutor.addParameter("longitud",0L);
        sqlQueryExecutor.addParameter("orden",orden);
        sqlQueryExecutor.addParameter("extension",extension);
        sqlQueryExecutor.executeUpdate(QuerysDB.INSERT_TBLSOLICITUDESDOCUMENTOS);
        sqlQueryExecutor.commit();


        return file;

    }

    /***
     * Método que genera el pdf de la referencia bancaria
     * @param folio
     * @return
     */
    private File getPdfReferenciaBancaria(Long folio){
        File file = null;
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String inicioTransaccion = format.format(new Date());
        Long idExternoSolicitud;
        Long idWebService = 81L;
        Long idLogWebService = 0L;
        String xmlEntrada = "";
        String xmlSalida = "";
        String urlArchivoAdjunto = "";

        try {
            idExternoSolicitud = Util.getIdExternoSolicitud(sqlQueryExecutor, folio);
            xmlEntrada = "{\"idCorporation\":" + idExternoSolicitud + "}";

            if (idExternoSolicitud > 0) {
                RequestGenericWs requestGenericWs = new RequestGenericWs();
                requestGenericWs.setIdCorporation(""+idExternoSolicitud);

                ResponseGenericWs responseGenericWs =Util.consumeGenericWs(sqlQueryExecutor, requestGenericWs, 81L); //81 = Yopter WS - Genere Pdf de Referencia bancaria
                if(responseGenericWs.getSuccess()) {
                    urlArchivoAdjunto = responseGenericWs.getDescription();
                    if(urlArchivoAdjunto != null){
                        file = Util.readPdfFromURL(urlArchivoAdjunto);
                        xmlSalida = "Se generó el pdf de la referencia bancaria ("+urlArchivoAdjunto+").";
                    }else{
                        xmlSalida = "No fue posible generar el pdf de la referencia bancaria (la url del archivo es null).";
                    }

                }else{
                    xmlSalida = "No fue posible generar el pdf de la referencia bancaria: "+responseGenericWs.getDescription();
                }

            } else {
                xmlSalida = "Acción no realizada con éxito: Folio no tiene un Id Corporation.";

            }

            idLogWebService = (Long) sqlQueryExecutor.executeQuery(QuerysDB.GET_IDLODWEBSERVICE).get(0, "IDLOGWEBSERVICE");
        }catch(Exception e){
            e.printStackTrace();
            xmlSalida = "Error al generar el pdf de la referencia bancaria: "+e.getMessage();
        }

        Util.insertLogWs(sqlQueryExecutor, idLogWebService, idWebService, xmlEntrada, xmlSalida, (Util.isEmpty(folio.toString()) ? null : new Long(folio.toString())), inicioTransaccion, null);

        return file;
    }

}
