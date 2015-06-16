package controllers.project;

import static play.data.Form.form;

import java.io.IOException;
import java.util.List;

import com.walmart.productgenome.matching.daos.ProjectDao;
import com.walmart.productgenome.matching.daos.RuleDao;
import com.walmart.productgenome.matching.models.data.Project;

import play.Logger;
import play.data.DynamicForm;
import play.mvc.Controller;
import play.mvc.Result;

import com.walmart.productgenome.matching.service.MatcherService;
import com.walmart.productgenome.matching.service.RuleService;

public class MatcherController extends Controller {

	public static Result addMatcher(String projectName) {
		DynamicForm form = form().bindFromRequest();
		Logger.info("PARAMETERS : " + form.data().toString());
		String matcherName = form.get("matcher_name");
		String table1Name = form.get("table1_name");
		String table2Name = form.get("table2_name");
		
		List<String> ruleNames = Utils.getFormArrayParameters(form,
				"rule_name_", 1);
				
		boolean saveToDisk = false;
		if (null != form.get("save_to_disk")) {
			saveToDisk = true;
		}

		try{
			Project project = ProjectDao.open(projectName);
			MatcherService.addMatcher(project,
					matcherName, ruleNames, saveToDisk);
			ProjectController.statusMessage = "Successfully added Matcher " +
					matcherName + ".";
		}
		catch (IOException e) {
			ProjectController.statusMessage = "Error: " + e.getMessage();
		}

		return redirect(controllers.project.routes.ProjectController.showProject(projectName));
	}
	
	public static Result editMatcherUsingGui(String projectName) {
		DynamicForm form = form().bindFromRequest();
		Logger.info("PARAMETERS : " + form.data().toString());
		String matcherName = form.get("matcher_name");
		List<String> ruleNames = Utils.getFormArrayParameters(form,
				"rule_name_", 1);

		boolean saveToDisk = false;
		if(null != form.get("save_to_disk")){
			saveToDisk = true;
		}

		try{
			Project project = ProjectDao.open(projectName);
			MatcherService.editMatcherUsingGui(project, matcherName, ruleNames,
					saveToDisk);
			ProjectController.statusMessage = "Successfully edited Matcher " +
					matcherName + ".";
		}
		catch (IOException e) {
			ProjectController.statusMessage = "Error: " + e.getMessage();
		}

		return redirect(controllers.project.routes.ProjectController.showProject(projectName));
	}
	
	public static Result deleteMatcher(String projectName) {
		DynamicForm form = form().bindFromRequest();
		Logger.info("PARAMETERS : " + form.data().toString());
		String matcherName = form.get("matcher_name");

		try {
			MatcherService.deleteMatcher(projectName, matcherName);
			ProjectController.statusMessage = "Successfully deleted Matcher " +
					matcherName + ".";
		}
		catch (IOException e) {
			ProjectController.statusMessage = "Error: " + e.getMessage();
		}
		return redirect(controllers.project.routes.ProjectController.showProject(projectName));
	}
}
