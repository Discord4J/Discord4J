package discord4j.rest.util;

import javax.annotation.Nullable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RouteUtils {

	private static final Pattern PARAMETER_PATTERN = Pattern.compile("\\{([\\w.]+)}");

	private RouteUtils() {
	}

	public static String expand(String template, Object... variables) {
		StringBuffer buf = new StringBuffer();
		Matcher matcher = PARAMETER_PATTERN.matcher(template);
		int index = 0;
		while (matcher.find()) {
			matcher.appendReplacement(buf, variables[index++].toString());
		}
		matcher.appendTail(buf);
		return buf.toString();
	}

	@Nullable
	public static String getMajorParam(String template, String complete) {
		int start = template.indexOf('{');
		if (start == -1) {
			return null;
		}

		int end = complete.indexOf('/', start);
		if (end == -1) {
			end = complete.length();
		}

		return complete.substring(start, end);
	}
}
