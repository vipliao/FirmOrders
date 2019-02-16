package com.firm.order.config.aouth.realm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.crypto.hash.Md5Hash;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.eis.SessionDAO;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;

import com.firm.order.modules.role.service.IRoleService;
import com.firm.order.modules.role.vo.RoleVO;
import com.firm.order.modules.user.service.IUserService;
import com.firm.order.modules.user.vo.UserVO;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ShiroRealm extends AuthorizingRealm {
	@Autowired
    private IUserService userService;
	@Autowired
    private IRoleService roleService;
	
	@Autowired
    private SessionDAO sessionDAO;
	
   // private IPermissionsService permissionsService;
    

    /**
     * 为当前登录的Subject授予角色和权限
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        // 获取当前登录的用户名,等价于(String)principals.fromRealm(this.getName()).iterator().next()
        String loginName = (String) super.getAvailablePrincipal(principals);
        List<String> roleList = new ArrayList<String>();
        List<String> permissionList = new ArrayList<String>();
        // 从数据库中获取当前登录用户的详细信息
		try {
			UserVO user = userService.queryUserByPhoneOrCode(loginName);
			if (null != user) {
	            // 实体类User中包含有用户角色的实体类信息
	            if (null != user.getRoleId()) {
	                // 获取当前登录用户的角色
	            	RoleVO role = roleService.findVOById(user.getRoleId(), RoleVO.class);
	                roleList.add(role.getRoleName());
	                
	                // 实体类Role中包含有角色权限的实体类信息
	               /* if (null != role.getPermissionsList()) {
	                    String permissionsList[] = role.getPermissionsList().split(",");
	                    // 获取权限
	                    for (int i = 0; i < permissionsList.length; i++) {
	                        PermissionsForm permissionsForm = new PermissionsForm();
	                        permissionsForm.setId(Integer.parseInt(permissionsList[i]));
	                        
	                        Permissions permi = permissionsService.getList(permissionsForm).get(0);
	                        permissionList.add(permi.getCode());
	                    }
	                }*/
	            }
	        } else {
	            throw new AuthorizationException("用户不存在！");
	        }
		} catch (Exception e) {
			log.error("为当前登录的Subject授予角色和权限方法:", e.getMessage());
			throw new AuthorizationException(e.getCause());
		}
        
        // 为当前用户设置角色和权限
        SimpleAuthorizationInfo simpleAuthorInfo = new SimpleAuthorizationInfo();
        simpleAuthorInfo.addRoles(roleList);
        simpleAuthorInfo.addStringPermissions(permissionList);
        return simpleAuthorInfo;
    }

    /**
     * 验证当前登录的Subject
     * 
     * @see 经测试:本例中该方法的调用时机为LoginController.login()方法中执行Subject.login()时
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authcToken) {
        // 获取基于用户名和密码的令牌
        // 实际上这个authcToken是从Controller里面currentUser.login(token)传过来的
        UsernamePasswordToken token = (UsernamePasswordToken) authcToken;
        log.debug("验证当前Subject时获取到token为" + ReflectionToStringBuilder.toString(token, ToStringStyle.MULTI_LINE_STYLE));
        //以下为只允许同一账户单个登录
		Collection<Session> sessions = sessionDAO.getActiveSessions();
		if (sessions.size() > 0) {
			for (Session session : sessions) {
				// 获得session中已经登录用户的信息
				if (null != session.getAttribute("currentUser")) {
					String loginUserCode = ((UserVO) session.getAttribute("currentUser")).getUserCode();
					String loginUserPhone = ((UserVO) session.getAttribute("currentUser")).getPhone();
					if (token.getUsername().equals(loginUserCode) || token.getUsername().equals(loginUserPhone)) {
						session.setTimeout(0); // 这里就把session清除，
					}
				}
			}
         }
		try {
			UserVO user = userService.anth(token.getUsername(), String.valueOf(token.getPassword()));
			if (null != user) {
				//String nd5Pwd = md5Pwd(String.valueOf(token.getCredentials()),token.getPrincipal().toString());
				AuthenticationInfo authcInfo = new SimpleAuthenticationInfo(token.getPrincipal(), token.getCredentials(),getName());
				this.setSession("currentUser", user);
				return authcInfo;
			} 
		} catch (Exception e) {
			log.error("验证当前登录的Subject方法:",e);
			throw new AuthorizationException(e.getMessage());
		}
		return null;
    }

    /**
     * 将一些数据放到ShiroSession中,以便于其它地方使用
     * 
     * @see 比如Controller,使用时直接用HttpSession.getAttribute(key)就可以取到
     */
    private void setSession(Object key, Object value) {
        Subject currentUser = SecurityUtils.getSubject();
        if (null != currentUser) {
            Session session = currentUser.getSession();
            System.out.println("Session默认超时时间为[" + session.getTimeout() + "]毫秒");
            if (null != session) {
                session.setAttribute(key, value);
            }
        }
    }
    
    public static String md5Pwd(String password, String salt) {
        // TODO Auto-generated method stub
        String md5Pwd = new Md5Hash(password, salt).toHex();
        return md5Pwd;
    }
 }
