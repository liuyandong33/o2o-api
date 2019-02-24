package build.dream.o2o.services;

import build.dream.common.auth.CustomUserDetails;
import build.dream.common.saas.domains.SystemUser;
import build.dream.common.saas.domains.Tenant;
import build.dream.common.utils.ApplicationHandler;
import build.dream.common.utils.ConfigurationUtils;
import build.dream.common.utils.SignatureUtils;
import build.dream.o2o.constants.Constants;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

@Service
public class ProxyService {
    public Map<String, String> obtainClientInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        SystemUser systemUser = customUserDetails.getSystemUser();
        Tenant tenant = customUserDetails.getTenant();
        Map<String, Object> branchInfo = customUserDetails.getBranchInfo();

        Map<String, String> clientInfo = new HashMap<String, String>();
        clientInfo.put(Constants.TENANT_ID, tenant.getId().toString());
        clientInfo.put(Constants.TENANT_CODE, tenant.getCode());
        clientInfo.put(Constants.BUSINESS, tenant.getBusiness());
        clientInfo.put(Constants.PARTITION_CODE, tenant.getPartitionCode());
        clientInfo.put(Constants.PUBLIC_KEY, customUserDetails.getPublicKey());
        clientInfo.put(Constants.PRIVATE_KEY, customUserDetails.getPrivateKey());
        return clientInfo;
    }

    public boolean verifySignature(Map<String, String> requestParameters, String publicKey) {
        Map<String, String> sortedRequestParameters = new TreeMap<String, String>(requestParameters);
        String signature = sortedRequestParameters.remove(Constants.SIGNATURE);
        ApplicationHandler.notBlank(signature, Constants.SIGNATURE);

        String timestamp = sortedRequestParameters.get(Constants.TIMESTAMP);
        ApplicationHandler.notBlank(timestamp, Constants.TIMESTAMP);

        String requestId = sortedRequestParameters.get(Constants.REQUEST_ID);
        ApplicationHandler.notBlank(requestId, Constants.REQUEST_ID);

        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<String, String> entry : sortedRequestParameters.entrySet()) {
            stringBuilder.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
        }
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);

        return SignatureUtils.verifySign(StringUtils.getBytesUtf8(stringBuilder.toString()), Base64.decodeBase64(publicKey), Base64.decodeBase64(signature), SignatureUtils.SIGNATURE_TYPE_SHA256_WITH_RSA);
    }

    public String obtainClientType() {
        String serviceName = ConfigurationUtils.getConfiguration(Constants.SERVICE_NAME);
        String clientType = null;
        if (Constants.SERVICE_NAME_APPAPI.equals(serviceName)) {
            clientType = Constants.CLIENT_TYPE_APP;
        } else if (Constants.SERVICE_NAME_POSAPI.equals(serviceName)) {
            clientType = Constants.CLIENT_TYPE_POS;
        } else if (Constants.SERVICE_NAME_WEBAPI.equals(serviceName)) {
            clientType = Constants.CLIENT_TYPE_WEB;
        } else if (Constants.SERVICE_NAME_O2OAPI.equals(serviceName)) {
            clientType = Constants.CLIENT_TYPE_O2O;
        }
        return clientType;
    }
}
