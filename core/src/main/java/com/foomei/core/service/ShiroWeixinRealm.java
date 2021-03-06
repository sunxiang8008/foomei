package com.foomei.core.service;

import com.foomei.common.mapper.BeanMapper;
import com.foomei.common.security.shiro.InactiveAccountException;
import com.foomei.common.security.shiro.ShiroUser;
import com.foomei.common.security.shiro.SkipCredentialsMatcher;
import com.foomei.common.security.shiro.WeixinToken;
import com.foomei.core.entity.Role;
import com.foomei.core.entity.User;
import com.foomei.core.entity.UserGroup;
import com.google.common.collect.Sets;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Set;

public class ShiroWeixinRealm extends AuthorizingRealm {

  @Autowired
  private UserService userService;

  public ShiroWeixinRealm(CacheManager cacheManager) {
    super(cacheManager);
  }

  /**
   * 认证回调函数,登录时调用.
   */
  @Override
  protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authcToken) throws AuthenticationException {
    String openId = ((WeixinToken) authcToken).getOpenId();
    if (openId == null) {
      throw new AccountException("Null OpenID are not allowed by this realm.");
    }
    User user = userService.getByWeixin(openId);

    if (user != null) {
      if (!user.isEnabled()) {
        if (user.isAccountInactived()) {
          throw new InactiveAccountException();
        }
        if (user.isAccountExpired()) {
          throw new ExpiredCredentialsException();
        }
        if (user.isAccountLocked()) {
          throw new LockedAccountException();
        }
        if (user.isAccountTerminated()) {
          throw new DisabledAccountException();
        }
      }

      return new SimpleAuthenticationInfo(BeanMapper.map(user, ShiroUser.class), null, getName());
    } else {
      throw new UnknownAccountException("No account found");
    }
  }

  /**
   * 授权查询回调函数, 进行鉴权但缓存中无用户的授权信息时调用.
   */
  @Override
  protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
    ShiroUser shiroUser = (ShiroUser) principals.getPrimaryPrincipal();
    User user = userService.getByLoginName(shiroUser.getLoginName());

    Set<Role> roles = Sets.newTreeSet(new RoleComparator());
    for (UserGroup group : user.getGroupList()) {
      roles.addAll(group.getRoleList());
    }
    roles.addAll(user.getRoleList());

    SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
    for (Role role : roles) {
      // 基于Role的权限信息
      info.addRole(role.getCode());
      // 基于Permission的权限信息
      info.addStringPermissions(role.getPermissions());
    }
    return info;
  }

  /**
   * 设定Password校验的Hash算法与迭代次数.
   */
  @PostConstruct
  public void initCredentialsMatcher() {
    SkipCredentialsMatcher matcher = new SkipCredentialsMatcher();
    setCredentialsMatcher(matcher);
  }

  @PostConstruct
  public void initAuthenticationTokenClass() {
    setAuthenticationTokenClass(WeixinToken.class);
  }

  public static class RoleComparator implements Comparator<Role>, Serializable {
    public int compare(Role role1, Role role2) {
      return role1.getCode().compareTo(role2.getCode());
    }
  }
}
