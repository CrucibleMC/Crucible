package net.cubespace.Yamler.Config;

import net.cubespace.Yamler.Config.Converter.Converter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author geNAZt (fabian.fassbender42@googlemail.com)
 */
public class ConfigMapper extends BaseConfigMapper {
	public Map<String, Object> saveToMap(Class clazz) throws Exception {
		Map<String, Object> returnMap = new HashMap<>();

		if (!clazz.getSuperclass().equals(YamlConfig.class) && !clazz.getSuperclass().equals(Object.class)) {
			Map<String, Object> map = saveToMap(clazz.getSuperclass());
			for (Map.Entry<String, Object> entry : map.entrySet()) {
				returnMap.put(entry.getKey(), entry.getValue());
			}
		}

		for (Field field : clazz.getDeclaredFields()) {
			if (doSkip(field)) {
				continue;
			}

			String path = "";

			switch (CONFIG_MODE) {
				case PATH_BY_UNDERSCORE:
					path = field.getName().replace("_", ".");
					break;
				case FIELD_IS_KEY:
					path = field.getName();
					break;
				case DEFAULT:
				default:
					String fieldName = field.getName();
					if (fieldName.contains("_")) {
						path = field.getName().replace("_", ".");
					} else {
						path = field.getName();
					}
					break;
			}

			if (field.isAnnotationPresent(Path.class)) {
				Path path1 = field.getAnnotation(Path.class);
				path = path1.value();
			}

			if (Modifier.isPrivate(field.getModifiers())) {
				field.setAccessible(true);
			}

			try {
				returnMap.put(path, field.get(this));
			} catch (IllegalAccessException e) {
			}
		}

		Converter mapConverter = converter.getConverter(Map.class);
		return (Map<String, Object>) mapConverter.toConfig(HashMap.class, returnMap, null);
	}

	public void loadFromMap(Map section, Class clazz) throws Exception {
		if (!clazz.getSuperclass().equals(YamlConfig.class) && !clazz.getSuperclass().equals(YamlConfig.class)) {
			loadFromMap(section, clazz.getSuperclass());
		}

		for (Field field : clazz.getDeclaredFields()) {
			if (doSkip(field)) {
				continue;
			}

			String path = (CONFIG_MODE.equals(ConfigMode.PATH_BY_UNDERSCORE)) ? field.getName().replaceAll("_", ".") : field.getName();

			if (field.isAnnotationPresent(Path.class)) {
				Path path1 = field.getAnnotation(Path.class);
				path = path1.value();
			}

			if (Modifier.isPrivate(field.getModifiers())) {
				field.setAccessible(true);
			}

			converter.fromConfig((YamlConfig) this, field, ConfigSection.convertFromMap(section), path);
		}
	}

	public Map<String, Object> saveToMap(Class<?> clazz, YamlConfig parent) throws Exception {
		Map<String, Object> returnMap = new HashMap<>();

		if (!clazz.getSuperclass().equals(YamlConfig.class) && !clazz.getSuperclass().equals(Object.class)) {
			Map<String, Object> map = saveToMap(clazz.getSuperclass(), parent);
			for (Map.Entry<String, Object> entry : map.entrySet()) {
				returnMap.put(entry.getKey(), entry.getValue());
			}
		}

		for (Field field : clazz.getDeclaredFields()) {
			if (doSkip(field)) {
				continue;
			}

			String path = "";

			switch (CONFIG_MODE) {
				case PATH_BY_UNDERSCORE:
					path = field.getName().replace("_", ".");
					break;
				case FIELD_IS_KEY:
					path = field.getName();
					break;
				case DEFAULT:
				default:
					String fieldName = field.getName();
					if (fieldName.contains("_")) {
						path = field.getName().replace("_", ".");
					} else {
						path = field.getName();
					}
					break;
			}

			if (field.isAnnotationPresent(Path.class)) {
				Path path1 = field.getAnnotation(Path.class);
				path = path1.value();
			}

			ArrayList<String> comments = new ArrayList<>();
			for (Annotation annotation : field.getAnnotations()) {
				if (annotation instanceof Comment) {
					Comment comment = (Comment) annotation;
					comments.add(comment.value());

				}

				if (annotation instanceof Comments) {
					Comments comment = (Comments) annotation;
					comments.addAll(Arrays.asList(comment.value()));
				}
			}

			if (comments.size() > 0) {
				for (String comment : comments) {
					parent.addComment(path, comment);
				}
			}

			if (Modifier.isPrivate(field.getModifiers())) {
				field.setAccessible(true);
			}

			try {
				returnMap.put(path, field.get(this));
			} catch (IllegalAccessException e) {
			}
		}

		Converter mapConverter = converter.getConverter(Map.class);
		return (Map<String, Object>) mapConverter.toConfig(HashMap.class, returnMap, null);
	}
}
