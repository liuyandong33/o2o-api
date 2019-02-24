package build.dream.o2o.controllers;

import build.dream.common.api.ApiRest;
import build.dream.common.controllers.BasicController;
import build.dream.common.utils.*;
import build.dream.o2o.constants.Constants;
import build.dream.o2o.services.ProxyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by liuyandong on 2017/7/22.
 */
@Controller
@RequestMapping(value = "/proxy")
public class ProxyController extends BasicController {
    @Autowired
    private ProxyService proxyService;
    private static final String ACCESS_TOKEN = "access_token";
    private static final String PARTITION_CODE = "partitionCode";
    private static final String SERVICE_NAME = "serviceName";
    private static final String CONTROLLER_NAME = "controllerName";
    private static final String ACTION_NAME = "actionName";
    private static final String URL = "url";

    /**
     * 普通 GET 请求
     *
     * @param serviceName
     * @param controllerName
     * @param actionName
     * @param httpServletRequest
     * @return
     */
    @RequestMapping(value = "/doGet/{serviceName}/{controllerName}/{actionName}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public String doGet(@PathVariable(value = SERVICE_NAME) String serviceName, @PathVariable(value = CONTROLLER_NAME) String controllerName, @PathVariable(value = ACTION_NAME) String actionName, HttpServletRequest httpServletRequest) {
        String result = null;
        Map<String, String> requestParameters = ApplicationHandler.getRequestParameters(httpServletRequest);
        try {
            result = callOtherSystem(serviceName, controllerName, actionName, httpServletRequest, requestParameters, false);
        } catch (Exception e) {
            LogUtils.error("处理失败", className, "doGet", e, requestParameters);
            result = GsonUtils.toJson(new ApiRest(e));
        }
        return result;
    }

    /**
     * 普通 POST 请求
     *
     * @param serviceName
     * @param controllerName
     * @param actionName
     * @param httpServletRequest
     * @return
     */
    @RequestMapping(value = "/doPost/{serviceName}/{controllerName}/{actionName}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public String doPost(@PathVariable(value = SERVICE_NAME) String serviceName, @PathVariable(value = CONTROLLER_NAME) String controllerName, @PathVariable(value = ACTION_NAME) String actionName, HttpServletRequest httpServletRequest) {
        String result = null;
        Map<String, String> requestParameters = ApplicationHandler.getRequestParameters(httpServletRequest);
        try {
            result = callOtherSystem(serviceName, controllerName, actionName, httpServletRequest, requestParameters, false);
        } catch (Exception e) {
            LogUtils.error("处理失败", className, "doPost", e, requestParameters);
            result = GsonUtils.toJson(new ApiRest(e));
        }
        return result;
    }

    /**
     * 签名 GET 请求
     *
     * @param serviceName
     * @param controllerName
     * @param actionName
     * @param httpServletRequest
     * @return
     */
    @RequestMapping(value = "/doGetSignature/{serviceName}/{controllerName}/{actionName}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public String doGetSignature(@PathVariable(value = SERVICE_NAME) String serviceName, @PathVariable(value = CONTROLLER_NAME) String controllerName, @PathVariable(value = ACTION_NAME) String actionName, HttpServletRequest httpServletRequest) {
        String result = null;
        Map<String, String> requestParameters = ApplicationHandler.getRequestParameters(httpServletRequest);
        try {
            result = callOtherSystem(serviceName, controllerName, actionName, httpServletRequest, requestParameters, true);
        } catch (Exception e) {
            LogUtils.error("处理失败", className, "doGetSignature", e, requestParameters);
            result = GsonUtils.toJson(new ApiRest(e));
        }
        return result;
    }

    /**
     * 签名 POST 请求
     *
     * @param serviceName
     * @param controllerName
     * @param actionName
     * @param httpServletRequest
     * @return
     */
    @RequestMapping(value = "/doPostSignature/{serviceName}/{controllerName}/{actionName}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public String doPostSignature(@PathVariable(value = SERVICE_NAME) String serviceName, @PathVariable(value = CONTROLLER_NAME) String controllerName, @PathVariable(value = ACTION_NAME) String actionName, HttpServletRequest httpServletRequest) {
        String result = null;
        Map<String, String> requestParameters = ApplicationHandler.getRequestParameters(httpServletRequest);
        try {
            result = callOtherSystem(serviceName, controllerName, actionName, httpServletRequest, requestParameters, true);
        } catch (Exception e) {
            LogUtils.error("处理失败", className, "doPostSignature", e, requestParameters);
            result = GsonUtils.toJson(new ApiRest(e));
        }
        return result;
    }

    /**
     * 免令牌 GET 请求调用其他系统
     *
     * @param partitionCode
     * @param serviceName
     * @param controllerName
     * @param actionName
     * @return
     */
    @RequestMapping(value = "/doGetPermit/{partitionCode}/{serviceName}/{controllerName}/{actionName}", method = RequestMethod.GET)
    public ResponseEntity<byte[]> doGetPermit(@PathVariable(value = PARTITION_CODE) String partitionCode, @PathVariable(value = SERVICE_NAME) String serviceName, @PathVariable(value = CONTROLLER_NAME) String controllerName, @PathVariable(value = ACTION_NAME) String actionName) {
        Map<String, String> requestParameters = ApplicationHandler.getRequestParameters();
        requestParameters.put("clientType", proxyService.obtainClientType());
        return ProxyUtils.doGetOrdinaryWithRequestParameters(partitionCode, serviceName, controllerName, actionName, requestParameters);
    }

    @RequestMapping(value = "/doGetPermitWithUrl", method = RequestMethod.GET)
    public ResponseEntity<byte[]> doGetPermitWithUrl() {
        String url = ApplicationHandler.getRequestParameter(URL);
        ValidateUtils.notNull(url, "参数错误！");

        String clientType = proxyService.obtainClientType();
        if (url.indexOf("?") > 0) {
            url = url + "&clientType=" + clientType;
        } else {
            url = url + "?clientType=" + clientType;
        }
        return ProxyUtils.doGetOrdinaryWithRequestParameters(url);
    }

    /**
     * 免令牌 POST 请求调用其他系统
     *
     * @param partitionCode
     * @param serviceName
     * @param controllerName
     * @param actionName
     * @return
     */
    @RequestMapping(value = "/doPostPermit/{partitionCode}/{serviceName}/{controllerName}/{actionName}", method = RequestMethod.POST)
    public ResponseEntity<byte[]> doPostPermit(@PathVariable(value = PARTITION_CODE) String partitionCode, @PathVariable(value = SERVICE_NAME) String serviceName, @PathVariable(value = CONTROLLER_NAME) String controllerName, @PathVariable(value = ACTION_NAME) String actionName) {
        Map<String, String> requestParameters = ApplicationHandler.getRequestParameters();
        requestParameters.put("clientType", proxyService.obtainClientType());
        return ProxyUtils.doPostOrdinaryWithRequestParameters(partitionCode, serviceName, controllerName, actionName, requestParameters);
    }

    /**
     * 调用其他系统
     *
     * @param serviceName
     * @param controllerName
     * @param actionName
     * @param httpServletRequest
     * @param requestParameters
     * @param isNeedVerifySignature
     * @return
     * @throws IOException
     */
    private String callOtherSystem(String serviceName, String controllerName, String actionName, HttpServletRequest httpServletRequest, Map<String, String> requestParameters, boolean isNeedVerifySignature) throws IOException {
        String result = null;
        Map<String, String> clientInfo = proxyService.obtainClientInfo();
        if (isNeedVerifySignature) {
            String publicKey = clientInfo.get(Constants.PUBLIC_KEY);
            ValidateUtils.isTrue(proxyService.verifySignature(requestParameters, publicKey), "签名错误！");
        }
        String partitionCode = clientInfo.get(Constants.PARTITION_CODE);
        if (httpServletRequest instanceof MultipartHttpServletRequest) {
            Map<String, Object> callOtherSystemRequestParameters = new HashMap<String, Object>(requestParameters);
            MultipartHttpServletRequest multipartHttpServletRequest = (MultipartHttpServletRequest) httpServletRequest;
            callOtherSystemRequestParameters.putAll(multipartHttpServletRequest.getFileMap());
            result = ProxyUtils.doPostOriginalWithRequestParametersAndFiles(partitionCode, serviceName, controllerName, actionName, callOtherSystemRequestParameters);
        } else {
            String method = httpServletRequest.getMethod();
            if (Constants.REQUEST_METHOD_GET.equals(method)) {
                result = ProxyUtils.doGetOriginalWithRequestParameters(partitionCode, serviceName, controllerName, actionName, requestParameters);
            } else if (Constants.REQUEST_METHOD_POST.equals(method)) {
                result = ProxyUtils.doPostOriginalWithRequestParameters(partitionCode, serviceName, controllerName, actionName, requestParameters);
            }
        }
        return result;
    }
}
