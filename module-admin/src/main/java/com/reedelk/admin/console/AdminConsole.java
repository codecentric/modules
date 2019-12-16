package com.reedelk.admin.console;

import com.reedelk.runtime.api.service.ConfigurationService;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.osgi.service.component.annotations.ServiceScope.SINGLETON;

@Component(service = AdminConsole.class, scope = SINGLETON, immediate = true)
public class AdminConsole {

    private static final Logger logger = LoggerFactory.getLogger(AdminConsole.class);

    private static final String PROPERTY_ADMIN_CONSOLE_ADDRESS = "admin.console.address";
    private static final String PROPERTY_ADMIN_CONSOLE_PORT = "admin.console.port";

    @Reference
    private ConfigurationService configurationService;

    @Activate
    public void start(BundleContext context) {

        ModuleIdProvider.id = context.getBundle().getBundleId();

        String bindAddress = configurationService.getString(PROPERTY_ADMIN_CONSOLE_ADDRESS);

        int bindPort = configurationService.getInt(PROPERTY_ADMIN_CONSOLE_PORT);

        logger.info(String.format("Admin console listening on http://%s:%d/console", bindAddress, bindPort));
    }
}
