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
 * FilterInvocationSecurityMetadataSource 即 过滤器调用安全元数据源
 * 用于根据用户当前的角色权限  匹配  安全元数据里面的角色权限， 若安全元数据里有当前角色的所需权限，则放行
 **/
@Component
public class FilterInvocationSecurityMetadataSourceImpl implements FilterInvocationSecurityMetadataSource {

    /**
     * 接口角色列表
     * 即银色了每个资源可以访问的 角色列表
     */
    private static List<UrlRoleDTO> urlRoleList;

    @Autowired
    private RoleDao roleDao;

    /**
     * 加载接口角色信息（各类角色的一级权限）
     * 被@PostConstruct修饰的方法会在服务器加载Servlet的时候运行，
     * 并且只会被服务器执行一次。PostConstruct在构造函数之后执行，init（）方法之前执行。
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

    /**
     * 返回了Collection<ConfigAttribute>，表示当前请求对应资源权限 可以访问的 角色列表
     * @param object
     * @return
     * @throws IllegalArgumentException
     */
    @Override
    public Collection<ConfigAttribute> getAttributes(Object object) throws IllegalArgumentException {
        // 修改接口角色关系后重新加载
        if (CollectionUtils.isEmpty(urlRoleList)) {
            this.loadDataSource();
        }
        // 调用过滤器，根据请求的权限判断是否放行
        FilterInvocation fi = (FilterInvocation) object;
        // 获取用户请求方式
        String method = fi.getRequest().getMethod();
        // 获取用户请求Url
        String url = fi.getRequest().getRequestURI();
        // 蚂蚁路径匹配器
        AntPathMatcher antPathMatcher = new AntPathMatcher();
        // 获取接口角色信息，若为匿名接口则放行，若无对应角色则禁止
        for (UrlRoleDTO urlRoleDTO : urlRoleList) {
            // 找到路径和方法都匹配的 资源权限
            if (antPathMatcher.match(urlRoleDTO.getUrl(), url) && urlRoleDTO.getRequestMethod().equals(method)) {
                // 查询该资源权限 可供访问的角色
                List<String> roleList = urlRoleDTO.getRoleList(); //用户角色名列表
                if (CollectionUtils.isEmpty(roleList)) { //列表为空，返回disable不可访问
                    return SecurityConfig.createList("disable");
                }
                // 返回字符串数组类型的 允许访问的角色名列表
                return SecurityConfig.createList(roleList.toArray(new String[]{}));//new String[]{}是为了转换数组类型
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
