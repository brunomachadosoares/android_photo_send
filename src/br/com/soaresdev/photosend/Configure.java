package br.com.soaresdev.photosend;

import android.util.Log;

public class Configure {

	private String toParser;
	private String host, user, passwd;
	
	public Configure(String toParser) {
		Log.w("PHOTOSEND_CONFIGURE", "Construtor do configure");
		this.toParser = toParser;
		this.parser();
	}
	
	private void parser() {
		Log.w("PHOTOSEND_CONFIGURE", "Inicio de parser");
		//Log.w("PHOTOSEND_CONFIGURE", "toParser [" + toParser + "]");
		host = toParser.substring(toParser.indexOf("HOST:")+5, toParser.indexOf("USER")-1);
		//Log.w("PHOTOSEND_CONFIGURE", "host: " + host);
		user = toParser.substring(toParser.indexOf("USER:")+5, toParser.indexOf("PASSWD")-1);
		passwd = toParser.substring(toParser.indexOf("PASSWD:")+7);
		Log.w("PHOTOSEND_CONFIGURE", host + "/" + user + "/" + passwd);
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPasswd() {
		return passwd;
	}

	public void setPasswd(String passwd) {
		this.passwd = passwd;
	}
	
	
}
