package ru.otus.appcontainer;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.Collectors;
import ru.otus.appcontainer.api.AppComponent;
import ru.otus.appcontainer.api.AppComponentsContainer;
import ru.otus.appcontainer.api.AppComponentsContainerConfig;

@SuppressWarnings("squid:S1068")
public class AppComponentsContainerImpl implements AppComponentsContainer {

    private final List<Object> appComponents = new ArrayList<>();
    private final Map<String, Object> appComponentsByName = new HashMap<>();

    private final Map<String, Integer> orderNames = new HashMap<>();

    public AppComponentsContainerImpl(Class<?> initialConfigClass) {
        processConfig(initialConfigClass);
    }

    private void processConfig(Class<?> configClass) {
        checkConfigClass(configClass);
        Method[] methods = configClass.getDeclaredMethods();
        List<Method> methodsWithoutParams = new ArrayList<>();

        try {
            Object configInstance = configClass.getDeclaredConstructor().newInstance();

            for (Method method : methods) {
                int orderM = method.getAnnotation(AppComponent.class).order();
                String name = method.getAnnotation(AppComponent.class).name();
                orderNames.put(name, orderM);
            }

            List<Map.Entry<String, Integer>> orderNamesSorted = orderNames.entrySet().stream()
                    .sorted(Map.Entry.comparingByValue())
                    .collect(Collectors.toList());

            System.out.println("orderNamesSorted = " + orderNamesSorted);

            for (int i = 0; i < orderNamesSorted.size(); i++) {
                String nameMethod = orderNamesSorted.get(i).getKey();
                Method m = Arrays.stream(methods)
                        .filter(method -> method.getName().equals(nameMethod))
                        .findFirst()
                        .get();

                Parameter[] parameters = m.getParameters();
                Object result;
                if (parameters.length == 0) {
                    result = m.invoke(configInstance);
                } else {
                    Object[] args = new Object[parameters.length];
                    for (int j = 0; j < parameters.length; j++) {
                        Parameter parameter = parameters[j];
                        String parName = parameter.getName();
                        args[j] = getAppComponent(parName);
                    }
                    result = m.invoke(configInstance, args);
                }

                appComponents.add(result);
                appComponentsByName.put(nameMethod, result);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void checkConfigClass(Class<?> configClass) {
        if (!configClass.isAnnotationPresent(AppComponentsContainerConfig.class)) {
            throw new IllegalArgumentException(String.format("Given class is not config %s", configClass.getName()));
        }
    }

    @Override
    public <C> C getAppComponent(Class<C> componentClass) {
        for (Object component : appComponentsByName.values()) {
            if (componentClass.isAssignableFrom(component.getClass())) {
                return (C) component;
            }
        }
        throw new RuntimeException("Component not found for type: " + componentClass);
    }

    @Override
    public <C> C getAppComponent(String componentName) {
        Object component = appComponentsByName.get(componentName);
        return (C) component;
    }
}
