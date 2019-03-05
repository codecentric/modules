package com.esb.component;

import com.esb.api.exception.ESBException;
import com.esb.api.message.Message;
import com.esb.flow.ExecutionNode;
import com.esb.services.javascript.JavascriptEngine;
import com.esb.services.javascript.ScriptEngine;

import javax.script.ScriptException;
import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonList;

public class Choice implements FlowControlComponent {

    private PathExpressionPair defaultPath;
    private List<PathExpressionPair> pathExpressionPairs = new ArrayList<>();

    @Override
    public List<ExecutionNode> apply(Message input) {
        for (PathExpressionPair pathExpressionPair : pathExpressionPairs) {
            if (pathExpressionPair.evaluate(input, Boolean.class)) {
                return singletonList(pathExpressionPair.pathReference);
            }
        }
        return singletonList(defaultPath.pathReference);
    }

    public void addPathExpressionPair(String expression, ExecutionNode pathExecutionNode) {
        pathExpressionPairs.add(new PathExpressionPair(expression, pathExecutionNode));
    }

    public void addDefaultPath(ExecutionNode pathExecutionNode) {
        defaultPath = new PathExpressionPair(Boolean.TRUE.toString(), pathExecutionNode);
    }

    // The engine should be part of the context.
    class PathExpressionPair {
        final String expression;
        final ExecutionNode pathReference;

        PathExpressionPair(String expression, ExecutionNode pathReference) {
            this.expression = expression;
            this.pathReference = pathReference;
        }

        <T> T evaluate(Message message, Class<T> resultClazz) {
            ScriptEngine engine = new JavascriptEngine();
            try {
                return engine.evaluate(message, this.expression, resultClazz);
            } catch (ScriptException e) {
                throw new ESBException(e);
            }
        }
    }

}
