package com.jussi.sslpinning;

import android.util.Log;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import javax.net.ssl.X509TrustManager;

/**
 * 证书绑定
 */
public class MyX509TrustManager2 implements X509TrustManager{

    final Certificate ca;

    MyX509TrustManager2(InputStream cert) throws Exception {

        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        try (InputStream caInput = new BufferedInputStream(cert)) {
            try {
                ca = cf.generateCertificate(caInput);
                Log.i("ssl", "ca = " + ((X509Certificate) ca).getSubjectDN());
                Log.i("ssl", "ca key = " + ((X509Certificate) ca).getPublicKey());
            } finally {
                caInput.close();
            }
        }

    }

    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {

    }

    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {

        // Make sure that it hasn't expired.
        chain[0].checkValidity();

        // Make sure that it has equals.
        if(!chain[0].equals(ca)){
            Log.e("ssl", "ssl check error!");
            throw new CertificateException();
        }else {
            Log.i("ssl","ssl check success!");
        }

//        for (X509Certificate cert : chain) {
//
//            // Make sure that it hasn't expired.
//            cert.checkValidity();
//
//            // Verify the certificate's public key chain.
//            Log.i("ssl", "cert key=" + cert.getPublicKey());
//
//            try {
//                cert.verify(((X509Certificate) ca).getPublicKey());
//            } catch (InvalidKeyException e) {
//                e.printStackTrace();
//            } catch (NoSuchAlgorithmException e) {
//                e.printStackTrace();
//            } catch (NoSuchProviderException e) {
//                e.printStackTrace();
//            } catch (SignatureException e) {
//                e.printStackTrace();
//            }
//
//        }

    }

    public X509Certificate[] getAcceptedIssuers() {

        return new X509Certificate[0];
    }

}