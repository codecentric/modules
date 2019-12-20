package com.reedelk.esb.services;

import com.reedelk.esb.module.ModulesManager;
import com.reedelk.esb.services.configuration.DefaultConfigurationService;
import com.reedelk.esb.services.converter.DefaultConverterService;
import com.reedelk.esb.services.hotswap.DefaultHotSwapService;
import com.reedelk.esb.services.hotswap.HotSwapListener;
import com.reedelk.esb.services.module.DefaultModuleService;
import com.reedelk.esb.services.module.EventListener;
import com.reedelk.esb.services.resource.DefaultResourceService;
import com.reedelk.esb.services.scriptengine.ScriptEngine;
import com.reedelk.runtime.api.configuration.ConfigurationService;
import com.reedelk.runtime.api.converter.ConverterService;
import com.reedelk.runtime.api.resource.ResourceService;
import com.reedelk.runtime.api.script.ScriptEngineService;
import com.reedelk.runtime.system.api.HotSwapService;
import com.reedelk.runtime.system.api.ModuleService;
import com.reedelk.runtime.system.api.SystemProperty;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationAdmin;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;
import java.util.Properties;

public class ServicesManager {

    private static final Dictionary NO_PROPERTIES = new Properties();

    private final EventListener eventListener;
    private final SystemProperty systemProperty;
    private final ModulesManager modulesManager;
    private final HotSwapListener hotSwapListener;
    private final ConfigurationAdmin configurationAdmin;

    private List<ServiceRegistration<?>> registeredServices = new ArrayList<>();

    private DefaultConfigurationService configurationService;

    public ServicesManager(EventListener eventListener,
                           HotSwapListener hotSwapListener,
                           ModulesManager modulesManager,
                           SystemProperty systemProperty,
                           ConfigurationAdmin configurationAdmin) {
        this.eventListener = eventListener;
        this.systemProperty = systemProperty;
        this.modulesManager = modulesManager;
        this.hotSwapListener = hotSwapListener;
        this.configurationAdmin = configurationAdmin;
    }

    public void registerServices(BundleContext context) {
        registerModuleService(context);
        registerHotSwapService(context);
        registerConverterService(context);
        registerScriptEngineService(context);
        registerConfigurationService(context);
        registerModuleFileProviderService(context);
    }

    public void unregisterServices() {
        registeredServices.forEach(ServiceRegistration::unregister);
    }

    public DefaultConfigurationService configurationService() {
        return configurationService;
    }

    private void registerConfigurationService(BundleContext context) {
        configurationService = new DefaultConfigurationService(configurationAdmin, systemProperty);
        configurationService.initialize();
        ServiceRegistration<ConfigurationService> registration =
                registerService(context, ConfigurationService.class, configurationService);
        registeredServices.add(registration);
    }

    private void registerScriptEngineService(BundleContext context) {
        ScriptEngine scriptEngineService = ScriptEngine.getInstance();
        ServiceRegistration<ScriptEngineService> registration =
                registerService(context, ScriptEngineService.class, scriptEngineService);
        registeredServices.add(registration);
    }

    private void registerHotSwapService(BundleContext context) {
        DefaultHotSwapService service = new DefaultHotSwapService(context, hotSwapListener);
        ServiceRegistration<HotSwapService> registration =
                registerService(context, HotSwapService.class, service);
        registeredServices.add(registration);
    }

    private void registerModuleService(BundleContext context) {
        DefaultModuleService service = new DefaultModuleService(context, modulesManager, systemProperty, eventListener);
        ServiceRegistration<ModuleService> registration =
                registerService(context, ModuleService.class, service);
        registeredServices.add(registration);
    }

    private void registerModuleFileProviderService(BundleContext context) {
        ScriptEngine scriptEngineService = ScriptEngine.getInstance();
        ResourceService service = new DefaultResourceService(scriptEngineService);
        ServiceRegistration<ResourceService> registration =
                registerService(context, ResourceService.class, service);
        registeredServices.add(registration);
    }

    private void registerConverterService(BundleContext context) {
        ConverterService service = DefaultConverterService.getInstance();
        ServiceRegistration<ConverterService> registration =
                registerService(context, ConverterService.class, service);
        registeredServices.add(registration);
    }

    @SuppressWarnings("unchecked")
    private <T> ServiceRegistration<T> registerService(BundleContext context, Class<T> serviceClazz, T serviceImplementation) {
        return context.registerService(serviceClazz, serviceImplementation, NO_PROPERTIES);
    }
}