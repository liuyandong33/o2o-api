package build.dream.o2o.controllers;

import build.dream.common.api.ApiRest;
import build.dream.common.constants.HttpHeaders;
import build.dream.common.controllers.BasicController;
import build.dream.common.utils.*;
import build.dream.o2o.constants.Constants;
import com.google.zxing.WriterException;
import org.apache.commons.lang.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.provider.token.ConsumerTokenServices;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping(value = "/auth")
public class AuthController extends BasicController {
    @Autowired
    @Qualifier("consumerTokenServices")
    private ConsumerTokenServices consumerTokenServices;

    @RequestMapping(value = "/logout")
    @ResponseBody
    public String logout() {
        Map<String, String> requestParameters = ApplicationHandler.getRequestParameters();
        MethodCaller methodCaller = () -> {
            String accessToken = requestParameters.get("accessToken");
            ApplicationHandler.notBlank(accessToken, "accessToken");
            boolean successful = consumerTokenServices.revokeToken(accessToken);
            Validate.isTrue(successful, "注销失败");

            ApiRest apiRest = new ApiRest();
            apiRest.setMessage("注销成功！");
            apiRest.setSuccessful(true);
            return apiRest;
        };
        return ApplicationHandler.callMethod(methodCaller, "注销失败", requestParameters);
    }

    @RequestMapping(value = "/generateLoginQRCode")
    public void scanCode(HttpServletResponse httpServletResponse) throws IOException, WriterException {
        OutputStream outputStream = httpServletResponse.getOutputStream();
        String url = CommonUtils.getOutsideUrl(Constants.SERVICE_NAME_WEBAPI, "auth", "scanCode");
        url = "http://192.168.31.200:8887/auth/scanCode?code=" + UUID.randomUUID().toString();
        httpServletResponse.setContentType(MimeMappingUtils.obtainMimeTypeByExtension(ZXingUtils.FORMAT_NAME_PNG));
        ZXingUtils.generateQRCode(400, 400, url, outputStream);
    }

    @RequestMapping(value = "/scanCode")
    public ModelAndView scanCode(HttpServletRequest httpServletRequest) {
        String code = ApplicationHandler.getRequestParameter(httpServletRequest, "code");
        String userAgent = httpServletRequest.getHeader(HttpHeaders.USER_AGENT);
        userAgent = userAgent.toLowerCase();
        if (userAgent.contains("micromessenger")) {

        } else if (userAgent.contains("alipay")) {

        }
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("redirect:https://www.baidu.com");
        return modelAndView;
    }

    @RequestMapping(value = "/pullUserInfo", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public String pullUserInfo() {
        String code = ApplicationHandler.getRequestParameter("code");
        return null;
    }
}
