import java.io.FileInputStream;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

public class SSLContextFactory {
	
	/*
	 * 使用openssl生成证书
	 * 
	 * 1. 生成根证书私钥
	 * openssl genrsa -out root.key 1024
	 * 2. 生成根证书
	 * openssl  req -new -x509 -key root.key -out root.crt
	 * 3. 生成网站证书的私钥
	 * openssl genrsa -out mywebsite.key 1024
	 * 4. 生成证书请求
	 * openssl req -new -key mywebsite.key -out mywebsite.csr
	 * 5. 使用根证书签名生成网站证书
	 * openssl ca -in mywebsite.csr -out mywebsite.crt -cert root.crt -keyfile root.key
	 * 6. 将私钥和证书合成PKCS12格式
	 * openssl pkcs12 -export -out mywebsite.pfx -inkey mywebsite.key -in mywebsite.crt
	 * 7. 将PKCS12转化成JKS
	 * keytool -importkeystore -srckeystore mywebsite.pfx -destkeystore mywebsite.jks -srcstoretype PKCS12 -deststoretype JKS
	 * 
	 * 直接使用keytool生成证书
	 * keytool -genkey -alias mykey -keyalg RSA -keypass mypass -keystore mystore -storepass mystorepass
	 * 
	 */
	
	static String keyStorePath = "openssl\\mywebsite.jks";
	static String storePwd = "storepass";
	static String certPwd = "storepass";
			
	public static SSLContext getSslContext() throws Exception {
        SSLContext sslContext = SSLContext.getInstance("TLSv1");
        KeyStore ks = KeyStore.getInstance("JKS");
        FileInputStream inputStream = new FileInputStream(keyStorePath);
        ks.load(inputStream, storePwd.toCharArray());
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ks, certPwd.toCharArray());
        sslContext.init(kmf.getKeyManagers(), null, null);
        inputStream.close();
        return sslContext;
    }
}
