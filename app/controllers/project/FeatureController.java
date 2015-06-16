package controllers.project;

import static play.data.Form.form;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import com.walmart.productgenome.matching.daos.ProjectDao;
import com.walmart.productgenome.matching.daos.RuleDao;
import com.walmart.productgenome.matching.daos.TableDao;
import com.walmart.productgenome.matching.models.data.Attribute;
import com.walmart.productgenome.matching.models.data.AttributePair;
import com.walmart.productgenome.matching.models.data.Project;
import com.walmart.productgenome.matching.models.data.Table;
import com.walmart.productgenome.matching.models.rules.Feature;
import com.walmart.productgenome.matching.service.FeatureService;
import com.walmart.productgenome.matching.service.FunctionService;

import play.Logger;
import play.data.DynamicForm;
import play.mvc.Controller;
import play.mvc.Result;

public class FeatureController extends Controller {
	
	public static Result addFeature(String name){
		DynamicForm form = form().bindFromRequest();
		Logger.info("PARAMETERS : " + form.data().toString());
		String featureName = form.get("feature_name");
		String functionName = form.get("function_name");
		String table1Name = form.get("table1_name");
		String table2Name = form.get("table2_name");
		String attr1Name = form.get("attr1_name");
		String attr2Name = form.get("attr2_name");

		boolean saveToDisk = false;
		if(null != form.get("save_to_disk")){
			saveToDisk = true;
		}

		try{
			RuleDao.addFeature(name, featureName, functionName, table1Name,
					table2Name, attr1Name, attr2Name, saveToDisk);
			ProjectController.statusMessage = "Successfully added Feature " + featureName + ".";
		}
		catch (IOException e) {
			flash("error", e.getMessage());
			ProjectController.statusMessage = "Error: " + e.getMessage();
		}

		return redirect(controllers.project.routes.ProjectController.showProject(name));
	}
	
	public static Result autoGenerateFeatures(String projectName) {
		DynamicForm form = form().bindFromRequest();
		Logger.info("PARAMETERS : " + form.data().toString());
		String table1Name = form.get("table1_name");
		String table2Name = form.get("table2_name");
		List<String> attribute1Names = Utils.getFormArrayParameters(form, "attr1_", 1);
		List<String> attribute2Names = Utils.getFormArrayParameters(form, "attr2_", 1);
		
		boolean saveToDisk = false;
		if(null != form.get("save_to_disk")){
			saveToDisk = true;
		}

		try{
			Project project = ProjectDao.open(projectName);
			Logger.info("Successfully opened project " + projectName);
			Table table1 = TableDao.open(projectName, table1Name);
			Logger.info("Successfully opened table " + table1Name);
			Table table2 = TableDao.open(projectName, table2Name);
			Logger.info("Successfully opened table " + table2Name);
			if (attribute1Names.size() != attribute2Names.size()) {
				throw new IllegalArgumentException("[EMS] Attribute pairings not proper");
			}
			List<AttributePair> attributePairs = new ArrayList<AttributePair>();
			for (int i = 0; i < attribute1Names.size(); i++) {
				String attr1Name = attribute1Names.get(i);
				Attribute attribute1 = table1.getAttributeByName(attr1Name);
				Logger.info("Successfully got attribute " + attr1Name +
						" from table " + table1Name);
				String attr2Name = attribute2Names.get(i);
				Attribute attribute2 = table2.getAttributeByName(attr2Name);
				Logger.info("Successfully got attribute " + attr2Name +
						" from table " + table2Name);
				attributePairs.add(new AttributePair(attribute1, attribute2));
			}
			
			List<Feature> features = FeatureService.recommendFeatures(project,
					table1, table2, attributePairs);
			FeatureService.addFeatures(project, features, saveToDisk);
			ProjectController.statusMessage = "Successfully generated " +
					features.size() + " features.\n";
			for (Feature f : features) {
				ProjectController.statusMessage += f.getName() + "\n"; 
			}	
		}
		catch (IOException e) {
			ProjectController.statusMessage = "Error: " + e.getMessage();
		}
		catch (Exception e) {
			ProjectController.statusMessage = "Error: " + e.toString();
		}
		return redirect(controllers.project.routes.ProjectController.showProject(projectName));
	}
	
	public static Result editFeatureUsingGui(String projectName) {
		DynamicForm form = form().bindFromRequest();
		Logger.info("PARAMETERS : " + form.data().toString());
		String featureName = form.get("feature_name");
		String functionName = form.get("function_name");
		String table1Name = form.get("table1_name");
		String table2Name = form.get("table2_name");
		String attr1Name = form.get("attr1_name");
		String attr2Name = form.get("attr2_name");

		boolean saveToDisk = false;
		if(null != form.get("save_to_disk")){
			saveToDisk = true;
		}

		try{
			Project project = ProjectDao.open(projectName);
			FeatureService.editFeatureUsingGui(project, featureName, functionName,
					table1Name, attr1Name, table2Name, attr2Name, saveToDisk);
			ProjectController.statusMessage = "Successfully edited Feature " +
					featureName + ".";
		}
		catch (IOException e) {
			ProjectController.statusMessage = "Error: " + e.getMessage();
		}
		catch (Exception e) {
			ProjectController.statusMessage = "Error: " + e.getMessage();
		}
		return redirect(controllers.project.routes.ProjectController.showProject(projectName));
	}
	
	public static Result deleteFeature(String projectName) {
		DynamicForm form = form().bindFromRequest();
		Logger.info("PARAMETERS : " + form.data().toString());
		String featureName = form.get("feature_name");

		try {
			FeatureService.deleteFeature(projectName, featureName);
			ProjectController.statusMessage = "Successfully deleted Feature " +
					featureName + ".";
		}
		catch (IOException e) {
			ProjectController.statusMessage = "Error: " + e.getMessage();
		}
		return redirect(controllers.project.routes.ProjectController.showProject(projectName));
	}
	
}
