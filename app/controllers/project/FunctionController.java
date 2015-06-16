package controllers.project;

import static play.data.Form.form;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import com.walmart.productgenome.matching.service.FunctionService;

import play.Logger;
import play.data.DynamicForm;
import play.mvc.Controller;
import play.mvc.Result;

public class FunctionController extends Controller {

	public static Result deleteFunction(String projectName){
		DynamicForm form = form().bindFromRequest();
		Logger.info("PARAMETERS : " + form.data().toString());
		String functionName = form.get("function_name");

		try {
			FunctionService.deleteFunction(projectName, functionName);
			ProjectController.statusMessage = "Successfully deleted Function " +
					functionName + ".";
		}
		catch (IOException e) {
			ProjectController.statusMessage = "Error: " + e.getMessage();
		}
		catch (SecurityException e) {
			ProjectController.statusMessage = "Error: " + e.getMessage();
		}
		catch (IllegalArgumentException e) {
			ProjectController.statusMessage = "Error: " + e.getMessage();
		}
		catch (ClassNotFoundException e) {
			ProjectController.statusMessage = "Error: " + e.getMessage();
		}
		catch (NoSuchMethodException e) {
			ProjectController.statusMessage = "Error: " + e.getMessage();
		}
		catch (InstantiationException e) {
			ProjectController.statusMessage = "Error: " + e.getMessage();
		}
		catch (IllegalAccessException e) {
			ProjectController.statusMessage = "Error: " + e.getMessage();
		}
		catch (InvocationTargetException e) {
			ProjectController.statusMessage = "Error: " + e.getMessage();
		}
		return redirect(controllers.project.routes.ProjectController.showProject(projectName));
	}

}
