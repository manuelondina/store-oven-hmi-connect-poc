package com.mercadona.poc.constants;

public class MessageTypeEnum {

	private MessageTypeEnum() {
	}

	public static final String INICIANDO = "Iniciando servicio";

	public static final String SUBIENDO = "Subiendo archivos";

	public static final String TERMINADO = "Transferencia terminada";

	public static final String ERROR = "Ocurrio algun problema en la transferencia";

	public static final String UNZIP = "Descomprimiendo";

	public static final String CARGA = "Empezando carga de horno";

	public static final String MONTANDO = "Montando Libro en HMI";

	public static final String CONECTANDOHMI = "Conexion con HMI";

	public static final String ACTIVANDOLIBRO = "Activando Libro en HMI";

	public static final String ERRORHMI = "ERROR en HMI";

	public static final String ERRORCONEXIONHMI = "ERROR conexion al HMI";

	public static final String EXITOHMI = "EXITO en HMI";

	public static final String IDLIBRO = "LIBRO ACTIVADO";

	public static final String TIEMPO = "TIEMPO TRANSCURRIDO";

	public static final String ERRORARCHIVO = "PROBLEMAS EN ARCHIVO";

	public static final String ERRORENCSV = "FALTAN CSV(s)";

	public static final String ERRORENLISTA1 = "FALTA LISTA_1";

	public static final String ERRORENZIP = "ERROR EN ZIP";

}