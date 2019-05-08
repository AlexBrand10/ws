package com.a4sys.luciWS.exportarventasdiariasWS;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.Types;

import com.a4sys.core.dao.DaoService;
import com.a4sys.luciWS.util.QuerysDB;
import com.a4sys.luciWS.util.SystemConstants;
import com.a4sys.luciWS.util.Util;
import com.a4sys.luciWS.util.WsErrors;
import com.a4sys.core.dao.ALQueryResult;
import com.a4sys.core.exceptions.SystemException;




/***
 * 
 * @author Martin Morones
 *
 */
public class WSExportarVentasDiariasService extends DaoService{


	/***
	 * Constructor de la Clase
	 */
	public WSExportarVentasDiariasService() {
		super(SystemConstants.JDNI);

	}

	/***
	 * METODO QUE CREA UNA REMESA , UN ARCHIVO Y LO GUARDA EN UN SFTP PARA LA CAPAÑA DE RSA
	 * @param idCampana
	 * @param idUsuarioResponsable
	 * @param idsLayOut 
	 * @param procedimiento 
	 * @param idSftp 
	 * @return
	 */
	public String ventasDiarias(String idCampana, Long idUsuarioResponsable, String fechaInicial, String fechaFinal, String idsLayOut) {

		String respuesta = "";
		String query = "";
		CallableStatement cs = null;
		Long idRemesaExport = 0L;
		

		//query = "{call ? := ExportEngine.RSASales(?,?,?,?)}";
		query = "{call ? := ExportEngine.ExportVentasDiarias(?,?,?,?,?)}";

		try {

			cs = sqlQueryExecutor.getConnection().prepareCall(query);
			
			cs.registerOutParameter(1, Types.INTEGER);
			cs.setString(2, idCampana);
			cs.setLong(3, idUsuarioResponsable);
			cs.setString(4, fechaInicial);
			cs.setString(5, fechaFinal);
			cs.setString(6, idsLayOut);
			cs.execute();      //ejecucion de funcion RSSALES

			idRemesaExport =  Long.parseLong(cs.getObject(1).toString()); //obtiene valor de retorno idRemesaExport

		} catch (SQLException e) {
			//e.printStackTrace();
			respuesta = "-1,Error en Base de Datos:"+e.getMessage();
		} catch (Exception e) {
			//e.printStackTrace();
			respuesta = "-1,Exepcion:"+e.getMessage();
		}finally{
			try {
				cs.close();				
			} catch (SQLException e) {
				//e.printStackTrace();
				respuesta = "-1,Error en Base de Datos:"+e.getMessage();
			}
		}

		if(respuesta.equals("")){	
			if(idRemesaExport.equals(0L)){
				respuesta = "11,"+WsErrors.WS_ERROR_11;
			}else{
			
			
				
				//Obtenemos todos los distinctos idLayouts que se utilizaron
				sqlQueryExecutor.addParameter("idRemesaExport", idRemesaExport);
				ALQueryResult idsLayouts = new ALQueryResult();
				try {
					idsLayouts = sqlQueryExecutor.executeQuery(QuerysDB.GET_IDSLAYOUT_BY_IDREMSAEXPORT);
				} catch (SystemException e) {
					respuesta = "-1,Error en Base de Datos:"+e.getMessage();
				} catch (SQLException e) {
					respuesta = "-1,Error en Base de Datos:"+e.getMessage();
				}
				
				//Iteramos los idslayout para genrear un archivo por cada uno y guardarlos en el sftp que tengan asociado
				String res = "";
				if(idsLayouts.size() != 0){
					
					String remesaName= "";
					for (int i = 0; i < idsLayouts.size(); i++) {
						Long idLayOut = (Long)idsLayouts.get(i,"IDLAYOUT");
						String fileName = Util.getFileNameByLayout(sqlQueryExecutor,idLayOut);
						remesaName += fileName + ",";
						Boolean encabezado = idsLayouts.get(0,"ENCABEZADO").toString().equals("S") ? true : false;
						Boolean pieDePagina = idsLayouts.get(0,"PIEDEPAGINA").toString().equals("S") ? true : false;
						res += Util.getFileGeneric(sqlQueryExecutor,idRemesaExport,encabezado,pieDePagina,(Long)idsLayouts.get(i,"IDSFTP"),idLayOut,fileName);
						if(!"".equals(res)){
							res +="<br />";
						}
					}
					
					
					//Actualizamos el nombre de la remesa al conjunto de todos los  archivos generados
					if("".equals(res)){ 
						try {
							sqlQueryExecutor.addParameter("idRemesaExport", idRemesaExport);
							sqlQueryExecutor.addParameter("name", remesaName.substring(0, remesaName.length()-1));
							sqlQueryExecutor.executeUpdate(QuerysDB.UPDATE_NAME_REMESAEXPORT);
							sqlQueryExecutor.commit();
						} catch (SystemException e) {
							respuesta = "-1,Error en Base de Datos:"+e.getMessage();
						} catch (SQLException e) {
							respuesta = "-1,Error en Base de Datos:"+e.getMessage();
						}
						
					}
					
				}
				
				
				if("".equals(res)){ 
					respuesta = "0,"+WsErrors.WS_ERROR_0;
					//Enviar Alerta
					sendAlertaOk(idRemesaExport);
				}else{
					respuesta = "12,"+res+ ", para la Campana con Id:"+idCampana;
					//RollBackRemesa									
					Util.rollbackRemesa(sqlQueryExecutor,idRemesaExport);
					sendAlerta(30l, res);
				}	
			}
		}
		return respuesta;
	}
	/***
	 * METODO QUE OBTIEENE LOS FOLIOS QUE GENERARON CON LA REMSA Y LOS 
	 * ENVIA EN ALERTA 29 
	 * @param idRemesaExport
	 */
	private void sendAlertaOk(Long idRemesaExport) {
		
		
		sqlQueryExecutor.addParameter("idRemesa", idRemesaExport);
		ALQueryResult res = null  ;
		try {
			res = sqlQueryExecutor.executeQuery(QuerysDB.GET_LAYOUTS);
		} catch (SystemException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String folios = "";
		if (res.size() != 0){
			for (int i = 0; i < res.size(); i++) {
				folios +=res.get(i,"FOLIO").toString()+",";
			}
			
			folios = folios.substring(0,folios.length()-1);
		}
		
		if(!folios.equals("")){
			
			String nombreRemesa =  res.get(0,"REMESAEXPORT").toString();
			String nombreCampana = res.get(0,"CAMPANA").toString();
			String params = nombreRemesa.concat("|").concat(res.size()+"").concat("|").concat(nombreCampana).concat("|").concat(folios);
			sendAlerta(29l, params );
			
		}
		
	}
	
	
	/***
	 * METODO QUE ENVIA UNA ALERTA CLASICA DE LUCI
	 * @param idAlerta
	 * @param variables
	 */
	private void sendAlerta(Long idAlerta, String variables){
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
