package com.goodobj.spring;

import com.goodobj.spring.anno.Autowired;
import com.goodobj.spring.anno.Component;
import com.goodobj.spring.anno.ComponentScan;

import java.beans.Introspector;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author yuanlei-003
 */
public class AresApplicationContext {

    private final Class<?> configClass;

    private final ConcurrentHashMap<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<String, Object> singletonBeanMap = new ConcurrentHashMap<>();

    public AresApplicationContext(Class<?> configClass) {
        this.configClass = configClass;

        if (configClass.isAnnotationPresent(ComponentScan.class)) {
            //获取扫描注解
            ComponentScan componentScanAnno = configClass.getAnnotation(ComponentScan.class);

            //获取扫描路径
            String scanPackage = componentScanAnno.value();

            //获取扫描字节码包绝对路径
            String scanPath = scanPackage.replace(".", "/");
            ClassLoader classLoader = this.getClass().getClassLoader();
            URL url = classLoader.getResource(scanPath);
            assert url != null;
            File scanDirectory = new File(url.getFile());
            if (!scanDirectory.isDirectory()) {
                throw new RuntimeException("scan directory error");
            }

            initBeanDefinitionMap(scanDirectory, scanPackage);

            initSingletonBeanMap();
        }
    }

    private void initBeanDefinitionMap(File scanDirectory, String scanPackage) {
        String osName = System.getProperty("os.name");
        if (osName.contains("Windows")) {
            scanPackage = scanPackage.replaceAll("\\.", "\\\\");
        } else {
            scanPackage = scanPackage.replaceAll("\\.", "/");
        }
        File[] files = scanDirectory.listFiles();
        assert files != null;
        for (File file : files) {
            if (file.isDirectory()) {
                initBeanDefinitionMap(file, scanPackage);
            }

            String fileAbsolutePath = file.getAbsolutePath();
            if (fileAbsolutePath.endsWith(".class")) {
                //获取字节码名称
                int beginIndex = fileAbsolutePath.indexOf(scanPackage);
                int endIndex = fileAbsolutePath.indexOf(".class");
                String clsName;
                if (osName.contains("Windows")) {
                    clsName = fileAbsolutePath.substring(beginIndex, endIndex).replaceAll("\\\\", ".");
                } else {
                    clsName = fileAbsolutePath.substring(beginIndex, endIndex).replaceAll("/", ".");
                }

                //获取字节码对象
                ClassLoader classLoader = this.getClass().getClassLoader();
                Class<?> cls;
                try {
                    cls = classLoader.loadClass(clsName);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }

                //判断是否是bean
                if (cls.isAnnotationPresent(Component.class)) {
                    //获取bean名称
                    Component componentAnno = cls.getAnnotation(Component.class);
                    String beanName = componentAnno.value();
                    if (beanName.isEmpty()) {
                        beanName = Introspector.decapitalize(cls.getSimpleName());
                    }

                    //设置定义信息对象
                    BeanDefinition beanDefinition = new BeanDefinition(cls, componentAnno.scope());

                    //设置到bean定义池中
                    beanDefinitionMap.put(beanName, beanDefinition);
                }
            }
        }
    }

    private void initSingletonBeanMap() {
        Set<Map.Entry<String, BeanDefinition>> entrySet = beanDefinitionMap.entrySet();
        for (Map.Entry<String, BeanDefinition> entry : entrySet) {
            BeanDefinition beanDefinition = entry.getValue();
            if ("singleton".equals(beanDefinition.getScope())) {
                String beanName = entry.getKey();
                Object bean = createBean(beanName, beanDefinition);
                singletonBeanMap.put(beanName, bean);
            }
        }
    }

    public Object getBean(String beanName) {
        BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
        if (beanDefinition == null) {
            throw new RuntimeException("bean definition not found");
        }
        String scope = beanDefinition.getScope();

        if ("singleton".equals(scope)) {
            //单例模式
            return singletonBeanMap.get(beanName);
        }

        //非单例模式
        return createBean(beanName, beanDefinition);
    }

    public Object getBean(Class<?> beanClass) {
        return null;
    }

    private Object createBean(String beanName, BeanDefinition beanDefinition) {
        Class<?> cls = beanDefinition.getCls();
        try {
            Object instance = cls.getConstructor().newInstance();
            Field[] declaredFields = cls.getDeclaredFields();
            for (Field declaredField : declaredFields) {
                if (declaredField.isAnnotationPresent(Autowired.class)){
                    declaredField.setAccessible(true);
                    Class<?> type = declaredField.getType();
                    declaredField.set(instance, getBean(declaredField.getName()));
                }
            }
            return instance;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

}
