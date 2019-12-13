package com.reedelk.esb;

import com.reedelk.esb.commons.Log;
import com.reedelk.esb.component.ComponentRegistry;
import com.reedelk.esb.component.RuntimeComponents;
import com.reedelk.esb.configuration.ApplyRuntimeConfiguration;
import com.reedelk.esb.lifecycle.*;
import com.reedelk.esb.module.ModulesManager;
import com.reedelk.esb.pubsub.Event;
import com.reedelk.esb.services.ServicesManager;
import com.reedelk.esb.services.hotswap.HotSwapListener;
import com.reedelk.esb.services.module.EventListener;
import com.reedelk.esb.services.module.EventService;
import com.reedelk.runtime.system.api.SystemProperty;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.osgi.service.component.annotations.ServiceScope.SINGLETON;

@Component(service = ESB.class, scope = SINGLETON, immediate = true)
public class ESB implements EventListener, HotSwapListener {

    private static final Logger logger = LoggerFactory.getLogger(ESB.class);

    @Reference
    private SystemProperty systemProperty;
    @Reference
    private ConfigurationAdmin configurationAdmin;

    protected BundleContext context;
    protected ModulesManager modulesManager;

    private EventService eventDispatcher;
    private ServicesManager servicesManager;
    private ComponentRegistry componentRegistry;

    @Activate
    public void start(BundleContext context) {
        this.context = context;

        modulesManager = new ModulesManager();

        componentRegistry = new ComponentRegistry(RuntimeComponents.allNames());
        eventDispatcher = new EventService(this);

        context.addBundleListener(eventDispatcher);
        context.addServiceListener(eventDispatcher);

        servicesManager = new ServicesManager(ESB.this, ESB.this,
                modulesManager, systemProperty, configurationAdmin);
        servicesManager.registerServices(context);

        ApplyRuntimeConfiguration.from(servicesManager.configurationService());
    }

    @Deactivate
    public void stop(BundleContext context) {
        context.removeBundleListener(eventDispatcher);
        context.removeServiceListener(eventDispatcher);
        servicesManager.unregisterServices();
    }

    @Override
    public synchronized void moduleInstalled(long moduleId) {
        StepRunner.get(context, modulesManager, componentRegistry)
                .next(new ModuleCreate())
                .next(new ModuleAdd())
                .execute(moduleId);
    }

    /**
     * Note that a module might be started already when this callback is called.
     * It is started when we update an already installed module.
     * Install:
     * - moduleInstalled: state=INSTALLED
     * - moduleStarted: state=STARTED
     *
     * Update (e.g. because component source code was changed - no hotswap -):
     * - moduleStopping: state=RESOLVED
     * - componentUnregistering: state=UNRESOLVED
     * - moduleStopped: state=UNRESOLVED (no-op)
     * - componentRegistered: state=STARTED (auto-start when all components are resolved)
     * - moduleStarted: state=STARTED (no-op)
     *
     * This is why we have this isModuleStarted check: the module might have been already started.
     */
    @Override
    public synchronized void moduleStarted(long moduleId) {
        if (!modulesManager.isModuleStarted(moduleId)) {
            StepRunner.get(context, modulesManager, componentRegistry, servicesManager.configurationService())
                    .next(new ModuleCheckNotNull())
                    .next(new ModuleValidate())
                    .next(new ModuleResolveDependencies())
                    .next(new ModuleBuild())
                    .next(new ModuleStart())
                    .execute(moduleId);
        }
    }

    @Override
    public synchronized void moduleStopping(long moduleId) {
        StepRunner.get(context, modulesManager)
                .next(new ModuleCheckNotNull())
                .next(new ModuleStopAndReleaseReferences())
                .next(new ModuleTransitionToInstalled())
                .execute(moduleId);
    }

    /**
     * When the OSGi container process is stopped, 'moduleStopping' is not called therefore the module is
     * still registered in the ModuleManager. 'moduleStopped' is called when the OSGi container shuts down
     * (skipping the call to moduleStopping). Note that when a module is stopped there is no more context
     * (Bundle Context) associated with it.
     */
    @Override
    public synchronized void moduleStopped(long moduleId) {
        if (modulesManager.isModuleStarted(moduleId)) {
            StepRunner.get(context, modulesManager)
                    .next(new ModuleCheckNotNull())
                    .next(new ModuleStopAndReleaseReferences())
                    .next(new ModuleTransitionToInstalled())
                    .execute(moduleId);
        }
        // Important: module uninstalled event *must* be fired even if a module
        // has not been started. This is because there might be modules providing
        // only components and/or Script Functions without any flow and subscribers
        // of this event must be notified to property unregister script functions.
        Event.fireModuleUninstalled(moduleId);
    }

    @Override
    public synchronized void moduleUninstalled(long moduleId) {
        if (modulesManager.isModuleRegistered(moduleId)) {
            StepRunner.get(context, modulesManager)
                    .next(new ModuleRemove())
                    .execute(moduleId);
        }
        // Important: module uninstalled event *must* be fired even if a module
        // has not been started. This is because there might be modules providing
        // only components and/or Script Functions without any flow and subscribers
        // of this event must be notified to property unregister script functions.
        Event.fireModuleUninstalled(moduleId);
    }

    @Override
    public synchronized void componentRegistered(String componentName) {
        componentRegistry.registerComponent(componentName);
        Log.componentRegistered(logger, componentName);

        modulesManager.findUnresolvedModules().forEach(unresolvedModule ->
                StepRunner.get(context, modulesManager, servicesManager.configurationService())
                        .next(new ModuleCheckNotNull())
                        .next(new ModuleUpdateRegisteredComponent(componentName))
                        .next(new ModuleBuild())
                        .next(new ModuleStart())
                        .execute(unresolvedModule.id()));
    }

    @Override
    public synchronized void componentUnregistering(String componentName) {
        componentRegistry.unregisterComponent(componentName);
        Log.componentUnRegistered(logger, componentName);

        modulesManager.findModulesUsingComponent(componentName).forEach(moduleUsingComponent ->
                StepRunner.get(context, modulesManager)
                        .next(new ModuleCheckNotNull())
                        .next(new ModuleStopAndReleaseReferences())
                        .next(new ModuleUpdateUnregisteredComponent(componentName))
                        .execute(moduleUsingComponent.id()));
    }

    @Override
    public synchronized void hotSwap(long moduleId, String resourcesRootDirectory) {
        StepRunner.get(context, modulesManager, componentRegistry, servicesManager.configurationService())
                .next(new ModuleCheckNotNull())
                .next(new ModuleStopAndReleaseReferences())
                .next(new ModuleRemove())
                .next(new ModuleHotSwap(resourcesRootDirectory))
                .next(new ModuleAdd())
                .next(new ModuleValidate())
                .next(new ModuleResolveDependencies())
                .next(new ModuleBuild())
                .next(new ModuleStart())
                .execute(moduleId);
    }
}
