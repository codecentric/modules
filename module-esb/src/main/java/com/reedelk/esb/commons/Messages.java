package com.reedelk.esb.commons;

import com.reedelk.runtime.api.commons.FlowError;
import org.json.JSONObject;

import static com.reedelk.runtime.api.commons.FlowError.Properties.*;

public class Messages {

    private Messages() {
    }

    private static String formatMessage(String template, Object ...args) {
        return String.format(template, args);
    }

    interface FormattedMessage {
        String format(Object ...args);
    }

    // The error message is in JSON format so that clients can always
    // parse the error whenever error.getMessage() is used in a script.
    public enum FlowErrorMessage implements FormattedMessage {
        DEFAULT;

        @Override
        public String format(Object... args) {
            return new JSONObject(new FlowError(
                    (long) args[0],
                    (String) args[1],
                    (String) args[2],
                    (String) args[3],
                    (String) args[4],
                    (String) args[5],
                    (String) args[6]),
                    new String[] { moduleId, moduleName, flowId, flowTitle, correlationId, errorType, errorMessage })
                    .toString(2);
        }
    }

    public enum Flow implements FormattedMessage {

        FORCE_STOP("Error forcing stop flow with id=[%s]: %s"),
        FORCE_STOP_WITH_TITLE("Error forcing stop flow with id=[%s] and title '%s': %s"),
        START("Flow with id=[%s] started."),
        START_WITH_TITLE("Flow with id=[%s] and title '%s' started."),
        START_ERROR("Error starting flow with id=[%s]: %s"),
        START_ERROR_WITH_TITLE("Error starting flow with id=[%s] and title '%s': %s"),
        STOP_WITH_TITLE("Flow with id=[%s] and title '%s' stopped."),
        STOP("Flow with id=[%s] stopped."),
        STOP_ERROR("Error stopping flow with id=[%s]: %s"),
        STOP_ERROR_WITH_TITLE("Error stopping flow with id=[%s] and title '%s': %s"),
        BUILD_ERROR("Error building flow with id=[%s]: %s"),
        BUILD_ERROR_WITH_TITLE("Error building flow with id=[%s] and title '%s': %s"),
        VALIDATION_ID_NOT_UNIQUE("Error validating module with name=[%s]: There are at least two flows with the same ID. Flow IDs must be unique."),
        VALIDATION_ID_NOT_VALID("Error validating module with name=[%s]: The 'id' property must be defined and not empty in any JSON flow definition.");

        private String msg;

        Flow(String msg) {
            this.msg = msg;
        }

        @Override
        public String format(Object... args) {
            return formatMessage(msg, args);
        }
    }

    public enum Subflow implements FormattedMessage {

        VALIDATION_ID_NOT_UNIQUE("Error validating module with name=[%s]: There are at least two subflows with the same ID. Subflow IDs must be unique."),
        VALIDATION_ID_NOT_VALID("Error validating module with name=[%s]: The 'id' property must be defined and not empty in any JSON subflow definition.");

        private String msg;

        Subflow(String msg) {
            this.msg = msg;
        }

        @Override
        public String format(Object... args) {
            return formatMessage(msg, args);
        }
    }

    public enum Config implements FormattedMessage {

        VALIDATION_ID_NOT_UNIQUE("Error validating module with name=[%s]: There are at least two configurations with the same ID. Configuration IDs must be unique."),
        VALIDATION_ID_NOT_VALID("Error validating module with name=[%s]: The 'id' property must be defined and not empty in any JSON configuration definition.");

        private String msg;

        Config(String msg) {
            this.msg = msg;
        }

        @Override
        public String format(Object... args) {
            return formatMessage(msg, args);
        }
    }

    public enum Module implements FormattedMessage {

        DESERIALIZATION_ERROR("Error de-serializing module with id=[%d], name=[%s]: %s"),
        FILE_FIND_IO_ERROR("An I/O occurred while reading file=[%s] in module with id=[%d], name=[%s]: %s"),
        START_FAILED("Could not start module named=[%s]"),
        INSTALL_SUCCESS("Module [%s] installed"),
        INSTALL_FAILED("Install failed: could not install module from path=[%s]"),
        INSTALL_FAILED_MODULE_ALREADY_INSTALLED("Install failed: module from file path=[%s] is already installed. Did you mean update?"),
        INSTALL_FAILED_MODULE_NAME_NOT_FOUND("Install failed: could not find module name from file path=[%s]"),
        INSTALL_MODULE_DIFFERENT_VERSION_PRESENT("Module [%s], version [%s] is replaced by version [%s]"),
        UNINSTALL_SUCCESS("Module [%s] uninstalled"),
        UPDATE_SUCCESS("Module [%s] updated"),
        UPDATE_FAILED_MODULE_NOT_FOUND("Update failed: could not find registered module in target file path=[%s]"),
        START_SUCCESS("Module [%s] started"),
        REMOVE_MODULE_FROM_DIRECTORY_ERROR("Module [%s], version [%s], file path=[%s] could not be removed from runtime modules directory"),
        REMOVE_MODULE_FROM_DIRECTORY_EXCEPTION("Module [%s], version [%s], file path=[%s] could not be removed from runtime modules directory: %s");

        private String msg;

        Module(String msg) {
            this.msg = msg;
        }

        @Override
        public String format(Object... args) {
            return formatMessage(msg, args);
        }
    }

    public enum Deserializer implements FormattedMessage {

        UNSUPPORTED_COLLECTION_TYPE("Error while mapping property=[%s]: not a supported collection type."),
        CONFIGURATION_NOT_FOUND("Could not find configuration with id=[%s]"),
        ERROR_READING_FILES_FROM_RESOURCE_FOLDER("Error reading files from resource folder, target path=[%s]"),
        ERROR_COMPONENT_NOT_FOUND("Expected Component=[%s] was not found"),
        SCRIPT_SOURCE_NOT_FOUND("Could not find script named=[%s] defined in resources/scripts folder. Please make sure that the referenced script exists."),
        SCRIPT_SOURCE_EMPTY("A script resource file must not be null or empty");

        private String msg;

        Deserializer(String msg) {
            this.msg = msg;
        }

        @Override
        public String format(Object... args) {
            return formatMessage(msg, args);
        }
    }

    public enum Component implements FormattedMessage {

        REGISTERED("Registered component=[%s]"),
        UN_REGISTERED("UnRegistered component=[%s]");

        private String msg;

        Component(String msg) {
            this.msg = msg;
        }

        @Override
        public String format(Object... args) {
            return formatMessage(msg, args);
        }
    }

    public enum ConfigProperty implements FormattedMessage {

        NOT_FOUND_WITH_KEY_AND_PID_AND_DEFAULT("Could not find config property with key=[%s] for config pid=[%s], using defaultValue=[%s]."),
        NOT_FOUND_WITH_KEY_AND_PID("Could not find config property with key=[%s] for config pid=[%s]."),
        NOT_FOUND_WITH_KEY("Could not find config property with key=[%s]."),
        UNSUPPORTED_CONVERSION("Unsupported conversion. Could not convert config property with key=[%s] for config pid=[%s] to type=[%s].");

        private String msg;

        ConfigProperty(String msg) {
            this.msg = msg;
        }

        @Override
        public String format(Object... args) {
            return formatMessage(msg, args);
        }

    }

    public enum Script implements FormattedMessage {

        SCRIPT_BLOCK_COMPILATION_ERROR("Could not compile script: %s,\n- Script code:\n%s"),
        SCRIPT_SOURCE_COMPILATION_ERROR("Could not compile script source: %s, \n- Source: %s\n- Module names: %s"),
        SCRIPT_EXECUTION_ERROR("Could not execute script: %s,\n- Script code:\n%s");

        private String msg;

        Script(String msg) {
            this.msg = msg;
        }

        @Override
        public String format(Object... args) {
            return formatMessage(msg, args);
        }
    }

    public enum Execution implements FormattedMessage {

        ERROR_FIRST_SUCCESSOR_LEADING_TO_END("Could not find first successor of component=[%s], leading to component=[%s]");

        private String msg;

        Execution(String msg) {
            this.msg = msg;
        }

        @Override
        public String format(Object... args) {
            return formatMessage(msg, args);
        }
    }

    public enum HotSwap implements FormattedMessage {

        MODULE_NOT_FOUND("Hot Swap failed: could not find registered module from target file path=[%s]");

        private String msg;

        HotSwap(String msg) {
            this.msg = msg;
        }

        @Override
        public String format(Object... args) {
            return formatMessage(msg, args);
        }
    }

    public enum PubSub implements FormattedMessage {

        ERROR_DELIVERING_MESSAGE("Could not deliver Service Bus Message");

        private String msg;

        PubSub(String msg) {
            this.msg = msg;
        }

        @Override
        public String format(Object... args) {
            return formatMessage(msg, args);
        }
    }

    public enum Resource implements FormattedMessage {

        RESOURCE_DYNAMIC_NOT_FOUND("Could not find resource with path=[%s] (evaluated from=%s) in module with id=[%d], name=[%s] defined in the project's 'src/main/resources' folder. Please make sure that the referenced resource exists at the given path."),
        RESOURCE_NOT_FOUND("Could not find resource with path=[%s] in module with id=[%d], name=[%s] defined in the project's 'src/main/resources' folder. Please make sure that the referenced resource exists at the given path."),
        ERROR_RESOURCE_NOT_FOUND_NULL("Resource could not be found: dynamic resource object was null"),
        ERROR_RESOURCE_NOT_FOUND_WITH_VALUE("Resource could not be found: dynamic resource path was=[%s]");

        private String msg;

        Resource(String msg) {
            this.msg = msg;
        }

        @Override
        public String format(Object... args) {
            return formatMessage(msg, args);
        }
    }
}
