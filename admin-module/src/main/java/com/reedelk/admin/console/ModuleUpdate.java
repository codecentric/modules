package com.reedelk.admin.console;

import com.reedelk.runtime.api.annotation.ESBComponent;
import com.reedelk.runtime.api.component.ProcessorSync;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.rest.api.InternalAPI;
import com.reedelk.runtime.rest.api.module.v1.ModulePUTReq;
import com.reedelk.runtime.rest.api.module.v1.ModulePUTRes;
import com.reedelk.runtime.system.api.ModuleService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;


@ESBComponent("Update module")
@Component(service = ModuleUpdate.class, scope = PROTOTYPE)
public class ModuleUpdate implements ProcessorSync {

    private static final Logger logger = LoggerFactory.getLogger(ModuleUpdate.class);

    @Reference
    private ModuleService service;

    @Override
    public Message apply(Message message, FlowContext flowContext) {

        String payload = message.payload();

        logger.info("Payload: " + payload);

        String resultJson = update(payload);

        return MessageBuilder.get().json(resultJson).build();
    }

    private String update(String json) {

        ModulePUTReq putRequest = InternalAPI.Module.V1.PUT.Req.deserialize(json);

        long moduleId = service.update(putRequest.getModuleFilePath());

        ModulePUTRes dto = new ModulePUTRes();

        dto.setModuleId(moduleId);

        return InternalAPI.Module.V1.PUT.Res.serialize(dto);
    }
}
