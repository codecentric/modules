package com.reedelk.esb.services.module;

import com.reedelk.esb.module.ModulesManager;
import com.reedelk.runtime.api.exception.ESBException;
import com.reedelk.runtime.system.api.Module;
import com.reedelk.runtime.system.api.ModuleService;
import com.reedelk.runtime.system.api.Modules;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.Set;

import static com.reedelk.esb.commons.FunctionWrapper.uncheckedConsumer;
import static com.reedelk.esb.commons.Preconditions.checkIsPresentAndGetOrThrow;
import static com.reedelk.esb.commons.Preconditions.checkState;
import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toSet;

public class ESBModuleService implements ModuleService {

    private static final Logger logger = LoggerFactory.getLogger(ESBModuleService.class);

    private final ModulesMapper mapper = new ModulesMapper();

    private final EventListener listener;
    private final BundleContext context;
    private final ModulesManager modulesManager;

    public ESBModuleService(BundleContext context, ModulesManager modulesManager, EventListener listener) {
        this.modulesManager = modulesManager;
        this.listener = listener;
        this.context = context;
    }

    @Override
    public long update(String modulePath) {
        Optional<Bundle> optionalBundle = getModuleAtPath(modulePath);
        Bundle bundleAtPath = checkIsPresentAndGetOrThrow(optionalBundle, "Update failed: could not find registered bundle in target file path=%s", modulePath);

        listener.moduleStopping(bundleAtPath.getBundleId());
        executeOperation(bundleAtPath, Bundle::stop, Bundle::update, Bundle::start);

        logger.debug("Module [{}] updated", bundleAtPath.getSymbolicName());

        return bundleAtPath.getBundleId();
    }

    @Override
    public long uninstall(String modulePath) {
        Optional<Bundle> optionalBundle = getModuleAtPath(modulePath);
        Bundle bundleAtPath = checkIsPresentAndGetOrThrow(optionalBundle, "Uninstall failed: could not find registered bundle in target file path=%s", modulePath);

        listener.moduleStopping(bundleAtPath.getBundleId());
        executeOperation(bundleAtPath, Bundle::stop, Bundle::uninstall);

        logger.debug("Module [{}] uninstalled", bundleAtPath.getSymbolicName());

        return bundleAtPath.getBundleId();
    }

    @Override
    public long install(String modulePath) {
        Optional<Bundle> optionalBundle = getModuleAtPath(modulePath);
        checkState(!optionalBundle.isPresent(), format("Install failed: the bundle in target file path=%s is already installed. Did you mean update?", modulePath));
        try {

            Bundle installedBundle = context.installBundle(modulePath);
            installedBundle.start();

            logger.debug("Module [{}] installed", installedBundle.getSymbolicName());

            return installedBundle.getBundleId();
        } catch (BundleException e) {
            throw new ESBException(e);
        }
    }

    @Override
    public long installOrUpdate(String modulePath) {
        Optional<Bundle> optionalBundle = getModuleAtPath(modulePath);
        if (optionalBundle.isPresent()) {
            return update(modulePath);
        } else {
            return install(modulePath);
        }
    }

    @Override
    public Modules modules() {
        Set<Module> mappedModules = modulesManager.allModules()
                .stream()
                .map(mapper::map)
                .collect(toSet());
        Modules modules = new Modules();
        modules.setModules(mappedModules);
        return modules;
    }

    private Optional<Bundle> getModuleAtPath(String bundlePath) {
        return Optional.ofNullable(context.getBundle(bundlePath));
    }

    private interface Operation {
        void execute(Bundle bundle) throws BundleException;
    }

    private void executeOperation(Bundle bundle, Operation... operations) {
        stream(operations)
                .forEachOrdered(
                        uncheckedConsumer(operation -> operation.execute(bundle)));

    }
}
