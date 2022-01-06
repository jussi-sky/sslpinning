package com.jussi.sslpinning;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

public class MainActivity extends AppCompatActivity {

    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = findViewById(R.id.button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable(){
                    @Override
                    public void run() {

                        try {
                            // 获取证书输入流
                            InputStream caInput = new BufferedInputStream(getAssets().open("uwca.crt"));
                            Certificate ca = CertificateFactory.getInstance("X.509").generateCertificate(caInput);
                            // 创建 Keystore 包含我们的证书
                            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                            keyStore.load(null, null);
                            keyStore.setCertificateEntry("ca", ca);
                            // 创建一个 TrustManager 仅把 Keystore 中的证书 作为信任的锚点
                            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm()); // 建议不要使用自己实现的X509TrustManager，而是使用默认的X509TrustManager
                            trustManagerFactory.init(keyStore);
                            // 用 TrustManager 初始化一个 SSLContext
                            SSLContext sslContext = SSLContext.getInstance("TLS");

                            // 使用默认的 TrustManager
//                            sslContext.init(null, trustManagerFactory.getTrustManagers(), new SecureRandom());

                            // 使用自定义的 TrustManager, 但不建议使用自定义的
                            sslContext.init(null, new TrustManager[]{
                                    new MyX509TrustManager2(getAssets().open("uwca.crt"))
                            }, null);

                            URL url = new URL("https://certs.cac.washington.edu/CAtest/");
                            HttpsURLConnection urlConnection = (HttpsURLConnection)url.openConnection();
                            urlConnection.setSSLSocketFactory(sslContext.getSocketFactory());
                            InputStream in = urlConnection.getInputStream();
                            copyInputStreamToOutputStream(in, System.out);

                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        } catch (NoSuchAlgorithmException e) {
                            e.printStackTrace();
                        } catch (KeyManagementException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });



    }

    private void copyInputStreamToOutputStream(InputStream in, PrintStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int c = 0;
        while ((c = in.read(buffer)) != -1) {
            out.write(buffer, 0, c);
        }
    }
}