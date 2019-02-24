package build.dream.o2o.utils;

import org.springframework.expression.ExpressionParser;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.expression.ExpressionBasedFilterInvocationSecurityMetadataSource;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedHashMap;

public class AuthenticationUtils {
    public static String getClientId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getPrincipal().toString();
    }

    public static LinkedHashMap<RequestMatcher, Collection<ConfigAttribute>> processMap(LinkedHashMap<RequestMatcher, Collection<ConfigAttribute>> requestMap, ExpressionParser expressionParser) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Class<ExpressionBasedFilterInvocationSecurityMetadataSource> expressionBasedFilterInvocationSecurityMetadataSourceClass = ExpressionBasedFilterInvocationSecurityMetadataSource.class;
        Method expressionBasedFilterInvocationSecurityMetadataSourceClassProcessMapMethod = expressionBasedFilterInvocationSecurityMetadataSourceClass.getDeclaredMethod("processMap", LinkedHashMap.class, ExpressionParser.class);
        expressionBasedFilterInvocationSecurityMetadataSourceClassProcessMapMethod.setAccessible(true);
        return (LinkedHashMap<RequestMatcher, Collection<ConfigAttribute>>) expressionBasedFilterInvocationSecurityMetadataSourceClassProcessMapMethod.invoke(null, requestMap, expressionParser);
    }
}
