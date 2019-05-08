package com.a4sys.luciWS.yopter;

import com.a4sys.core.dao.ALQueryResult;
import com.a4sys.core.dao.DaoService;
import com.a4sys.core.dao.SQLQueryExecutor;
import com.a4sys.core.exceptions.SystemException;
import com.a4sys.luciWS.domain.Adjuntos;
import com.a4sys.luciWS.domain.RequestGenericEmail;
import com.a4sys.luciWS.facturaDigital.FacturaDigitalService;
import com.a4sys.luciWS.util.*;
import com.a4sys.luciWS.utilWS.WSUitl;
import com.a4sys.luciWS.utilWS.WSUtilService;
import com.amazonaws.util.StringUtils;
import com.google.gson.Gson;

import javax.ws.rs.core.Response;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.a4sys.luciWS.util.SystemConstants.KEY;

/**
 * Created by mmorones on 5/14/18.
 */
public class YopterService extends DaoService {


    public YopterService() {
        super(SystemConstants.JDNI);
    }


    /***
     * metodo que verifica si un usuario existe en luci
     * @param yopterRequest
     * @return
     */
    public YopterResponse login(YopterRequest yopterRequest) {

        YopterResponse yopterResponse = new YopterResponse();
        yopterResponse.setIdUser(-1);

        ALQueryResult userData = new ALQueryResult();
        try {
            sqlQueryExecutor.addParameter("login", yopterRequest.getUser());
            userData = sqlQueryExecutor.executeQueryUsingQueryString("select idUsuario,login, " +
                    "gpEngine.decryptit(password, Fipskey) password, activo, vendedor " +
                    "from tblusuarios where login = {login}");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        //Revisamos que econtramos algo si no
        if (userData.size() != 0) {
            // revisamos si los password coinciden
            if (userData.get(0, "PASSWORD").toString().equals(yopterRequest.getPassword())) {
                //revisamo si el usuario esta activo
                if (userData.get(0, "ACTIVO").toString().equals("S")) {
                    //revisamos que sea el usuario vendedor
                    if(userData.get(0, "VENDEDOR").toString().equals("S")){
                        yopterResponse.setIdUser(new Integer(userData.get(0, "IDUSUARIO").toString()));
                        yopterResponse.setMessage("");
                    } else {
                        yopterResponse.setMessage("El usuario no es vendedor.");
                    }
                } else {
                    yopterResponse.setMessage("El usuario esta suspendido");
                }
            } else {
                yopterResponse.setMessage("La contraseña no es correcta.");
            }

        } else {
            yopterResponse.setMessage("El usuario no existe en el sistema.");
        }

        return yopterResponse;
    }

    public boolean addOrUpdateCommerce(CommerceRequest commerceRequest){
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String inicioTransaccion = format.format(new Date());
        boolean respuesta=true;

        String folio = null;
        String xmlOut = "";
        String idError = "";


        if (isValidData(commerceRequest)) {

            //ALTA
            String folioLuci = (null == commerceRequest.getFolioLuci() ? "" : (commerceRequest.getFolioLuci().equals("null") ? "" : commerceRequest.getFolioLuci()));
            if (StringUtils.isNullOrEmpty(folioLuci) ) {

                //validamos que el usuario tenga solo una campaña asignada
                String oneIdCampaign = getCampaignByIdUser(commerceRequest);
                String oneIdProveedor = getProveedorByIdUser(commerceRequest);
                String dataCampaign[] = oneIdCampaign.split("~");
                String dataProveedor[] = oneIdProveedor.split("~");
                if (Integer.parseInt(dataCampaign[0]) > 1 && Integer.parseInt(dataProveedor[0]) > 1) {
                    int idCampaign = Integer.parseInt(dataCampaign[1]);
                    int idProveedor = Integer.parseInt(dataProveedor[1]);
                    String saveSuccess = "0";

                    //REvisamos si existe el IdCorporation como idExterno solicitud, de ser asi no hacemos nada
                    //mandamos 200 y guardamos errro
                    if( !existIdCorporation(commerceRequest.getIdCorporation(),idCampaign)) {

                        saveSuccess = saveCommerce(commerceRequest, idCampaign, idProveedor);
                        if (Integer.parseInt(saveSuccess) > 0) {
                            folio = saveSuccess;
                            xmlOut = "Acción realizada con éxito, comercio registrado.";
                            idError = "0";
                        } else if (saveSuccess.equals("-1")) {
                            xmlOut = "Ocurrió un error al registrar el comercio. No se encontró el producto en la tabla de equivalencias";
                            idError = "-1";
                            respuesta = true;
                        } else if (saveSuccess.equals("-2")) {
                            xmlOut = "Ocurrió un error al registrar el comercio. No se encontró el número de sucursales";
                            idError = "-1";
                            respuesta = true;
                        } else {
                            xmlOut = "Ocurrió un error al registrar el comercio.";
                            idError = "-1";
                            respuesta = false;
                        }
                    }else{
                        xmlOut = "El comercio ya fue previamente registrado en Luci.";
                        idError = "-1";
                        respuesta = true;
                    }
                } else {
                    xmlOut = "Ocurrió un error al registrar el comercio (Usuario tiene asignado más de una campaña y/o proveedor).";
                    idError = "-1";
                    respuesta = true;
                }

            } else {

                folio = folioLuci;
                if (isEmptyDataFromBD(Integer.parseInt(folio))) {
                    int updateSuccess = 0;
                    updateSuccess = updateCommerceData(commerceRequest, Integer.parseInt(folio));
                    if (updateSuccess == 1) {
                        xmlOut = "Acción realizada con éxito, comercio actualizado.";
                        idError = "1";
                        continuaFolioComercio(new Long(folio), 111L, 0L, 1L);
                    } else if(updateSuccess == -1){
                        xmlOut = "Ocurrió un error al actualizar el comercio. No se encontró el producto en la tabla de equivalencias";
                        idError = "-1";
                        respuesta = true;
                    } else if(updateSuccess == -2){
                        xmlOut = "Ocurrió un error al actualizar el comercio. No se encontró el número de sucursales";
                        idError = "-1";
                        respuesta = true;
                    } else {
                        xmlOut = "Ocurrió un error al actualizar el comercio.";
                        idError = "-1";
                        respuesta = false;
                    }
                    //No cumplen las validaciones
                } else {
                    xmlOut = "No se pudo actualizar el comercio debido a que la fecha de pago o el id externo solicitud no están vacíos.";
                    idError = "-1";
                    respuesta = true;
                }
            }

        } else {
            xmlOut = "El idLuciUser, commerceName y/o folio  vienen vacíos.";
            idError = "-1";
            respuesta = false;
        }

        //Si se actualizo o creo nuevo mandamos generar factura
        if(idError.equals("1") || idError.equals("0")){
            //Mandamos genear la factura
            FacturaDigitalService facturaDigitalService = new FacturaDigitalService();
            try {
                facturaDigitalService.generarFacturaOrNotaVenta(new Long(folio), new Long(commerceRequest.getIdLuciUser()));
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                facturaDigitalService.closeService();
            }

        }

        //Guardamos el Log de Consumo de WEb Service
        saveLog(commerceRequest.toString(), xmlOut.toString(), (folio == null ? null : folio), inicioTransaccion, idError);
        return respuesta;
    }

    /***
     * metodo que revisa que un idCorporation(IdExternoSolicitud) no exista en luci
     * @param idCorporation
     * @return
     */
    private boolean existIdCorporation(int idCorporation, int idCampana) {
        boolean exist = false;
        try{
            sqlQueryExecutor.addParameter("idExternoSolicitud",idCorporation);
            sqlQueryExecutor.addParameter("idCampana",idCampana);

            ALQueryResult res = sqlQueryExecutor.executeQueryUsingQueryString("select count(1) res from tblSolicitudes where idcampana = idCampana " +
                    "and idExternoSolicitud = {idExternoSolicitud}");

            if(!res.get(0,"RES").toString().equals("0")){
                exist = true;
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        return  exist;
    }

    /***
     * metodo que valida que el idLuciUser y el nombre del comercio no estén vacíos
     * @param commerceRequest
     * @return
     */
    public boolean isValidData(CommerceRequest commerceRequest) {
        if (commerceRequest.getIdLuciUser() == 0 || commerceRequest.getCommerceData().getCommerceName() == null
                || commerceRequest.getCommerceData().getCommerceName().equals("") || commerceRequest.getCommerceData().getCommerceName().length() == 0
                || commerceRequest.getFechaCompromiso() == null) {
            return false;
        }
        return true;
    }

    /***
     * metodo que valida si el comercio existe o no
     * @param commerceRequest
     * @return
     */
    public String existCommerceByIdUser(CommerceRequest commerceRequest) {
        ALQueryResult res = new ALQueryResult();
        try {
            sqlQueryExecutor.addParameter("commerceName", commerceRequest.getCommerceData().getCommerceName());
            sqlQueryExecutor.addParameter("idLuciUser", commerceRequest.getIdLuciUser());
            res = sqlQueryExecutor.executeQueryUsingQueryString("select \n" +
                    "count(*) as EXIST \n" +
                    "from tblsolicitudes sol \n" +
                    "join tblcomercios comer on (comer.folio = sol.folio) \n" +
                    "where sol.idusuariovendedor = {idLuciUser} \n" +
                    "and Trim(RegExp_Replace(UPPER(nombrecomercial), '[^A-Z0-9]', '')) = Trim(RegExp_Replace(UPPER({commerceName}), '[^A-Z0-9]', '')) \n" +
                    " ");
            if (res.size() > 0) {
                if (!res.get(0, "EXIST").toString().equals("0")) {

                    res = sqlQueryExecutor.executeQueryUsingQueryString("select \n" +
                            "comer.idcomercio||'~'||comer.folio as ID \n" +
                            "from tblsolicitudes sol \n" +
                            "join tblcomercios comer on (comer.folio = sol.folio) \n" +
                            "where sol.idusuariovendedor = {idLuciUser} \n" +
                            "and Trim(RegExp_Replace(UPPER(nombrecomercial), '[^A-Z0-9]', '')) = Trim(RegExp_Replace(UPPER({commerceName}), '[^A-Z0-9]', '')) \n" +
                            " ");
                    return res.get(0, "ID").toString();
                } else {
                    return "0";
                }
            }
            return "0";
        } catch (Exception e) {
            return "1";
        }
    }

    /**
     * metodo que actualiza el comercio y la solicitud
     *
     * @param commerceRequest
     * @param folio
     * @return
     */
    public int updateCommerceData(CommerceRequest commerceRequest, int folio) {
        ALQueryResult resultCommerce = new ALQueryResult();
        ALQueryResult resultSolicitud = new ALQueryResult();
        ALQueryResult resultAnexos = new ALQueryResult();
        int resCommerce = 0;
        int resSolicitud = 0;
        int resAnexos = 0;
        try {
            sqlQueryExecutor.autocommit(false);
            String lastName[] = commerceRequest.getCommerceData().getLastName().split(" ");
            String firstLastName = "";
            String secondLastName = "";
            String idFormaPago = "2";
            String requierefactura = "N";
            String noSucursal = "";
            String idProducto = "";
            String sSqlQuery = "";

            for (int i = 0; i < lastName.length; i++) {
                if (i == 0) {
                    firstLastName = lastName[0];
                } else {
                    secondLastName += lastName[i];
                }
            }
            if (commerceRequest.getPayment().getType().equals("card")) {
                idFormaPago = "1";
            }else if(commerceRequest.getPayment().getType().equals("store")){
                idFormaPago = "3";
            }
            if(commerceRequest.getInvoice() != null) {
                if (commerceRequest.getInvoice().getAddress().length() != 0 && commerceRequest.getInvoice().getBusinessName().length() != 0 &&
                        commerceRequest.getInvoice().getRfc().length() != 0 && commerceRequest.getInvoice().getEmail().length() != 0) {
                    requierefactura = "S";
                }
            }

            noSucursal = getNoSucursalByEquivalencia(commerceRequest);
            if(noSucursal.equals("0")){
                return -2;
            }

            idProducto = getIdProductoByEquivalencia(commerceRequest.getPlan().getIdPlan().toString());
            if(idProducto.equals("0") ||  idProducto.equals("-1")){
                return -1;
            }

            //sqlQueryExecutor.addParameter("idCommerce", idCommerce);
            sqlQueryExecutor.addParameter("folio", folio);
            sqlQueryExecutor.addParameter("idCorporation", commerceRequest.getIdCorporation());
            sqlQueryExecutor.addParameter("name", commerceRequest.getCommerceData().getName());
            sqlQueryExecutor.addParameter("firstLastName", firstLastName);
            sqlQueryExecutor.addParameter("secondLastName", secondLastName);
            sqlQueryExecutor.addParameter("businessName",  commerceRequest.getInvoice() != null ? commerceRequest.getInvoice().getBusinessName() : "");
            sqlQueryExecutor.addParameter("rfc", commerceRequest.getInvoice() != null ? commerceRequest.getInvoice().getRfc() : "");
            sqlQueryExecutor.addParameter("email", commerceRequest.getInvoice() != null ? commerceRequest.getInvoice().getEmail() : "");
            sqlQueryExecutor.addParameter("idFormaPago", idFormaPago);
            sqlQueryExecutor.addParameter("requierefactura", requierefactura);
            sqlQueryExecutor.addParameter("noSucursal", noSucursal);
            sqlQueryExecutor.addParameter("idProducto", idProducto);
            sqlQueryExecutor.addParameter("fechaCompromiso", commerceRequest.getFechaCompromiso());

            /*************************Auditoria*********************************/
            resultCommerce = sqlQueryExecutor.executeQueryUsingQueryString("select fechacreacioncomercio," +
                    " nombrecontacto," +
                    " apaternocontacto,"+
                    " amaternocontacto,"+
                    " idformapago,"+
                    " requierefactura,"+
                    " razonsocial,"+
                    " rfc,"+
                    " emailfacturacion"+
                    " from tblcomercios " +
                    "where folio = {folio} ");

            sqlQueryExecutor.addParameter("idUsuarioAuditoria", commerceRequest.getIdLuciUser());
            sqlQueryExecutor.addParameter("fechaCreacionComercioAuditoria", resultCommerce.get(0, "FECHACREACIONCOMERCIO"));
            sqlQueryExecutor.addParameter("nombreContactoAuditoria", resultCommerce.get(0, "NOMBRECONTACTO"));
            sqlQueryExecutor.addParameter("apaternoContactoAuditoria", resultCommerce.get(0, "APATERNOCONTACTO"));
            sqlQueryExecutor.addParameter("amaternoContactoAuditoria", resultCommerce.get(0, "AMATERNOCONTACTO"));
            sqlQueryExecutor.addParameter("idFormaPagoAuditoria", resultCommerce.get(0, "IDFORMAPAGO"));
            sqlQueryExecutor.addParameter("requiereFacturaAuditoria", resultCommerce.get(0, "REQUIEREFACTURA"));
            sqlQueryExecutor.addParameter("razonSocialAuditoria", resultCommerce.get(0, "RAZONSOCIAL"));
            sqlQueryExecutor.addParameter("rfcAuditoria", resultCommerce.get(0, "RFC"));
            sqlQueryExecutor.addParameter("emailFacturacionAuditoria", resultCommerce.get(0, "EMAILFACTURACION"));
            sSqlQuery = "insert into TBLAUDITORIA (FECHA, IDUSUARIO, IDTABLA, TIPOMOVTO, ID, VALORANTERIOR, IDCAMPO, IDREFERENCIA) values (sysdate, {idUsuarioAuditoria}, 1026, 'C', {folio}, {fechaCreacionComercioAuditoria}, 1220, null) ";
            sqlQueryExecutor.executeUpdateUsingString(sSqlQuery);
            sSqlQuery = "insert into TBLAUDITORIA (FECHA, IDUSUARIO, IDTABLA, TIPOMOVTO, ID, VALORANTERIOR, IDCAMPO, IDREFERENCIA) values (sysdate, {idUsuarioAuditoria}, 1026, 'C', {folio}, {nombreContactoAuditoria}, 1147, null) ";
            sqlQueryExecutor.executeUpdateUsingString(sSqlQuery);
            sSqlQuery = "insert into TBLAUDITORIA (FECHA, IDUSUARIO, IDTABLA, TIPOMOVTO, ID, VALORANTERIOR, IDCAMPO, IDREFERENCIA) values (sysdate, {idUsuarioAuditoria}, 1026, 'C', {folio}, {apaternoContactoAuditoria}, 1148, null) ";
            sqlQueryExecutor.executeUpdateUsingString(sSqlQuery);
            sSqlQuery = "insert into TBLAUDITORIA (FECHA, IDUSUARIO, IDTABLA, TIPOMOVTO, ID, VALORANTERIOR, IDCAMPO, IDREFERENCIA) values (sysdate, {idUsuarioAuditoria}, 1026, 'C', {folio}, {amaternoContactoAuditoria}, 1149, null) ";
            sqlQueryExecutor.executeUpdateUsingString(sSqlQuery);
            sSqlQuery = "insert into TBLAUDITORIA (FECHA, IDUSUARIO, IDTABLA, TIPOMOVTO, ID, VALORANTERIOR, IDCAMPO, IDREFERENCIA) values (sysdate, {idUsuarioAuditoria}, 1026, 'C', {folio}, {idFormaPagoAuditoria}, 1222, null) ";
            sqlQueryExecutor.executeUpdateUsingString(sSqlQuery);
            sSqlQuery = "insert into TBLAUDITORIA (FECHA, IDUSUARIO, IDTABLA, TIPOMOVTO, ID, VALORANTERIOR, IDCAMPO, IDREFERENCIA) values (sysdate, {idUsuarioAuditoria}, 1026, 'C', {folio}, {requiereFacturaAuditoria}, 1152, null) ";
            sqlQueryExecutor.executeUpdateUsingString(sSqlQuery);
            sSqlQuery = "insert into TBLAUDITORIA (FECHA, IDUSUARIO, IDTABLA, TIPOMOVTO, ID, VALORANTERIOR, IDCAMPO, IDREFERENCIA) values (sysdate, {idUsuarioAuditoria}, 1026, 'C', {folio}, {razonSocialAuditoria}, 1135, null) ";
            sqlQueryExecutor.executeUpdateUsingString(sSqlQuery);
            sSqlQuery = "insert into TBLAUDITORIA (FECHA, IDUSUARIO, IDTABLA, TIPOMOVTO, ID, VALORANTERIOR, IDCAMPO, IDREFERENCIA) values (sysdate, {idUsuarioAuditoria}, 1026, 'C', {folio}, {rfcAuditoria}, 1136, null) ";
            sqlQueryExecutor.executeUpdateUsingString(sSqlQuery);
            sSqlQuery = "insert into TBLAUDITORIA (FECHA, IDUSUARIO, IDTABLA, TIPOMOVTO, ID, VALORANTERIOR, IDCAMPO, IDREFERENCIA) values (sysdate, {idUsuarioAuditoria}, 1026, 'C', {folio}, {emailFacturacionAuditoria}, 1151, null) ";
            sqlQueryExecutor.executeUpdateUsingString(sSqlQuery);


            resultSolicitud = sqlQueryExecutor.executeQueryUsingQueryString("select idExternosolicitud," +
                    " idproducto"+
                    " from tblsolicitudes " +
                    "where folio = {folio} ");

            sqlQueryExecutor.addParameter("idExternosolicitudAuditoria", resultSolicitud.get(0, "IDEXTERNOSOLICITUD"));
            sqlQueryExecutor.addParameter("idProductoAuditoria", resultSolicitud.get(0, "IDPRODUCTO"));
            sSqlQuery = "insert into TBLAUDITORIA (FECHA, IDUSUARIO, IDTABLA, TIPOMOVTO, ID, VALORANTERIOR, IDCAMPO, IDREFERENCIA) values (sysdate, {idUsuarioAuditoria}, 1, 'C', {folio}, {idExternosolicitudAuditoria}, 148, null) ";
            sqlQueryExecutor.executeUpdateUsingString(sSqlQuery);
            sSqlQuery = "insert into TBLAUDITORIA (FECHA, IDUSUARIO, IDTABLA, TIPOMOVTO, ID, VALORANTERIOR, IDCAMPO, IDREFERENCIA) values (sysdate, {idUsuarioAuditoria}, 1, 'C', {folio}, {idProductoAuditoria}, 115, null) ";
            sqlQueryExecutor.executeUpdateUsingString(sSqlQuery);


            resultAnexos = sqlQueryExecutor.executeQueryUsingQueryString("select nosucursal" +
                    " from tblanexossolicitud " +
                    "where folio = {folio} ");

            if(resultAnexos.size() == 0) {

                sSqlQuery = "insert into TBLAUDITORIA (FECHA, IDUSUARIO, IDTABLA, TIPOMOVTO, ID, VALORANTERIOR, IDCAMPO, IDREFERENCIA) values (sysdate, {idUsuarioAuditoria}, 85, 'C', {folio}, null, 1189, null) ";
                sqlQueryExecutor.executeUpdateUsingString(sSqlQuery);
            }else{
                sqlQueryExecutor.addParameter("noSucursalAuditoria", resultAnexos.get(0, "NOSUCURSAL"));
                sSqlQuery = "insert into TBLAUDITORIA (FECHA, IDUSUARIO, IDTABLA, TIPOMOVTO, ID, VALORANTERIOR, IDCAMPO, IDREFERENCIA) values (sysdate, {idUsuarioAuditoria}, 85, 'C', {folio}, {noSucursalAuditoria}, 1189, null) ";
                sqlQueryExecutor.executeUpdateUsingString(sSqlQuery);
            }

            /************************** fin Auditoria ******************************************/
            resCommerce = sqlQueryExecutor.executeUpdateUsingString(" update tblcomercios  set " +
                    " fechacreacioncomercio = sysdate," +
                    " nombrecontacto = {name}, " +
                    " apaternocontacto = {firstLastName}, " +
                    " amaternocontacto = {secondLastName}, " +
                    " idformapago = {idFormaPago}, " +
                    " requierefactura = {requierefactura}, " +
                    " razonsocial = {businessName}, " +
                    " rfc = {rfc}, " +
                    " emailfacturacion = {email}, " +
                    " fechaCompromiso = to_Date({fechaCompromiso},'dd/mm/yyyy') " +
                    " where folio = {folio} ");

            resSolicitud = sqlQueryExecutor.executeUpdateUsingString(" update tblsolicitudes  set " +
                    " idExternosolicitud = {idCorporation}, " +
                    " idproducto = {idProducto} " +
                    " where folio = {folio} ");

            resAnexos = sqlQueryExecutor.executeUpdateUsingString(" MERGE INTO tblanexossolicitud ane USING  " +
                    " (SELECT {folio} AS folio FROM dual ) res "+
                    " ON (ane.folio = res.folio) "+
                    " WHEN MATCHED THEN UPDATE SET nosucursal = {noSucursal} " +
                    " WHEN NOT MATCHED THEN INSERT (FOLIO, NOSUCURSAL) VALUES ({folio}, {noSucursal}) ");

            if (resCommerce == 1 && resSolicitud == 1 && resAnexos == 1) {
                sqlQueryExecutor.commit();
                return 1;
            } else {
                return 0;
            }
        } catch (Exception e) {
            try {
                sqlQueryExecutor.rollBack();
            } catch (Exception e1) {
                return 0;
            }
            return 0;
        }
    }


    /***
     * metodo que valida que la fechapago y idexternosolicitud estén vacios
     * @param folio
     * @return
     */
    public boolean isEmptyDataFromBD(int folio){
        ALQueryResult res = new ALQueryResult();
        int resCommerce = 0;
        int resSolicitud = 0;
        try{
            sqlQueryExecutor.addParameter("folio", folio);
            res = sqlQueryExecutor.executeQueryUsingQueryString("select \n" +
                    "count(*) as EXISTFECHAPAGO \n" +
                    "from tblcomercios \n" +
                    "where folio = {folio} \n" +
                    "and fechapago is null ");
            if(res.size() > 0){
                resCommerce = Integer.parseInt(res.get(0, "EXISTFECHAPAGO").toString());
            }

            res = sqlQueryExecutor.executeQueryUsingQueryString("select \n" +
                    "count(*) as EXISTIDCORPORATION \n" +
                    "from tblsolicitudes \n" +
                    "where folio = {folio} \n" +
                    "and idexternosolicitud is null or idexternosolicitud = {folio}");
            if(res.size() > 0){
                resSolicitud = Integer.parseInt(res.get(0, "EXISTIDCORPORATION").toString());
            }

            if(resCommerce == 1 && resSolicitud == 1){
                return true; //si existen los campos fechapago y idexternosolicitud igual a null
            }else{
                return false;
            }

        }catch(Exception e){
            return true;
        }
    }

    /***
     * metodo que registra un nuevo comercio
     * @param commerceRequest
     * @return
     */
    public String saveCommerce(CommerceRequest commerceRequest, int idCampaign, int idProveedor){
        Long folio = 0L;
        Long idCliente = 0L;
        Long idCommerce = 0L;
        String sSqlQuery = "";
        try{


            sqlQueryExecutor.autocommit(false);
            folio = getIDNuevoTabla(1L);
            idCliente = getIDNuevoTabla(2L);
            idCommerce = getIDNuevoTabla(1026L);

            String lastName[] = commerceRequest.getCommerceData().getLastName().split(" ");
            String firstLastName = "";
            String secondLastName = "";
            String requierefactura = "N";
            String idFormaPago = "2";
            String noSucursal = "";
            String idProducto = "";

            for (int i = 0; i < lastName.length; i++) {
                if (i == 0) {
                    firstLastName = lastName[0];
                } else {
                    secondLastName += lastName[i];
                }
            }

            if(commerceRequest.getInvoice() != null) {
                if (commerceRequest.getInvoice().getAddress().length() != 0 && commerceRequest.getInvoice().getBusinessName().length() != 0 &&
                        commerceRequest.getInvoice().getRfc().length() != 0 && commerceRequest.getInvoice().getEmail().length() != 0) {
                    requierefactura = "S";
                }
            }

            if (commerceRequest.getPayment().getType().equals("card")) {
                idFormaPago = "1";
            }else if(commerceRequest.getPayment().getType().equals("store")){
                idFormaPago = "3";
            }

            noSucursal = getNoSucursalByEquivalencia(commerceRequest);
            if(noSucursal.equals("0")){
                return "-2";
            }

            idProducto = getIdProductoByEquivalencia(commerceRequest.getPlan().getIdPlan().toString());
            if(idProducto.equals("0") ||  idProducto.equals("-1")){
                return "-1";
            }
            sqlQueryExecutor.addParameter("folio", folio);
            sqlQueryExecutor.addParameter("idCampaign", idCampaign);
            sqlQueryExecutor.addParameter("idProveedor", idProveedor);
            sqlQueryExecutor.addParameter("idCliente", idCliente);
            sqlQueryExecutor.addParameter("idCorporation", commerceRequest.getIdCorporation());
            sqlQueryExecutor.addParameter("idUsuarioCaptura", commerceRequest.getIdLuciUser());
            sqlQueryExecutor.addParameter("idCommerce", idCommerce);

            sqlQueryExecutor.addParameter("businessName", commerceRequest.getInvoice() != null ? commerceRequest.getInvoice().getBusinessName() : "");
            sqlQueryExecutor.addParameter("rfc", commerceRequest.getInvoice() != null ?commerceRequest.getInvoice().getRfc() : "");
            sqlQueryExecutor.addParameter("emailFacturacion", commerceRequest.getInvoice() != null ? commerceRequest.getInvoice().getEmail() : "");
            sqlQueryExecutor.addParameter("address", commerceRequest.getInvoice() != null ? commerceRequest.getInvoice().getAddress() : "");

            sqlQueryExecutor.addParameter("commerceName", commerceRequest.getCommerceData().getCommerceName());
            sqlQueryExecutor.addParameter("name", commerceRequest.getCommerceData().getName());
            sqlQueryExecutor.addParameter("firstLastName", firstLastName);
            sqlQueryExecutor.addParameter("secondLastName", secondLastName);
            sqlQueryExecutor.addParameter("emailContact", commerceRequest.getCommerceData().getEmail());
            sqlQueryExecutor.addParameter("requierefactura", requierefactura);
            sqlQueryExecutor.addParameter("idFormaPago", idFormaPago);

            sqlQueryExecutor.addParameter("noSucursal", noSucursal);
            sqlQueryExecutor.addParameter("idProducto", idProducto);
            sqlQueryExecutor.addParameter("fechaCompromiso", commerceRequest.getFechaCompromiso().toString());


            //TBLCLIENTES
            sSqlQuery = "insert into TBLCLIENTES (IDCLIENTE,EDAD,CURP,RFC,NOMBRE,APATERNO,FECHANACIMIENTO) values ({idCliente},33,'XXXX840423XXXXXX','XXXX840423XXX','XXXXXXXX','XXXXXXXX',TO_DATE('23041984','DDMMYYYY'))";
            sqlQueryExecutor.executeUpdateUsingString(sSqlQuery);


            //TBLSOLICITUDES
            sSqlQuery = "insert into TBLSOLICITUDES (FOLIO, idExternosolicitud, idcampana, idproducto, idproveedor, idCliente, idEstadoSolicitud, idUsuarioCaptura,FechaIngreso, FechaUltMovto, idCfgAsignacionCredito, idusuariovendedor) values ({folio}, {idCorporation}, {idCampaign}, {idProducto}, {idProveedor}, {idCliente}, 1, {idUsuarioCaptura}, sysdate, sysdate, (Select IDCfgAsignacionCredito From dicCampanas Where IDCampana = {idCampaign}), {idUsuarioCaptura})";
            sqlQueryExecutor.executeUpdateUsingString(sSqlQuery);


            //TBLDOMICILIOS
            sSqlQuery = "insert into TBLDOMICILIOS (idCliente) values ({idCliente})";
            sqlQueryExecutor.executeUpdateUsingString(sSqlQuery);

            //TBLANEXOSSOLICITUD
            sSqlQuery = "insert into TBLANEXOSSOLICITUD (FOLIO,nosucursal) values ({folio}, {noSucursal})";
            sqlQueryExecutor.executeUpdateUsingString(sSqlQuery);

            //TBLCONTROLSOLICITUDES
            sSqlQuery = "insert into TBLCONTROLSOLICITUDES (FOLIO) values ({folio})";
            sqlQueryExecutor.executeUpdateUsingString(sSqlQuery);

            //TBLCOMERCIOS
            sSqlQuery = "insert into TBLCOMERCIOS (IDCOMERCIO, FOLIO, NOMBRE, APATERNO, AMATERNO, RAZONSOCIAL, RFC, NOMBRECOMERCIAL, IDTIPODOMICILIO, CALLE, NOEXTERIOR, NOINTERIOR, COLONIA, CPOSTAL, IDMUNICIPIO, TELEFONO, NOMBRECONTACTO, APATERNOCONTACTO, AMATERNOCONTACTO, TELEFONOCELULARCONTACTO, EMAILCONTACTO, REQUIEREFACTURA, HORARIOATENCION, FECHACITAVENTAS, PUESTOCONTACTO, FOLIOFACTURA, FECHAFACTURA, FECHAPAGO, FECHACREACIONCOMERCIO, IDFORMAPAGO, IDESTADOPROSPECCION, EMAILFACTURACION, FECHACOMPROMISO) " +
                    "values ({idCommerce}, {folio}, null, null, null, {businessName}, {rfc}, {commerceName}, 4, {address}, null, null, null, null, null, null, {name}, {firstLastName}, {secondLastName}, null, {emailContact}, {requierefactura}, null, null, null, null, null, null, sysdate, {idFormaPago}, null, {emailFacturacion}, to_date({fechaCompromiso},'dd/mm/yyyy'))";
            sqlQueryExecutor.executeUpdateUsingString(sSqlQuery);

            sqlQueryExecutor.commit();
            if(Util.continueFromHere(sqlQueryExecutor, new Long(folio))) {
                return folio.toString();
            }
        }catch(SQLException e){
            try {
                sqlQueryExecutor.rollBack();
            } catch (Exception e1) {
                return "0";
            }
            return "0";
        }
        return folio.toString();
    }

    /***
     * metodo que inserta log del webservice 78
     * @param logIn
     * @param logOut
     * @param folio
     * @param inicioTransaccion
     */
    public void saveLog(String logIn, String logOut, String folio, String inicioTransaccion, String idError){
        Long idLogWebService;
        Long idWebService = 78l;
        try {
            //obtenemos el idLogWebService
            idLogWebService = (Long)sqlQueryExecutor.executeQuery(QuerysDB.GET_IDLODWEBSERVICE).get(0,"IDLOGWEBSERVICE");
            Util.insertLogWs(sqlQueryExecutor, idLogWebService,  idWebService, logIn , logOut, (null == folio ? null : new Long(folio)), inicioTransaccion, idError);
        } catch (SystemException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    /***
     *
     * @param idTabla
     * @return
     */
    private Long getIDNuevoTabla(Long idTabla) {

        Long id = 0L;
        StringBuffer secuenciaName = new StringBuffer("");
        secuenciaName.append("select ");
        try {
            switch (idTabla.intValue()) {
                case 1:
                    secuenciaName.append("SECFOLIO");
                    break;
                case 2:
                    secuenciaName.append("SECIDCLIENTE");
                    break;
                case 1026:
                    secuenciaName.append("SECIDCOMERCIO");
                    break;
                default:
                    secuenciaName = new StringBuffer("");
                    break;
            }
            if (!secuenciaName.toString().equals("")) {
                secuenciaName.append(".NextVal as ID from dual");
                ALQueryResult alID = sqlQueryExecutor.executeQueryUsingQueryString(secuenciaName.toString());
                id = (Long) alID.get(0, "ID");
            }
        } catch (SystemException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return id;
    }


    /***
     * metodo que obtiene el id de la campaña asignada al usuario
     * @param commerceRequest
     * @return
     */
    public String getCampaignByIdUser(CommerceRequest commerceRequest) {
        ALQueryResult res = new ALQueryResult();
        try {
            sqlQueryExecutor.addParameter("idLuciUser", commerceRequest.getIdLuciUser());
            res = sqlQueryExecutor.executeQueryUsingQueryString("select \n" +
                    "count(*) as EXIST \n" +
                    "from tblusuarioscampanas \n" +
                    "where idusuario = {idLuciUser} \n" +
                    " ");
            if (res.size() > 0) {
                if (res.get(0, "EXIST").toString().equals("1")) {

                    res = sqlQueryExecutor.executeQueryUsingQueryString("select \n" +
                            "idusuario||'~'||idcampana as ID \n" +
                            "from tblusuarioscampanas \n" +
                            "where idusuario = {idLuciUser} \n" +
                            " ");
                    return res.get(0, "ID").toString();
                } else {
                    return "0";
                }
            }
            return "0";
        } catch (Exception e) {
            return "1";
        }
    }

    /***
     * metodo que obtiene el id del proveedor asignado al usuario
     * @param commerceRequest
     * @return
     */
    public String getProveedorByIdUser(CommerceRequest commerceRequest) {
        ALQueryResult res = new ALQueryResult();
        try {
            sqlQueryExecutor.addParameter("idLuciUser", commerceRequest.getIdLuciUser());
            res = sqlQueryExecutor.executeQueryUsingQueryString("select \n" +
                    "count(*) as EXIST \n" +
                    "from tblusuariosproveedores \n" +
                    "where idusuario = {idLuciUser} \n" +
                    " ");
            if (res.size() > 0) {
                if (res.get(0, "EXIST").toString().equals("1")) {

                    res = sqlQueryExecutor.executeQueryUsingQueryString("select \n" +
                            "idusuario||'~'||idproveedor as ID \n" +
                            "from tblusuariosproveedores \n" +
                            "where idusuario = {idLuciUser} \n" +
                            " ");
                    return res.get(0, "ID").toString();
                } else {
                    return "0";
                }
            }
            return "0";
        } catch (Exception e) {
            return "1";
        }
    }



    /**
     * metodo que verifica si la key recibida coincide con la que tenemos
     * @param key
     * @return
     */
    public boolean isKeyValid(String key){
        if(KEY.equals(key)){
            return true;
        }else{
            return false;
        }
    }

    /**
     * metodo que registra el pago a un comercio; siempre y cuando exista el comercio
     * @param yopterRequest
     * @return
     */
    public boolean registerPayment(YopterRequest yopterRequest) {
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String inicioTransaccion = format.format(new Date());
        ALQueryResult alQueryResult;
        ALQueryResult alQueryResultFolio;
        long idLogWebService;
        String salida="";
        boolean respuesta=true;
        Long folio=0l;
        try {
            sqlQueryExecutor.addParameter("idComercio",yopterRequest.getIdCorporation().toString());

            alQueryResultFolio=sqlQueryExecutor.executeQueryUsingQueryString("select folio from tblsolicitudes where IDEXTERNOSOLICITUD= {idComercio}");
            if(alQueryResultFolio.size()>0) {
                folio = Long.parseLong(alQueryResultFolio.get(0, "FOLIO").toString());
                sqlQueryExecutor.addParameter("folio",folio);
                alQueryResult = sqlQueryExecutor.executeQueryUsingQueryString("select FECHAPAGO from tblcomercios where folio= {folio}");
                if(alQueryResult.size()>0){
                    if(alQueryResult.get(0,"FECHAPAGO") == null) {
                        sqlQueryExecutor.executeUpdateUsingString("update TBLCOMERCIOS set FECHAPAGO = sysdate where folio= {folio}");
                        sqlQueryExecutor.executeUpdate(QuerysDB.INSERT_HISTORIA_SOLICITUD);
                        salida="El pago fue registrado exitosamente";

                        //Mandamos genear la factura a publico en general si no requirio factura
                        FacturaDigitalService facturaDigitalService = new FacturaDigitalService();
                        try{
                            facturaDigitalService.generaFacturaPublico(new Long(folio),1L);
                        }catch (Exception e){
                            e.printStackTrace();
                        }finally {
                            facturaDigitalService.closeService();
                        }

                        continuaFolioComercio(new Long(folio), 9L, 104L, 1L);

                        //envio de email's
                        sendEmails(sqlQueryExecutor,folio);

                    }else{
                        salida="El comercio con id: "+yopterRequest.getIdCorporation()+" ya tiene una fecha de pago registrada con fecha: "+alQueryResult.get(0,"FECHAPAGO").toString();

                    }}else{
                    salida="No existe el comercio solicitado";
                    respuesta= false;
                }
            }else{
                salida="No existe registro para el id: "+yopterRequest.getIdCorporation();
                respuesta= false;
            }

            idLogWebService = (Long)sqlQueryExecutor.executeQuery(QuerysDB.GET_IDLODWEBSERVICE).get(0,"IDLOGWEBSERVICE");
            Util.insertLogWs(sqlQueryExecutor, idLogWebService,  77L, "Confirmación de pago comercio: "+yopterRequest.getIdCorporation().toString() , salida,(folio == 0l? null:folio) , inicioTransaccion, null);
            sqlQueryExecutor.commit();



        } catch (SQLException e) {
            e.printStackTrace();
            try {
                sqlQueryExecutor.rollBack();
            } catch (SQLException e1) {
                e1.printStackTrace();
                respuesta= false;
            }
            respuesta= false;
        }
        return respuesta;
    }

    private void sendEmails(SQLQueryExecutor sqlQueryExecutor, Long folio) {
        ALQueryResult alQueryResult;
        String emailContacto="";
        String emailVendedor="";
        String emailComisiones="";

        try {
            sqlQueryExecutor.addParameter("folio",folio);
            alQueryResult= sqlQueryExecutor.executeQueryUsingQueryString("select us.EMAIL,tbc.EMAILCONTACTO,(select emailconfirmacionpago from TBLCFGSISTEMA) emailComisiones from TBLSOLICITUDES sol join TBLUSUARIOS us on sol.idusuariovendedor=us.idusuario left join TBLCOMERCIOS tbc on sol.folio=tbc.folio where sol.folio={folio}");
            if(alQueryResult.size()>0){
                emailContacto=alQueryResult.get(0,"EMAILCONTACTO").toString();
                emailVendedor=alQueryResult.get(0,"EMAIL").toString();
                emailComisiones=alQueryResult.get(0,"EMAILCOMISIONES").toString();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        RequestGenericEmail requestGenericEmail = new RequestGenericEmail();

        requestGenericEmail.setFolio(folio.toString());
        requestGenericEmail.setIdNotificacionEmail("4");
        requestGenericEmail.setEmail(emailContacto);
        requestGenericEmail.setIdUsuario("1");
        requestGenericEmail.setIdCorporation(folio.toString());
        requestGenericEmail.setAdjuntos(null);

        WSUtilService wsUtilService=new WSUtilService();
        //contacto
        wsUtilService.sendGenericMail(requestGenericEmail);
        //vendedor
        requestGenericEmail.setEmail(emailVendedor);
        wsUtilService.sendGenericMail(requestGenericEmail);
        //comisiones
        requestGenericEmail.setEmail(emailComisiones);
        wsUtilService.sendGenericMail(requestGenericEmail);
    }


    /***
     * metodo que obtiene el id del producto de la tabla de equivalencias
     * @param valor
     * @return
     */
    public String getIdProductoByEquivalencia(String valor) {
        ALQueryResult res = new ALQueryResult();
        try {
            sqlQueryExecutor.addParameter("valor", valor);
            res = sqlQueryExecutor.executeQueryUsingQueryString("select \n" +
                    "count(*) as EXIST \n" +
                    "from dicequivalencias \n" +
                    "where valor = {valor} \n" +
                    " ");
            if (res.size() > 0) {
                if (res.get(0, "EXIST").toString().equals("1")) {

                    res = sqlQueryExecutor.executeQueryUsingQueryString("select \n" +
                            "idregistro as ID \n" +
                            "from dicequivalencias \n" +
                            "where valor = {valor} \n" +
                            " ");
                    return res.get(0, "ID").toString();
                } else {
                    return "0";
                }
            }
            return "0";
        } catch (Exception e) {
            return "-1";
        }
    }


    /***
     * metodo que obtiene el no de sucursal
     * @param commerceRequest
     * @return
     */
    public String getNoSucursalByEquivalencia(CommerceRequest commerceRequest) {
        String noSucursal = "0";
        try {
            if(commerceRequest.getPlan().getIdPlan().equals("5b033e0c61a647067fdd8698")){
                noSucursal = "1";
            }else if(commerceRequest.getPlan().getIdPlan().equals("5b033efe61a647067fdd8699")){
                noSucursal = "2";
            }else if(commerceRequest.getPlan().getIdPlan().equals("5b033f2261a647067fdd869a")){
                noSucursal = "3";
            }else if(commerceRequest.getPlan().getIdPlan().equals("5b033f5661a647067fdd869b")){
                noSucursal = "4";
            }else if(commerceRequest.getPlan().getIdPlan().equals("5b033f8961a647067fdd869c")){
                noSucursal = "5";
            }else if(commerceRequest.getPlan().getIdPlan().equals("5b033fe661a647067fdd869d")){
                noSucursal = "1";
            }else if(commerceRequest.getPlan().getIdPlan().equals("5b03405961a647067fdd869e")){
                noSucursal = "1";
            }

            if (!noSucursal.equals("0")) {
                return noSucursal;
            } else {
                return "0";
            }

        } catch (Exception e) {
            return "-1";
        }
    }


    /***
     * metodo que devuelve false o true si el idusuario de luci tiene comercios asignados
     * @param idLuciUser
     * @return
     */
    public boolean existCommerceByIdUser(String idLuciUser){
        ALQueryResult res = new ALQueryResult();
        try {
            sqlQueryExecutor.addParameter("idLuciUser", idLuciUser);
            res = sqlQueryExecutor.executeQueryUsingQueryString("select \n" +
                    "count(*) as EXIST \n" +
                    "from tblsolicitudes sol \n" +
                    "join tblcomercios comer on (comer.folio = sol.folio) \n" +
                    "where sol.idusuariovendedor = {idLuciUser} \n" +
                    "and sol.idestadosolicitud = 1 \n" +
                    "and comer.fechapago is null \n" +
                    "and not exists (select 1 from tbllogwebServices where folio = sol.folio and idWebService = 78 and idError in ('0','1') ) \n" +
                    " ");
            if (res.size() > 0) {
                if (res.get(0, "EXIST").toString().equals("0")) {
                    return false;
                } else {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }

    }

    /***
     * metodo que obtiene todos los comercios deacuerdo al id usuario luci
     * @param idLuciUser
     * @return
     */
    public ALQueryResult getCommerceByIdUser(String idLuciUser) {
        ALQueryResult res = new ALQueryResult();
        try {
            sqlQueryExecutor.addParameter("idLuciUser", idLuciUser);
            res = sqlQueryExecutor.executeQueryUsingQueryString("select \n" +
                    "comer.folio, \n" +
                    "upper(comer.nombrecomercial) as COMMERCE \n" +
                    "from tblsolicitudes sol \n" +
                    "join tblcomercios comer on (comer.folio = sol.folio) \n" +
                    "where sol.idusuariovendedor = {idLuciUser} \n" +
                    "and sol.idestadosolicitud = 1 \n" +
                    "and comer.fechapago is null \n" +
                    "and not exists (select 1 from tbllogwebServices where folio = sol.folio and idWebService = 78 and idError in ('0','1') ) \n" +
                    "order by comer.nombrecomercial \n" +
                    " ");

            return res;
        } catch (Exception e) {
            return null;
        }
    }

    public void continuaFolioComercio(Long folio, Long idProcesoMover, Long idTipoMesaControl, Long idUsuario){
        try {
            sqlQueryExecutor.addParameter("folio", folio);
            sqlQueryExecutor.addParameter("idProceso", idProcesoMover);
            sqlQueryExecutor.addParameter("idTipoMesaControl", idTipoMesaControl);
            ALQueryResult res = sqlQueryExecutor.executeQueryUsingQueryString("select TOOLSENGINE.continuaFolioComercio({folio}, {idProceso}, {idTipoMesaControl}, {idUsuarioResponsable}) as RES from dual ");
            String respuesta = res.get(0, "RES").toString();

            if(respuesta.equals("1")){

                sqlQueryExecutor.addParameter("idUsuarioResponsable", idUsuario);
                sqlQueryExecutor.addParameter("idEventoHistorico",150L ); //Solicitud continuada automáticamente.
                sqlQueryExecutor.addParameter("observaciones", "Se continua el folio automáticamente.");
                sqlQueryExecutor.addParameter("idProceso", idProcesoMover ); //Proceso  Mensajeria

                sqlQueryExecutor.executeUpdate(QuerysDB.INSERT_HISTORIA_SOLICITUD_MINIMA);
                sqlQueryExecutor.commit();
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
    }
}
