package build.dream.o2o.processors;

import build.dream.o2o.auth.CustomFilterInvocationSecurityMetadataSource;
import org.springframework.beans.BeansException;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liuyandong on 2017/6/21.
 */
@Component
public class BeanPostProcessor implements org.springframework.beans.factory.config.BeanPostProcessor {
    private List<FilterSecurityInterceptor> filterSecurityInterceptors = new ArrayList<FilterSecurityInterceptor>();
    private CustomFilterInvocationSecurityMetadataSource customFilterInvocationSecurityMetadataSource = null;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof FilterSecurityInterceptor) {
            filterSecurityInterceptors.add((FilterSecurityInterceptor) bean);
            if (customFilterInvocationSecurityMetadataSource != null) {
                for (FilterSecurityInterceptor filterSecurityInterceptor : filterSecurityInterceptors) {
                    filterSecurityInterceptor.setSecurityMetadataSource(customFilterInvocationSecurityMetadataSource);
                }
                filterSecurityInterceptors.clear();
            }
        }

        if (bean instanceof CustomFilterInvocationSecurityMetadataSource) {
            customFilterInvocationSecurityMetadataSource = (CustomFilterInvocationSecurityMetadataSource) bean;
        }
        return bean;
    }
}
