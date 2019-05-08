package com.a4sys.luciWS.util;
/***
 * Clase que Contiene los posibles errores para los distintos methos del web service
 * @author fedora
 *
 */
public class WsErrors {
	
	public static final String WS_ERROR_MENOS_1 = "Error Desconocido.";
	public static final String WS_ERROR_0 	    = "Ok.";
	public static final String WS_ERROR_1       = "No se recibieron todos los parámetros completos para la petición.";
	public static final String WS_ERROR_2  		= "La Clave de Validación no es Valida.";
	public static final String WS_ERROR_3  		= "Se ha alcanzado el limite de días para esperar una confirmación.";
	public static final String WS_ERROR_4  		= "No se Encontran Datos.";
	public static final String WS_ERROR_5  		= "Se ha enviado otra nueva clave de autenticación a su correo electrónico.";
	public static final String WS_ERROR_6  		= "No se Obtubieron Datos Para Esa Confirmación.";
	public static final String WS_ERROR_7  		= "Ocurrio Un Error al Reenviar La Confirmación.";
	public static final String WS_ERROR_8  		= "La Notificación ya fue Confirmada.";
	public static final String WS_ERROR_9  		= "El Metodo al Que intentas Accesar no Existe.";
	public static final String WS_ERROR_10 		= "La clave de Validación no es Vigente.";
	public static final String WS_ERROR_11 		= "No fue posible Generar ninguna Remesa.";
	public static final String WS_ERROR_12 		= "Ocurrió un error al obtener la URL del reporte.";
	public static final String WS_ERROR_13 		= "Ocurrió un error al generar el archivo.";
	public static final String WS_ERROR_15 		= "Ocurrió un error al ingresar el registro de la llamada.";
	public static final String WS_ERROR_16 		= "Folio incorrecto.";
	public static final String WS_ERROR_17 		= "El folio en LUCI no existe, por favor ingrese un folio válido.";
	public static final String WS_ERROR_18 		= "No se Encontraron Datos de cliente y/o TDC Para Realizar el Cargo.";


	//Estatus 5: Errores internos getClientInfo
	public static final String WS_S5_MSG_1     	= "Error en la información.";
	public static final String WS_S5_MSG_2     	= "El folio recibido no es un número.";
	public static final String WS_S5_MSG_3     	= "El folio recibido no existe en el sistema.";
	public static final String WS_S5_MSG_4     	= "El usuario y/o contraseña no son válidos.";
	
}
