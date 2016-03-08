package controllers.project;

import static play.data.Form.form;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.walmart.productgenome.matching.daos.EvaluateDao;
import com.walmart.productgenome.matching.daos.MatchingDao;
import com.walmart.productgenome.matching.daos.ProjectDao;
import com.walmart.productgenome.matching.daos.TableDao;
import com.walmart.productgenome.matching.evaluate.EvaluationSummary;
import com.walmart.productgenome.matching.models.DefaultType;
import com.walmart.productgenome.matching.models.audit.ItemPairAudit;
import com.walmart.productgenome.matching.models.data.Project;
import com.walmart.productgenome.matching.models.data.Table;
import com.walmart.productgenome.matching.models.data.Tuple;
import com.walmart.productgenome.matching.models.rules.Feature;
import com.walmart.productgenome.matching.models.rules.Matcher;
import com.walmart.productgenome.matching.service.FeatureService;
import com.walmart.productgenome.matching.service.ToolsService;
import com.walmart.productgenome.matching.service.debug.Debug;

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

public class ToolsController extends Controller {
	
	public static Result viewPairs(String projectName) {
		
		DynamicForm form = form().bindFromRequest();
		Logger.info("PARAMETERS : " + form.data().toString());
		String table1Name = form.get("table1_name");
		String table2Name = form.get("table2_name");
		String pairsTableName = form.get("tuple_pairs_table_name");
		String outputFileName = form.get("output_file_name");
		String pairIdsList = form.get("pair_ids_list");
		
		Project project = ProjectDao.open(projectName);
		try {
			Table table1 = TableDao.open(projectName, table1Name);
			Table table2 = TableDao.open(projectName, table2Name);
			Table pairsTable = TableDao.open(projectName, pairsTableName);
			
			int n = ToolsService.outputPairs(projectName, pairsTable, table1, table2, outputFileName, pairIdsList);
			ProjectController.statusMessage = "Successfully output " + n + " pairs in file " + outputFileName;
		}
		catch (IOException e) {
			ProjectController.statusMessage = "Error: " + e.getMessage();
		}
		catch (Exception e) {
			ProjectController.statusMessage = "Error: " + e.getMessage();
		}
		
		return redirect(controllers.project.routes.ProjectController.showProject(projectName));
	}
	
	public static Result cleanLabeledPairsUsingMatcher(String projectName) {
		DynamicForm form = form().bindFromRequest();
		String table1Name = form.get("table1_name");
		String table2Name = form.get("table2_name");
		String inputLabeledPairsName= form.get("input_labeled_pairs");
		String matcherName = form.get("matcher_name");
		String actionType = form.get("action_type");
		String errorType = form.get("error_type");
		String outputLabeledPairsName= form.get("output_labeled_pairs");
	
		Logger.info("params: " + table1Name + ", " + table2Name + ", "
				+ inputLabeledPairsName + ", " + matcherName + ", "
				+ actionType + ", " + errorType + ", " + outputLabeledPairsName);
		
		try {
			Table table1 = TableDao.open(projectName, table1Name);
			Table table2 = TableDao.open(projectName, table2Name);
			Table inputLabeledPairsTable = TableDao.open(projectName,
					inputLabeledPairsName);
			String matchesName = "matches";
			Map<Tuple, ItemPairAudit> itemPairAudits = new HashMap<Tuple, ItemPairAudit>();
			Project project = ProjectDao.open(projectName);
			Matcher matcher = project.findMatcherByName(matcherName);

			String featuresTableName = inputLabeledPairsName + "_features";
			List<Feature> features = project.getFeatures();
			Table featuresTable = FeatureService.generateFeatures(featuresTableName,
					projectName, inputLabeledPairsTable, table1, table2, features, true);
			Logger.info("Generated feature vectors");
			Table matches = MatchingDao.match(projectName, inputLabeledPairsTable, featuresTable,
					table1, table2, matcherName, matchesName, itemPairAudits,
					null, null);
			Logger.info("Generated matches");
			EvaluationSummary  evalSummary = EvaluateDao.evaluateWithGold("tmp",
					projectName, matches, inputLabeledPairsName);
			Logger.info("Generated evaluation summary");
			Table outputPairsTable = ToolsService.cleanLabeledPairs(projectName, inputLabeledPairsTable,
					outputLabeledPairsName, evalSummary, errorType, actionType);
			Logger.info("Generated cleaned table");
			// save the table - this automatically updates the project but does not save it
			Set<DefaultType> defaultTypes = new HashSet<DefaultType>();
			boolean saveToDisk = true;
			TableDao.save(outputPairsTable, defaultTypes, saveToDisk);
			ProjectController.statusMessage = "Successfully created table " +
					outputLabeledPairsName + " with " + outputPairsTable.getSize() + " tuples";
		}
		catch (IOException e) {
			ProjectController.statusMessage = "Error: " + e.getMessage();
		}
		catch (Exception e) {
			ProjectController.statusMessage = "Error: " + e.getMessage();
		}
		return redirect(controllers.project.routes.ProjectController.showProject(projectName));
	}
	
}
