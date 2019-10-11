package com.reedelk.rest.server.configurer;

import com.reedelk.rest.commons.HttpProtocol;
import com.reedelk.rest.configuration.listener.*;
import com.reedelk.runtime.api.exception.ESBException;
import io.netty.handler.ssl.SslContextBuilder;
import reactor.netty.tcp.TcpServer;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;

import static java.util.Objects.requireNonNull;

public class ServerSecurityConfigurer {

    public static TcpServer configureSecurity(TcpServer bootstrap, ListenerConfiguration configuration) {
        // Security is configured if and only if the protocol is HTTPS
        if (!HttpProtocol.HTTPS.equals(configuration.getProtocol())) return bootstrap;
        if (configuration.getSecurityConfiguration() == null) return bootstrap;

        SecurityConfiguration securityConfig = configuration.getSecurityConfiguration();

        return bootstrap.secure(sslContextSpec -> {

            SslContextBuilder contextBuilder;

            ServerSecurityType configurationType = securityConfig.getType();
            if (ServerSecurityType.KEY_STORE.equals(configurationType)) {
                KeyStoreConfiguration keyStoreConfig = requireNonNull(securityConfig.getKeyStore(), "key store config");
                contextBuilder = SslContextBuilder.forServer(getKeyManagerFactory(keyStoreConfig));

            } else if (ServerSecurityType.CERTIFICATE_AND_PRIVATE_KEY.equals(configurationType)) {
                // TODO: Error handling
                CertificateAndPrivateKeyConfiguration config =
                        requireNonNull(securityConfig.getCertificateAndPrivateKey(), "certificate and private key configuration");
                File certificateFile = new File(config.getCertificateFile());
                File privateKeyFile = new File(config.getPrivateKeyFile());
                contextBuilder = SslContextBuilder.forServer(certificateFile, privateKeyFile);

            } else {
                throw new ESBException("Wrong config");
            }

            if (securityConfig.getUseTrustStore() != null && securityConfig.getUseTrustStore()) {
                TrustStoreConfiguration trustStoreConfiguration =
                        requireNonNull(securityConfig.getTrustStoreConfiguration(), "trust store config");
                contextBuilder.trustManager(getTrustManagerFactory(trustStoreConfiguration));
            }

            try {
                sslContextSpec.sslContext(contextBuilder.build());
            } catch (SSLException e) {
                throw new ESBException(e);
            }
        });
    }


    private static TrustManagerFactory getTrustManagerFactory(TrustStoreConfiguration config) {
        String type = config.getType();
        String location = config.getPath();
        String password = config.getPassword();
        String algorithm = config.getAlgorithm();
        try {
            String alg = algorithm == null ? TrustManagerFactory.getDefaultAlgorithm() : algorithm;
            TrustManagerFactory factory = TrustManagerFactory.getInstance(alg);
            KeyStore keyStore = type == null ? KeyStore.getInstance(KeyStore.getDefaultType()) : KeyStore.getInstance(type);
            try (FileInputStream fileInputStream = new FileInputStream(location)) {
                keyStore.load(fileInputStream, password.toCharArray());
            }
            factory.init(keyStore);
            return factory;
        } catch (Exception e) {
            throw new ESBException(e);
        }
    }

    private static KeyManagerFactory getKeyManagerFactory(KeyStoreConfiguration config) {
        try {
            String type = config.getType();
            String location = config.getPath();
            String password = config.getPassword();
            String algorithm = config.getAlgorithm();
            String alg = algorithm == null ? KeyManagerFactory.getDefaultAlgorithm() : algorithm;
            KeyManagerFactory factory = KeyManagerFactory.getInstance(alg);
            KeyStore keyStore = type == null ? KeyStore.getInstance(KeyStore.getDefaultType()) : KeyStore.getInstance(type);
            try (FileInputStream fileInputStream = new FileInputStream(location)) {
                keyStore.load(fileInputStream, password.toCharArray());
            }
            factory.init(keyStore, password.toCharArray());
            return factory;
        } catch (Exception e) {
            throw new ESBException(e);
        }
    }
}
