package com.yuming.blog.handler;

import com.yuming.blog.dao.RoleDao;
import com.yuming.blog.dto.UrlRoleDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.List;

/**
 * 调用安全元数据源 筛选器
 *
 **/
@Component
public class FilterInvocationSecurityMetadataSourceImpl implements FilterInvocationSecurityMetadataSource {

    /**
     * 接口角色列表
     */
    private static List<UrlRoleDTO> urlRoleList;

    @Autowired
    private RoleDao roleDao;

    /**
     * 加载接口角色信息（各类角色的一级权限）
     */
    @PostConstruct
    private void loadDataSource() {
        urlRoleList = roleDao.listUrlRoles();
    }

    /**
     * 清空接口角色信息
     */
    public void clearDataSource() {
        urlRoleList = null;
    }

    @Override
    public Collection<ConfigAttribute> getAttributes(Object object) throws IllegalArgumentException {
        // 修改接口角色关系后重新加载
        if (CollectionUtils.isEmpty(urlRoleList)) {
            this.loadDataSource();
        }
        FilterInvocation fi = (FilterInvocation) object;
        // 获取用户请求方式
        String method = fi.getRequest().getMethod();
        // 获取用户请求Url
        String url = fi.getRequest().getRequestURI();
        AntPathMatcher antPathMatcher = new AntPathMatcher();
        // 获取接口角色信息，若为匿名接口则放行，若无对应角色则禁止
        for (UrlRoleDTO urlRoleDTO : urlRoleList) {
            if (antPathMatcher.match(urlRoleDTO.getUrl(), url) && urlRoleDTO.getRequestMethod().equals(method)) {
                List<String> roleList = urlRoleDTO.getRoleList(); //用户角色名列表
                if (CollectionUtils.isEmpty(roleList)) {
                    return SecurityConfig.createList("disable");
                }
                return SecurityConfig.createList(roleList.toArray(new String[]{}));
            }
        }
        return null;
    }

    @Override
    public Collection<ConfigAttribute> getAllConfigAttributes() {
        return null;
    }

    @Override
    public boolean supports(Class<?> aClass) {
        return FilterInvocation.class.isAssignableFrom(aClass);
    }

}
