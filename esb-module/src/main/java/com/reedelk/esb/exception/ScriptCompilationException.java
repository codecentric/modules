package com.reedelk.esb.exception;

import com.reedelk.runtime.api.exception.ESBException;
import com.reedelk.runtime.api.script.ScriptBlock;
import com.reedelk.runtime.api.script.ScriptSource;

import java.util.Collection;

import static com.reedelk.esb.commons.Messages.Script.SCRIPT_BLOCK_COMPILATION_ERROR;
import static com.reedelk.esb.commons.Messages.Script.SCRIPT_SOURCE_COMPILATION_ERROR;
import static com.reedelk.runtime.api.commons.StackTraceUtils.rootCauseMessageOf;

public class ScriptCompilationException extends ESBException {

    public ScriptCompilationException(ScriptSource scriptSource, Throwable cause) {
        super(messageFrom(scriptSource, cause), cause);
    }

    public ScriptCompilationException(ScriptBlock scriptBlock, Throwable cause) {
        super(messageFrom(scriptBlock, cause), cause);
    }

    private static String messageFrom(ScriptSource source, Throwable cause) {
        Collection<String> moduleNames = source.scriptModuleNames();
        String scriptResource = source.resource();
        String error = rootCauseMessageOf(cause);
        return SCRIPT_SOURCE_COMPILATION_ERROR.format(error, scriptResource, moduleNames);
    }

    private static String messageFrom(ScriptBlock scriptBlock, Throwable cause) {
        String body = scriptBlock.body();
        String error = rootCauseMessageOf(cause);
        return SCRIPT_BLOCK_COMPILATION_ERROR.format(error, body);
    }
}
