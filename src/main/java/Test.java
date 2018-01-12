import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultSaslConfig;
import com.rabbitmq.client.impl.SocketFrameHandler;
import sun.security.ssl.SSLSocketImpl;

import javax.net.ssl.*;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class Test {

    private static Object getField(final Object target, final String name) {
        try {
            final Field field = target.getClass().getDeclaredField(name);
            field.setAccessible(true);
            return field.get(target);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get field '" + name + "' on " + target);
        }
    }

    public static class TrustEverythingTrustManager implements X509TrustManager {
        @Override
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }

        @Override
        public void checkClientTrusted(final java.security.cert.X509Certificate[] certs, final String authType) {
        }

        @Override
        public void checkServerTrusted(final java.security.cert.X509Certificate[] certs, final String authType) {
        }
    }

    static SSLContext createSslContext()
            throws UnrecoverableKeyException, NoSuchAlgorithmException,
            KeyStoreException, IOException, CertificateException, KeyManagementException, NoSuchProviderException {

        final KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
        keystore.load(null, null);

        final KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(
                KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keystore, "".toCharArray());

        final SSLContext sslContext = SSLContext.getInstance("TLS");

        final String defaultAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
        final TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(defaultAlgorithm);
        trustManagerFactory.init(keystore);

        sslContext.init(keyManagerFactory.getKeyManagers(),
                new TrustManager[]{new TrustEverythingTrustManager()},
                null);

        return sslContext;
    }

    public static void main(String[] args) {

        try {
            final ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(args[0]);
            factory.useSslProtocol(createSslContext());
            factory.setSaslConfig(DefaultSaslConfig.PLAIN);
            factory.setRequestedHeartbeat(10);
            factory.setAutomaticRecoveryEnabled(false);

            final Connection connection = factory.newConnection();

            System.out.println("connection = " + connection);

            // Get actual TCP socket raw output stream
            final SocketFrameHandler _frameHandler = (SocketFrameHandler) getField(connection, "_frameHandler");
            final SSLSocketImpl _socket = (SSLSocketImpl) getField(_frameHandler, "_socket");
            final OutputStream sockOutput = (OutputStream) getField(_socket, "sockOutput");

            System.out.println("Sleeping for 5 sec");
            Thread.sleep(5000);

            sockOutput.flush();
            // record type = 0x17 - APPLICATION_DATA
            // version = 0x303 - TLS 1.2
            // length = 0x0002
            // then two zero bytes of "payload"
            sockOutput.write(new byte[] {0x17, 0x03, 0x03, 0x00, 0x02, 0x00, 0x00});
            sockOutput.flush();

            System.out.println("Rubbish sent");

            System.out.println("Finished");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

