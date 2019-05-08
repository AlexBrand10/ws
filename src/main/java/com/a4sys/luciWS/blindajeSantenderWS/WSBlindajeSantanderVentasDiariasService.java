package com.a4sys.luciWS.blindajeSantenderWS;

import java.io.IOException;
import java.io.PrintStream;
import java.io.OutputStream;
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
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;




/***
 * 
 * @author Martin Morones
 *
 */
public class WSBlindajeSantanderVentasDiariasService extends DaoService{


	/***
	 * Constructor de la Clase
	 */
	public WSBlindajeSantanderVentasDiariasService() {
		super(SystemConstants.JDNI);

	}

	/***
	 * METODO QUE CREA UNA REMESA , UN ARCHIVO Y LO GUARDA EN UN SFTP PARA LA CAPAÑA DE RSA
	 * @param idCampana
	 * @param idUsuarioResponsable
	 * @return
	 */
	public String ventasDiarias(String idCampana, Long idUsuarioResponsable, String fechaInicial, String fechaFinal) {

		WsErrors error = new WsErrors();
		String respuesta = "";
		String query = "";
		CallableStatement cs = null;
		Long idRemesaExport = 0L;


		query = "{call ? := ExportEngine.BlindajeSantanderSales(?,?,?,?)}";

		try {

			cs = sqlQueryExecutor.getConnection().prepareCall(query);
			
			cs.registerOutParameter(1, Types.INTEGER);
			cs.setString(2, idCampana);
			cs.setLong(3, idUsuarioResponsable);
			cs.setString(4, fechaInicial);
			cs.setString(5, fechaFinal);
			cs.execute();      //ejecucion de funcion BlindajeSantanderSales

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
			
		respuesta = respuesta.replace("\n", " ").replace(":", " ");
		
		if(respuesta.equals("")){	
			if(idRemesaExport.equals(0L)){
				respuesta = "11,"+error.WS_ERROR_11;
			}else{
				

				//Obtenemos todos los distinctos idLayouts que se utilizaron
				sqlQueryExecutor.addParameter("idRemesaExport", idRemesaExport);
				ALQueryResult idsLayouts = new ALQueryResult();
				try {
					idsLayouts = sqlQueryExecutor.executeQuery(QuerysDB.GET_IDSLAYOUT_BY_IDREMSAEXPORT);
				} catch (SystemException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
					respuesta = "-1,Error en Base de Datos:"+e.getMessage();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
					respuesta = "-1,Error en Base de Datos:"+e.getMessage();
				}
				
				//Iteramos los idslayout para genrear un archivo por cada uno y guardarlos en el sftp que tengan asociado
				String res = "";
				if(idsLayouts.size() != 0){
					
					for (int i = 0; i < idsLayouts.size(); i++) {				
						String fileName = null;
						Long idLayOut = (Long)idsLayouts.get(i,"IDLAYOUT");
						if(idLayOut.equals(7l)){
							fileName = idsLayouts.get(i,"REMESAEXPORT").toString();
						}else{
							fileName = Util.getFileNameByLayout(sqlQueryExecutor,idLayOut);
						}
						res += Util.getFileGeneric(sqlQueryExecutor,idRemesaExport,true,false,(Long)idsLayouts.get(i,"IDSFTP"),idLayOut,fileName);
						if(!"".equals(res)){
							res +="<br />";
						}
					}
					
				}				
				//String res =Util.getFileGeneric(sqlQueryExecutor,idRemesaExport,true,true,(Long)idsLayouts.get(i,"IDSFTP"),(Long)idsLayouts.get(i,"IDLAYOUT")); //id fpt blindaje Santander --> 2
				if("".equals(res)){ 
					respuesta = "0,"+error.WS_ERROR_0;
					//Enviar Alerta
					sendAlertaOk(idRemesaExport);
				}else{
					respuesta = "12,"+res;
					//RollbackRemesa
					Util.rollbackRemesa(sqlQueryExecutor,idRemesaExport);
					sendAlerta(32l, res);
				}	
			}
		}else{			
			sendAlerta(32l, respuesta);
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
			sendAlerta(31l, folios);
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
