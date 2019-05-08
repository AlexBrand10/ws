package com.a4sys.luciWS.banorteWS;

import java.sql.SQLException;

import com.a4sys.core.dao.ALQueryResult;
import com.a4sys.core.dao.DaoService;
import com.a4sys.luciWS.util.QuerysDB;
import com.a4sys.luciWS.util.SystemConstants;
import com.a4sys.luciWS.util.WsErrors;

public class BanorteWSService  extends DaoService{
	
	public BanorteWSService(){
		super(SystemConstants.JDNI);
	}
	
	
	/***
	 * Método que ingresa el registro de llamadas
	 * y llama al otro método que inserta el log del webservice para la venta de banorte
	 * @param folio
	 * @param folioFur
	 * @param telefonoContacto
	 * @param idLlamada
	 * @param gradoEnojo
	 * @param calificacion
	 * @param etiquetaGrabacion
	 * @param fechaLlamada
	 * @return respuesta
	 */
	public String insertRecorCall(String folio, String folioFur, String telefonoContacto, String idLlamada, String gradoEnojo, String calificacion,String etiquetaGrabacion, String fechaLlamada){
		String respuesta = "";
		String idRegistroLlamada= getNextValRegistroLlamada();
		//mandamos los parámetros
		sqlQueryExecutor.addParameter("idRegistroLlamada", idRegistroLlamada);
		sqlQueryExecutor.addParameter("folio", folio);
		sqlQueryExecutor.addParameter("folioFur", folioFur);
		sqlQueryExecutor.addParameter("telefonoContacto", telefonoContacto);
		sqlQueryExecutor.addParameter("idLlamada", idLlamada);
		sqlQueryExecutor.addParameter("gradoEnojo", gradoEnojo);
		sqlQueryExecutor.addParameter("calificacion", calificacion);
		sqlQueryExecutor.addParameter("etiquetaGrabacion", etiquetaGrabacion);
		sqlQueryExecutor.addParameter("fechaLlamada", fechaLlamada);
		
		try{
			//realizamos el insert
			sqlQueryExecutor.autocommit(false);
			sqlQueryExecutor.executeUpdate(QuerysDB.INSERT_RECORD_CALL);
			sqlQueryExecutor.commit();
			//actualizamos tblsolicitudes con el idregistrollamada
			sqlQueryExecutor.executeUpdate(QuerysDB.UPDATE_FOLIO_REGISTROLLAMADA);
			sqlQueryExecutor.commit();
		}catch(SQLException e){
			//regresamos respuesta con error
			respuesta = "-1,Error en Base de Datos:"+e.getCause().toString();
		} catch (Exception e) {
			//regresamos respuesta con error
			respuesta = "-1,Exepcion:"+e.getCause().toString();
		}
		//si no hubo error mandamos mensaje de éxito.
		if("".equals(respuesta)){ 
			respuesta = "0,"+WsErrors.WS_ERROR_0;
		}
		
		//insertamos el log del webservice
		 String xml = "folio: "+folio+", folioFur: "+folioFur+", telefonoContacto: "+telefonoContacto+", idLlamada: "+idLlamada+", gradoEnojo: "+gradoEnojo+", calificacion: "+calificacion+", etiquetaGrabacion: "+etiquetaGrabacion+", fechaLlamada: "+fechaLlamada;
		 respuesta = insertLogWebService(xml, respuesta, fechaLlamada, folio);	
			
	
		//regresamos respuesta
		return respuesta;
		
	}
	
	/***
	 * Método que ingresa el registro en el log del webservice para la venta de banorte
	 * @param xml
	 * @param respuesta
	 * @param fechaLlamada
	 * @param folioFur
	 * @return respuesta
	 */
	public String insertLogWebService(String xml, String respuesta, String fechaLlamada, String folioFur){
		String respuestaWS = "0,"+WsErrors.WS_ERROR_0;
		//agregamos los parámetros
		sqlQueryExecutor.addParameter("xml", xml);
		sqlQueryExecutor.addParameter("xmlsalida", respuesta);
		//sqlQueryExecutor.addParameter("fechaevento", fechaLlamada+" 00:00:00");
		sqlQueryExecutor.addParameter("folio", folioFur);
		
		try{
			//realizamos el insert
			sqlQueryExecutor.autocommit(false);
			sqlQueryExecutor.executeUpdate(QuerysDB.INSERT_LOG_WS);
			sqlQueryExecutor.commit();
		}catch(SQLException e){
			
			//si no hubo error al insertar en la tabla de registros
			// mandamos que ocurrio un error al insertar el log del ws
			// de lo contrario mandamos exito pero sin insertar el log del ws
			
		    //respuestaWS = "15,"+WsErrors.WS_ERROR_15+":"+e.getCause().toString();
			e.printStackTrace();
			
		} catch (Exception e2) {
			//respuestaWS = "15,"+WsErrors.WS_ERROR_15+":"+e2.getCause().toString();
			e2.printStackTrace();
		}
		
		/*if("0,Ok.".equals(respuesta) && "0,Ok.".equals(respuestaWS)){ 
			respuestaWS = "0,"+WsErrors.WS_ERROR_0;
		}else if("0,Ok.".equals(respuesta) && "14,Ok.".equals(respuestaWS)){ 
			respuestaWS = "14,"+WsErrors.WS_ERROR_14;
		}else if("0,Ok.".equals(respuesta) && "14,Registro de llamada con éxito pero ocurrió un error al registrar el log del webservice.".equals(respuestaWS)){ 
			respuestaWS = "14,"+WsErrors.WS_ERROR_14;
		}else{
			respuestaWS = "15,"+WsErrors.WS_ERROR_15;
		}*/
		
		
		
		return respuestaWS;
	}
	
	/***
	 * Método que obtiene el folio en luci por medio del idexternosolicitud.
	 * @param folioFur
	 * @return folio en luci
	 */
	public String getFolioByIdExternoSolicitud(String folioFur){
		String folio = "0";
		sqlQueryExecutor.cleanParameter();
		sqlQueryExecutor.addParameter("folioFur", folioFur);
		try{
			ALQueryResult res = sqlQueryExecutor.executeQuery(QuerysDB.GET_MAX_FOLIO);
			if(res.get(0, "FOLIO") != null ){
				folio = res.get(0, "FOLIO").toString();
				
			}
		}catch(SQLException e){
			e.printStackTrace();
			return folio;
		}
		return folio;
	}
	
	/*** 
	 * Método que obtiene la secuencia de la tabla registros llamadas.
	 * @return idregistrollamadas.
	 *  **/
	
	public String getNextValRegistroLlamada(){
		String idRegistroLlamada="0";
		try{
			ALQueryResult res = sqlQueryExecutor.executeQuery(QuerysDB.GET_SEC_NEXT);
			if(res.get(0, "ID") != null ){
				idRegistroLlamada = res.get(0, "ID").toString();
				
			}
		}catch(SQLException e){
			e.printStackTrace();
			return idRegistroLlamada;
		}
		
		return idRegistroLlamada;
	}
}
