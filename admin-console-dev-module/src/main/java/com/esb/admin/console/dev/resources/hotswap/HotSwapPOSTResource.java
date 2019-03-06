package com.esb.admin.console.dev.resources.hotswap;

import com.esb.admin.console.dev.commons.RequestBody;
import com.esb.internal.api.HotSwapService;
import com.esb.internal.api.InternalAPI;
import com.esb.internal.api.hotswap.v1.HotSwapPOSTReq;
import com.esb.internal.api.hotswap.v1.HotSwapPOSTRes;
import org.takes.Request;
import org.takes.Response;
import org.takes.Take;
import org.takes.rs.RsWithBody;
import org.takes.rs.RsWithHeader;
import org.takes.rs.RsWithStatus;

import java.io.IOException;

import static com.esb.admin.console.dev.commons.HttpHeader.CONTENT_TYPE;
import static com.esb.api.message.MimeType.APPLICATION_JSON;
import static java.net.HttpURLConnection.HTTP_OK;

public class HotSwapPOSTResource implements Take {

    private final HotSwapService service;

    HotSwapPOSTResource(HotSwapService service) {
        this.service = service;
    }

    @Override
    public Response act(Request request) throws IOException {
        String json = RequestBody.from(request);
        HotSwapPOSTReq hotSwapReq = InternalAPI.HotSwap.V1.POST.Req.deserialize(json);

        long moduleId = service.hotSwap(hotSwapReq.getModuleFilePath(), hotSwapReq.getResourcesRootDirectory());

        HotSwapPOSTRes dto = new HotSwapPOSTRes();
        dto.setModuleId(moduleId);

        return new RsWithBody(
                new RsWithStatus(
                        new RsWithHeader(CONTENT_TYPE, APPLICATION_JSON.toString()), HTTP_OK),
                InternalAPI.HotSwap.V1.POST.Res.serialize(dto));

    }
}
