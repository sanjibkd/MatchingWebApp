package controllers.project;

import static play.data.Form.form;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.walmart.productgenome.matching.daos.ProjectDao;
import com.walmart.productgenome.matching.daos.RuleDao;
import com.walmart.productgenome.matching.daos.TableDao;
import com.walmart.productgenome.matching.models.data.Attribute;
import com.walmart.productgenome.matching.models.data.AttributePair;
import com.walmart.productgenome.matching.models.data.Project;
import com.walmart.productgenome.matching.models.data.Table;
import com.walmart.productgenome.matching.models.rules.Feature;
import com.walmart.productgenome.matching.service.ConfusionMatrix;
import com.walmart.productgenome.matching.service.FeatureService;
import com.walmart.productgenome.matching.service.FeatureStatistics;
import com.walmart.productgenome.matching.service.FunctionService;
import com.walmart.productgenome.matching.service.RuleService;
import com.walmart.productgenome.matching.service.TableService;
import com.walmart.productgenome.matching.service.RuleService.RuleBasedModel;

import play.Logger;
import play.data.DynamicForm;
import play.mvc.Call;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;
import play.twirl.api.Html;
import views.html.common.common_main;
import views.html.common.common_topnav;
import views.html.project.project_main_topbar;
import views.html.project.list_feature_stats;

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
	
	public static Result computeFeatureCosts(String projectName) {
		DynamicForm form = form().bindFromRequest();
		Logger.info("PARAMETERS : " + form.data().toString());
		String table1Name = form.get("table1_name");
		String table2Name = form.get("table2_name");
		String pairsTableName = form.get("tuple_pairs_table_name");
		String[] featureNames = request().body().asFormUrlEncoded().get("feature_names[]");

		Project project = ProjectDao.open(projectName);
		try {
			Table table1 = TableDao.open(projectName, table1Name);
			Table table2 = TableDao.open(projectName, table2Name);
			Table pairsTable = TableDao.open(projectName, pairsTableName);
			List<Feature> features = new ArrayList<Feature>();
			for (String f : featureNames) {
				features.add(project.findFeatureByName(f));
			}
			Map<String, FeatureStatistics> featureStatsMap = FeatureService.computeFeatureCosts(
					projectName, pairsTable, table1, table2, features);
			
			if (null != featureStatsMap && !featureStatsMap.isEmpty()) {
				long numTuplePairs = pairsTable.getSize();
				String statusMessage = "Successfully computed costs of " +
						featureStatsMap.size() + " features for " + numTuplePairs +
						" tuple pairs in table " + pairsTableName;
				return showFeatureStats(project, featureStatsMap, numTuplePairs,
						statusMessage);
			}
		}
		catch (IOException e) {
			ProjectController.statusMessage = "Error: " + e.getMessage();
		}
		catch (Exception e) {
			ProjectController.statusMessage = "Error: " + e.getMessage();
		}
		return redirect(controllers.project.routes.ProjectController.showProject(projectName));
	}
	
	private static Result showFeatureStats(Project project,
			Map<String, FeatureStatistics> featureStatsMap, long numTuplePairs,
			String statusMessage) {

		String pageTitle = "EMS: Feature Statistics";
		String projectName = project.getName();
		Html topBar = project_main_topbar.render(projectName);
		Html topNav = common_topnav.render(project);

		List<String> featureNames = new ArrayList<String>();
		List<Long> counts0 = new ArrayList<Long>();
		List<Long> counts1 = new ArrayList<Long>();
		List<Long> countsNull = new ArrayList<Long>();
		List<Long> bucketCounts1 = new ArrayList<Long>();
		List<Long> bucketCounts2 = new ArrayList<Long>();
		List<Long> bucketCounts3 = new ArrayList<Long>();
		List<Long> bucketCounts4 = new ArrayList<Long>();
		List<Long> bucketCounts5 = new ArrayList<Long>();
		List<Long> avgFeatureCostPerTuple = new ArrayList<Long>();
		List<Long> avgFeatureCostPerTupleExcludingNull = new ArrayList<Long>();
		
		for (String f : featureStatsMap.keySet()) {
			FeatureStatistics featureStats = featureStatsMap.get(f);
			featureNames.add(f);
			counts0.add(featureStats.getCount0());
			counts1.add(featureStats.getCount1());
			countsNull.add(featureStats.getCountNull());
			long[] bucketCounts = featureStats.getBucketCounts();
			bucketCounts1.add(bucketCounts[0]);
			bucketCounts2.add(bucketCounts[1]);
			bucketCounts3.add(bucketCounts[2]);
			bucketCounts4.add(bucketCounts[3]);
			bucketCounts5.add(bucketCounts[4]);
			avgFeatureCostPerTuple.add(featureStats.getAvgFeatureCostPerTuple()/1000);
			avgFeatureCostPerTupleExcludingNull.add(featureStats.getAvgFeatureCostPerTupleExcludingNull()/1000);
		}

		Html content = list_feature_stats.render(featureNames, counts0, counts1,
				countsNull, bucketCounts1, bucketCounts2, bucketCounts3,
				bucketCounts4, bucketCounts5, avgFeatureCostPerTuple,
				avgFeatureCostPerTupleExcludingNull, numTuplePairs,
				statusMessage);

		List<Call> dynamicJs = new ArrayList<Call>();
		dynamicJs.add(controllers.project.routes.ProjectController.javascriptRoutes());
		dynamicJs.add(controllers.routes.Assets.at("javascripts/project/project.js"));

		List<Call> dynamicCss = new ArrayList<Call>();
		dynamicCss.add(controllers.routes.Assets.at("stylesheets/project/project.css"));

		Html page = common_main.render(
				pageTitle, topBar, topNav, content, dynamicCss, dynamicJs);
		return ok(page);
	}
}
