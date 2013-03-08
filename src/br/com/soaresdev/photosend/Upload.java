package br.com.soaresdev.photosend;

import java.io.FileInputStream;
import java.io.IOException;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import android.util.Log;

public class Upload {

	private static boolean connected = false;
	private static FTPClient ftp = null;

	public static boolean isConnected() {
		return connected;
	}
	
	private void makeConn(String host, String user, String passwd) {

		if (isConnected() == true) {
			Log.w("SURFACE", "Upload: Já está conectado!");
			return;
		}

		try {
			ftp = new FTPClient();
			
			Log.w("SURFACE", "Conectando");
			ftp.connect(host, 21);
			
			Log.w("SURFACE", "Setando parametros");
			ftp.setRemoteVerificationEnabled(false);
			ftp.setBufferSize(2000000);

			Log.w("SURFACE", "Fazendo login");
			ftp.login(user, passwd);

			ftp.enterLocalPassiveMode();

			ftp.setFileType(FTP.BINARY_FILE_TYPE);

			connected = true;

			Log.w("SURFACE", "Conectado e logado");

		} catch (IOException e) {
			Log.w("SURFACE", "MakeConn: excpt: " + e.getMessage());
			throw new Error(e.getMessage());
		}

	}

	public void uploadNow(String host, String user, String passwd,
			String dst_file, String src_file) {

		Log.w("SURFACE", "Inicio do uploadNow");
		Log.w("SURFACE", "Upload, parama: " + host + "/" + user + "/" + passwd);

		try {
			makeConn(host, user, passwd);
			
			FileInputStream fis = null;

			Log.w("SURFACE", "Passou por aqui, conn já está ok!");

			fis = new FileInputStream(src_file);

			Log.w("SURFACE", "Enviando arquivo");
			ftp.storeFile(dst_file, fis);

			Log.w("SURFACE", "Arquivo enviado. Fechando conexoes...");

			fis.close();

		} catch (IOException e) {
			Log.w("SURFACE", "Fail uploadNow: " + e.getMessage());
			throw new Error(e.getMessage());
		}
	}

	public static void logout() {

		try {
			if(isConnected() == true) {
				ftp.logout();
				connected = false;
			}
		} catch (IOException e) {
			Log.w("SURFACE", "Fail logout: " + e.getMessage());

		}
	}
}
