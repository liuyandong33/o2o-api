package build.dream.o2o.auth;

import build.dream.common.utils.TupleUtils;
import build.dream.o2o.constants.Constants;
import build.dream.o2o.utils.AuthenticationUtils;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.AnyRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import scala.Tuple2;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class CustomFilterInvocationSecurityMetadataSource implements FilterInvocationSecurityMetadataSource {
    private static List<String> permitAllRequestPath = new ArrayList<String>();
    private static final String PROXY_DO_GET = "/proxy/doGet";
    private static final String PROXY_DO_POST = "/proxy/doPost";
    private static final String PROXY_DO_GET_SIGNATURE = "/proxy/doGetSignature";
    private static final String PROXY_DO_POST_SIGNATURE = "/proxy/doPostSignature";

    private static String buildRequestPath(String prefix, String serviceName, String controllerName, String actionName) {
        return prefix + "/" + serviceName + "/" + controllerName + "/" + actionName;
    }

    static {
        permitAllRequestPath.add("/favicon.ico");
        permitAllRequestPath.add("/auth/logout");
        permitAllRequestPath.add("/proxy/doGetPermit/**");
        permitAllRequestPath.add("/proxy/doPostPermit/**");
        permitAllRequestPath.add("/proxy/doGetPermitWithUrl");
    }

    private static String obtainPrefix(int accessMode) {
        if (accessMode == Constants.ACCESS_MODE_GET) {
            return PROXY_DO_GET;
        } else if (accessMode == Constants.ACCESS_MODE_POST) {
            return PROXY_DO_POST;
        } else if (accessMode == Constants.ACCESS_MODE_GET_SIGNATURE) {
            return PROXY_DO_GET_SIGNATURE;
        } else if (accessMode == Constants.ACCESS_MODE_POST_SIGNATURE) {
            return PROXY_DO_POST_SIGNATURE;
        }
        return null;
    }

    private static Tuple2<RequestMatcher, Collection<ConfigAttribute>> buildRequest(String serviceName, String controllerName, String actionName, int accessMode, String privilegeCode) {
        RequestMatcher requestMatcher = new AntPathRequestMatcher(buildRequestPath(obtainPrefix(accessMode), serviceName, controllerName, actionName));
        List<ConfigAttribute> configAttributes = new ArrayList<ConfigAttribute>();
        configAttributes.add(new SecurityConfig(String.format(Constants.HAS_AUTHORITY_FORMAT, privilegeCode)));
        return TupleUtils.buildTuple2(requestMatcher, configAttributes);
    }

    private Map<RequestMatcher, Collection<ConfigAttribute>> requestMap;

    public Map<RequestMatcher, Collection<ConfigAttribute>> getRequestMap() {
        return requestMap;
    }

    public void setRequestMap(Map<RequestMatcher, Collection<ConfigAttribute>> requestMap) {
        this.requestMap = requestMap;
    }

    public void init() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        LinkedHashMap<RequestMatcher, Collection<ConfigAttribute>> requestMap = new LinkedHashMap<RequestMatcher, Collection<ConfigAttribute>>();
        for (String requestPath : permitAllRequestPath) {
            RequestMatcher requestMatcher = new AntPathRequestMatcher(requestPath);
            List<ConfigAttribute> configAttributes = new ArrayList<ConfigAttribute>();
            configAttributes.add(new SecurityConfig(Constants.PERMIT_ALL));
            requestMap.put(requestMatcher, configAttributes);
        }

        List<ConfigAttribute> configAttributes = new ArrayList<ConfigAttribute>();
        configAttributes.add(new SecurityConfig(Constants.AUTHENTICATED));
        requestMap.put(AnyRequestMatcher.INSTANCE, configAttributes);
        this.requestMap = AuthenticationUtils.processMap(requestMap, new SpelExpressionParser());
    }

    public CustomFilterInvocationSecurityMetadataSource() {

    }

    public CustomFilterInvocationSecurityMetadataSource(LinkedHashMap<RequestMatcher, Collection<ConfigAttribute>> requestMap) {
        this.requestMap = requestMap;
    }


    public Collection<ConfigAttribute> getAllConfigAttributes() {
        Set<ConfigAttribute> allAttributes = new HashSet<ConfigAttribute>();

        for (Map.Entry<RequestMatcher, Collection<ConfigAttribute>> entry : requestMap.entrySet()) {
            allAttributes.addAll(entry.getValue());
        }
        return allAttributes;
    }

    public Collection<ConfigAttribute> getAttributes(Object object) {
        final HttpServletRequest request = ((FilterInvocation) object).getRequest();
        for (Map.Entry<RequestMatcher, Collection<ConfigAttribute>> entry : requestMap.entrySet()) {
            if (entry.getKey().matches(request)) {
                return entry.getValue();
            }
        }
        return null;
    }

    public boolean supports(Class<?> clazz) {
        return FilterInvocation.class.isAssignableFrom(clazz);
    }
}
