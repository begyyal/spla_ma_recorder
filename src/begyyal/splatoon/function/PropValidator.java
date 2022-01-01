package begyyal.splatoon.function;

import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Maps;

import begyyal.commons.constant.Strs;

public class PropValidator {

    private static final Map<String, Predicate<String>> ruleMap = Maps.newHashMap();
    {
	ruleMap.put("tablePath", PropValidator::strIsValidPath);
	ruleMap.put("term", v -> StringUtils.isNotBlank(v) &&
		Arrays.stream(v.split(Strs.comma)).allMatch(PropValidator::strIsPositiveInt));
	ruleMap.put("pollingIntervalSec", PropValidator::strIsPositiveInt);
	ruleMap.put("windowHeight", PropValidator::strIsPositiveInt);
	ruleMap.put("windowWidth", PropValidator::strIsPositiveInt);
    }

    private PropValidator() {
    }

    private static boolean strIsValidPath(String str) {
	try {
	    Paths.get(str);
	} catch (InvalidPathException e) {
	    return false;
	}
	return true;
    }

    private static boolean strIsPositiveInt(String str) {
	int num = 0;
	try {
	    num = Integer.parseInt(str);
	} catch (NumberFormatException | NullPointerException e) {
	    return false;
	}
	return num > 0;
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
