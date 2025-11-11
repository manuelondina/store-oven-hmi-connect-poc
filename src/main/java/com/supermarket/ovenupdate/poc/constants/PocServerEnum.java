package com.supermarket.ovenupdate.poc.constants;

public class PocServerEnum {
	private PocServerEnum() {
	}

	public static final String PROTOCOL = "http://";
	public static final String START_HTML = "/start.html";
	public static final String LOGIN_FORM = "/FormLogin";
	public static final String LOGIN_TOKEN = "%=Token";
	public static final String FILES = "/files?";
	public static final String DEL_FILES = "/files/";
	public static final String VERBO = "POST";
	public static final String UP_SERVER = "UP=TRUE&FORCEBROWSE";
	public static final String DELETE_FILE = "?DELETE&UP=TRUE&SingleUseToken=";
	public static final String SIEMENS_COOKIE = "siemens_ad_session=";
	public static final String MULTIPART_CONTENT_TYPE = "multipart/form-data; boundary=";
	public static final String PORT = ":80";

	public static final String FORMAT_MULTIUSE = "\r\n--";
	public static final String FORMAT_MULTIUSE_SEGMENT = "\r\nContent-Disposition: form-data; name=\"MultiUseToken\";\r\n\r\n";;

	public static final String CONTENT_SERVER = "Content-Disposition: form-data; name=\"uplTheFile\"; filename=\"";
	public static final String CONTENT_SERVER_END = "\"\r\nContent-Type: application/octet-stream\r\n\r\n";
	public static final String BOUNDARY = "----------------------------";
	public static final String SOAP_RUNTIME = "/soap/RuntimeAccess";
}
