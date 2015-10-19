package controllers.project;

import static play.data.Form.form;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.walmart.productgenome.matching.daos.ProjectDao;
import com.walmart.productgenome.matching.daos.RuleDao;
import com.walmart.productgenome.matching.daos.TableDao;
import com.walmart.productgenome.matching.models.data.Project;
import com.walmart.productgenome.matching.models.data.Table;
import com.walmart.productgenome.matching.models.rules.Feature;

import play.Logger;
import play.data.DynamicForm;
import play.mvc.Call;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;
import play.twirl.api.Html;
import views.html.common.common_main;
import views.html.common.common_topnav;
import views.html.project.list_rules;
import views.html.project.project_main_topbar;
import views.html.project.list_evaluated_models;

import com.walmart.productgenome.matching.service.ConfusionMatrix;
import com.walmart.productgenome.matching.service.FeatureService;
import com.walmart.productgenome.matching.service.MatcherService;
import com.walmart.productgenome.matching.service.RuleService;
import com.walmart.productgenome.matching.service.TableService;
import com.walmart.productgenome.matching.service.RuleService.RuleBasedModel;

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

	public static Result compareModelsUsingCV(String projectName) {
		DynamicForm form = form().bindFromRequest();
		Logger.info("PARAMETERS : " + form.data().toString());
		String table1Name = form.get("table1_name");
		String table2Name = form.get("table2_name");
		String trainsetName = form.get("trainset_name");
		String[] featureNames = request().body().asFormUrlEncoded().get("feature_names[]");
		String[] modelNames = request().body().asFormUrlEncoded().get("model_names[]");
		
		int numRuns = Integer.parseInt(form.get("num_runs"));
		int numFolds = Integer.parseInt(form.get("num_folds"));
		double matchingThreshold = Double.parseDouble(form.get("matching_threshold"));
		
		Project project = ProjectDao.open(projectName);
		List<Feature> features = new ArrayList<Feature>();
		for (String f : featureNames) {
			features.add(project.findFeatureByName(f));
		}
		int numFeatures = features.size();
		
		String statusMessage = "";
		Map<String, ConfusionMatrix> modelEvaluations = new HashMap<String, ConfusionMatrix>();
		try {
			Table table1 = TableDao.open(projectName, table1Name);
			Table table2 = TableDao.open(projectName, table2Name);
			Table trainPairsTable = TableDao.open(projectName, trainsetName);
			String trainFeaturesTableName = trainsetName + "_features";
			Table trainFeaturesTable = FeatureService.generateFeatures(trainFeaturesTableName,
					projectName, trainPairsTable, table1, table2, features, true);
			/*
			modelEvaluations = MatcherService.evaluateModelsUsingCV(trainFeaturesTable,
					modelNames, numFolds);
			*/
			modelEvaluations = MatcherService.evaluateModelsUsingCVWithMatchingThreshold(trainFeaturesTable,
					modelNames, numRuns, numFolds, matchingThreshold);
			if (modelEvaluations != null && !modelEvaluations.isEmpty()) {
				int numModelsEvaluated = modelEvaluations.size();
				if (numModelsEvaluated == 1) {
					statusMessage = "Successfully evaluated 1 model using "
							+ numFolds + "-fold cross validation\n";
				}
				else {
					statusMessage = "Successfully evaluated " + numModelsEvaluated +
							" models using " + numFolds + "-fold cross validation\n";
				}
			}
		}
		catch (IOException e) {
			ProjectController.statusMessage = "Error: " + e.getMessage();
		}
		catch (Exception e) {
			ProjectController.statusMessage = "Error: " + e.getMessage();
		}
		return showEvaluatedModels(project, modelEvaluations,
				numFeatures,
				statusMessage);
	}

	public static Result compareModelsUsingTrainTest(String projectName) {
		DynamicForm form = form().bindFromRequest();
		Logger.info("PARAMETERS : " + form.data().toString());
		String table1Name = form.get("table1_name");
		String table2Name = form.get("table2_name");
		String trainsetName = form.get("trainset_name");
		String testsetName = form.get("testset_name");
		String[] featureNames = request().body().asFormUrlEncoded().get("feature_names[]");
		String[] modelNames = request().body().asFormUrlEncoded().get("model_names[]");

		Project project = ProjectDao.open(projectName);
		List<Feature> features = new ArrayList<Feature>();
		for (String f : featureNames) {
			features.add(project.findFeatureByName(f));
		}
		int numFeatures = features.size();
		
		String statusMessage = "";
		Map<String, ConfusionMatrix> modelEvaluations = new HashMap<String, ConfusionMatrix>();
		try {
			Table table1 = TableDao.open(projectName, table1Name);
			Table table2 = TableDao.open(projectName, table2Name);
			Table trainPairsTable = TableDao.open(projectName, trainsetName);
			Table testPairsTable = TableDao.open(projectName, testsetName);
			
			String trainFeaturesTableName = trainsetName + "_features";
			String testFeaturesTableName = testsetName + "_features";
			Table trainFeaturesTable = FeatureService.generateFeatures(trainFeaturesTableName,
					projectName, trainPairsTable, table1, table2, features, true);
			Table testFeaturesTable = FeatureService.generateFeatures(testFeaturesTableName,
					projectName, testPairsTable, table1, table2, features, true);
			modelEvaluations = MatcherService.evaluateModelsUsingTrainTest(trainFeaturesTable,
					testFeaturesTable, modelNames);

			if (modelEvaluations != null && !modelEvaluations.isEmpty()) {
				int numModelsEvaluated = modelEvaluations.size();
				if (numModelsEvaluated == 1) {
					statusMessage = "Successfully evaluated 1 model using "
							+ "train-test evaluation\n";
				}
				else {
					statusMessage = "Successfully evaluated " + numModelsEvaluated +
							" models using train-test evaluation\n";
				}
			}
		}
		catch (IOException e) {
			ProjectController.statusMessage = "Error: " + e.getMessage();
		}
		catch (Exception e) {
			ProjectController.statusMessage = "Error: " + e.getMessage();
		}
		return showEvaluatedModels(project, modelEvaluations,
				numFeatures,
				statusMessage);
	}
	
	private static Result showEvaluatedModels(Project project,
			Map<String, ConfusionMatrix> modelEvaluations, int numFeatures,
			String statusMessage) {

		String pageTitle = "EMS: Evaluated Models";
		String projectName = project.getName();
		Html topBar = project_main_topbar.render(projectName);
		Html topNav = common_topnav.render(project);

		List<String> modelNames = new ArrayList<String>();
		List<Double> precisions = new ArrayList<Double>();
		List<Double> recalls = new ArrayList<Double>();
		List<Double> f1s = new ArrayList<Double>();
		List<Double> accuracies = new ArrayList<Double>();
		List<Long> tps = new ArrayList<Long>();
		List<Long> fps = new ArrayList<Long>();
		List<Long> tns = new ArrayList<Long>();
		List<Long> fns = new ArrayList<Long>();
		List<Double> fprs = new ArrayList<Double>();
		List<Double> fnrs = new ArrayList<Double>();
		List<Double> times = new ArrayList<Double>();
		List<String> modelOptions = new ArrayList<String>();
		List<String> modelInfos = new ArrayList<String>();
		long numTrainInstances = 0;
		long numMatches = 0;
		if (modelEvaluations != null) {
			for (String m : modelEvaluations.keySet()) {
				ConfusionMatrix confusionMatrix = modelEvaluations.get(m);
				Logger.info("Model: " + m + ",  " + confusionMatrix);

				long truePositives = confusionMatrix.getTruePositives();
				long falsePositives = confusionMatrix.getFalsePositives();
				long falseNegatives = confusionMatrix.getFalseNegatives();
				long trueNegatives = confusionMatrix.getTrueNegatives();
				
				numTrainInstances = truePositives + falsePositives + trueNegatives
						+ falseNegatives;
				numMatches = truePositives + falseNegatives;
				
				double precision = 100.0 * truePositives / (truePositives + falsePositives);
				double recall = 100.0 * truePositives / (truePositives + falseNegatives);
				double f1 = (2 * precision * recall) / (precision + recall);
				double accuracy = 100.0 * (truePositives + trueNegatives) /
						(truePositives + trueNegatives + falsePositives + falseNegatives);

				double fpr = 100.0 * falsePositives / (falsePositives + trueNegatives);
				double fnr = 100.0 * falseNegatives / (truePositives + falseNegatives);
				
				double timeSec = confusionMatrix.getTimeMillis() / 1000.0 ;
				
				modelNames.add(m);
				
				DecimalFormat df = new DecimalFormat("#.##");
				if (Double.isNaN(precision)) {
					precision = -1.0;	
				}
				precisions.add(Double.valueOf(df.format(precision)));
				
				if (Double.isNaN(recall)) {
					recall = -1.0;	
				}
				recalls.add(Double.valueOf(df.format(recall)));
				if (Double.isNaN(f1)) {
					f1 = -1.0;	
				}
				f1s.add(Double.valueOf(df.format(f1)));
				accuracies.add(Double.valueOf(df.format(accuracy)));
				tps.add(truePositives);
				fps.add(falsePositives);
				tns.add(trueNegatives);
				fns.add(falseNegatives);
				fprs.add(Double.valueOf(df.format(fpr)));
				fnrs.add(Double.valueOf(df.format(fnr)));
				times.add(Double.valueOf(df.format(timeSec)));
				modelOptions.add(confusionMatrix.getModelOptions());
				modelInfos.add(confusionMatrix.getModelInfo());
			}
		}
		
		Html content = list_evaluated_models.render(modelNames, precisions,
				recalls, f1s, accuracies, tps, fps, tns, fns, fprs, fnrs,
				times, modelOptions, numTrainInstances, numMatches,
				(numTrainInstances - numMatches), numFeatures,
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
