package build.dream.o2o.auth;

import build.dream.common.api.ApiRest;
import build.dream.common.auth.VipUserDetails;
import build.dream.common.catering.domains.Branch;
import build.dream.common.catering.domains.Vip;
import build.dream.common.saas.domains.Tenant;
import build.dream.common.saas.domains.TenantSecretKey;
import build.dream.common.utils.*;
import build.dream.o2o.constants.Constants;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component
public class CustomUserDetailsService implements UserDetailsService {
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Map<String, String> requestParameters = ApplicationHandler.getRequestParameters();
        String tenantId = requestParameters.get("tenantId");
        Tenant tenant = DatabaseHelper.find(Tenant.class, BigInteger.valueOf(Long.valueOf(tenantId)));
        ValidateUtils.notNull(tenant, "商户不存在！");

        String partitionCode = tenant.getPartitionCode();
        String business = tenant.getBusiness();
        String serviceName = CommonUtils.getServiceName(business);

        Map<String, String> obtainHeadquartersInfoRequestParameters = new HashMap<String, String>();
        obtainHeadquartersInfoRequestParameters.put("tenantId", tenantId);
        ApiRest obtainHeadquartersInfoResult = ProxyUtils.doGetWithRequestParameters(partitionCode, serviceName, "branch", "obtainHeadquartersInfo", obtainHeadquartersInfoRequestParameters);
        ValidateUtils.isTrue(obtainHeadquartersInfoResult.isSuccessful(), obtainHeadquartersInfoResult.getError());
        Branch headquarters = (Branch) obtainHeadquartersInfoResult.getData();

        Map<String, String> obtainVipInfoRequestParameters = new HashMap<String, String>();
        obtainVipInfoRequestParameters.put("tenantId", tenantId);
        obtainVipInfoRequestParameters.put("branchId", headquarters.getId().toString());
        obtainVipInfoRequestParameters.put("openId", username);

        ApiRest obtainVipInfoResult = ProxyUtils.doGetWithRequestParameters(partitionCode, serviceName, "vip", "obtainVipInfo", obtainVipInfoRequestParameters);
        ValidateUtils.isTrue(obtainVipInfoResult.isSuccessful(), obtainVipInfoResult.getError());
        Vip vip = (Vip) obtainVipInfoResult.getData();

        TenantSecretKey tenantSecretKey = DatabaseHelper.find(TenantSecretKey.class, TupleUtils.buildTuple3(TenantSecretKey.ColumnName.TENANT_ID, Constants.SQL_OPERATION_SYMBOL_EQUAL, tenant.getId()));

        Collection<GrantedAuthority> authorities = Collections.emptySet();
        VipUserDetails vipUserDetails = new VipUserDetails();
        vipUserDetails.setUsername(username);
        vipUserDetails.setPassword("{MD5}" + DigestUtils.md5Hex("123456"));
        vipUserDetails.setAuthorities(authorities);
        vipUserDetails.setTenant(tenant);
        vipUserDetails.setPublicKey(tenantSecretKey.getPublicKey());
        vipUserDetails.setPrivateKey(tenantSecretKey.getPrivateKey());
        vipUserDetails.setClientType(Constants.CLIENT_TYPE_O2O);
        vipUserDetails.setVip(vip);

        return vipUserDetails;
    }
}
