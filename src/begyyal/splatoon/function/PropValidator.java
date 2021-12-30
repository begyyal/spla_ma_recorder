package begyyal.splatoon.function;

import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.google.common.collect.Maps;

import begyyal.commons.constant.Strs;

public class PropValidator {

    private static final Map<String, Predicate<String>> ruleMap = Maps.newHashMap();
    {
	ruleMap.put("tablePath", v -> {
	    try {
		Paths.get(v);
	    } catch (InvalidPathException e) {
		return false;
	    }
	    return true;
	});
	ruleMap.put("term", v -> StringUtils.isNotBlank(v) &&
		Arrays.stream(v.split(Strs.comma)).allMatch(NumberUtils::isCreatable));
	ruleMap.put("windowHeight", NumberUtils::isCreatable);
	ruleMap.put("windowWidth", NumberUtils::isCreatable);
    }

    private PropValidator() {
    }

    public static boolean exec() {
	var res = ResourceBundle.getBundle("common");
	for (var e : ruleMap.entrySet()) {
	    var v = res.getString(e.getKey());
	    if (!e.getValue().test(v)) {
		System.out.println(
		    "[ERROR] Properties validation detects invalid format. -> " + e.getKey());
		return false;
	    }
	}
	return true;
    }
}
