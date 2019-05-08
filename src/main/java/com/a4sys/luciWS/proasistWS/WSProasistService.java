package com.a4sys.luciWS.proasistWS;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.a4sys.core.dao.ALQueryResult;
import com.a4sys.core.dao.ConnectionJDBC;
import com.a4sys.core.dao.DaoService;
import com.a4sys.core.dao.domain.Consulta;
import com.a4sys.core.dao.domain.ParametroConsulta;
import com.a4sys.core.exceptions.SystemException;
import com.a4sys.luciWS.domain.MailAddressData;
import com.a4sys.luciWS.util.QuerysDB;
import com.a4sys.luciWS.util.SystemConstants;
import com.a4sys.luciWS.util.Util;
import com.a4sys.luciWS.util.WsErrors;

/***
 * 
 * @author Martin Morones
 *
 */
public class WSProasistService extends DaoService{


	public WSProasistService() {
		super(SystemConstants.JDNI);		
	}


	/***
	 * METODO QUE OBTIENE LOS DATOS BASICOS APARTIR DEL ID DE LA NOTIFICACION
	 * @param idConfirmacion
	 * @return
	 */
	public ALQueryResult getBasicData (Long idConfirmacion,String claveAutenticacion){
		List<ParametroConsulta> parametros = new ArrayList<ParametroConsulta>();
		parametros.add(new ParametroConsulta("idConfirmacion", idConfirmacion.toString()));
		parametros.add(new ParametroConsulta("claveAutenticacion", claveAutenticacion));


		Consulta consulta = new Consulta( (claveAutenticacion == null ? QuerysDB.GET_DATA_CONFIRMACION : QuerysDB.GET_DATA_CONFIRMACION_WITH_TOKEN  ), parametros);
		ALQueryResult res = new ALQueryResult();
		try {
			res = ejecutarConsulta(consulta);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return res;
	}


	/***
	 * METODO QUE OBTIENE LOS DATOS BASICOS APARTIR DEL ID DE LA NOTIFICACION
	 * @param idConfirmacion
	 * @return
	 */
	public ALQueryResult getCountConfirmacion (Long folio,Long idTipoNotificacion){
		List<ParametroConsulta> parametros = new ArrayList<ParametroConsulta>();
		parametros.add(new ParametroConsulta("idTipoNotificacion", idTipoNotificacion.toString()));
		parametros.add(new ParametroConsulta("folio", folio.toString()));
		Consulta consulta = new Consulta(QuerysDB.GET_COUNT_CONFIRMACION, parametros);
		ALQueryResult res = new ALQueryResult();
		try {
			res = ejecutarConsulta(consulta);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return res;
	}


	/***
	 * METODO QUE OBTIENE LOS DATOS BASICOS APARTIR DEL ID DE LA NOTIFICACION
	 * @param idConfirmacion
	 * @return
	 */
	public Boolean sendNotificaction (Long folio,Long idTipoNotificacion){

		Boolean respuesta  = true;
		List<ParametroConsulta> parametros = new ArrayList<ParametroConsulta>();		
		parametros.add(new ParametroConsulta("idTipoNotificacion", idTipoNotificacion.toString()));
		parametros.add(new ParametroConsulta("folio", folio.toString()));
		Consulta consulta = new Consulta(QuerysDB.SEND_NOTIFICATION, parametros);
		ALQueryResult res =  new ALQueryResult();

		try {
			res = ejecutarConsulta(consulta);
			if( res.size() == 0 ){respuesta = false;}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			respuesta = false;
		}
		return respuesta;
	}

	/***
	 * METODO QUE MARCA COMO 
	 * @param idConfirmacion
	 * @return
	 */
	public Integer updateConfirmacion (Long idConfirmacion,String confirmacion){
		List<ParametroConsulta> parametros = new ArrayList<ParametroConsulta>();
		parametros.add(new ParametroConsulta("idConfirmacion", idConfirmacion.toString()));
		parametros.add(new ParametroConsulta("confirmacion", confirmacion));
		Consulta consulta = new Consulta(confirmacion.equalsIgnoreCase("C") ? QuerysDB.UPDATE_CONFIRMACION_C : QuerysDB.UPDATE_CONFIRMACION_N, parametros);		
		Integer res = 0;

		try {
			res = ejecutarConsultaUpdate(consulta);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return res;
	}


	/***
	 * METOODO QUE VERIFICA SI EL TOKEN ESTA VIGENTE Y ES VALIDO
	 * @param idConfirmacion
	 * @param claveAutenticacion
	 * @return
	 */
	public String isValidToken(Long idConfirmacion , String claveAutenticacion){

		String respuesta = "";

		ALQueryResult data = getBasicData(idConfirmacion,claveAutenticacion);
		ALQueryResult count = new ALQueryResult();
		if( data.size() != 0 ){
			//veficamos si ya fue confirmada la notificacion
			if( !Boolean.parseBoolean(data.get(0,"YACONFIRMADO") == null ? "false" : data.get(0,"YACONFIRMADO").toString()) ){
				//verificamos si el token es valido
				if( !Boolean.parseBoolean(data.get(0,"TOKENESVALIDO") == null ? "false" : data.get(0,"TOKENESVALIDO").toString()) ){
					respuesta = "2,"+WsErrors.WS_ERROR_2;				
				}else{				

					//verificamos si el token esta vigente	
					if( ! Boolean.parseBoolean(data.get(0,"TOKENESTAVIGENTE") == null ? "false" : data.get(0,"TOKENESTAVIGENTE").toString()) ){					
						Long maxIntentos = (Long)data.get(0,"ENVIOSPERMITIDOS");

						//Contamos cuantas confirmaciones ha tenido de la misma notificacion
						count = getCountConfirmacion((Long)data.get(0,"FOLIO"), (Long)data.get(0,"IDTIPONOTIFICACION"));					
						if ( (Long)count.get(0,"RES") >= maxIntentos){						
							respuesta = "3,"+WsErrors.WS_ERROR_3;
						}else{
							// se marca la confirmacion como no Confirmada
							updateConfirmacion(idConfirmacion, "N");
							//reenviarNotificacion con token nuevo
							if( sendNotificaction((Long)data.get(0,"FOLIO"), (Long)data.get(0,"IDTIPONOTIFICACION")) ){																			
								respuesta = "5,"+WsErrors.WS_ERROR_5;
							}else{
								respuesta = "7,"+WsErrors.WS_ERROR_7;
							}

						}
					}
				}
			}else{
				respuesta = "8,"+WsErrors.WS_ERROR_8;
			}
		}else{
			respuesta = "6,"+WsErrors.WS_ERROR_6;
		}

		return respuesta;
	}



	/***
	 * METODO QUE OBTIENE LOS DATOS DEL DOMICILIO PARA SE MOSTRADOS 
	 * @param idConfirmacion
	 * @param token
	 * @return
	 */	
	public ALQueryResult getDataAddress( Long idConfirmacion){

		ALQueryResult data = getBasicData(idConfirmacion,null);

		if( data.size() != 0 ){

			CallableStatement cs = null;
			ResultSet resultSet = null; 			

			Connection con = new ConnectionJDBC(SystemConstants.JDNI).getConection();	
			try {        	
				cs = con.prepareCall("{call GpEngine.getDomicilioDeCorrespondencia(?,?)}");
				cs.setLong(1, (Long)data.get(0,"FOLIO"));
				cs.registerOutParameter(2, -10); //OracleType.Cursor = -10
				cs.execute();

				resultSet = (ResultSet) cs.getObject(2);

				data = new ALQueryResult();
				data = sqlQueryExecutor.resultSetToAlQueryResult(resultSet);

			} catch (Exception e) {
				e.printStackTrace();
			} finally{
				try {
					cs.close();
					con.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

		}

		return data;
	}




	/***
	 * METODO QUE ACTUALIZA LOS DATOS DE DOMICILIO DE CORRESPONDENCIA
	 * @param data
	 * @return
	 * @throws Exception 
	 */	
	public String saveDataAddress(String json) throws Exception{
		String respuesta = "";

		MailAddressData mailAddressData = (MailAddressData)Util.fromJson(json, MailAddressData.class);

		ALQueryResult basicData = getBasicData(mailAddressData.getIdConfirmacion(),null);

		//si existen datos 
		if( basicData.size() != 0 ){

			sqlQueryExecutor.addParameter("folio", (Long)basicData.get(0,"FOLIO"));
			sqlQueryExecutor.addParameter("calle", mailAddressData.getCalle());
			sqlQueryExecutor.addParameter("noExterior", mailAddressData.getNoExterior());
			sqlQueryExecutor.addParameter("noInterior", mailAddressData.getNoInterior());
			sqlQueryExecutor.addParameter("cPostal", mailAddressData.getcPostal());
			sqlQueryExecutor.addParameter("idMunicipio", mailAddressData.getIdMunicipio());
			sqlQueryExecutor.addParameter("colonia", mailAddressData.getColonia());
			sqlQueryExecutor.addParameter("telefonoCasa", mailAddressData.getTelefonoCasa());
			sqlQueryExecutor.addParameter("entreCalle1",mailAddressData.getEntreCalle1() );
			sqlQueryExecutor.addParameter("entreCalle2", mailAddressData.getEntreCalle2());
			sqlQueryExecutor.addParameter("horaInicioEntrega", mailAddressData.getHoraInicioEntrega());
			sqlQueryExecutor.addParameter("horaFinEntrega", mailAddressData.getHoraFinEntrega());
			sqlQueryExecutor.addParameter("indicacionesEntrega",mailAddressData.getIndicacionesEntrega() );
			sqlQueryExecutor.addParameter("telefonoOficina", mailAddressData.getTelefonoOficina());

			ALQueryResult addressCfg = sqlQueryExecutor.executeQuery(QuerysDB.GET_CFG_MAIL_ADDRESS);

			if( addressCfg.size() != 0 ){
				Boolean clienteEsContratante = addressCfg.get(0,"CLIENTEESCONTRATANTE").toString().equals("S") ? true : false;
				Boolean domClienteEsContratante = addressCfg.get(0,"DOMCLIENTEESCONTRATANTE").toString().equals("S") ? true : false;
				Boolean domCorreponEsContratante = addressCfg.get(0,"DOMCORRESPONESDOMCONTRATANTE").toString().equals("S") ? true : false;
				Boolean domCorreponEsCliente = addressCfg.get(0,"DOMCORRESPONDENCIAESDOMCLIENTE").toString().equals("S") ? true : false;

				String querys [] = new String[2];

				String updateAllCorrespondance ="update tbldomiciliosadicionales set calle = {calle},noExterior = {noExterior},noInterior = {noInterior},colonia = {colonia}, cpostal = {cPostal},telefono = {telefonoCasa}, " +
						"entreCalle1 = {entreCalle1} ,entreCalle2 = {entreCalle2}, horainicioEntrega = {horaInicioEntrega},horaFinEntrega = {horaFinEntrega},indicacionesEntrega = {indicacionesEntrega}, " +
						"telefonoOficina = {telefonoOficina}, idMunicipio = {idMunicipio}  where idcliente = (select idCliente from tblsolicitudes where folio = {folio}) and idTipoDomicilioAdicional = 3";

				String updateOnlyTblDomicilio ="update tbldomicilios set calle = {calle},noExterior = {noExterior},noInterior = {noInterior},colonia = {colonia}, cpostal = {cPostal},telefono = {telefonoCasa}, idMunicipio = {idMunicipio}  " +
						"where idcliente = (select idCliente from tblsolicitudes where folio = {folio}) "; 

				String updateOnlyContractor = "update tbldomiciliosadicionales set calle = {calle},noExterior = {noExterior},noInterior = {noInterior},colonia = {colonia}, cpostal = {cPostal},telefono = {telefonoCasa}, idMunicipio = {idMunicipio}  " +
						"where idcliente = (select idCliente from tblsolicitudes where folio = {folio}) and idTipoDomicilioAdicional = 2";

				String updateCorrespondance ="update tbldomiciliosadicionales set entreCalle1 = {entreCalle1} ,entreCalle2 = {entreCalle2}, horainicioEntrega = {horaInicioEntrega},horaFinEntrega = {horaFinEntrega}," +
						"indicacionesEntrega = {indicacionesEntrega},telefonoOficina = {telefonoOficina}" +
						"where idcliente = (select idCliente from tblsolicitudes where folio = {folio}) and idTipoDomicilioAdicional = 3";



				querys[1] = updateCorrespondance;

				if(clienteEsContratante){

					querys[0] = updateOnlyTblDomicilio;

					if(!domCorreponEsCliente){					
						if(!domCorreponEsContratante){							
							querys[0] = updateAllCorrespondance; 
							querys[1] = null;
						}
					}

				}else{

					if(domClienteEsContratante){
						querys[0] = updateOnlyTblDomicilio;

						if(!domCorreponEsCliente){					
							if(!domCorreponEsContratante){							
								querys[0] = updateAllCorrespondance;
								querys[1] = null;
							}
						}

					}else{
						querys[0] = updateOnlyContractor;

						if(!domCorreponEsContratante){
							querys[0] = updateAllCorrespondance;
							querys[1] = null;
						}
					}

				}

				//Ejecutamos los updates
				for (int i = 0; i < querys.length; i++) {
					try {
						if(querys[i] != null){
							sqlQueryExecutor.executeUpdateUsingString(querys[i]);
						}
					} catch (Exception e) {							
						sqlQueryExecutor.rollBack();													
						throw e;
					}
				}
				sqlQueryExecutor.commit();

				try {
					saveHistoryApplication(mailAddressData.getIdConfirmacion(), 112l); // guardar historia por la actualizacion del domicilio de correspondencia
				} catch (Exception e) {}

			}else{
				respuesta = "6,"+WsErrors.WS_ERROR_6;
			}

		}else{
			respuesta = "6,"+WsErrors.WS_ERROR_6;
		}



		return respuesta;
	}

	/***
	 * GUARDA UNA HISTORIA DE SOLICITUDES
	 * @param idConfirmacion
	 * @param idEventoHistorico
	 * @throws SQLException 
	 * @throws SystemException 
	 * @throws Exception 
	 */
	public void saveHistoryApplication(Long idConfirmacion,Long idEventoHistorico) throws SystemException, SQLException {
		sqlQueryExecutor.addParameter("idConfirmacion", idConfirmacion);
		ALQueryResult dataForHistory = sqlQueryExecutor.executeQuery(QuerysDB.GET_DATA_FOR_HISTORY);
		if(dataForHistory.size() != 0){

			//guardamos la historia de la solicitud
			sqlQueryExecutor.addParameter("folio",dataForHistory.get(0,"FOLIO"));
			sqlQueryExecutor.addParameter("idEstadoSolicitud",dataForHistory.get(0,"IDESTADOSOLICITUD"));
			sqlQueryExecutor.addParameter("idEventoHistoricoSol",idEventoHistorico);
			sqlQueryExecutor.addParameter("idUsuario",1l);					
			sqlQueryExecutor.executeUpdate(QuerysDB.SAVE_HISTORY_APPLICATION);	
			sqlQueryExecutor.commit();				

		}
	}

	/****
	 * METODO QUE CONFIRMA LA NOTIFICACION DE RECIBIDA
	 * @param idConfirmacion
	 * @param claveAutenticacion
	 * @return
	 */
	public String cofirmNotification(Long idConfirmacion , String claveAutenticacion){
		String respuesta = "";
		ALQueryResult data = getBasicData(idConfirmacion,claveAutenticacion);		
		if( data.size() != 0 ){
			//veficamos si ya fue confirmada la notificacion
			if( !Boolean.parseBoolean(data.get(0,"YACONFIRMADO") == null ? "false" : data.get(0,"YACONFIRMADO").toString()) ){
				//verificamos si el token es valido
				if( !Boolean.parseBoolean(data.get(0,"TOKENESVALIDO") == null ? "false" : data.get(0,"TOKENESVALIDO").toString()) ){
					respuesta = "2,"+WsErrors.WS_ERROR_2;				
				}else{				
					//verificamos si el token esta vigente	
					if( !Boolean.parseBoolean(data.get(0,"TOKENESTAVIGENTE") == null ? "false" : data.get(0,"TOKENESTAVIGENTE").toString()) ){			
						respuesta = "10,"+WsErrors.WS_ERROR_10;
					}else{
						// se actualiza la notificacion
						try {
							updateConfirmacion(idConfirmacion, "C");
							saveHistoryApplication(idConfirmacion, 109l); //confimacion de notificacion							
							respuesta = "0,"+WsErrors.WS_ERROR_0;							
						} catch (Exception e) {
							respuesta = "-1,"+e.getCause() == null ? e.getMessage() : e.getCause().toString();
						}							


					}
				}
			}else{
				respuesta = "8,"+WsErrors.WS_ERROR_8;
			}
		}else{
			respuesta = "6,"+WsErrors.WS_ERROR_6;

		}	

		return respuesta;
	}


}
