package com.goodobj.spring;

/**
 * @author yuanlei-003
 */
public class BeanDefinition {

    private Class<?> cls;

    private String scope;

    public BeanDefinition() {
    }

    public BeanDefinition(Class<?> cls, String scope) {
        this.cls = cls;
        this.scope = scope;
    }

    public Class<?> getCls() {
        return cls;
    }

    public void setCls(Class<?> cls) {
        this.cls = cls;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }
}
