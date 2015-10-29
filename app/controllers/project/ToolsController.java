package controllers.project;

import static play.data.Form.form;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.walmart.productgenome.matching.daos.ProjectDao;
import com.walmart.productgenome.matching.daos.TableDao;
import com.walmart.productgenome.matching.models.DefaultType;
import com.walmart.productgenome.matching.models.data.Project;
import com.walmart.productgenome.matching.models.data.Table;
import com.walmart.productgenome.matching.models.rules.Feature;
import com.walmart.productgenome.matching.service.FeatureService;
import com.walmart.productgenome.matching.service.ToolsService;

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
}
