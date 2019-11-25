package com.reedelk.esb.flow;

import com.reedelk.esb.commons.Messages;
import com.reedelk.esb.execution.FlowExecutorEngine;
import com.reedelk.esb.graph.ExecutionGraph;
import com.reedelk.esb.graph.ExecutionNode;
import com.reedelk.runtime.api.component.Inbound;
import com.reedelk.runtime.api.component.InboundEventListener;
import com.reedelk.runtime.api.component.OnResult;
import com.reedelk.runtime.api.exception.FlowExecutionException;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import org.osgi.framework.Bundle;

import java.util.Optional;

import static com.reedelk.esb.commons.Preconditions.checkArgument;
import static com.reedelk.esb.commons.Preconditions.checkState;

public class Flow implements InboundEventListener {

    private final long moduleId;
    private final String moduleName;
    private final String flowId;
    private final String flowTitle;

    private final ExecutionGraph executionGraph;
    private final FlowExecutorEngine executionEngine;

    private boolean started = false;

    public Flow(final long moduleId,
                final String moduleName,
                final String flowId,
                final String flowTitle,
                final ExecutionGraph executionGraph,
                final FlowExecutorEngine executionEngine) {
        this.moduleId = moduleId;
        this.moduleName = moduleName;
        this.flowId = flowId;
        this.flowTitle = flowTitle;
        this.executionGraph = executionGraph;
        this.executionEngine = executionEngine;
    }

    public String getFlowId() {
        return flowId;
    }

    public Optional<String> getFlowTitle() {
        return Optional.ofNullable(flowTitle);
    }

    public boolean isUsingComponent(String targetComponentName) {
        checkArgument(targetComponentName != null, "Component Name");

        Optional<ExecutionNode> found = executionGraph
                .findOne(executionNode -> executionNode.isUsingComponent(targetComponentName));
        return found.isPresent();
    }

    public void releaseReferences(Bundle bundle) {
        checkState(!isStarted(), "Flow references can be released only when the flow is stopped!");
        executionGraph.applyOnNodes(ReleaseReferenceConsumer.get(bundle));
    }

    public boolean isStarted() {
        synchronized (this) {
            return started;
        }
    }

    public void start() {
        synchronized (this) {
            Inbound inbound = getInbound();
            inbound.addEventListener(this);
            inbound.onStart();
            started = true;
        }
    }

    public void stopIfStarted() {
        synchronized (this) {
            if (started) {
                forceStop();
            }
        }
    }

    public void forceStop() {
        synchronized (this) {
            try {
                Inbound inbound = getInbound();
                inbound.removeEventListener();
                inbound.onShutdown();
            } finally {
                started = false;
            }
        }
    }

    @Override
    public void onEvent(Message message, OnResult onResult) {
        executionEngine.onEvent(message, new OnResultFlowExceptionWrapper(onResult));
    }

    private Inbound getInbound() {
        return (Inbound) executionGraph.getRoot().getComponent();
    }

    class OnResultFlowExceptionWrapper implements OnResult {

        private final OnResult delegate;

        OnResultFlowExceptionWrapper(OnResult delegate) {
            this.delegate = delegate;
        }

        @Override
        public void onResult(Message message, FlowContext flowContext) {
            delegate.onResult(message, flowContext);
        }

        @Override
        public void onError(Throwable throwable, FlowContext flowContext) {
            String error = Messages.Flow.EXECUTION_ERROR.format(moduleId, moduleName, flowId, flowTitle,
                    throwable.getClass().getName(), throwable.getMessage());
            FlowExecutionException wrapped = new FlowExecutionException(moduleId, moduleName, flowId, flowTitle, error, throwable);
            delegate.onError(wrapped,flowContext);
        }
    }
}
