package org.test;

import org.test.annotation.*;
import org.test.utils.HelperUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@ComponentScan
public class CustomSpringApplication {
    private static Logger logger = Logger.getLogger(CustomSpringApplication.class.getName());

    public static void main(String[] args) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        CustomSpringApplication customSpringApplication = new CustomSpringApplication();
        Set<Class> classes = customSpringApplication.getAllClassesInPackage("org.test");
        logger.info("Clazzes in Current Package: " + classes);

        Map<String, Object> beans = new HashMap<>();
        List<Class> configurations = new ArrayList<>(); // Can have multiple Configurations.
        List<Class> components = new ArrayList<>();
        List<Process> processes = new ArrayList<>();

        // Priority @Configuration > @Component.
        for (Class clazz : classes) {
            if(clazz.getAnnotation(Configuration.class) != null){
                // Processed Class is a Configuration class.
                logger.info("@Configuration: " + clazz);
                configurations.add(clazz);
            }else if (clazz.getAnnotation(Component.class) != null){
                // Process classes with Components.
                logger.info("@Component: " + clazz);
               components.add(clazz);
            }
        }

        // Read all the beans in the configuration class.
        for (Class clazz : configurations) {
            Object configObj = clazz.getDeclaredConstructor().newInstance();
            Method[] methods =  clazz.getDeclaredMethods(); // Non static Method.
            // Parse the method.
            for (Method method : methods) {
                if(method.getAnnotation(Bean.class) != null){
                    if (method.getParameterCount() == 0){ // For Simplicity, assume 0.
                        Object bean = method.invoke(configObj);
                        // Stores a Bean of only one instance. New Pojo not created.
                        beans.putIfAbsent(bean.getClass().getName(), bean); // Add bean to HashSet.
                    }
                };
            }
        }

        // Process all @Component Annotations.
        for (Class clazz : components) {
            Constructor[] constructors = clazz.getDeclaredConstructors();
            if(constructors.length != 1){
                throw new RuntimeException("Can't have 0 or more than one constructor using @Component.");
            }
            if(constructors[0].getAnnotation(Autowired.class) == null){
                throw new RuntimeException("Can't find @Autowired annotation using @Component.");
            }

            Class[] constructorParameters =  constructors[0].getParameterTypes();

            Object[] parameters = new Object[constructorParameters.length];
            int ctr = -1;
            for(Class paramClazz : constructorParameters){
                if(!beans.containsKey(paramClazz.getName())){
                    throw new RuntimeException("Can't find bean with name " + paramClazz.getName());
                }
                parameters[++ctr] = beans.get(paramClazz.getName());
            }

            processes.add((Process) constructors[0].newInstance(parameters));
        }

        // Dependency Injection done and Dusted :)
        for (Process process : processes) {
            process.startProcess();
            process.stopProcess();
        }
    }

    private Set<Class> getAllClassesInPackage(String packageName) {
        logger.info("Scanning Package: " + packageName);
        InputStream inputStream = ClassLoader.getSystemClassLoader()
                .getResourceAsStream(packageName.replaceAll("[.]", "/"));

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        return reader.lines()
                .filter(line -> line.endsWith(".class"))
                .map(line -> HelperUtils.getClass(line, packageName))
                .collect(Collectors.toSet());
    }

}
