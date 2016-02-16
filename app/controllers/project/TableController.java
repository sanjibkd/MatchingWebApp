package controllers.project;

import static play.data.Form.form;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import play.Logger;
import play.data.DynamicForm;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;

import com.walmart.productgenome.matching.daos.ProjectDao;
import com.walmart.productgenome.matching.daos.TableDao;
import com.walmart.productgenome.matching.models.DefaultType;
import com.walmart.productgenome.matching.models.data.Project;
import com.walmart.productgenome.matching.models.data.Table;
import com.walmart.productgenome.matching.service.FeatureService;
import com.walmart.productgenome.matching.service.TableService;

public class TableController extends Controller {

	public static Result importTableFromCSV(String projectName) {
		DynamicForm form = form().bindFromRequest();
		Logger.info("PARAMETERS : " + form.data().toString());
		String tableName = form.get("table_name");

		MultipartFormData body = request().body().asMultipartFormData();
		FilePart fp = body.getFile("csv_file_path");

		if (fp != null) {
			String fileName = fp.getFilename();
			String contentType = fp.getContentType();
			Logger.info("fileName: " + fileName + ", contentType: " + contentType);
			File file = fp.getFile();
			Set<DefaultType> defaultTypes = new HashSet<DefaultType>();
			boolean saveToDisk = false;
			if(null != form.get("table1_default")){
				defaultTypes.add(DefaultType.TABLE1);
			}
			if(null != form.get("table2_default")){
				defaultTypes.add(DefaultType.TABLE2);
			}
			if(null != form.get("candset_default")){
				defaultTypes.add(DefaultType.CAND_SET);
			}
			if(null != form.get("matches_default")){
				defaultTypes.add(DefaultType.MATCHES);
			}
			if(null != form.get("gold_default")){
				defaultTypes.add(DefaultType.GOLD);
			}
			if(null != form.get("save_to_disk")){
				saveToDisk = true;
			}
			try{
				Table table = TableDao.importFromCSVWithHeader(projectName, tableName,
						file.getAbsolutePath());

				// save the table - this automatically updates the project but
				// does not save it
				TableDao.save(table, defaultTypes, saveToDisk);
				ProjectController.statusMessage = "Successfully imported Table " +
						tableName + " with " + table.getSize() + " tuples.";
			}
			catch(IOException ioe){
				ProjectController.statusMessage = "Error: Cannot import table.\n" +
						"Reason: " + ioe.getMessage();
			}
		} else {
			ProjectController.statusMessage = "Error: Cannot import table.\n" + 
					"Reason: Missing file + ";
		}
		return redirect(controllers.project.routes.ProjectController.showProject(projectName));
	}
	
	public static Result importTableFromJson(String projectName) {
		DynamicForm form = form().bindFromRequest();
		Logger.info("PARAMETERS : " + form.data().toString());
		String tableName = form.get("table_name");

		MultipartFormData body = request().body().asMultipartFormData();
		FilePart fp = body.getFile("json_file_path");

		if (fp != null) {
			String fileName = fp.getFilename();
			String contentType = fp.getContentType();
			Logger.info("fileName: " + fileName + ", contentType: " + contentType);
			File file = fp.getFile();
			Set<DefaultType> defaultTypes = new HashSet<DefaultType>();
			boolean saveToDisk = false;
			if(null != form.get("table1_default")){
				defaultTypes.add(DefaultType.TABLE1);
			}
			if(null != form.get("table2_default")){
				defaultTypes.add(DefaultType.TABLE2);
			}
			if(null != form.get("candset_default")){
				defaultTypes.add(DefaultType.CAND_SET);
			}
			if(null != form.get("matches_default")){
				defaultTypes.add(DefaultType.MATCHES);
			}
			if(null != form.get("gold_default")){
				defaultTypes.add(DefaultType.GOLD);
			}
			if(null != form.get("save_to_disk")){
				saveToDisk = true;
			}
			try{
				Table table = TableService.importFromJson(projectName, tableName,
						file.getAbsolutePath());

				// save the table - this automatically updates the project but
				// does not save it
				TableDao.save(table, defaultTypes, saveToDisk);
				ProjectController.statusMessage = "Successfully imported Table " +
						tableName + " with " + table.getSize() + " tuples.";
			}
			catch(IOException ioe) {
				ProjectController.statusMessage = "Error: Cannot import table.\n" +
						"Reason: " + ioe.getMessage();
			}
		} else {
			ProjectController.statusMessage = "Error: Cannot import table.\n" +
					"Reason: Missing file.";
		}
		return redirect(controllers.project.routes.ProjectController.showProject(projectName));
	}
	
	public static Result importTable(String projectName) {
		DynamicForm form = form().bindFromRequest();
		Logger.info("PARAMETERS : " + form.data().toString());
		String tableName = form.get("table_name");

		MultipartFormData body = request().body().asMultipartFormData();
		FilePart fp = body.getFile("table_file_path");

		if (fp != null) {
			String fileName = fp.getFilename();
			String contentType = fp.getContentType();
			Logger.info("fileName: " + fileName + ", contentType: " + contentType);
			File file = fp.getFile();
			Set<DefaultType> defaultTypes = new HashSet<DefaultType>();
			boolean saveToDisk = false;
			if (null != form.get("table1_default")) {
				defaultTypes.add(DefaultType.TABLE1);
			}
			if (null != form.get("table2_default")) {
				defaultTypes.add(DefaultType.TABLE2);
			}
			if (null != form.get("candset_default")) {
				defaultTypes.add(DefaultType.CAND_SET);
			}
			if (null != form.get("matches_default")) {
				defaultTypes.add(DefaultType.MATCHES);
			}
			if (null != form.get("gold_default")) {
				defaultTypes.add(DefaultType.GOLD);
			}
			if (null != form.get("save_to_disk")) {
				saveToDisk = true;
			}
			try {
				Table table = TableDao.importTable(projectName, tableName,
						file.getAbsolutePath());

				// save the table - this automatically updates the project but
				// does not save it
				TableDao.save(table, defaultTypes, saveToDisk);
				ProjectController.statusMessage = "Successfully imported Table " +
						tableName + " with " + table.getSize() + " tuples.";
			}
			catch(IOException ioe){
				ProjectController.statusMessage = "Error: Cannot import table.\n" +
						"Reason: " + ioe.getMessage();
			}
		}
		else {
			ProjectController.statusMessage = "Error: Missing file";
		}
		return redirect(controllers.project.routes.ProjectController.showProject(projectName));
	}
	
	public static Result deleteTable(String projectName) {
		DynamicForm form = form().bindFromRequest();
		Logger.info("PARAMETERS : " + form.data().toString());
		String tableName = form.get("table_name");
		
		try {
			Table table = TableDao.open(projectName, tableName);
			TableDao.deleteTable(table);
			ProjectController.statusMessage = String.format("Successfully deleted " +
					"Table %s.", tableName);
		}
		catch(IOException ioe) {
			ProjectController.statusMessage = "Error: " + ioe.getMessage();
		}
		return redirect(controllers.project.routes.ProjectController.showProject(projectName));
	}
	
	public static Result splitTable(String projectName) {
		DynamicForm form = form().bindFromRequest();
		Logger.info("PARAMETERS : " + form.data().toString());
		String tableName = form.get("table_name");
		String split1Name = form.get("split_name_1");
		String split2Name = form.get("split_name_2");
		String splitRatioStr = form.get("split_ratio");
		
		try {
			Double splitRatio = Double.parseDouble(splitRatioStr);
			Table table = TableDao.open(projectName, tableName);
			Set<DefaultType> defaultTypes = new HashSet<DefaultType>();
			Table[] splitTables = TableService.splitTable(table,
					split1Name, split2Name, splitRatio);
			// save the split tables - this automatically updates and saves the project
			for (Table t: splitTables) {
				TableDao.save(t, defaultTypes, true);
			}
			ProjectController.statusMessage = String.format("Successfully split " +
					"Table %s.", tableName);
		}
		catch(IOException ioe) {
			ProjectController.statusMessage = "Error: " + ioe.getMessage();
		}
		return redirect(controllers.project.routes.ProjectController.showProject(projectName));
	}
}
