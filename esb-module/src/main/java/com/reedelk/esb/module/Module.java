package com.reedelk.esb.module;

import com.reedelk.esb.commons.ModuleStateLogger;
import com.reedelk.esb.flow.Flow;
import com.reedelk.esb.module.state.Error;
import com.reedelk.esb.module.state.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static com.reedelk.esb.commons.Preconditions.checkArgument;
import static com.reedelk.esb.commons.Preconditions.checkState;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

public class Module implements State {

    private static final Map<Class, Collection<Class>> ALLOWED_TRANSITIONS;

    static {
        Map<Class, Collection<Class>> tmp = new HashMap<>();
        tmp.put(Error.class, singletonList(Unresolved.class)); // when a dependency component is uninstalled, the state goes from error to unresolved.
        tmp.put(Started.class, singletonList(Stopped.class));
        tmp.put(Installed.class, asList(Error.class, Unresolved.class));
        tmp.put(Resolved.class, asList(Unresolved.class, Stopped.class, Error.class));
        tmp.put(Unresolved.class, asList(Resolved.class, Unresolved.class));
        tmp.put(Stopped.class, asList(Started.class, Resolved.class, Unresolved.class, Error.class));
        ALLOWED_TRANSITIONS = tmp;
    }

    private ModuleDeserializer deserializer;
    private State state;

    private final long moduleId;
    private final String name;
    private final String version;
    private final String moduleFilePath;

    private Module(final long moduleId, final String name, final String version, final String moduleFilePath, final ModuleDeserializer deserializer) {
        this.name = name;
        this.version = version;
        this.moduleId = moduleId;
        this.deserializer = deserializer;
        this.moduleFilePath = moduleFilePath;
        setState(new Installed());
    }

    public static Builder builder() {
        return new Builder();
    }

    public long id() {
        return moduleId;
    }

    public String name() {
        return name;
    }

    public String version() {
        return version;
    }

    public String moduleFilePath() {
        return moduleFilePath;
    }

    public void unresolve(Collection<String> unresolvedComponents, Collection<String> resolvedComponents) {
        isAllowedTransition(Unresolved.class);
        setState(new Unresolved(resolvedComponents, unresolvedComponents));
    }

    public void resolve(Collection<String> resolvedComponents) {
        isAllowedTransition(Resolved.class);
        setState(new Resolved(resolvedComponents));
    }

    public void start(Collection<Flow> flows) {
        isAllowedTransition(Started.class);
        Collection<String> resolvedComponents = state.resolvedComponents();
        setState(new Started(flows, resolvedComponents));
    }

    public void stop(Collection<Flow> flows) {
        isAllowedTransition(Stopped.class);
        Collection<String> resolvedComponents = state.resolvedComponents();
        setState(new Stopped(flows, resolvedComponents));
    }

    /**
     * The transition to error state, depends on the previous state. We MUST
     * carry over information about resolvedComponents (when previous state
     * is either Resolved or Stopped). This is required because a module MIGHT
     * transition to Unresolved state from error when a component is unregistered.
     *
     * @param exceptions the exception/s which caused this module to transition to error state.
     * @see com.reedelk.esb.lifecycle.ResolveModuleDependencies#run
     */
    public void error(Collection<Exception> exceptions) {
        isAllowedTransition(Error.class);
        Error newState = state instanceof Installed ?
                new Error(exceptions, emptyList()) :
                new Error(exceptions, state.resolvedComponents());
        setState(newState);
    }

    public void error(Exception exception) {
        error(singletonList(exception));
    }

    @Override
    public Collection<Flow> flows() {
        return state.flows();
    }

    @Override
    public Collection<Exception> errors() {
        return state.errors();
    }

    @Override
    public Collection<String> resolvedComponents() {
        return state.resolvedComponents();
    }

    @Override
    public Collection<String> unresolvedComponents() {
        return state.unresolvedComponents();
    }

    @Override
    public ModuleState state() {
        return state.state();
    }

    @SuppressWarnings("unchecked")
    private void isAllowedTransition(Class transitionTo) {
        Collection<Class> allowedNextStates = ALLOWED_TRANSITIONS.get(state.getClass());
        boolean allowed = allowedNextStates.stream().anyMatch(transitionTo::isAssignableFrom);

        checkState(allowed, format("Module cannot transition from state=%s to state=%s",
                state.getClass().getSimpleName(), transitionTo.getSimpleName()));
    }

    private void setState(State newState) {
        state = newState;
        ModuleStateLogger.log(this);
    }

    public DeserializedModule deserialize() {
        return deserializer.deserialize();
    }

    public static class Builder {

        private long moduleId;

        private String name;
        private String version;
        private String moduleFilePath;
        private ModuleDeserializer deserializer;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder version(String version) {
            this.version = version;
            return this;
        }

        public Builder moduleFilePath(String moduleFilePath) {
            this.moduleFilePath = moduleFilePath;
            return this;
        }

        public Builder moduleId(long moduleId) {
            this.moduleId = moduleId;
            return this;
        }

        public Builder deserializer(ModuleDeserializer deserializer) {
            this.deserializer = deserializer;
            return this;
        }

        public Module build() {
            checkArgument(name != null, "name");
            checkArgument(version != null, "version");
            checkArgument(moduleFilePath != null, "module file path");
            checkArgument(deserializer != null, "module deserializer");
            return new Module(moduleId, name, version, moduleFilePath, deserializer);
        }
    }
}
