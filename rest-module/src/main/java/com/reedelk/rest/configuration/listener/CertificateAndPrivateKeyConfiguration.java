package com.reedelk.rest.configuration.listener;

import com.reedelk.runtime.api.annotation.Property;
import com.reedelk.runtime.api.component.Implementor;
import org.osgi.service.component.annotations.Component;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@Component(service = CertificateAndPrivateKeyConfiguration.class, scope = PROTOTYPE)
public class CertificateAndPrivateKeyConfiguration implements Implementor {

    @Property("Certificate")
    private String certificateFile;
    @Property("Private key")
    private String privateKeyFile;

    public String getCertificateFile() {
        return certificateFile;
    }

    public void setCertificateFile(String certificateFile) {
        this.certificateFile = certificateFile;
    }

    public String getPrivateKeyFile() {
        return privateKeyFile;
    }

    public void setPrivateKeyFile(String privateKeyFile) {
        this.privateKeyFile = privateKeyFile;
    }
}
