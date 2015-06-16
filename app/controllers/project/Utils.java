package controllers.project;

import java.util.ArrayList;
import java.util.List;

import play.data.DynamicForm;

public class Utils {

	public static List<String> getFormArrayParameters(DynamicForm form,
			String param, int start) {
		List<String> vals = new ArrayList<String>();
		String val;
		while((val = form.get(param + start)) != null) {
			vals.add(val);
			start++;
		}
		return vals;
	}
}
