package de.nauriculus.bonkraids.utils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.xml.bind.DatatypeConverter;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class WalletAPI {
	
	/**
	 * Checks if a wallet exists for the given user UUID by making a GET request to the NexiLabs API.
	 * @param userUUID the UUID of the user to check for a wallet
	 * @return true if a wallet exists for the user, false otherwise
	 */
	
	public static String decrypt(String messageBase64, String keyBase64, String ivBase64) throws Exception {
        byte[] messageArray = DatatypeConverter.parseBase64Binary(messageBase64);
        byte[] keyArray = DatatypeConverter.parseBase64Binary(keyBase64);
        byte[] iv = DatatypeConverter.parseBase64Binary(ivBase64);

        SecretKey secretKey = new SecretKeySpec(keyArray, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
        cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));
        return new String(cipher.doFinal(messageArray));
    }
	
	public static String getWalletWithoutPIN(String userUUID) {
	    try {
	        // Load the certificate from the cert.pem file
	        CertificateFactory cf = CertificateFactory.getInstance("X.509");
	        Certificate cert;
	        try (InputStream certFile = new FileInputStream("/etc/letsencrypt/live/certificate.crt")) {
	            cert = cf.generateCertificate(certFile);
	        }

	        // Create a KeyStore and add the certificate to it
	        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
	        keyStore.load(null, null);
	        keyStore.setCertificateEntry("cert", cert);

	        // Create a TrustManagerFactory and initialize it with the KeyStore
	        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
	        trustManagerFactory.init(keyStore);

	        // Create an SSLContext and initialize it with the TrustManagerFactory
	        SSLContext sslContext = SSLContext.getInstance("TLS");
	        sslContext.init(null, trustManagerFactory.getTrustManagers(), null);

	        // Create an HttpsURLConnection for the wallet request and set the SSLContext for this connection only
	        URL url = new URL("/getWalletOnly?uuid=" + userUUID);
	        HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
	        con.setSSLSocketFactory(sslContext.getSocketFactory());
	        con.setRequestMethod("GET");

	        // Get the response code from the connection
	        int responseCode = con.getResponseCode();

	        // If the response code indicates success, read the response and check if a wallet exists for the user
	        if (responseCode == HttpsURLConnection.HTTP_OK) {
	            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
	            String inputLine;
	            StringBuffer response = new StringBuffer();

	            // Read the response into a StringBuffer
	            while ((inputLine = in.readLine()) != null) {
	                response.append(inputLine);
	            }
	            in.close();

	            JSONParser parser = new JSONParser();
	            Object obj = null;
	            try {
	                obj = parser.parse(response.toString());

	            } catch (ParseException e1) {
	            	 return "ERROR";
	            }

	            JSONObject jsonObject = (JSONObject) obj;
	         
	            JSONArray results = (JSONArray) jsonObject.get("results");
	            
	            for (Object objj : results) {

	                JSONObject jsonTx = (JSONObject) objj;

	                String wallet = (String) jsonTx.get("WALLET");
	                return wallet;
	            }
	        }

	        // If the response code indicates an error, log an error message and return false
	        else {
	            System.out.println("Error while checking wallet for user ID " + userUUID);
	            return "ERROR";
	        }
	    }

	    // If an exception is thrown, log an error message and return false
	    catch (Exception e) {
	        System.out.println("Exception while checking/creating wallet: " + e.getMessage());
	        return "ERROR";
	    }
	    System.out.println("Exception while checking/creating wallet: ");
	    return "ERROR";
	}
	
    public static String sendSPL(String houseWallet, String receiver, String amount, String houseWalletID) {
	    try {
	        // Load the certificate from the cert.pem file
	        CertificateFactory cf = CertificateFactory.getInstance("X.509");
	        Certificate cert;
	        try (InputStream certFile = new FileInputStream("/etc/letsencrypt/live/certificate.crt")) {
	            cert = cf.generateCertificate(certFile);
	        }

	        // Create a KeyStore and add the certificate to it
	        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
	        keyStore.load(null, null);
	        keyStore.setCertificateEntry("cert", cert);

	        // Create a TrustManagerFactory and initialize it with the KeyStore
	        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
	        trustManagerFactory.init(keyStore);

	        // Create an SSLContext and initialize it with the TrustManagerFactory
	        SSLContext sslContext = SSLContext.getInstance("TLS");
	        sslContext.init(null, trustManagerFactory.getTrustManagers(), null);

	        // Create an HttpsURLConnection for the wallet request and set the SSLContext for this connection only
	        URL url = new URL("/sendspl?houseWallet=" + houseWallet + "&receiver=" + receiver + "&amount=" + amount + "&houseWalletID=" + houseWalletID+ "&secret=");  
	        HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
	        con.setSSLSocketFactory(sslContext.getSocketFactory());
	        con.setRequestMethod("GET");
	        

	        // Send the request and get the response code
	        int responseCode = con.getResponseCode();

	        // If the response code is HTTP_OK (200), read the response and extract the transaction signature
	        if (responseCode == HttpsURLConnection.HTTP_OK) {
	            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
	            String inputLine;
	            StringBuffer response = new StringBuffer();

	            while ((inputLine = in.readLine()) != null) {
	                response.append(inputLine);
	            }
	            in.close();

	            // Parse the response as JSON and check if it contains a transaction signature
	            JSONParser parser = new JSONParser();
	            Object obj = null;
	            try {
	                obj = parser.parse(response.toString());
	            } catch (ParseException e1) {
	                return "ERROR";
	            }
	            JSONObject jsonObject = (JSONObject) obj;
	            System.out.println(jsonObject);
	            if(!response.toString().contains("signature")) {
	                return "ERROR";
	            }
	            else {
	                String sign = (String) jsonObject.get("signature");
	                System.out.println("Transaction sent: " + sign);
	                return sign;
	            }
	        } 
	        // If the response code is not HTTP_OK, return an error message
	        else {
	            System.out.println("Error while sending transaction for user ID " + houseWallet);
	            return "ERROR";
	        }
	    } catch (Exception e) {
	        System.out.println("Exception while sending transaction: " + e.getMessage());
	        return "ERROR";
	    }
	}

	
	public static String getUserWallet(String secretPass, String userUUID) {
	    try {
	    	
	        // Load the certificate from the cert.pem file
	        CertificateFactory cf = CertificateFactory.getInstance("X.509");
	        Certificate cert;
	        try (InputStream certFile = new FileInputStream("/etc/letsencrypt/live/certificate.crt")) {
	            cert = cf.generateCertificate(certFile);
	        }

	        // Create a KeyStore and add the certificate to it
	        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
	        keyStore.load(null, null);
	        keyStore.setCertificateEntry("cert", cert);

	        // Create a TrustManagerFactory and initialize it with the KeyStore
	        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
	        trustManagerFactory.init(keyStore);

	        // Create an SSLContext and initialize it with the TrustManagerFactory
	        SSLContext sslContext = SSLContext.getInstance("TLS");
	        sslContext.init(null, trustManagerFactory.getTrustManagers(), null);

	        // Create an HttpsURLConnection for the wallet request and set the SSLContext for this connection only
	        URL url = new URL("/getWallet?secret=" + secretPass + "&uuid=" + userUUID);
	        
	        HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
	        con.setSSLSocketFactory(sslContext.getSocketFactory());
	        con.setRequestMethod("GET");
	        
	        int responseCode = con.getResponseCode();

	        if (responseCode == HttpsURLConnection.HTTP_OK) {
	            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
	            String inputLine;
	            StringBuffer response = new StringBuffer();

	            while ((inputLine = in.readLine()) != null) {
	                response.append(inputLine);
	            }
	            in.close();

	            JSONParser parser = new JSONParser();
	            Object obj = null;
				try {
					obj = parser.parse(response.toString());
					
				} catch (ParseException e1) {
					return null;
				}

				JSONObject jsonObject = (JSONObject) obj;
	            
	            JSONArray results = (JSONArray) jsonObject.get("results");
	    		for (Object objj : results) {
	    			JSONObject jsonTx = (JSONObject) objj;
	    			
	    			String wallet = (String) jsonTx.get("WALLET");
	    			return wallet;
	          }
	        } else {
	            System.out.println("Error while checking wallet for user ID " + userUUID);
	            return null;
	        }
	    } catch (Exception e) {
	        System.out.println("Exception while checking/creating wallet: " + e.getMessage());
	        return null;
	    }
	    System.out.println("Exception while checking/creating wallet: ");
		return null;
	}
}
