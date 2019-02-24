package org.springframework.security.oauth2.common.exceptions;

import build.dream.o2o.constants.Constants;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.apache.commons.lang.ArrayUtils;
import org.springframework.web.util.HtmlUtils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map.Entry;
import java.util.UUID;

public class OAuth2ExceptionJackson2Serializer extends StdSerializer<OAuth2Exception> {
    private static final String[] OLD_ERROR_CODES = {"invalid_client", "unauthorized_client",
            "invalid_grant", "invalid_scope",
            "invalid_token", "invalid_request",
            "redirect_uri_mismatch", "unsupported_grant_type",
            "unsupported_response_type", "insufficient_scope",
            "access_denied"};
    private static final String[] NEW_ERROR_CODES = {Constants.ERROR_CODE_INVALID_CLIENT, Constants.ERROR_CODE_UNAUTHORIZED_CLIENT,
            Constants.ERROR_CODE_INVALID_GRANT, Constants.ERROR_CODE_INVALID_SCOPE,
            Constants.ERROR_CODE_INVALID_TOKEN, Constants.ERROR_CODE_INVALID_REQUEST,
            Constants.ERROR_CODE_REDIRECT_URI_MISMATCH, Constants.ERROR_CODE_UNSUPPORTED_GRANT_TYPE,
            Constants.ERROR_CODE_UNSUPPORTED_RESPONSE_TYPE, Constants.ERROR_CODE_INSUFFICIENT_SCOPE,
            Constants.ERROR_CODE_ACCESS_DENIED};

    public OAuth2ExceptionJackson2Serializer() {
        super(OAuth2Exception.class);
    }

    @Override
    public void serialize(OAuth2Exception value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        jgen.writeStartObject();
        jgen.writeBooleanField("successful", false);
        jgen.writeStringField("className", null);
        jgen.writeStringField("message", null);
        jgen.writeObjectFieldStart("error");

        String code = null;
        int index = ArrayUtils.indexOf(OLD_ERROR_CODES, value.getOAuth2ErrorCode());
        if (index >= 0) {
            code = NEW_ERROR_CODES[index];
        } else {
            code = Constants.ERROR_CODE_UNKNOWN_ERROR;
        }

        jgen.writeStringField("code", code);
        String errorMessage = value.getMessage();
        if (errorMessage != null) {
            errorMessage = HtmlUtils.htmlEscape(errorMessage);
        }
        jgen.writeStringField("message", errorMessage);
        jgen.writeEndObject();
        if (value.getAdditionalInformation() != null) {
            jgen.writeObjectFieldStart("data");
            for (Entry<String, String> entry : value.getAdditionalInformation().entrySet()) {
                String key = entry.getKey();
                String add = entry.getValue();
                jgen.writeStringField(key, add);
            }
            jgen.writeEndObject();
        } else {
            jgen.writeObjectField("data", null);
        }
        jgen.writeStringField("id", UUID.randomUUID().toString());
        jgen.writeStringField("timestamp", new SimpleDateFormat(Constants.DEFAULT_DATE_PATTERN).format(new Date()));
        jgen.writeStringField("signature", null);
        jgen.writeBooleanField("zipped", false);
        jgen.writeBooleanField("encrypted", false);
        jgen.writeEndObject();
    }
}
