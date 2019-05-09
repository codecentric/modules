package com.esb.component;

import com.esb.api.exception.ESBException;
import com.esb.api.message.Message;
import com.esb.flow.ExecutionNode;
import com.esb.services.scriptengine.ESBJavascriptEngine;
import com.esb.services.scriptengine.ScriptEngine;

import javax.script.ScriptException;
import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonList;

public class ChoiceWrapper extends Choice implements FlowControlComponent {

    private static final ScriptEngine ENGINE = new ESBJavascriptEngine();

    private List<PathExpressionPair> pathExpressionPairs = new ArrayList<>();

    @Override
    public List<ExecutionNode> apply(Message input) {
        for (PathExpressionPair pathExpressionPair : pathExpressionPairs) {
            if (pathExpressionPair.evaluate(input, Boolean.class)) {
                return singletonList(pathExpressionPair.pathReference);
            }
        }
        return singletonList(getDefaultPathOrThrow().pathReference);
    }

    public void addPathExpressionPair(String expression, ExecutionNode pathExecutionNode) {
        pathExpressionPairs.add(new PathExpressionPair(expression, pathExecutionNode));
    }

    private PathExpressionPair getDefaultPathOrThrow() {
        return pathExpressionPairs.stream()
                .filter(pathExpressionPair -> pathExpressionPair.expression.equals(DEFAULT_CONDITION))
                .findFirst()
                .orElseThrow(() -> new ESBException("Default choice condition could not be found"));
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
            try {
                return ENGINE.evaluate(message, this.expression, resultClazz);
            } catch (ScriptException e) {
                throw new ESBException(e);
            }
        }
    }

}
