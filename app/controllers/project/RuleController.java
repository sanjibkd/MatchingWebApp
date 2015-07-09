package controllers.project;

import static play.data.Form.form;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import play.Logger;
import play.data.DynamicForm;
import play.libs.Json;
import play.mvc.Call;
import play.mvc.Controller;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Result;
import play.mvc.Results;
import play.twirl.api.Html;
import views.html.browser.browser_topbar;
import views.html.common.common_main;
import views.html.common.common_topnav;
import views.html.facets_layout.facets_layout;
import views.html.project.project_main;
import views.html.project.project_main_topbar;
import views.html.project.list_rules;
import views.html.project.show_pairs;
import views.html.project.list_models;
import weka.classifiers.trees.RandomForest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.walmart.productgenome.matching.daos.MatchingDao;
import com.walmart.productgenome.matching.daos.ProjectDao;
import com.walmart.productgenome.matching.daos.RuleDao;
import com.walmart.productgenome.matching.daos.TableDao;
import com.walmart.productgenome.matching.models.DefaultType;
import com.walmart.productgenome.matching.models.audit.ItemPairAudit;
import com.walmart.productgenome.matching.models.audit.MatchStatus;
import com.walmart.productgenome.matching.models.data.Attribute;
import com.walmart.productgenome.matching.models.data.Project;
import com.walmart.productgenome.matching.models.data.Table;
import com.walmart.productgenome.matching.models.data.Tuple;
import com.walmart.productgenome.matching.models.rules.Feature;
import com.walmart.productgenome.matching.models.rules.Matcher;
import com.walmart.productgenome.matching.models.rules.Rule;
import com.walmart.productgenome.matching.models.rules.Term;
import com.walmart.productgenome.matching.models.rules.functions.Function;
import com.walmart.productgenome.matching.service.ActiveLearner;
import com.walmart.productgenome.matching.service.FeatureService;
import com.walmart.productgenome.matching.service.RuleService;
import com.walmart.productgenome.matching.service.ConfusionMatrix;
import com.walmart.productgenome.matching.service.RuleService.RuleBasedModel;
import com.walmart.productgenome.matching.service.TableService;
import com.walmart.productgenome.matching.service.debug.Debug;
import com.walmart.productgenome.matching.service.debug.MatchingSummary;
import com.walmart.productgenome.matching.service.explorer.AttrStats;
import com.walmart.productgenome.matching.service.explorer.ExplorerDriver;
import com.walmart.productgenome.matching.service.recommender.FunctionRecommender;

public class RuleController extends Controller {

	public static final int TOP_F1_RULES_COUNT = 30;
	
	private static ActiveLearner al;
	private static Table labeledPairs;
	private static List<Object> pairIds;
	private static Table unlabeledPairsTable;
	private static int numIterations = 0;
	private static Table evalPairs;
	
	public static Result addFunction(String name){
		DynamicForm form = form().bindFromRequest();
		Logger.info("PARAMETERS : " + form.data().toString());
		String functionName = form.get("function_name");
		String functionDesc = form.get("function_desc");
		String functionClass = form.get("function_class");
		boolean saveToDisk = false;
		if(null != form.get("save_to_disk")){
			saveToDisk = true;
		}
		try{
			RuleDao.addFunction(name, functionName, functionDesc, functionClass,
					saveToDisk);
			ProjectController.statusMessage = "Successfully added Function " +
					functionName + ".";
		}
		catch (IOException e) {
			flash("error", e.getMessage());
			ProjectController.statusMessage = "Error: " + e.getMessage();
		}
		catch (SecurityException e) {
			flash("error", e.getMessage());
			ProjectController.statusMessage = "Error: " + e.getMessage();
		} catch (IllegalArgumentException e) {
			flash("error", e.getMessage());
			ProjectController.statusMessage = "Error: " + e.getMessage();
		} catch (ClassNotFoundException e) {
			flash("error", e.getMessage());
			ProjectController.statusMessage = "Error: " + e.getMessage();
		} catch (NoSuchMethodException e) {
			flash("error", e.getMessage());
			ProjectController.statusMessage = "Error: " + e.getMessage();
		} catch (InstantiationException e) {
			flash("error", e.getMessage());
			ProjectController.statusMessage = "Error: " + e.getMessage();
		} catch (IllegalAccessException e) {
			flash("error", e.getMessage());
			ProjectController.statusMessage = "Error: " + e.getMessage();
		} catch (InvocationTargetException e) {
			flash("error", e.getMessage());
			ProjectController.statusMessage = "Error: " + e.getMessage();
		}
		return redirect(controllers.project.routes.ProjectController.showProject(name));
	}

	public static Result importFunctions(String name){
		DynamicForm form = form().bindFromRequest();
		Logger.info("PARAMETERS : " + form.data().toString());

		boolean saveToDisk = false;
		if(null != form.get("save_to_disk")){
			saveToDisk = true;
		}

		MultipartFormData body = request().body().asMultipartFormData();
		FilePart fp = body.getFile("csv_file_path");

		if (fp != null) {
			String fileName = fp.getFilename();
			String contentType = fp.getContentType();
			Logger.info("fileName: " + fileName + ", contentType: " + contentType);
			File file = fp.getFile();

			try{
				// import the functions - this automatically updates the project but does not save it
				List<Function> functions = RuleDao.importFunctions(name, file.getAbsolutePath(), saveToDisk);
				ProjectController.statusMessage = "Successfully imported " + functions.size() + " functions.";
			}
			catch(IOException ioe){
				flash("error", ioe.getMessage());
				ProjectController.statusMessage = "Error: " + ioe.getMessage();
			} catch (SecurityException e) {
				flash("error", e.getMessage());
				ProjectController.statusMessage = "Error: " + e.getMessage();
			} catch (IllegalArgumentException e) {
				flash("error", e.getMessage());
				ProjectController.statusMessage = "Error: " + e.getMessage();
			} catch (ClassNotFoundException e) {
				flash("error", e.getMessage());
				ProjectController.statusMessage = "Error: " + e.getMessage();
			} catch (NoSuchMethodException e) {
				flash("error", e.getMessage());
				ProjectController.statusMessage = "Error: " + e.getMessage();
			} catch (InstantiationException e) {
				flash("error", e.getMessage());
				ProjectController.statusMessage = "Error: " + e.getMessage();
			} catch (IllegalAccessException e) {
				flash("error", e.getMessage());
				ProjectController.statusMessage = "Error: " + e.getMessage();
			} catch (InvocationTargetException e) {
				flash("error", e.getMessage());
				ProjectController.statusMessage = "Error: " + e.getMessage();
			}
		} else {
			flash("error", "Missing file");
			ProjectController.statusMessage = "Error: Missing file";
		}
		return redirect(controllers.project.routes.ProjectController.showProject(name));
	}

	// TODO: Sanjib, review
	public static Result getRecommendedFunctions(String name) {

		List<String> functionNames = new ArrayList<String>();

		JsonNode json = request().body().asJson();

		String table1Name = json.findPath("table1_name_f").asText();
		String table2Name = json.findPath("table2_name_f").asText();
		String attr1Name = json.findPath("attr1_name_f").asText();
		String attr2Name = json.findPath("attr2_name_f").asText();

		ObjectNode result = Json.newObject();
		result.put("functionNames", Json.toJson(functionNames));

		if (json.size() == 0 || attr1Name.equals("null") || attr2Name.equals("null")) {
			return ok(Json.toJson(result));
		}

		try {
			Project project = ProjectDao.open(name);
			Table table1 = TableDao.open(name, table1Name);
			Table table2 = TableDao.open(name, table2Name);
			Attribute attr1 = table1.getAttributeByName(attr1Name);
			Attribute attr2 = table2.getAttributeByName(attr2Name);

			if (attr1 != null && attr2 != null) {
				// Note: topkfrequency is not going to be used hence the 0 arguments.
				AttrStats attr1Stats = ExplorerDriver.getAttrStats(table1, attr1, 0, 0);
				AttrStats attr2Stats = ExplorerDriver.getAttrStats(table2, attr2, 0, 0);
				List<Function> allFunctions = project.getFunctions();
				List<Function> functions1 = FunctionRecommender.getRecFunctionsForAttr(
						allFunctions, attr1Stats);
				List<Function> functions2 = FunctionRecommender.getRecFunctionsForAttr(
						allFunctions, attr2Stats);

				// get the intersection of recommended functions.
				for (Function function: functions1) {
					if (functions2.contains(function)) {
						functionNames.add(function.getName());
					}
				}
			}
		} catch (IOException e) {
			flash("error", e.getMessage());
			ProjectController.statusMessage = "Error: " + e.getMessage();
		}

		result.put("functionNames", Json.toJson(functionNames));

		return ok(result);
	}

	public static Result addRule(String name){
		DynamicForm form = form().bindFromRequest();
		Logger.info("PARAMETERS : " + form.data().toString());
		String ruleName = form.get("rule_name");
		List<String> featureNames = Utils.getFormArrayParameters(form, "feature", 1);
		List<String> operators = Utils.getFormArrayParameters(form, "op", 1);
		List<String> values = Utils.getFormArrayParameters(form, "val", 1);

		boolean saveToDisk = false;
		if(null != form.get("save_to_disk")){
			saveToDisk = true;
		}

		try{
			RuleDao.addRule(name, ruleName, featureNames, operators, values, saveToDisk);
			ProjectController.statusMessage = "Successfully added Rule " + ruleName + ".";
		}
		catch (IOException e) {
			flash("error", e.getMessage());
			ProjectController.statusMessage = "Error: " + e.getMessage();
		}

		return redirect(controllers.project.routes.ProjectController.showProject(name));
	}

	public static Result editRule(String name) {
		DynamicForm form = form().bindFromRequest();
		Logger.info("PARAMETERS : " + form.data().toString());
		String ruleName = form.get("rule_name");
		String ruleString = form.get("rule_string");

		boolean saveToDisk = false;
		if(null != form.get("save_to_disk")){
			saveToDisk = true;
		}

		try{
			RuleDao.editRule(name, ruleName, ruleString, saveToDisk);
			ProjectController.statusMessage = "Successfully edited Rule " + ruleName + ".";
		}
		catch (IOException e) {
			flash("error", e.getMessage());
			ProjectController.statusMessage = "Error: " + e.getMessage();
		}

		return redirect(controllers.project.routes.ProjectController.showProject(name));
	}

	public static Result editRuleUsingGui(String projectName) {
		DynamicForm form = form().bindFromRequest();
		Logger.info("PARAMETERS : " + form.data().toString());
		String ruleName = form.get("rule_name");
		List<String> featureNames = Utils.getFormArrayParameters(form, "feature", 1);
		List<String> operators = Utils.getFormArrayParameters(form, "op", 1);
		List<String> values = Utils.getFormArrayParameters(form, "val", 1);

		boolean saveToDisk = false;
		if(null != form.get("save_to_disk")){
			saveToDisk = true;
		}

		try{
			Project project = ProjectDao.open(projectName);
			RuleService.editRuleUsingGui(project, ruleName, featureNames,
					operators, values, saveToDisk);
			ProjectController.statusMessage = "Successfully added Rule " + ruleName + ".";
		}
		catch (IOException e) {
			ProjectController.statusMessage = "Error: " + e.getMessage();
		}

		return redirect(controllers.project.routes.ProjectController.showProject(projectName));
	}

	public static Result importFeatures(String name){
		DynamicForm form = form().bindFromRequest();
		Logger.info("PARAMETERS : " + form.data().toString());
		String table1Name = form.get("table1_name");
		String table2Name = form.get("table2_name");

		MultipartFormData body = request().body().asMultipartFormData();
		FilePart fp = body.getFile("csv_file_path");

		if (fp != null) {
			String fileName = fp.getFilename();
			String contentType = fp.getContentType();
			Logger.info("fileName: " + fileName + ", contentType: " + contentType);
			File file = fp.getFile();
			Project project = ProjectDao.open(name);

			try{
				Table table1 = TableDao.open(name, table1Name);
				Table table2 = TableDao.open(name, table2Name);
				List<Feature> features = RuleDao.importFeaturesFromCSVWithHeader(project,
						table1, table2, file.getAbsolutePath());
				// save the features - this automatically updates and saves the project
				System.out.println(features);
				System.out.println(name);
				RuleDao.save(name, features);
				ProjectController.statusMessage = "Successfully imported " + 
						features.size() + " features.";
			}
			catch(IOException ioe){
				flash("error", ioe.getMessage());
				ProjectController.statusMessage = "Error: " + ioe.getMessage();
			}
		} else {
			flash("error", "Missing file");
			ProjectController.statusMessage = "Error: Missing file";
		}
		return redirect(controllers.project.routes.ProjectController.showProject(name));
	}

	public static Result importRules(String name){
		DynamicForm form = form().bindFromRequest();
		Logger.info("PARAMETERS : " + form.data().toString());
		String table1Name = form.get("table1_name");
		String table2Name = form.get("table2_name");

		MultipartFormData body = request().body().asMultipartFormData();
		FilePart fp = body.getFile("csv_file_path");

		if (fp != null) {
			String fileName = fp.getFilename();
			String contentType = fp.getContentType();
			Logger.info("fileName: " + fileName + ", contentType: " + contentType);
			File file = fp.getFile();
			Project project = ProjectDao.open(name);

			try{
				List<Rule> rules = RuleDao.importRulesFromCSVWithHeader(project,
						table1Name, table2Name, file.getAbsolutePath());
				// save the features - this automatically updates and saves the project
				RuleDao.saveRules(name, rules);
				ProjectController.statusMessage = "Successfully imported " + rules.size() + " rules.";
			}
			catch(IOException ioe){
				flash("error", ioe.getMessage());
				ProjectController.statusMessage = "Error: " + ioe.getMessage();
			}
		} else {
			flash("error", "Missing file");
			ProjectController.statusMessage = "Error: Missing file";
		}
		return redirect(controllers.project.routes.ProjectController.showProject(name));
	}

	public static Result importMatchers(String name){
		DynamicForm form = form().bindFromRequest();
		Logger.info("PARAMETERS : " + form.data().toString());
		String table1Name = form.get("table1_name");
		String table2Name = form.get("table2_name");

		MultipartFormData body = request().body().asMultipartFormData();
		FilePart fp = body.getFile("csv_file_path");

		if (fp != null) {
			String fileName = fp.getFilename();
			String contentType = fp.getContentType();
			Logger.info("fileName: " + fileName + ", contentType: " + contentType);
			File file = fp.getFile();
			Project project = ProjectDao.open(name);

			try{
				List<Matcher> matchers = RuleDao.importMatchersFromCSVWithHeader(project,
						table1Name, table2Name, file.getAbsolutePath());
				// save the features - this automatically updates and saves the project
				RuleDao.saveMatchers(name, matchers);
				ProjectController.statusMessage = "Successfully imported " + matchers.size() + " matchers.";
			}
			catch(IOException ioe){
				flash("error", ioe.getMessage());
				ProjectController.statusMessage = "Error: " + ioe.getMessage();
			}
		} else {
			flash("error", "Missing file");
			ProjectController.statusMessage = "Error: Missing file";
		}
		return redirect(controllers.project.routes.ProjectController.showProject(name));
	}

	public static Result editMatcher(String name){
		DynamicForm form = form().bindFromRequest();
		Logger.info("PARAMETERS : " + form.data().toString());
		String matcherName = form.get("matcher_name_ed");
		String matcherString = form.get("matcher_string");

		boolean saveToDisk = false;
		if(null != form.get("save_to_disk")){
			saveToDisk = true;
		}

		try{
			RuleDao.editMatcher(name, matcherName, matcherString, saveToDisk);
			ProjectController.statusMessage = "Successfully edited Matcher " + matcherName + ".";
		}
		catch (IOException e) {
			flash("error", e.getMessage());
			ProjectController.statusMessage = "Error: " + e.getMessage();
		}

		return redirect(controllers.project.routes.ProjectController.showProject(name));
	}

	public static Result editFeature(String name) {
		DynamicForm form = form().bindFromRequest();
		Logger.info("PARAMETERS : " + form.data().toString());
		String featureName = form.get("feature_name_ed");
		String featureString = form.get("feature_string");

		boolean saveToDisk = false;
		if(null != form.get("save_to_disk")){
			saveToDisk = true;
		}

		try{
			RuleDao.editFeature(name, featureName, featureString, saveToDisk);
			ProjectController.statusMessage = "Successfully edited Feature " + featureName + ".";
		}
		catch (IOException e) {
			flash("error", e.getMessage());
			ProjectController.statusMessage = "Error: " + e.getMessage();
		}

		return redirect(controllers.project.routes.ProjectController.showProject(name));
	}

	private static int getNumMatches(Table matches) {
		int numMatches = 0;
		List<Attribute> attributes = matches.getAttributes();
		Attribute labelAttr = attributes.get(attributes.size()-1);
		for(Object obj : matches.getAllValuesForAttribute(labelAttr)) {
			int label = (Integer) obj;
			if(MatchStatus.MATCH.getLabel() == label) {
				numMatches++;
			}
		}
		return numMatches;
	}

	public static Result doMatch(String projectName) {
		DynamicForm form = form().bindFromRequest();
		Logger.info("PARAMETERS: " + form.data().toString());
		String table1Name = form.get("table1_name");
		String table2Name = form.get("table2_name");
		String candsetName = form.get("candset_name");
		String matcherName = form.get("matcher_name");
		String matchesName = form.get("matches_name");
		String[] table1AttributeNames = request().body().asFormUrlEncoded().get("attr1_names[]");
		String[] table2AttributeNames = request().body().asFormUrlEncoded().get("attr2_names[]");

		Set<DefaultType> defaultTypes = new HashSet<DefaultType>();
		boolean saveToDisk = false;
		if (null != form.get("matches_default")) {
			defaultTypes.add(DefaultType.MATCHES);
		}
		if (null != form.get("save_to_disk")) {
			saveToDisk = true;
		}

		try {
			Table matches = MatchingDao.match(projectName, candsetName, matcherName,
					matchesName, table1Name, table2Name, table1AttributeNames,
					table2AttributeNames);
			// save the matches - this automatically updates and saves the project
			TableDao.save(matches, defaultTypes, saveToDisk);
			int numMatches = getNumMatches(matches);
			int totalPairs = (int) matches.getSize();
			ProjectController.statusMessage = "Successfully created Matches " +
					matchesName + " having " + numMatches +
					" positive matches and " + (totalPairs-numMatches) +
					" negative matches.\n";
		}
		catch (IOException ioe) {
			ProjectController.statusMessage = "Error: " + ioe.getMessage(); 
		}
		return redirect(controllers.project.routes.ProjectController.showProject(projectName));
	}

	public static Result doMatchDebug(String projectName) {
		DynamicForm form = form().bindFromRequest();
		Logger.info("PARAMETERS: " + form.data().toString());
		String table1Name = form.get("table1_name");
		String table2Name = form.get("table2_name");
		String candsetName = form.get("candset_name");
		String matcherName = form.get("matcher_name");
		String matchesName = form.get("matches_name");
		String matchingSummaryName = form.get("matching_summary_name");
		Map<Tuple, ItemPairAudit> itemPairAudits = new HashMap<Tuple, ItemPairAudit>();

		String[] table1AttributeNames = request().body().asFormUrlEncoded().get("attr1_names[]");
		String[] table2AttributeNames = request().body().asFormUrlEncoded().get("attr2_names[]");

		Set<DefaultType> defaultTypes = new HashSet<DefaultType>();
		boolean saveToDisk = false;
		if (null != form.get("matches_default")) {
			defaultTypes.add(DefaultType.MATCHES);
		}
		if (null != form.get("save_to_disk")) {
			saveToDisk = true;
		}

		try {
			Table matches = MatchingDao.match(projectName, candsetName, matcherName,
					matchesName, itemPairAudits, table1Name, table2Name,
					table1AttributeNames, table2AttributeNames);

			// save the matches - this automatically updates and saves the project
			TableDao.save(matches, defaultTypes, saveToDisk);
			//int numMatches = getNumMatches(matches);

			MatchingSummary matchingSummary = Debug.getMatchingSummary(itemPairAudits);
			int numMatches = matchingSummary.getTotalMatches();
			int totalPairs = matchingSummary.getTotalPairs();

			ProjectController.statusMessage = "Successfully created Matches " +
					matchesName + " having " + numMatches +
					" positive matches and " + (totalPairs-numMatches) +
					" negative matches.\n";
			matchingSummary.setName(matchingSummaryName);
			matchingSummary.setProjectName(projectName);
			matchingSummary.setTable1Name(table1Name);
			matchingSummary.setTable2Name(table2Name);
			matchingSummary.setCandsetName(candsetName);
			matchingSummary.setMatchesName(matchesName);
			matchingSummary.setMatcherName(matcherName);
			matchingSummary.setItemPairAudits(itemPairAudits);

			// add matching summary - this automatically updates and saves the project
			MatchingDao.addMatchingSummary(projectName, matchingSummary);
		} catch (IOException ioe) {
			flash("error", ioe.getMessage());
			ProjectController.statusMessage = "Error: " + ioe.getMessage(); 
		}
		return redirect(controllers.project.routes.ProjectController.showProject(projectName));
	}

	public static Result getRuleString(String projectName, String ruleName) {
		Logger.info("rule name: " + ruleName);
		String ruleString = "";
		if(null != ruleName && !"null".equals(ruleName) && !ruleName.isEmpty() &&
				!"undefined".equals(ruleName)) {
			ruleString = RuleDao.getRuleString(projectName, ruleName);
		}
		ObjectNode result = Json.newObject();
		result.put("ruleString", ruleString);
		return ok(result);
	}

	public static Result getMatcherString(String projectName, String matcherName) {
		String matcherString = "";
		if(null != matcherName && !"null".equals(matcherName)) {
			matcherString = RuleDao.getMatcherString(projectName, matcherName);
		}	
		ObjectNode result = Json.newObject();
		result.put("matcherString", matcherString);
		return ok(result);
	}

	public static Result getFeatureString(String projectName, String featureName) {
		String featureString = "";
		if(null != featureName && !"null".equals(featureName)) {
			featureString = RuleDao.getFeatureString(projectName, featureName);
		}	
		ObjectNode result = Json.newObject();
		result.put("featureString", featureString);
		return ok(result);
	}

	public static Result saveFunction(String projectName){
		DynamicForm form = form().bindFromRequest();
		Logger.info("PARAMETERS : " + form.data().toString());
		String functionName = form.get("function_name");

		try{
			RuleDao.saveFunction(projectName, functionName);
			ProjectController.statusMessage = "Successfully saved Function " + functionName + ".";
		}
		catch (IOException e) {
			flash("error", e.getMessage());
			ProjectController.statusMessage = "Error: " + e.getMessage();
		}
		catch (SecurityException e) {
			flash("error", e.getMessage());
			ProjectController.statusMessage = "Error: " + e.getMessage();
		} catch (IllegalArgumentException e) {
			flash("error", e.getMessage());
			ProjectController.statusMessage = "Error: " + e.getMessage();
		} catch (ClassNotFoundException e) {
			flash("error", e.getMessage());
			ProjectController.statusMessage = "Error: " + e.getMessage();
		} catch (NoSuchMethodException e) {
			flash("error", e.getMessage());
			ProjectController.statusMessage = "Error: " + e.getMessage();
		} catch (InstantiationException e) {
			flash("error", e.getMessage());
			ProjectController.statusMessage = "Error: " + e.getMessage();
		} catch (IllegalAccessException e) {
			flash("error", e.getMessage());
			ProjectController.statusMessage = "Error: " + e.getMessage();
		} catch (InvocationTargetException e) {
			flash("error", e.getMessage());
			ProjectController.statusMessage = "Error: " + e.getMessage();
		}
		return redirect(controllers.project.routes.ProjectController.showProject(projectName));
	}

	public static Result saveAllFunctions(String projectName) {
		try {
			// save the functions - this automatically updates the project but does not save it
			RuleDao.saveAllFunctions(projectName);
			ProjectController.statusMessage = "Successfully saved unsaved functions to disk.";
		}
		catch(IOException ioe) {
			flash("error", ioe.getMessage());
			ProjectController.statusMessage = "Error: " + ioe.getMessage();
		}
		return redirect(controllers.project.routes.ProjectController.showProject(projectName));
	}

	public static Result saveFeature(String projectName) {
		DynamicForm form = form().bindFromRequest();
		Logger.info("PARAMETERS : " + form.data().toString());
		String featureName = form.get("feature_name");

		try{
			RuleDao.saveFeature(projectName, featureName);
			ProjectController.statusMessage = "Successfully saved Feature " + featureName + ".";
		}
		catch (IOException e) {
			flash("error", e.getMessage());
			ProjectController.statusMessage = "Error: " + e.getMessage();
		}
		return redirect(controllers.project.routes.ProjectController.showProject(projectName));
	}

	public static Result saveAllFeatures(String projectName) {
		try {
			// save the features - this automatically updates the project but does not save it
			RuleDao.saveAllFeatures(projectName);
			ProjectController.statusMessage = "Successfully saved all unsaved features to disk.";
		}
		catch(IOException ioe) {
			flash("error", ioe.getMessage());
			ProjectController.statusMessage = "Error: " + ioe.getMessage();
		}
		return redirect(controllers.project.routes.ProjectController.showProject(projectName));
	}

	public static Result saveRule(String projectName) {
		DynamicForm form = form().bindFromRequest();
		Logger.info("PARAMETERS : " + form.data().toString());
		String ruleName = form.get("rule_name");

		try{
			RuleDao.saveRule(projectName, ruleName);
			ProjectController.statusMessage = "Successfully saved Rule " + ruleName + ".";
		}
		catch (IOException e) {
			flash("error", e.getMessage());
			ProjectController.statusMessage = "Error: " + e.getMessage();
		}
		return redirect(controllers.project.routes.ProjectController.showProject(projectName));
	}

	public static Result saveAllRules(String projectName) {
		try {
			// save the rules - this automatically updates the project but does not save it
			RuleDao.saveAllRules(projectName);
			ProjectController.statusMessage = "Successfully saved all unsaved rules to disk.";
		}
		catch(IOException ioe) {
			flash("error", ioe.getMessage());
			ProjectController.statusMessage = "Error: " + ioe.getMessage();
		}
		return redirect(controllers.project.routes.ProjectController.showProject(projectName));
	}

	public static Result saveMatcher(String projectName) {
		DynamicForm form = form().bindFromRequest();
		Logger.info("PARAMETERS : " + form.data().toString());
		String matcherName = form.get("matcher_name");

		try{
			RuleDao.saveMatcher(projectName, matcherName);
			ProjectController.statusMessage = "Successfully saved Matcher " + matcherName + ".";
		}
		catch (IOException e) {
			flash("error", e.getMessage());
			ProjectController.statusMessage = "Error: " + e.getMessage();
		}
		return redirect(controllers.project.routes.ProjectController.showProject(projectName));
	}

	public static Result saveAllMatchers(String projectName) {
		try {
			// save the matchers - this automatically updates the project but does not save it
			RuleDao.saveAllMatchers(projectName);
			ProjectController.statusMessage = "Successfully saved all unsaved matchers to disk.";
		}
		catch(IOException ioe) {
			flash("error", ioe.getMessage());
			ProjectController.statusMessage = "Error: " + ioe.getMessage();
		}
		return redirect(controllers.project.routes.ProjectController.showProject(projectName));
	}

	private static double[] getPrecisionAndCoverage(String ruleStats) {
		int index = ruleStats.indexOf("/");
		String c = ruleStats.substring(1, index);
		String m = ruleStats.substring(index + 1, ruleStats.length() - 1);
		double cov = Double.parseDouble(c);
		double mis = Double.parseDouble(m);
		double precision = 100 * (1 - (mis/cov));
		double[] precCov = {precision, cov};
		return precCov;
	}

	/*
	private static Result showLearnedRules(Project project,
			Map<String, String> rules, String ruleNamePrefix, String table1Name,
			String table2Name, String statusMessage) {

		String pageTitle = "EMS: Learned Rules";
		String projectName = project.getName();
		Html topBar = project_main_topbar.render(projectName);
		Html topNav = common_topnav.render(project);

		List<String> ruleNames = new ArrayList<String>();
		List<String> ruleStrings = new ArrayList<String>();
		List<Double> precisions = new ArrayList<Double>();
		List<Double> coverages = new ArrayList<Double>();

		int i = 0;
		for (String r : rules.keySet()) {
			ruleStrings.add(r);
			ruleNames.add(ruleNamePrefix+i);
			String ruleStats = rules.get(r);
			double[] precCov = getPrecisionAndCoverage(ruleStats);
			precisions.add(precCov[0]);
			coverages.add(precCov[1]);
			i++;
		}
		Html content = list_rules.render(ruleNames, ruleStrings, precisions,
				coverages, precisions, coverages, projectName, table1Name, table2Name, statusMessage);

		List<Call> dynamicJs = new ArrayList<Call>();
		dynamicJs.add(controllers.project.routes.ProjectController.javascriptRoutes());
		dynamicJs.add(controllers.routes.Assets.at("javascripts/project/project.js"));

		List<Call> dynamicCss = new ArrayList<Call>();
		dynamicCss.add(controllers.routes.Assets.at("stylesheets/project/project.css"));

		Html page = common_main.render(
				pageTitle, topBar, topNav, content, dynamicCss, dynamicJs);
		return ok(page);
	}
	 */
	
	private static Set<String> getUniqueFeaturesInRule(String rule) {
		Set<String> features = new HashSet<String>();
		String[] terms = rule.split(" AND ");
		for (String t: terms) {
			String[] vals = t.split(" ");
			String featureName = vals[0].trim();
			features.add(featureName);
		}
		return features;
	}
	
	private static Result showLearnedRules2(Project project,
			Map<String, ConfusionMatrix> ruleEvaluations, String ruleNamePrefix, String table1Name,
			String table2Name, String statusMessage) {

		String pageTitle = "EMS: Learned Rules";
		String projectName = project.getName();
		Html topBar = project_main_topbar.render(projectName);
		Html topNav = common_topnav.render(project);

		List<String> ruleNames = new ArrayList<String>();
		List<String> ruleStrings = new ArrayList<String>();
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
		List<Integer> numTermsInRule = new ArrayList<Integer>();
		List<Integer> numUniqueFeaturesInRule = new ArrayList<Integer>();
		
		SortedMap<Double, Set<String>> f1ToRule = new TreeMap<Double, Set<String>>(Collections.reverseOrder());
		
		int i = 0;
		for (String r : ruleEvaluations.keySet()) {
			ConfusionMatrix confusionMatrix = ruleEvaluations.get(r);
			Logger.info("r: " + r + ",  " + confusionMatrix);

			// Pruning
			long truePositives = confusionMatrix.getTruePositives();
			if (truePositives == 0) {
				continue;
			}

			long falsePositives = confusionMatrix.getFalsePositives();
			long trueNegatives = confusionMatrix.getTrueNegatives();
			long falseNegatives = confusionMatrix.getFalseNegatives();
			
			double precision = 100.0 * truePositives / (truePositives + falsePositives);
			double recall = 100.0 * truePositives / (truePositives + falseNegatives);
			double f1 = (2 * precision * recall) / (precision + recall);
			double accuracy = 100.0 * (truePositives + trueNegatives) /
					(truePositives + trueNegatives + falsePositives + falseNegatives);
			double fpr = 100.0 * falsePositives / (falsePositives + trueNegatives);
			double fnr = 100.0 * falseNegatives / (truePositives + falseNegatives);
			
			ruleStrings.add(r);
			ruleNames.add(ruleNamePrefix+i);

			DecimalFormat df = new DecimalFormat("#.##");
			precisions.add(Double.valueOf(df.format(precision)));
			recalls.add(Double.valueOf(df.format(recall)));
			f1s.add(Double.valueOf(df.format(f1)));
			accuracies.add(Double.valueOf(df.format(accuracy)));
			tps.add(truePositives);
			fps.add(falsePositives);
			tns.add(trueNegatives);
			fns.add(falseNegatives);
			fprs.add(Double.valueOf(df.format(fpr)));
			fnrs.add(Double.valueOf(df.format(fnr)));
			numTermsInRule.add(r.split(" AND ").length);
			Set<String> features = getUniqueFeaturesInRule(r);
			numUniqueFeaturesInRule.add(features.size());
			if (!f1ToRule.containsKey(f1)) {
				f1ToRule.put(f1, new HashSet<String>());
			}
			f1ToRule.get(f1).add(r);
			i++;
		}

		Map<Integer, Set<String>> ruleCountsOfFeatures = getRuleCountsOfFeatures(f1ToRule);
		List<Integer> ruleCounts = new ArrayList<Integer>();
		List<String> featureSets = new ArrayList<String>();
		
		StringBuilder sb = new StringBuilder();
		for (int c: ruleCountsOfFeatures.keySet()) {
			Set<String> features = ruleCountsOfFeatures.get(c);
			for (String f: features) {
				sb.append(f);
				sb.append(", ");
			}
			ruleCounts.add(c);
			featureSets.add(sb.toString());
			sb.setLength(0);
		}
		
		Html content = list_rules.render(ruleNames, ruleStrings, precisions,
				recalls, f1s, accuracies, tps, fps, tns, fns, fprs, fnrs,
				numTermsInRule, numUniqueFeaturesInRule, projectName, table1Name,
				table2Name, statusMessage, ruleCounts, featureSets);

		List<Call> dynamicJs = new ArrayList<Call>();
		dynamicJs.add(controllers.project.routes.ProjectController.javascriptRoutes());
		dynamicJs.add(controllers.routes.Assets.at("javascripts/project/project.js"));

		List<Call> dynamicCss = new ArrayList<Call>();
		dynamicCss.add(controllers.routes.Assets.at("stylesheets/project/project.css"));

		Html page = common_main.render(
				pageTitle, topBar, topNav, content, dynamicCss, dynamicJs);
		return ok(page);
	}

	private static Map<Integer, Set<String>> getRuleCountsOfFeatures(
			SortedMap<Double, Set<String>> f1ToRule) {
		Logger.info("Size of f1ToRule: " + f1ToRule.size());
		Map<String, Integer> ruleCountsOfFeatures = new HashMap<String, Integer>();
		int i = 0;
		for (Double f1: f1ToRule.keySet()) {
			Set<String> rules = f1ToRule.get(f1);
			for (String rule: rules) {
				Set<String> features = getUniqueFeaturesInRule(rule);
				for (String f: features) {
					if (ruleCountsOfFeatures.containsKey(f)) {
						int count = ruleCountsOfFeatures.get(f) + 1;
						ruleCountsOfFeatures.put(f, count);
					}
					else {
						ruleCountsOfFeatures.put(f, 1);
					}
				}
			}
			i++;
			if (i == TOP_F1_RULES_COUNT) {
				break;
			}
		}
		Logger.info("Size of ruleCountsOfFeatures: " + ruleCountsOfFeatures.size());
		Map<Integer, Set<String>> invRuleCountsOfFeatures = new HashMap<Integer, Set<String>>();
		for (int c : ruleCountsOfFeatures.values()) {
			invRuleCountsOfFeatures.put(c, new HashSet<String>());
		}
		for (String f: ruleCountsOfFeatures.keySet()) {
			int c = ruleCountsOfFeatures.get(f);
			invRuleCountsOfFeatures.get(c).add(f);
		}
		Logger.info("Size of invRuleCountsOfFeatures: " + invRuleCountsOfFeatures.size());
		return invRuleCountsOfFeatures;
	}

	public static Result addRules(String projectName, int numRules) {
		DynamicForm form = form().bindFromRequest();
		Logger.info("PARAMETERS : " + form.data().toString());
		//String[] ruleNames = form.get("rule_name[]");

		boolean saveToDisk = false;
		if(null != form.get("save_to_disk")){
			saveToDisk = true;
		}

		//int count = 0;
		for(int i=0; i<numRules; i++) {
			if(null != form.get("rule_checkbox_" + i)) {
				//count++;
				String ruleName = form.get("rule_name_" + i);
				String ruleString = form.get("rule_string_" + i);
				Logger.info(ruleName + ", " + ruleString);
				try {
					RuleService.addRule(projectName, ruleName, ruleString, saveToDisk);
					ProjectController.statusMessage += "Successfully added rule " +
							ruleName + " to the project\n";
				} catch (IOException e) {
					ProjectController.statusMessage += "Error: Failed to add rule " +
							ruleName + " to project due to " + e.getMessage() + "\n";
				}
			}
		}
		return redirect(controllers.project.routes.ProjectController.showProject(projectName));
	}

	public static Result addActiveLearningRules(String projectName, String table1Name,
			String table2Name, int modelIndex, int numRules, String option) {
		DynamicForm form = form().bindFromRequest();
		Logger.info("PARAMETERS : " + form.data().toString());
		//String[] ruleNames = form.get("rule_name[]");

		boolean saveToDisk = false;
		if(null != form.get("save_to_disk")){
			saveToDisk = true;
		}

		//int count = 0;
		for(int i=0; i<numRules; i++) {
			if(null != form.get("rule_checkbox_" + i)) {
				//count++;
				String ruleName = form.get("rule_name_" + i);
				String ruleString = form.get("rule_string_" + i);
				Logger.info(ruleName + ", " + ruleString);
				try {
					RuleService.addRule(projectName, ruleName, ruleString, saveToDisk);
					ProjectController.statusMessage += "Successfully added rule " +
							ruleName + " to the project\n";
				} catch (IOException e) {
					ProjectController.statusMessage += "Error: Failed to add rule " +
							ruleName + " to project due to " + e.getMessage() + "\n";
				}
			}
		}
		return redirect(controllers.project.routes.RuleController.showRulesForLearnedModel(projectName,
				table1Name, table2Name, modelIndex, option));
	}
	
	public static Result learnRulesTrainingData(String projectName) {
		DynamicForm form = form().bindFromRequest();
		Logger.info("PARAMETERS : " + form.data().toString());
		String table1Name = form.get("table1_name");
		String table2Name = form.get("table2_name");
		String trainsetName = form.get("trainset_name");
		String[] featureNames = request().body().asFormUrlEncoded().get("feature_names[]");
		String modelName = form.get("model_name");

		String options = form.get("options");
		String testsetName = form.get("testset_name");
		String trainPercentStr = form.get("train_percent");
		String numFolds = form.get("num_folds");

		Project project = ProjectDao.open(projectName);
		Map<String, String> rules = new HashMap<String, String>();
		Map<String, ConfusionMatrix> ruleEvaluations = new HashMap<String, ConfusionMatrix>();
		try {
			Table table1 = TableDao.open(projectName, table1Name);
			Table table2 = TableDao.open(projectName, table2Name);
			Table trainPairsTable = TableDao.open(projectName, trainsetName);
			Table testPairsTable;
			if ("1".equals(options)) {
				// use training data
				testPairsTable = trainPairsTable;
			}
			else if ("2".equals(options)) {
				// use test data
				testPairsTable = TableDao.open(projectName, testsetName);
			}

			else if ("3".equals(options)) {
				// split training data into train and test splits
				double trainPercent = Double.parseDouble(trainPercentStr); 
				Table[] trainTestTables = TableService.split(trainPairsTable, trainPercent);
				trainPairsTable = trainTestTables[0];
				testPairsTable = trainTestTables[1];
			}
			else {
				// not implemented yet
				return Results.TODO;
			}
			List<Feature> features = new ArrayList<Feature>();
			for (String f : featureNames) {
				features.add(project.findFeatureByName(f));
			}
			String featuresTableName = trainsetName + "_features";
			Table featuresTable = FeatureService.generateFeatures(featuresTableName,
					projectName, trainPairsTable, table1, table2, features, true);
			RuleBasedModel modelType = RuleBasedModel.valueOf(modelName);
			rules = RuleService.learnRulesUsingTrainingData(featuresTable,
					modelType);

			String ruleNamePrefix = modelName;

			if (null != rules && !rules.isEmpty()) {
				String statusMessage = "";
				if (rules.size() == 1) {
					statusMessage = "Successfully learned 1 rule: " +
							ruleNamePrefix + "0";
				}
				else {
					statusMessage = "Successfully learned " + rules.size() +
							" rules: [" + ruleNamePrefix + "0 - " +
							ruleNamePrefix + (rules.size()-1) + "]";
				}
				ruleEvaluations = RuleService.evaluateRulesUsingTestData(rules.keySet(),
						testPairsTable, project, table1, table2);
				return showLearnedRules2(project, ruleEvaluations, ruleNamePrefix,
						table1Name, table2Name, statusMessage);
				//return showLearnedRules(project, rules, ruleNamePrefix,
				//		table1Name, table2Name, statusMessage);
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

	public static Result learnRulesTrainingDataOptimized(String projectName) {
		DynamicForm form = form().bindFromRequest();
		Logger.info("PARAMETERS : " + form.data().toString());
		String table1Name = form.get("table1_name");
		String table2Name = form.get("table2_name");
		String trainsetName = form.get("trainset_name");
		String[] featureNames = request().body().asFormUrlEncoded().get("feature_names[]");
		String modelName = form.get("model_name");

		String options = form.get("options");
		String testsetName = form.get("testset_name");
		String trainPercentStr = form.get("train_percent");
		String numFolds = form.get("num_folds");

		Project project = ProjectDao.open(projectName);
		Map<String, String> rules = new HashMap<String, String>();
		Map<String, ConfusionMatrix> ruleEvaluations = new HashMap<String, ConfusionMatrix>();
		try {
			Table table1 = TableDao.open(projectName, table1Name);
			Table table2 = TableDao.open(projectName, table2Name);
			Table trainPairsTable = TableDao.open(projectName, trainsetName);
			Table testPairsTable;
			if ("1".equals(options)) {
				// use training data
				testPairsTable = trainPairsTable;
			}
			else if ("2".equals(options)) {
				// use test data
				testPairsTable = TableDao.open(projectName, testsetName);
			}

			else if ("3".equals(options)) {
				// split training data into train and test splits
				double trainPercent = Double.parseDouble(trainPercentStr); 
				Table[] trainTestTables = TableService.split(trainPairsTable, trainPercent);
				trainPairsTable = trainTestTables[0];
				testPairsTable = trainTestTables[1];
			}
			else {
				// not implemented yet
				return Results.TODO;
			}
			List<Feature> features = new ArrayList<Feature>();
			for (String f : featureNames) {
				features.add(project.findFeatureByName(f));
			}
			String featuresTableName = trainsetName + "_features";
			Table featuresTable = FeatureService.generateFeatures(featuresTableName,
					projectName, trainPairsTable, table1, table2, features, true);
			RuleBasedModel modelType = RuleBasedModel.valueOf(modelName);
			rules = RuleService.learnRulesUsingTrainingData(featuresTable,
					modelType);

			String ruleNamePrefix = modelName;

			if (null != rules && !rules.isEmpty()) {
				String statusMessage = "";
				if (rules.size() == 1) {
					statusMessage = "Successfully learned 1 rule: " +
							ruleNamePrefix + "0";
				}
				else {
					statusMessage = "Successfully learned " + rules.size() +
							" rules: [" + ruleNamePrefix + "0 - " +
							ruleNamePrefix + (rules.size()-1) + "]";
				}
				String testFeaturesTableName = testsetName + "_features";
				Table testFeaturesTable = FeatureService.generateFeatures(testFeaturesTableName,
						projectName, testPairsTable, table1, table2, features, true);
				ruleEvaluations = RuleService.evaluateRulesUsingTestFeaturesData(rules.keySet(),
						testFeaturesTable, project, table1, table2);
				return showLearnedRules2(project, ruleEvaluations, ruleNamePrefix,
						table1Name, table2Name, statusMessage);
				//return showLearnedRules(project, rules, ruleNamePrefix,
				//		table1Name, table2Name, statusMessage);
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
	
	public static Result learnRulesTrainingData2(String projectName) {
		DynamicForm form = form().bindFromRequest();
		Logger.info("PARAMETERS : " + form.data().toString());
		String table1Name = form.get("table1_name");
		String table2Name = form.get("table2_name");
		String labeledDataName = form.get("labeled_data_name");
		String[] featureNames = request().body().asFormUrlEncoded().get("feature_names[]");
		String ruleNamePrefix = form.get("rule_name_prefix");
		String modelName = form.get("model_name");

		Project project = ProjectDao.open(projectName);
		Map<String, String> rules = new HashMap<String, String>();
		Map<String, ConfusionMatrix> ruleEvaluations = new HashMap<String, ConfusionMatrix>();
		try {

			Table table1 = TableDao.open(projectName, table1Name);
			Table table2 = TableDao.open(projectName, table2Name);
			Table labeledData = TableDao.open(projectName, labeledDataName);
			Table[] trainTestTables = TableService.split(labeledData, 2.0);
			Table trainPairsTable = trainTestTables[0];
			Table testPairsTable = trainTestTables[1];
			List<Feature> features = new ArrayList<Feature>();
			for (String f : featureNames) {
				features.add(project.findFeatureByName(f));
			}
			String featuresTableName = trainPairsTable.getName() + "_features";
			Table featuresTable = FeatureService.generateFeatures(featuresTableName,
					projectName, trainPairsTable, table1, table2, features, true);
			RuleBasedModel modelType = RuleBasedModel.valueOf(modelName);
			rules = RuleService.learnRulesUsingTrainingData(featuresTable,
					modelType);

			if (null != rules && !rules.isEmpty()) {
				String statusMessage = "";
				if (rules.size() == 1) {
					statusMessage = "Successfully learned 1 rule: " +
							ruleNamePrefix + "0";
				}
				else {
					statusMessage = "Successfully learned " + rules.size() +
							" rules: [" + ruleNamePrefix + "0 - " +
							ruleNamePrefix + (rules.size()-1) + "]";
				}
				ruleEvaluations = RuleService.evaluateRulesUsingTestData(rules.keySet(),
						testPairsTable, project, table1, table2);
				return showLearnedRules2(project, ruleEvaluations, ruleNamePrefix,
						table1Name, table2Name, statusMessage);
				//return showLearnedRules(project, rules, ruleNamePrefix,
				//		table1Name, table2Name, statusMessage);
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

	public static Result getAllRuleNames(String projectName) {
		ObjectNode result = Json.newObject();
		//Logger.info("projectName: " + projectName);
		Project project = ProjectDao.open(projectName);
		List<String> ruleNames = project.getRuleNames();
		ObjectMapper mapper = new ObjectMapper();
		ArrayNode array = mapper.valueToTree(ruleNames);
		result.putArray("rules").addAll(array);
		return ok(result);
	}
	
	public static Result learnRulesActiveLearning(String projectName) {
		DynamicForm form = form().bindFromRequest();
		Logger.info("PARAMETERS : " + form.data().toString());
		String table1Name = form.get("table1_name");
		String table2Name = form.get("table2_name");
		String seedLabeledPairs = form.get("seed_labeled_pairs");
		String unlabeledPairs = form.get("unlabeled_pairs");
		String[] featureNames = request().body().asFormUrlEncoded().get("feature_names[]");
		//String testsetName = form.get("test_data");
		String validationPercentStr = form.get("validation_percent");
		String batchSizeStr = form.get("batch_size");

		Project project = ProjectDao.open(projectName);
		//Map<String, String> rules = new HashMap<String, String>();
		//Map<String, ConfusionMatrix> ruleEvaluations = new HashMap<String, ConfusionMatrix>();
		try {
			Table table1 = TableDao.open(projectName, table1Name);
			Table table2 = TableDao.open(projectName, table2Name);
			Table seedLabeledPairsTable = TableDao.open(projectName, seedLabeledPairs);
			createLabeledPairs(seedLabeledPairsTable);
			unlabeledPairsTable = TableDao.open(projectName, unlabeledPairs);
			//Table testPairsTable = TableDao.open(projectName, testsetName);
			List<Feature> features = new ArrayList<Feature>();
			for (String f : featureNames) {
				features.add(project.findFeatureByName(f));
			}
			String unlabeledFeaturesTableName = unlabeledPairs + "_features";
			removeSeedFromUnlabeledPairs(seedLabeledPairsTable);
			Table unlabeledFeaturesTable = FeatureService.generateFeatures(unlabeledFeaturesTableName,
					projectName, unlabeledPairsTable, table1, table2, features, false);
			String seedLabeledFeaturesTableName = seedLabeledPairs + "_features";
			Table seedLabeledFeaturesTable = FeatureService.generateFeatures(seedLabeledFeaturesTableName,
					projectName, seedLabeledPairsTable, table1, table2, features, true);
			double validationPercent = Double.parseDouble(validationPercentStr);
			int batchSize = Integer.parseInt(batchSizeStr);
			al = new ActiveLearner(seedLabeledFeaturesTable, unlabeledFeaturesTable,
					validationPercent, batchSize);
			numIterations = 0;
			pairIds = al.runNextIteration();
			numIterations++;
			for (Object pairId : pairIds) {
				Logger.info("Pair Id: " + pairId);
			}
			String status = "Showing " + pairIds.size() + " pairs to be labeled " +
					"for round " + numIterations + " of active learning.";
			Logger.info(status);
			return showPairs(project, pairIds, unlabeledPairsTable, table1,
					table2, status);
		}
		catch (IOException e) {
			ProjectController.statusMessage = "Error: " + e.getMessage();
		}
		catch (Exception e) {
			ProjectController.statusMessage = "Error: " + e.getMessage();
		}
		return redirect(controllers.project.routes.ProjectController.showProject(projectName));
	}

	private static void removeSeedFromUnlabeledPairs(Table seedLabeledPairsTable) {
		for (Object seedPairId : seedLabeledPairsTable.getAllIdsInOrder()) {
			unlabeledPairsTable.removeTuple(seedPairId);
		}
		Logger.info("Size of unlabeled pairs: " + unlabeledPairsTable.getSize());
	}

	private static void createLabeledPairs(Table seedLabeledPairsTable) throws IOException {
		labeledPairs = new Table(seedLabeledPairsTable);
		labeledPairs.setName("EMS_AL_Labeled_Pairs");
		labeledPairs.setDescription("This table was produced incrementally from " +
				"the active learning rounds");
		List<Attribute> attributes = labeledPairs.getAttributes();
		Attribute commentAttribute = new Attribute("user_comment",
				Attribute.Type.TEXT);
		attributes.add(attributes.size()-1, commentAttribute);
		for (Tuple tuple : labeledPairs.getAllTuples()) {
			tuple.setAttributeValue(commentAttribute, "User supplied seed");
		}
		// add the table to project
		TableDao.save(labeledPairs, new HashSet<DefaultType>(), false);
		evalPairs = labeledPairs;
	}

	private static Result showPairs(Project project, List<Object> pairIds,
			Table pairs, Table table1, Table table2, String status) {
		List<Tuple> table1Tuples = new ArrayList<Tuple>();
		List<Tuple> table2Tuples = new ArrayList<Tuple>();
		List<Attribute> pairAttributes = pairs.getAttributes();
		//Logger.info("Num attributes: " + pairAttributes.size());
		for (Object pairId : pairIds) {
			//Logger.info("Pair Id: " + pairId);
			Tuple pair = pairs.getTuple(pairId);
			//Logger.info(pair.toString());
			Object id1 = pair.getAttributeValue(pairAttributes.get(1)); //assumption: pairId, id1, id2
			Object id2 = pair.getAttributeValue(pairAttributes.get(2)); //assumption: pairId, id1, id2
			//Logger.info("id1: " + id1 + ", id2: " + id2);
			Tuple tuple1 = table1.getTuple(id1);
			//Logger.info(tuple1.toString());
			Tuple tuple2 = table2.getTuple(id2);
			//Logger.info(tuple2.toString());
			table1Tuples.add(tuple1);
			table2Tuples.add(tuple2);
		}

		String pageTitle = "EMS: Pairs to be labeled";
		String projectName = project.getName();

		Html topBar = project_main_topbar.render(projectName);
		Html topNav = common_topnav.render(project);
		List<String> scores = getConfidenceScoresOfLearnedModels();
		Html content = show_pairs.render(projectName, table1.getName(),
				table2.getName(), table1Tuples, table1.getAttributes(),
				table2Tuples, table2.getAttributes(), pairs.getName(), pairIds,
				scores, status);

		List<Call> dynamicJs = new ArrayList<Call>();
		dynamicJs.add(controllers.project.routes.ProjectController.javascriptRoutes());
		dynamicJs.add(controllers.routes.Assets.at("javascripts/project/project.js"));

		List<Call> dynamicCss = new ArrayList<Call>();
		dynamicCss.add(controllers.routes.Assets.at("stylesheets/project/project.css"));
		//dynamicCss.add(controllers.routes.Assets.at("stylesheets/debug/debug.css"));

		Html page = common_main.render(pageTitle, topBar, topNav, content,
				dynamicCss, dynamicJs);
		return ok(page);
	}

	private static List<MatchStatus> getMatchStatuses(List<String> matchOptions) {
		Logger.info("Match Options (" + matchOptions.size() + ")");
		for (String s : matchOptions) {
			Logger.info(s);
		}
		List<MatchStatus> matchStatuses = new ArrayList<MatchStatus>();
		for (String matchOption : matchOptions) {
			if ("1".equals(matchOption)) {
				// match
				matchStatuses.add(MatchStatus.MATCH);
			}
			else if ("2".equals(matchOption)) {
				// non-match
				matchStatuses.add(MatchStatus.NON_MATCH);
			}
			else {
				// cannnot say
				//TODO: treat this as a non-match for now, handle later
				matchStatuses.add(MatchStatus.NON_MATCH);
			}
		}
		Logger.info("Match Statuses (" + matchStatuses.size() + ")");
		for (MatchStatus ms : matchStatuses) {
			Logger.info("" + ms.getLabel());
		}
		return matchStatuses;
	}

	private static void processUserLabeledPairs(List<MatchStatus> matchStatuses,
			List<String> comments) {
		List<Attribute> labeledPairAttributes = labeledPairs.getAttributes();
		List<Attribute> unlabeledPairAttributes = unlabeledPairsTable.getAttributes();
		int numAttributes = labeledPairAttributes.size();
		Attribute pairIdAttribute = labeledPairAttributes.get(0);
		Attribute id1Attribute = labeledPairAttributes.get(1);
		Attribute id2Attribute = labeledPairAttributes.get(2);
		Attribute commentAttribute = labeledPairAttributes.get(numAttributes-2);
		Attribute labelAttribute = labeledPairAttributes.get(numAttributes-1);
		for (int i = 0; i < pairIds.size(); i++) {
			Object pairId = pairIds.get(i);
			Tuple pair = unlabeledPairsTable.getTuple(pairId);
			Object id1 = pair.getAttributeValue(unlabeledPairAttributes.get(1));
			Object id2 = pair.getAttributeValue(unlabeledPairAttributes.get(2));
			Tuple labeledPair = new Tuple();
			labeledPair.setAttributeValue(pairIdAttribute, pairId);
			labeledPair.setAttributeValue(id1Attribute, id1);
			labeledPair.setAttributeValue(id2Attribute, id2);
			labeledPair.setAttributeValue(commentAttribute, comments.get(i));
			labeledPair.setAttributeValue(labelAttribute, matchStatuses.get(i).getLabel());
			for (int j = 3; j < numAttributes-2; j++) {
				Attribute attribute = labeledPairAttributes.get(j);
				labeledPair.setAttributeValue(attribute, pair.getAttributeValue(attribute));
			}
			labeledPairs.addTuple(labeledPair);
		}
	}

	public static Result submitLabeledPairs(String projectName, String table1Name,
			String table2Name) {
		DynamicForm form = form().bindFromRequest();
		Logger.info("PARAMETERS : " + form.data().toString());
		String submitOption = form.get("submit_option");
		try {
			Project project = ProjectDao.open(projectName);
			Table table1 = TableDao.open(projectName, table1Name);
			Table table2 = TableDao.open(projectName, table2Name);
			if ("Stop".equals(submitOption)) {
				//terminate the active learning
				//show all the models learned
				Logger.info("Stop active learning");
				return showRulesForLearnedModel(projectName, table1Name,
						table2Name, numIterations-1, "Stop");
			}
			else if("Pause".equals(submitOption)) {
				//pause the active learning
				//show all the models learned
				Logger.info("Pause active learning");
				return showRulesForLearnedModel(projectName, table1Name,
						table2Name, numIterations-1, "Resume");
			}
			
			Logger.info("Continue active learning");
			//next iteration
			List<String> matchOptions = Utils.getFormArrayParameters(form, "match_option_", 0);
			List<MatchStatus> matchStatuses = getMatchStatuses(matchOptions);
			List<String> comments = Utils.getFormArrayParameters(form, "comment_", 0);
			processUserLabeledPairs(matchStatuses, comments);
			al.processUserLabeledPairs(matchStatuses, comments);

			pairIds = al.runNextIteration();
			numIterations++;
			for (Object pairId : pairIds) {
				Logger.info("Pair Id: " + pairId);
			}
			String status = "Showing " + pairIds.size() + " pairs for labeling " +
					"from iteration " + numIterations + " of active learning";
			Logger.info(status);
			return showPairs(project, pairIds, unlabeledPairsTable, table1,
					table2, status);
		} catch (Exception e) {
			ProjectController.statusMessage = "Error: " + e.getMessage();
		}
		return redirect(controllers.project.routes.ProjectController.showProject(projectName));
	}

	private static List<String> getConfidenceScoresOfLearnedModels() {
		List<Double> confidenceScores = al.getAverageConfidenceOfModels();
		List<String> scores = new ArrayList<String>();
		DecimalFormat df = new DecimalFormat("##.##");
		for (double d : confidenceScores) {
			scores.add(df.format(100.0 * d));
		}
		return scores;
	}

	public static Result showRulesForLearnedModel(String projectName,
			String table1Name, String table2Name, int modelIndex, String option) {
		Project project = ProjectDao.open(projectName);
		try{
			Table table1 = TableDao.open(projectName, table1Name);
			Table table2 = TableDao.open(projectName, table2Name);
			
			// List<RandomForest> models = al.getLearnedModels();
			List<String> scores = getConfidenceScoresOfLearnedModels();
			RandomForest model = al.getLearnedModels().get(modelIndex);
			Map<String, String> rules = RuleService.getRulesFromModel(model);
			List<String> ruleNames = new ArrayList<String>();
			List<String> ruleStrings = new ArrayList<String>();
			List<Double> precisions = new ArrayList<Double>();
			List<Double> recalls = new ArrayList<Double>();
			List<Double> f1s = new ArrayList<Double>();
			List<Double> accuracies = new ArrayList<Double>();
			if (null != rules && !rules.isEmpty()) {
				Map<String, ConfusionMatrix> ruleEvaluations = RuleService.evaluateRulesUsingTestData(rules.keySet(),
						evalPairs, project, table1, table2);

				String ruleNamePrefix = "AL_RF_";
				int i = 0;
				for (String r : ruleEvaluations.keySet()) {
					ConfusionMatrix confusionMatrix = ruleEvaluations.get(r);
					Logger.info("r: " + r + ",  " + confusionMatrix);

					// Pruning
					long truePositives = confusionMatrix.getTruePositives();
					if (truePositives == 0) {
						continue;
					}

					long falsePositives = confusionMatrix.getFalsePositives();
					double precision = 100.0 * truePositives / (truePositives + falsePositives);

					long falseNegatives = confusionMatrix.getFalseNegatives();
					double recall = 100.0 * truePositives / (truePositives + falseNegatives);

					double f1 = (2 * precision * recall) / (precision + recall);
					long trueNegatives = confusionMatrix.getTrueNegatives();
					double accuracy = 100.0 * (truePositives + trueNegatives) /
							(truePositives + trueNegatives + falsePositives + falseNegatives);

					ruleStrings.add(r);
					ruleNames.add(ruleNamePrefix+i);

					DecimalFormat df = new DecimalFormat("##.##");
					precisions.add(Double.valueOf(df.format(precision)));
					recalls.add(Double.valueOf(df.format(recall)));
					f1s.add(Double.valueOf(df.format(f1)));
					accuracies.add(Double.valueOf(df.format(accuracy)));

					i++;
				}
			}

			String pageTitle = "EMS: Show Rules for Learned Models";
			Html topBar = project_main_topbar.render(projectName);
			Html topNav = common_topnav.render(project);
			Html content = list_models.render(project, scores, modelIndex,
					ruleNames, ruleStrings, precisions, recalls, f1s, accuracies,
					table1Name, table2Name, labeledPairs.getName(),
					evalPairs.getName(), option);

			List<Call> dynamicJs = new ArrayList<Call>();
			dynamicJs.add(controllers.project.routes.ProjectController.javascriptRoutes());
			dynamicJs.add(controllers.routes.Assets.at("javascripts/project/project.js"));

			List<Call> dynamicCss = new ArrayList<Call>();
			dynamicCss.add(controllers.routes.Assets.at("stylesheets/project/project.css"));

			Html page = common_main.render(
					pageTitle, topBar, topNav, content, dynamicCss, dynamicJs);
			return ok(page);
		}
		catch (IOException e) {
			ProjectController.statusMessage = "Error: " + e.getMessage();
		}
		catch (Exception e) {
			ProjectController.statusMessage = "Error: " + e.getMessage();
		}
		return redirect(controllers.project.routes.ProjectController.showProject(projectName));
	}

	public static Result resumeActiveLearning(String projectName,
			String table1Name, String table2Name) {
		Project project = ProjectDao.open(projectName);
		try{
			Table table1 = TableDao.open(projectName, table1Name);
			Table table2 = TableDao.open(projectName, table2Name);
			String status = "Showing " + pairIds.size() + " pairs for labeling " +
					"from iteration " + numIterations + " of active learning";
			Logger.info(status);
			return showPairs(project, pairIds, unlabeledPairsTable, table1,
					table2, status);
		}
		catch (IOException e) {
			ProjectController.statusMessage = "Error: " + e.getMessage();
		}
		catch (Exception e) {
			ProjectController.statusMessage = "Error: " + e.getMessage();
		}
		return redirect(controllers.project.routes.ProjectController.showProject(projectName));
	}
	
	public static Result evaluateActiveLearningRules(String projectName,
			String table1Name, String table2Name, int modelIndex, String option) {
		DynamicForm form = form().bindFromRequest();
		Logger.info("PARAMETERS: " + form.data().toString());
		String evalPairsTableName = form.get("eval_pairs");
		try{
			evalPairs = TableDao.open(projectName, evalPairsTableName);
			return showRulesForLearnedModel(projectName, table1Name, table2Name,
					modelIndex, option);
		}
		catch (IOException e) {
			ProjectController.statusMessage = "Error: " + e.getMessage();
		}
		catch (Exception e) {
			ProjectController.statusMessage = "Error: " + e.getMessage();
		}
		return redirect(controllers.project.routes.ProjectController.showProject(projectName));
	}
	
	public static Result getAttributesForFeature(String projectName, String featureName) {
		String attr1Name = "";
		String attr2Name = "";
		if (null != featureName && !"null".equals(featureName) &&
				null != projectName && !"null".equals(projectName)) {
			Project project = ProjectDao.open(projectName);
			Feature feature = project.findFeatureByName(featureName);
			if (null != feature) {
				attr1Name = feature.getAttribute1().getName();
				attr2Name = feature.getAttribute2().getName();
			}
		}	
		ObjectNode result = Json.newObject();
		result.put("attr1Name", attr1Name);
		result.put("attr2Name", attr2Name);
		return ok(result);
	}
	
	public static Result getFunctionForFeature(String projectName, String featureName) {
		String functionName = "";
		if (null != featureName && !"null".equals(featureName) &&
				null != projectName && !"null".equals(projectName)) {
			Project project = ProjectDao.open(projectName);
			Feature feature = project.findFeatureByName(featureName);
			if (null != feature) {
				functionName = feature.getFunction().getName();
			}
		}	
		ObjectNode result = Json.newObject();
		result.put("functionName", functionName);
		return ok(result);
	}
	
	public static Result getTermsForRule(String projectName, String ruleName) {
		List<String> featureNames = new ArrayList<String>();
		List<String> relops = new ArrayList<String>();
		List<String> values = new ArrayList<String>();
		if (null != ruleName && !"null".equals(ruleName) &&
				null != projectName && !"null".equals(projectName)) {
			Project project = ProjectDao.open(projectName);
			Rule rule = project.findRuleByName(ruleName);
			if (null != rule) {
				List<Term> terms = rule.getTerms();
				for (Term t : terms) {
					String featureName = t.getFeature1().getName();
					String relop = t.getRelop().getName();
					String value = String.valueOf(t.getValue());
					featureNames.add(featureName);
					relops.add(relop);
					values.add(value);
					Logger.info("value: " + value);
				}
			}
		}
		Logger.info("Values: ");
		for (String v: values) {
			Logger.info("v: " + v);
		}
		
		ObjectNode result = Json.newObject();
		ObjectMapper mapper = new ObjectMapper();
		ArrayNode featureNamesArray = mapper.valueToTree(featureNames);
		ArrayNode relopsArray = mapper.valueToTree(relops);
		ArrayNode valuesArray = mapper.valueToTree(values);
		result.putArray("featureNames").addAll(featureNamesArray);
		result.putArray("relops").addAll(relopsArray);
		result.putArray("values").addAll(valuesArray);
		return ok(result);
	}
	
	public static Result getRulesForMatcher(String projectName, String matcherName) {
		List<String> ruleNames = new ArrayList<String>();
		if (null != matcherName && !"null".equals(matcherName) &&
				null != projectName && !"null".equals(projectName)) {
			Project project = ProjectDao.open(projectName);
			Matcher matcher = project.findMatcherByName(matcherName);
			if (null != matcher) {
				List<Rule> rules = matcher.getRules();
				for (Rule r : rules) {
					String ruleName = r.getName();
					ruleNames.add(ruleName);
				}
			}
		}
		ObjectNode result = Json.newObject();
		ObjectMapper mapper = new ObjectMapper();
		ArrayNode ruleNamesArray = mapper.valueToTree(ruleNames);
		result.putArray("ruleNames").addAll(ruleNamesArray);
		return ok(result);
	}
	
	public static Result deleteRule(String projectName) {
		DynamicForm form = form().bindFromRequest();
		Logger.info("PARAMETERS : " + form.data().toString());
		String ruleName = form.get("rule_name");

		try {
			RuleService.deleteRule(projectName, ruleName);
			ProjectController.statusMessage = "Successfully deleted Rule " +
					ruleName + ".";
		}
		catch (IOException e) {
			ProjectController.statusMessage = "Error: " + e.getMessage();
		}
		return redirect(controllers.project.routes.ProjectController.showProject(projectName));
	}
}
