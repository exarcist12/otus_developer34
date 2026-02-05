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

    private final Map<Class<?>, List<Object>> appComponentsByType = new HashMap<>();

    public AppComponentsContainerImpl(Class<?> initialConfigClass) {
        processConfig(initialConfigClass);
    }

    public AppComponentsContainerImpl(Class<?>... initialConfigClasses) {
        processConfig(initialConfigClasses);
    }

    private void processConfig(Class<?> configClass) {
        checkConfigClass(configClass);
        Method[] methods = configClass.getDeclaredMethods();
        List<Method> methodsWithoutParams = new ArrayList<>();

        try {
            Object configInstance = configClass.getDeclaredConstructor().newInstance();
            Set<String> usedNames = new HashSet<>();
            for (Method method : methods) {
                if (method.isAnnotationPresent(AppComponent.class)) {
                    String name = method.getAnnotation(AppComponent.class).name();
                    if (usedNames.contains(name)) {
                        throw new IllegalStateException();
                    }
                    usedNames.add(name);
                    int orderM = method.getAnnotation(AppComponent.class).order();
                    orderNames.put(name, orderM);
                }
            }

            List<Map.Entry<String, Integer>> orderNamesSorted = orderNames.entrySet().stream()
                    .sorted(Map.Entry.comparingByValue())
                    .collect(Collectors.toList());

            System.out.println("orderNamesSorted = " + orderNamesSorted);

            for (int i = 0; i < orderNamesSorted.size(); i++) {
                String nameMethod = orderNamesSorted.get(i).getKey();

                Method m = Arrays.stream(methods)
                        .filter(method -> {
                            AppComponent annotation = method.getAnnotation(AppComponent.class);
                            return annotation != null && annotation.name().equals(nameMethod);
                        })
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
                        var paramType = parameter.getType();
                        args[j] = getAppComponent(paramType);
                    }
                    result = m.invoke(configInstance, args);
                }

                appComponents.add(result);
                appComponentsByName.put(nameMethod, result);
                var componentType = m.getReturnType();
                appComponentsByType
                        .computeIfAbsent(componentType, k -> new ArrayList<>())
                        .add(result);
                for (var iface : componentType.getInterfaces()) {
                    appComponentsByType
                            .computeIfAbsent(iface, k -> new ArrayList<>())
                            .add(result);
                }
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void processConfig(Class<?>... configClasses) {
        for (Class<?> configClass : configClasses) {
            checkConfigClass(configClass);
        }

        try {
            for (Class<?> configClass : configClasses) {
                Method[] methods = configClass.getDeclaredMethods();
                Set<String> usedNames = new HashSet<>();

                for (Method method : methods) {
                    if (method.isAnnotationPresent(AppComponent.class)) {
                        String name = method.getAnnotation(AppComponent.class).name();
                        if (usedNames.contains(name)) {
                            throw new IllegalStateException();
                        }
                        usedNames.add(name);
                        int orderM = method.getAnnotation(AppComponent.class).order();
                        orderNames.put(name, orderM);
                    }
                }
            }

            List<Map.Entry<String, Integer>> orderNamesSorted = orderNames.entrySet().stream()
                    .sorted(Map.Entry.comparingByValue())
                    .collect(Collectors.toList());

            System.out.println("orderNamesSorted = " + orderNamesSorted);

            for (int i = 0; i < orderNamesSorted.size(); i++) {
                String nameMethod = orderNamesSorted.get(i).getKey();

                Method m = null;
                Object configInstanceForMethod = null;

                for (Class<?> configClass : configClasses) {
                    Method[] methods = configClass.getDeclaredMethods();
                    Optional<Method> foundMethod = Arrays.stream(methods)
                            .filter(method -> {
                                AppComponent annotation = method.getAnnotation(AppComponent.class);
                                return annotation != null && annotation.name().equals(nameMethod);
                            })
                            .findFirst();

                    if (foundMethod.isPresent()) {
                        m = foundMethod.get();
                        configInstanceForMethod =
                                configClass.getDeclaredConstructor().newInstance();
                        break;
                    }
                }

                if (m == null) {
                    throw new RuntimeException("Method not found for component: " + nameMethod);
                }

                Parameter[] parameters = m.getParameters();
                Object result;
                if (parameters.length == 0) {
                    result = m.invoke(configInstanceForMethod);
                } else {
                    Object[] args = new Object[parameters.length];
                    for (int j = 0; j < parameters.length; j++) {
                        Parameter parameter = parameters[j];
                        var paramType = parameter.getType();
                        args[j] = getAppComponent(paramType);
                    }
                    result = m.invoke(configInstanceForMethod, args);
                }

                appComponents.add(result);
                appComponentsByName.put(nameMethod, result);
                var componentType = m.getReturnType();
                appComponentsByType
                        .computeIfAbsent(componentType, k -> new ArrayList<>())
                        .add(result);
                for (var iface : componentType.getInterfaces()) {
                    appComponentsByType
                            .computeIfAbsent(iface, k -> new ArrayList<>())
                            .add(result);
                }
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void checkConfigClass(Class<?> configClass) {
        if (!configClass.isAnnotationPresent(AppComponentsContainerConfig.class)) {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public <C> C getAppComponent(Class<C> componentClass) {
        List<Object> foundComponents = new ArrayList<>();

        for (Object component : appComponentsByName.values()) {
            if (componentClass.isAssignableFrom(component.getClass())) {
                foundComponents.add(component);
            }
        }

        if (foundComponents.isEmpty() || foundComponents.size() > 1) {
            throw new RuntimeException();
        }

        return (C) foundComponents.get(0);
    }

    @Override
    public <C> C getAppComponent(String componentName) {
        Object component = appComponentsByName.get(componentName);
        if (component == null) {
            throw new RuntimeException();
        }
        return (C) component;
    }
}
