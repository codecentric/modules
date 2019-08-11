package com.reedelk.esb.lifecycle;

import com.reedelk.esb.module.Module;
import org.osgi.framework.Bundle;

import static com.reedelk.esb.commons.Preconditions.checkState;

public class CheckModuleNotNull extends AbstractStep<Module, Module> {

    @Override
    public Module run(Module module) {
        Bundle bundle = bundle();
        long moduleId = bundle.getBundleId();
        checkState(module != null,
                "Module with id=[%d], symbolic name=[%s] was not found in Module Manager",
                moduleId,
                bundle.getSymbolicName());
        return module;
    }
}
