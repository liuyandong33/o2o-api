package build.dream.o2o.auth;

import build.dream.common.saas.domains.Tenant;
import build.dream.common.utils.ApplicationHandler;
import build.dream.common.utils.DatabaseHelper;
import build.dream.common.utils.ValidateUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@Component
public class CustomUserDetailsService implements UserDetailsService {
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Map<String, String> requestParameters = ApplicationHandler.getRequestParameters();
        String tenantId = requestParameters.get("tenantId");
        Tenant tenant = DatabaseHelper.find(Tenant.class, BigInteger.valueOf(Long.valueOf(tenantId)));
        ValidateUtils.notNull(tenant, "商户不存在！");

        Collection<GrantedAuthority> authorities = Collections.emptySet();
        VipUserDetails vipUserDetails = new VipUserDetails();
        vipUserDetails.setUsername(username);
        vipUserDetails.setPassword("{MD5}" + DigestUtils.md5Hex("123456"));
        vipUserDetails.setAuthorities(authorities);
        vipUserDetails.setTenant(tenant);

        return vipUserDetails;
    }
}
