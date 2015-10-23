/* TODO Sanjib: Fatemah comments:
- Project should be clear from the context. We do not need to save the project inside attributes.
- Instead of triggering a change on load, create a function that will take care of that change and use
it both in load and when change happens. Please see setRecommendedFunctions.
*/

$(document).ready(function(){

	$(window).load(function() {
	
		$('#table1_name').trigger('change'); // blocking
		$('#table2_name').trigger('change'); // blocking
		$('#table1_name_bm').trigger('change'); // blocking multi
		$('#table2_name_bm').trigger('change'); // blocking multi
		$('#table1_name_m').trigger('change'); // matching in normal mode
		$('#table2_name_m').trigger('change'); // matching in normal mode
		$('#table1_name_d').trigger('change'); // matching in debug mode
		$('#table2_name_d').trigger('change'); // matching in debug mode
		$('#table1_name_f').trigger('change'); // add feature
		$('#table2_name_f').trigger('change'); // add feature
		$('#matches_name_e').trigger('change');
		$('#gold_name_e').trigger('change');
		$('#rule_name_e').trigger('change'); // edit a rule
		$('#rule_name_e_gui').trigger('change'); // edit a rule using GUI
		$('#matcher_name_ed').trigger('change'); // edit a matcher
		$('#matcher_name_ed_gui').trigger('change'); // edit a matcher using GUI
		$('#feature_name_ed').trigger('change'); //edit a feature
		$('#feature_name_ed_gui').trigger('change'); //edit a feature using GUI
		$('#table1_name_agf').trigger('change'); // auto generate features
		$('#table2_name_agf').trigger('change'); // auto generate features
		$('.match-option-buttons input[type="radio"]').trigger('change'); //show pairs active learning
		 setRecommendedFunctions();
		 //setRecommendedFunctionsForEditFeatureGui();
//		 addTableRowHtml();
//		 addTableHeaderHtml();
	}); 	
    
	$('#new_project_btn').click(function() {
		$('#new-project-modal').modal('show');
	});

	$('#projects_table').tablesorter();
	$('#browse_functions_table').tablesorter();
	$('#rules_table').tablesorter();
	$('#rules_table_al').tablesorter();
	$('#models_cv_table').tablesorter();
	$('#feature_stats_table').tablesorter();
	
	/**** Blocking ****/
	$('#table1_name').on('change', function()  {
		var table1Name = $('#table1_name').val();
		var projectName = $(this).attr("project_name");
		projectJsRoutes.controllers.project.ProjectController.getAttributes(projectName, table1Name).ajax({
			async: false,
    		success : function(data) {
    			var attribs = data["attributes"];
    			$("#attr1_name").html("");
    			$("#attr1_names").html("");
    			$.each(attribs, function(index, attrib) {
    				$("#attr1_name").append("<option value=\"" + attrib + "\">" + attrib +"</option>");
    				if(index != 0) {
    					$("#attr1_names").append("<option value=\"" + attrib + "\">" + attrib +"</option>");
    				}
    			});
		    },
	        error: function(xhr, status, error) {
  				$('body').html(xhr.responseText);
			}
		});
	});

	/**** Blocking ****/
	$('#table2_name').on('change', function()  {
		var table2Name = $('#table2_name').val();
		var projectName = $(this).attr("project_name");
		projectJsRoutes.controllers.project.ProjectController.getAttributes(projectName, table2Name).ajax({
			async: false,
    		success : function(data) {
    			var attribs = data["attributes"];
    			$("#attr2_name").html("");
    			$("#attr2_names").html("");
    			$.each(attribs, function(index, attrib) {
    				$("#attr2_name").append("<option value=\"" + attrib + "\">" + attrib +"</option>");
    				if(index != 0) {
    					$("#attr2_names").append("<option value=\"" + attrib + "\">" + attrib +"</option>");
    				}
    			});
		    },
	        error: function(xhr, status, error) {
  				$('body').html(xhr.responseText);
			}
		});
	});

	/**** Multiple attribute equivalence based blocking ****/
	$('#table1_name_bm').on('change', function()  {
		var table1Name = $('#table1_name_bm').val();
		var projectName = $(this).attr("project_name");
		projectJsRoutes.controllers.project.ProjectController.getAttributes(projectName, table1Name).ajax({
			async: false,
    		success : function(data) {
    			var attribs = data["attributes"];
    			$(".bm_attr1").html("");
    			$("#bm_attr1_names").html("");
    			$.each(attribs, function(index, attrib) {
    				$(".bm_attr1").append("<option value=\"" + attrib + "\">" + attrib +"</option>");
    				if(index != 0) {
    					$("#bm_attr1_names").append("<option value=\"" + attrib + "\">" + attrib +"</option>");
    				}
    			});
		    },
	        error: function(xhr, status, error) {
  				$('body').html(xhr.responseText);
			}
		});
	});

	$('#table2_name_bm').on('change', function()  {
		var table2Name = $('#table2_name_bm').val();
		var projectName = $(this).attr("project_name");
		projectJsRoutes.controllers.project.ProjectController.getAttributes(projectName, table2Name).ajax({
			async: false,
    		success : function(data) {
    			var attribs = data["attributes"];
    			$(".bm_attr2").html("");
    			$("#bm_attr2_names").html("");
    			$.each(attribs, function(index, attrib) {
    				$(".bm_attr2").append("<option value=\"" + attrib + "\">" + attrib +"</option>");
    				if(index != 0) {
    					$("#bm_attr2_names").append("<option value=\"" + attrib + "\">" + attrib +"</option>");
    				}
    			});
		    },
	        error: function(xhr, status, error) {
  				$('body').html(xhr.responseText);
			}
		});
	});
	
	var attrPairCntBm = 1;
	$('#btAdd_bm').click(function() {
		var projectName = $(this).attr("project_name");
		var table1Name = $("#table1_name_bm").val();
		var table2Name = $("#table2_name_bm").val();
		var attributes1;
		projectJsRoutes.controllers.project.ProjectController.getAttributes(projectName, table1Name).ajax({
			async: false,
    		success : function(data) {
    			attributes1 = data["attributes"];
		    },
	        error: function(xhr, status, error) {
  				$('body').html(xhr.responseText);
			}
		});
		var attributes2;
		projectJsRoutes.controllers.project.ProjectController.getAttributes(projectName, table2Name).ajax({
			async: false,
    		success : function(data) {
    			attributes2 = data["attributes"];
		    },
	        error: function(xhr, status, error) {
  				$('body').html(xhr.responseText);
			}
		});
		
        if(attrPairCntBm == 0) {
			// Show the table header
        	$('#row_bm_0').show();
		}
        
		attrPairCntBm = attrPairCntBm + 1;
		// Add a row in the table
		var row_html = '<tr id="row_bm_' + attrPairCntBm + '"><td>' + 
			'<select class="bm_attr1" id="bm_attr1_' + attrPairCntBm + '" name="attr1_' + attrPairCntBm +
			'" style="width:250px">';
		$.each(attributes1, function(index, attribute1) {
			row_html = row_html + '<option value="' + attribute1 + '">' + attribute1 +
						'</option>';
		});
		row_html = row_html + '</select></td><td>' +
			'<select class="attr2" id="attr2_' + attrPairCntBm + '" name="attr2_' + attrPairCntBm +
			'" style="width:250px">';
		$.each(attributes2, function(index, attribute2) {
			row_html = row_html + '<option value="' + attribute2 + '">' + attribute2 +
						'</option>';
		});
		row_html = row_html + '</select></td></tr>';
			
		$('#blocking_attribute_pairs_table tr:last').after(row_html);
		
		if(attrPairCntBm == 1) {
			$('#btRemove_bm').removeAttr('disabled'); 
			$('#btRemove_bm').attr('class', 'bt');
			$('#btRemoveAll_bm').removeAttr('disabled'); 
			$('#btRemoveAll_bm').attr('class', 'bt');
		}
		
    });
	
	$('#btRemove_bm').click(function() {
		$('#row_bm_' + attrPairCntBm).remove();
        attrPairCntBm = attrPairCntBm - 1;
        if (attrPairCntBm == 0) {
        	// Hide the table header
        	$('#row_bm_0').hide();
        	$('#btRemove_bm').attr('class', 'bt-disable'); 
            $('#btRemove_bm').attr('disabled', 'disabled');
            $('#btRemoveAll_bm').attr('class', 'bt-disable'); 
            $('#btRemoveAll_bm').attr('disabled', 'disabled');
        }
    });
	
    $('#btRemoveAll_bm').click(function() {
        while(attrPairCntBm > 0) {
        	$('#btRemove_bm').trigger('click');
        }
    });
    
	/**** Matching normal mode ****/
	$('#table1_name_m').on('change', function()  {
		var table1Name = $('#table1_name_m').val();
		var projectName = $(this).attr("project_name");
		projectJsRoutes.controllers.project.ProjectController.getAttributes(projectName, table1Name).ajax({
			async: false,
    		success : function(data) {
    			var attribs = data["attributes"];
    			$("#attr1_names_m").html("");
    			$.each(attribs, function(index, attrib) {
    				if(index != 0) {
    					$("#attr1_names_m").append("<option value=\"" + attrib + "\">" + attrib +"</option>");
    				}
    			});
		    },
	        error: function(xhr, status, error) {
  				$('body').html(xhr.responseText);
			}
		});
	});

	/**** Matching normal mode ****/
	$('#table2_name_m').on('change', function()  {
		var table2Name = $('#table2_name_m').val();
		var projectName = $(this).attr("project_name");
		projectJsRoutes.controllers.project.ProjectController.getAttributes(projectName, table2Name).ajax({
			async: false,
    		success : function(data) {
    			var attribs = data["attributes"];
    			$("#attr2_names_m").html("");
    			$.each(attribs, function(index, attrib) {
    				if(index != 0) {
    					$("#attr2_names_m").append("<option value=\"" + attrib + "\">" + attrib +"</option>");
    				}
    			});
		    },
	        error: function(xhr, status, error) {
  				$('body').html(xhr.responseText);
			}
		});
	});
	
	/**** Matching debug mode ****/
	$('#table1_name_d').on('change', function()  {
		var table1Name = $('#table1_name_d').val();
		var projectName = $(this).attr("project_name");
		projectJsRoutes.controllers.project.ProjectController.getAttributes(projectName, table1Name).ajax({
			async: false,
    		success : function(data) {
    			var attribs = data["attributes"];
    			$("#attr1_names_d").html("");
    			$.each(attribs, function(index, attrib) {
    				if(index != 0) {
    					$("#attr1_names_d").append("<option value=\"" + attrib + "\">" + attrib +"</option>");
    				}
    			});
		    },
	        error: function(xhr, status, error) {
  				$('body').html(xhr.responseText);
			}
		});
	});

	/**** Matching debug mode ****/
	$('#table2_name_d').on('change', function()  {
		var table2Name = $('#table2_name_d').val();
		var projectName = $(this).attr("project_name");
		projectJsRoutes.controllers.project.ProjectController.getAttributes(projectName, table2Name).ajax({
			async: false,
    		success : function(data) {
    			var attribs = data["attributes"];
    			$("#attr2_names_d").html("");
    			$.each(attribs, function(index, attrib) {
    				if(index != 0) {
    					$("#attr2_names_d").append("<option value=\"" + attrib + "\">" + attrib +"</option>");
    				}
    			});
		    },
	        error: function(xhr, status, error) {
  				$('body').html(xhr.responseText);
			}
		});
	});
	
	/**** Add feature ****/
	$('#table1_name_f').on('change', function()  {
		var table1Name = $('#table1_name_f').val();
		var projectName = $(this).attr("project_name");
		//alert("table1: " + table1Name + ", projectName: " + projectName);
		projectJsRoutes.controllers.project.ProjectController.getAttributes(projectName, table1Name).ajax({
			async: false,
    		success : function(data) {
    			var attribs = data["attributes"];
    			//alert("Size of attribs: " + attribs.length);
    			$("#attr1_name_f").html("");
    			$.each(attribs, function(index, attrib) {
    				$("#attr1_name_f").append("<option value=\"" + attrib + "\">" + attrib +"</option>");
    			});
		    },
	        error: function(xhr, status, error) {
  				$('body').html(xhr.responseText);
			}
		});
	});

	/**** Add feature ****/
	$('#table2_name_f').on('change', function()  {
		var table2Name = $('#table2_name_f').val();
		var projectName = $(this).attr("project_name");
		projectJsRoutes.controllers.project.ProjectController.getAttributes(projectName, table2Name).ajax({
			async: false,
    		success : function(data) {
    			var attribs = data["attributes"];
    			$("#attr2_name_f").html("");
    			$.each(attribs, function(index, attrib) {
    				$("#attr2_name_f").append("<option value=\"" + attrib + "\">" + attrib +"</option>");
    			});
		    },
	        error: function(xhr, status, error) {
  				$('body').html(xhr.responseText);
			}
		});
	});

	/**** Edit a feature using GUI ****/
	$('#feature_name_ed_gui').on('change', function()  {
		var featureName = $('#feature_name_ed_gui').val();
		var projectName = $(this).attr("project_name");
		var table1Name = $('#table1_name_f_gui').val();
		var table2Name = $('#table2_name_f_gui').val();
		//alert("featureName: " + featureName + ", table1Name: " + table1Name + ", table2Name: " + table2Name);
		projectJsRoutes.controllers.project.ProjectController.getAttributes(projectName, table1Name).ajax({
			async: false,
    		success : function(data) {
    			var attribs = data["attributes"];
    			//alert("Size of attribs for table1: " + attribs.length);
    			$("#attr1_name_f_gui").html("");
    			$.each(attribs, function(index, attrib) {
    				$("#attr1_name_f_gui").append("<option value=\"" + attrib + "\">" + attrib +"</option>");
    			});
		    },
	        error: function(xhr, status, error) {
  				$('body').html(xhr.responseText);
			}
		});
		projectJsRoutes.controllers.project.ProjectController.getAttributes(projectName, table2Name).ajax({
			async: false,
    		success : function(data) {
    			var attribs = data["attributes"];
    			//alert("Size of attribs for table2: " + attribs.length);
    			$("#attr2_name_f_gui").html("");
    			$.each(attribs, function(index, attrib) {
    				$("#attr2_name_f_gui").append("<option value=\"" + attrib + "\">" + attrib +"</option>");
    			});
		    },
	        error: function(xhr, status, error) {
  				$('body').html(xhr.responseText);
			}
		});
		projectJsRoutes.controllers.project.RuleController.getAttributesForFeature(projectName, featureName).ajax({
    		success : function(data) {
    			var attr1Name = data["attr1Name"];
    			var attr2Name = data["attr2Name"];
    			//alert("Attributes for the feature: " + attr1Name + ", " + attr2Name);
    			$('#attr1_name_f_gui').find('option[value="' + attr1Name + '"]').attr("selected",true);
    			$('#attr2_name_f_gui').find('option[value="' + attr2Name + '"]').attr("selected",true);
    			setRecommendedFunctionsForEditFeatureGui();
		    },
	        error: function(xhr, status, error) {
  				$('body').html(xhr.responseText);
			}
		});
	});
	
	/**** Edit a feature using GUI ****/
	$('#table1_name_f_gui').on('change', function()  {
		var table1Name = $('#table1_name_f_gui').val();
		var projectName = $(this).attr("project_name");
		projectJsRoutes.controllers.project.ProjectController.getAttributes(projectName, table1Name).ajax({
			async: false,
    		success : function(data) {
    			var attribs = data["attributes"];
    			$("#attr1_name_f_gui").html("");
    			$.each(attribs, function(index, attrib) {
    				$("#attr1_name_f_gui").append("<option value=\"" + attrib + "\">" + attrib +"</option>");
    			});
		    },
	        error: function(xhr, status, error) {
  				$('body').html(xhr.responseText);
			}
		});
		var featureName = $('#feature_name_ed_gui').val();
		projectJsRoutes.controllers.project.RuleController.getAttributesForFeature(projectName, featureName).ajax({
    		success : function(data) {
    			var attr1Name = data["attr1Name"];
    			$('#attr1_name_f_gui').find('option[value="' + attr1Name + '"]').attr("selected",true);
    			setRecommendedFunctionsForEditFeatureGui();
		    },
	        error: function(xhr, status, error) {
  				$('body').html(xhr.responseText);
			}
		});
	});

	/**** Edit a feature using GUI ****/
	$('#table2_name_f_gui').on('change', function()  {
		var table2Name = $('#table2_name_f_gui').val();
		var projectName = $(this).attr("project_name");
		projectJsRoutes.controllers.project.ProjectController.getAttributes(projectName, table2Name).ajax({
			async: false,
    		success : function(data) {
    			var attribs = data["attributes"];
    			$("#attr2_name_f_gui").html("");
    			$.each(attribs, function(index, attrib) {
    				$("#attr2_name_f_gui").append("<option value=\"" + attrib + "\">" + attrib +"</option>");
    			});
		    },
	        error: function(xhr, status, error) {
  				$('body').html(xhr.responseText);
			}
		});
		var featureName = $('#feature_name_ed_gui').val();
		projectJsRoutes.controllers.project.RuleController.getAttributesForFeature(projectName, featureName).ajax({
    		success : function(data) {
    			var attr2Name = data["attr2Name"];
    			$('#attr2_name_f_gui').find('option[value="' + attr2Name + '"]').attr("selected",true);
    			setRecommendedFunctionsForEditFeatureGui();
		    },
	        error: function(xhr, status, error) {
  				$('body').html(xhr.responseText);
			}
		});
	});
	
	/**** Add rule ****/
	var iCnt = 1;
	$('#btAdd').click(function() {
		var projectName = $(this).attr("project_name");
		var features;
		projectJsRoutes.controllers.project.ProjectController.getFeatureNames(projectName).ajax({
			async: false,
    		success : function(data) {
    			features = data["features"];
		    },
	        error: function(xhr, status, error) {
  				$('body').html(xhr.responseText);
			}
		});
		var relops;
		projectJsRoutes.controllers.project.ProjectController.getRelationalOperatorNames().ajax({
			async: false,
    		success : function(data) {
    			relops = data["relops"];
		    },
	        error: function(xhr, status, error) {
  				$('body').html(xhr.responseText);
			}
		});
        if(iCnt == 0) {
			// Show the table header
        	$('#row0').show();
		}
        
		iCnt = iCnt + 1;
		// Add a row in the table
		var row_html = '<tr id="row' + iCnt + '"><td>' + 
			'<select id="feature' + iCnt + '" name="feature' + iCnt +
			'" style="width:190px">';
		$.each(features, function(index, feature) {
			row_html = row_html + '<option value="' + feature + '">' + feature +
						'</option>';
		});
		row_html = row_html + '</select></td><td>' +
			'<select id="op' + iCnt + '" name="op' + iCnt + '" style="width:210px">';
		$.each(relops, function(index, relop) {
			row_html = row_html + '<option value="' + relop + '">' + relop +
						'</option>';
		});
		row_html = row_html + '</select></td><td>' +
			'<input type="text" id="val' + iCnt + '" name="val' + iCnt +
			'" style="width:50px" required>' +
			'</td></tr>';
			
		$('#rule_table tr:last').after(row_html);
		
		if(iCnt == 1) {
			$('#btRemove').removeAttr('disabled'); 
			$('#btRemove').attr('class', 'bt');
			$('#btRemoveAll').removeAttr('disabled'); 
			$('#btRemoveAll').attr('class', 'bt');
		}
		
    });
	
	$('#btRemove').click(function() {
		$('#row' + iCnt).remove();
        iCnt = iCnt - 1;
        if (iCnt == 0) {
        	// Hide the table header
        	$('#row0').hide();
        	$('#btRemove').attr('class', 'bt-disable'); 
            $('#btRemove').attr('disabled', 'disabled');
            $('#btRemoveAll').attr('class', 'bt-disable'); 
            $('#btRemoveAll').attr('disabled', 'disabled');
        }
    });
	
    $('#btRemoveAll').click(function() {
        while(iCnt > 0) {
        	$('#btRemove').trigger('click');
        }
    });
	
	$('#matches_name_e').on('change', function()  {
		var matchesName = $('#matches_name_e').val();
		var projectName = $(this).attr("project_name");
		var defaultMatchesId1 = $(this).attr("default_matches_id1");
		var defaultMatchesId2 = $(this).attr("default_matches_id2");
		var defaultMatchesLabel = $(this).attr("default_matches_label");
		projectJsRoutes.controllers.project.ProjectController.getAttributes(projectName, matchesName).ajax({
    		success : function(data) {
    			var attribs = data["attributes"];
    			$("#matches_id1").html("");
    			$("#matches_id2").html("");
    			$("#matches_label_id").html("");
    			$.each(attribs, function(index, attrib) {
    				if(defaultMatchesId1 == attrib) {
    					$("#matches_id1").append("<option value=\"" + attrib + "\" selected>" + attrib +"</option>");
    				}
    				else {
    					$("#matches_id1").append("<option value=\"" + attrib + "\">" + attrib +"</option>");
    				}
    				if(defaultMatchesId2 == attrib) {
    					$("#matches_id2").append("<option value=\"" + attrib + "\" selected>" + attrib +"</option>");
    				}
    				else {
    					$("#matches_id2").append("<option value=\"" + attrib + "\">" + attrib +"</option>");
    				}
    				if(defaultMatchesLabel == attrib) {
    					$("#matches_label_id").append("<option value=\"" + attrib + "\" selected>" + attrib +"</option>");
    				}
    				else {
    					$("#matches_label_id").append("<option value=\"" + attrib + "\">" + attrib +"</option>");
    				}
    			});
		    },
	        error: function(xhr, status, error) {
  				$('body').html(xhr.responseText);
			}
		});
	});
	
	$('#gold_name_e').on('change', function()  {
		var goldName = $('#gold_name_e').val();
		var projectName = $(this).attr("project_name");
		var defaultGoldId1 = $(this).attr("default_gold_id1");
		var defaultGoldId2 = $(this).attr("default_gold_id2");
		var defaultGoldLabel = $(this).attr("default_gold_label");
		projectJsRoutes.controllers.project.ProjectController.getAttributes(projectName, goldName).ajax({
    		success : function(data) {
    			var attribs = data["attributes"];
    			$("#gold_id1").html("");
    			$("#gold_id2").html("");
    			$("#gold_label_id").html("");
    			$.each(attribs, function(index, attrib) {
    				if(defaultGoldId1 == attrib) {
    					$("#gold_id1").append("<option value=\"" + attrib + "\" selected>" + attrib +"</option>");
    				}
    				else {
    					$("#gold_id1").append("<option value=\"" + attrib + "\">" + attrib +"</option>");
    				}
    				if(defaultGoldId2 == attrib) {
    					$("#gold_id2").append("<option value=\"" + attrib + "\" selected>" + attrib +"</option>");
    				}
    				else {
    					$("#gold_id2").append("<option value=\"" + attrib + "\">" + attrib +"</option>");
    				}
    				if(defaultGoldLabel == attrib) {
    					$("#gold_label_id").append("<option value=\"" + attrib + "\" selected>" + attrib +"</option>");
    				}
    				else {
    					$("#gold_label_id").append("<option value=\"" + attrib + "\">" + attrib +"</option>");
    				}
    			});
		    },
	        error: function(xhr, status, error) {
  				$('body').html(xhr.responseText);
			}
		});
	});
	
	/**** Edit a rule ****/
	$('#rule_name_e').on('change', function()  {
		var ruleName = $('#rule_name_e').val();
		var projectName = $(this).attr("project_name");
		projectJsRoutes.controllers.project.RuleController.getRuleString(projectName, ruleName).ajax({
    		success : function(data) {
    			var ruleString = data["ruleString"];
    			$('#rule_string').val(ruleString);
		    },
	        error: function(xhr, status, error) {
  				$('body').html(xhr.responseText);
			}
		});
	});
	
	var termCnt = 0;
	/**** Edit a rule using GUI ****/
	$('#rule_name_e_gui').on('change', function()  {
		var ruleName = $('#rule_name_e_gui').val();
		var projectName = $(this).attr("project_name");
		projectJsRoutes.controllers.project.RuleController.getTermsForRule(projectName, ruleName).ajax({
    		success : function(data) {
    			var featureNames = data["featureNames"];
    			var ops = data["relops"];
    			var values = data["values"];
    			$('#btRemoveAll_gui').trigger('click');
    			var length = featureNames.length;
    			for (i = 1; i <= length; i++) {
    				$('#btAdd_gui').trigger('click');
    				var featureName = featureNames[i-1];
    				var rop = ops[i-1];
    				var value = values[i-1];
    				//alert(featureName + " " + rop + " " + value);
    				$('#edit_rule_using_gui_modal').find('#feature' + i + '_gui').find('option[value="' + featureName + '"]').attr("selected",true);
    				$('#edit_rule_using_gui_modal').find('#op' + i + '_gui').find('option[value="' + rop + '"]').attr("selected",true);
    				$('#edit_rule_using_gui_modal').find('#val' + i + '_gui').val(value);
    			}
		    },
	        error: function(xhr, status, error) {
  				$('body').html(xhr.responseText);
			}
		});
	});
	
	$('#btAdd_gui').click(function() {
		var projectName = $(this).attr("project_name");
		var features;
		projectJsRoutes.controllers.project.ProjectController.getFeatureNames(projectName).ajax({
			async: false,
    		success : function(data) {
    			features = data["features"];
		    },
	        error: function(xhr, status, error) {
  				$('body').html(xhr.responseText);
			}
		});
		var relops;
		projectJsRoutes.controllers.project.ProjectController.getRelationalOperatorNames().ajax({
			async: false,
    		success : function(data) {
    			relops = data["relops"];
		    },
	        error: function(xhr, status, error) {
  				$('body').html(xhr.responseText);
			}
		});
        if(termCnt == 0) {
			// Show the table header
        	$('#row0_gui').show();
		}
        
		termCnt = termCnt + 1;
		// Add a row in the table
		var row_html = '<tr id="row' + termCnt + '_gui"><td>' + 
			'<select id="feature' + termCnt + '_gui" name="feature' + termCnt +
			'" style="width:190px">';
		$.each(features, function(index, feature) {
			row_html = row_html + '<option value="' + feature + '">' + feature +
						'</option>';
		});
		row_html = row_html + '</select></td><td>' +
			'<select id="op' + termCnt + '_gui" name="op' + termCnt + '" style="width:210px">';
		$.each(relops, function(index, relop) {
			row_html = row_html + '<option value="' + relop + '">' + relop +
						'</option>';
		});
		row_html = row_html + '</select></td><td>' +
			'<input type="text" id="val' + termCnt + '_gui" name="val' + termCnt +
			'" style="width:50px" required>' +
			'</td></tr>';
			
		$('#rule_table_gui tr:last').after(row_html);
		
		if(termCnt == 1) {
			$('#btRemove_gui').removeAttr('disabled'); 
			$('#btRemove_gui').attr('class', 'bt');
			$('#btRemoveAll_gui').removeAttr('disabled'); 
			$('#btRemoveAll_gui').attr('class', 'bt');
		}
		
    });
	
	$('#btRemove_gui').click(function() {
		$('#row' + termCnt + '_gui').remove();
        termCnt = termCnt - 1;
        if (termCnt == 0) {
        	// Hide the table header
        	$('#row0_gui').hide();
        	$('#btRemove_gui').attr('class', 'bt-disable'); 
            $('#btRemove_gui').attr('disabled', 'disabled');
            $('#btRemoveAll_gui').attr('class', 'bt-disable'); 
            $('#btRemoveAll_gui').attr('disabled', 'disabled');
        }
    });
	
    $('#btRemoveAll_gui').click(function() {
        while(termCnt > 0) {
        	$('#btRemove_gui').trigger('click');
        }
    });
	
	/**** Edit a matcher ****/
	$('#matcher_name_ed').on('change', function()  {
		var matcherName = $('#matcher_name_ed').val();
		var projectName = $(this).attr("project_name");
		projectJsRoutes.controllers.project.RuleController.getMatcherString(projectName, matcherName).ajax({
    		success : function(data) {
    			var matcherString = data["matcherString"];
    			$('#matcher_string').val(matcherString);
		    },
	        error: function(xhr, status, error) {
  				$('body').html(xhr.responseText);
			}
		});
	});
	
	/**** Edit a matcher using GUI ****/
	var ruleCountEditMatcherGui = 0;
	$('#matcher_name_ed_gui').on('change', function()  {
		var matcherName = $('#matcher_name_ed_gui').val();
		var projectName = $(this).attr("project_name");
		projectJsRoutes.controllers.project.RuleController.getRulesForMatcher(projectName, matcherName).ajax({
    		success : function(data) {
    			var ruleNames = data["ruleNames"];
    			$('#btRemoveAll_am_gui').trigger('click');
    			var length = ruleNames.length;
    			for (i = 1; i <= length; i++) {
    				$('#btAdd_am_gui').trigger('click');
    				var ruleName = ruleNames[i-1];
    				$('#edit_matcher_using_gui_modal').find('#rule_name_' + i + '_gui').find('option[value="' + ruleName + '"]').attr("selected",true);
    			}
		    },
	        error: function(xhr, status, error) {
  				$('body').html(xhr.responseText);
			}
		});
	});
	
	/**** Edit a matcher using GUI ****/
	$('#btAdd_am_gui').click(function() {
		var projectName = $(this).attr("project_name");
		var ruleNames;
		projectJsRoutes.controllers.project.RuleController.getAllRuleNames(projectName).ajax({
			async: false,
    		success : function(data) {
    			ruleNames = data["rules"];
    			if(ruleCountEditMatcherGui == 0) {
    				// Show the table header
    	        	$('#row_am_0_gui').show();
    			}
    	        
    			ruleCountEditMatcherGui = ruleCountEditMatcherGui + 1;
    			// Add a row in the table
    			var row_html = '<tr id="row_am_' + ruleCountEditMatcherGui + '_gui"><td>' + 
    				'<select class="rule_name" id="rule_name_' + ruleCountEditMatcherGui +
    				'_gui" name="rule_name_' + ruleCountEditMatcherGui + '" style="width:250px">';
    			$.each(ruleNames, function(index, ruleName) {
    				row_html = row_html + '<option value="' + ruleName + '">' +
    					ruleName + '</option>';
    			});
    			row_html = row_html + '</select></td>';
    				
    			$('#matcher_table_gui tr:last').after(row_html);
   
    			if(ruleCountEditMatcherGui == 1) {
    				$('#btRemove_am_gui').removeAttr('disabled'); 
    				$('#btRemove_am_gui').attr('class', 'bt');
    				$('#btRemoveAll_am_gui').removeAttr('disabled'); 
    				$('#btRemoveAll_am_gui').attr('class', 'bt');
    			}
		    },
	        error: function(xhr, status, error) {
  				$('body').html(xhr.responseText);
			}
		});
    });
	
	/**** Edit a matcher using GUI ****/
	$('#btRemove_am_gui').click(function() {
		$('#row_am_' + ruleCountEditMatcherGui + '_gui').remove();
        ruleCountEditMatcherGui = ruleCountEditMatcherGui - 1;
        if (ruleCountEditMatcherGui == 0) {
        	// Hide the table header
        	$('#row_am_0_gui').hide();
        	$('#btRemove_am_gui').attr('class', 'bt-disable'); 
            $('#btRemove_am_gui').attr('disabled', 'disabled');
            $('#btRemoveAll_am_gui').attr('class', 'bt-disable'); 
            $('#btRemoveAll_am_gui').attr('disabled', 'disabled');
        }
    });
	
	/**** Edit a matcher using GUI ****/
    $('#btRemoveAll_am_gui').click(function() {
        while(ruleCountEditMatcherGui > 0) {
        	$('#btRemove_am_gui').trigger('click');
        }
    });
    
	/**** Edit a feature ****/
	$('#feature_name_ed').on('change', function()  {
		var featureName = $('#feature_name_ed').val();
		var projectName = $(this).attr("project_name");
		projectJsRoutes.controllers.project.RuleController.getFeatureString(projectName, featureName).ajax({
    		success : function(data) {
    			var featureString = data["featureString"];
    			$('#feature_string').val(featureString);
		    },
	        error: function(xhr, status, error) {
  				$('body').html(xhr.responseText);
			}
		});
	});
	
	/**** Function Popovers ****/
	$('.functionName-tooltip').popover({ 
	    html : true, 
	    placement: 'right',
	    content: function() {
	      return $(this).siblings('.functionName-popover').html();
	    }
	});
	
	/**** Function recommendation ****/
	$(document).on("change", "#table1_name_f, #table2_name_f, #attr1_name_f, #attr2_name_f", function(event) {
 		setRecommendedFunctions();
	});
	
	/**** Select learned rules training data ****/
	$('#toggle_all').click(function(event) {
		if(this.checked) {
			// Iterate each checkbox
			$('.toggle_rule').each(function() {
				this.checked = true;
			});
		}
		else {
			$('.toggle_rule').each(function() {
				this.checked = false;
			});
		}
	});
	
	$(".toggle_rule").click(function() {
	    if (!this.checked) {
	        $("#toggle_all").attr('checked', false);
	    }
	});
	
	/**** Select learned rules active learning ****/
	$('#toggle_all_al').click(function(event) {
		if(this.checked) {
			// Iterate each checkbox
			$('.toggle_rule_al').each(function() {
				this.checked = true;
			});
		}
		else {
			$('.toggle_rule_al').each(function() {
				this.checked = false;
			});
		}
	});
	
	$(".toggle_rule_al").click(function() {
	    if (!this.checked) {
	        $("#toggle_all_al").attr('checked', false);
	    }
	});
	
	/**** Select all features during learning rules using active learning ****/
	$('#select_all_features_al').click(function() {
	    $('#feature_names_al option').prop('selected', true);
	});
	
	/**** Select no features during learning rules using active learning ****/
	$('#select_no_features_al').click(function() {
	    $('#feature_names_al option').prop('selected', false);
	});
	
	/**** Select all features during learning rules using training data ****/
	$('#select_all_features_td').click(function() {
	    $('#feature_names_td option').prop('selected', true);
	});
	
	/**** Select no features during learning rules using training data ****/
	$('#select_no_features_td').click(function() {
	    $('#feature_names_td option').prop('selected', false);
	});
	
	/**** Select all features during comparing models using cross validation ****/
	$('#select_all_features_cmcv').click(function() {
	    $('#feature_names_cmcv option').prop('selected', true);
	});
	
	/**** Select no features during comparing models using cross validation ****/
	$('#select_no_features_cmcv').click(function() {
	    $('#feature_names_cmcv option').prop('selected', false);
	});
	
	/**** Select all models during comparing models using cross validation ****/
	$('#select_all_models_cmcv').click(function() {
	    $('#model_names_cmcv option').prop('selected', true);
	});
	
	/**** Select no models during comparing models using cross validation ****/
	$('#select_no_models_cmcv').click(function() {
	    $('#model_names_cmcv option').prop('selected', false);
	});
	
	/**** Select all features during comparing models using train test evaluation ****/
	$('#select_all_features_cmtt').click(function() {
	    $('#feature_names_cmtt option').prop('selected', true);
	});
	
	/**** Select no features during comparing models using train test evaluation ****/
	$('#select_no_features_cmtt').click(function() {
	    $('#feature_names_cmtt option').prop('selected', false);
	});
	
	/**** Select all models during comparing models using train test evaluation ****/
	$('#select_all_models_cmtt').click(function() {
	    $('#model_names_cmtt option').prop('selected', true);
	});
	
	/**** Select no models during comparing models using train test evaluation ****/
	$('#select_no_models_cmtt').click(function() {
	    $('#model_names_cmtt option').prop('selected', false);
	});
	
	/**** Select all features when computing feature stats ****/
	$('#select_all_features_cfc').click(function() {
	    $('#feature_names_cfc option').prop('selected', true);
	});
	
	/**** Select no features when computing feature costs ****/
	$('#select_no_features_cfc').click(function() {
	    $('#feature_names_cfc option').prop('selected', false);
	});
	
	/**** Select all features when computing feature vectors ****/
	$('#select_all_features_cfv').click(function() {
	    $('#feature_names_cfv option').prop('selected', true);
	});
	
	/**** Select no features when computing feature vectors ****/
	$('#select_no_features_cfv').click(function() {
	    $('#feature_names_cfv option').prop('selected', false);
	});
	
	/**** Select all features when debugging a model using train test evaluation ****/
	$('#select_all_features_dmtt').click(function() {
	    $('#feature_names_dmtt option').prop('selected', true);
	});
	
	/**** Select no features when debugging a model using train test evaluation ****/
	$('#select_no_features_dmtt').click(function() {
	    $('#feature_names_dmtt option').prop('selected', false);
	});
	
	/**** Learning options for supervised learning of rules ****/
	$(".radioBtn").click(function() {
	    $("#test_data_div").hide();
	    $("#train_percent_div").hide();
	    $("#num_folds_div").hide();
	    if($('#option2').is(':checked')) {
	    	$("#test_data_div").show();
	    }
	    else if($('#option3').is(':checked')) {
	    	$("#train_percent_div").show();
	    }
	    else if($('#option4').is(':checked')) {
	    	$("#num_folds_div").show();
	    }
	});
	
	/**** Auto generate features ****/
	$('#table1_name_agf').on('change', function()  {
		var table1Name = $('#table1_name_agf').val();
		var projectName = $(this).attr("project_name");
		projectJsRoutes.controllers.project.ProjectController.getAttributes(projectName, table1Name).ajax({
			async: false,
    		success : function(data) {
    			var attribs = data["attributes"];
    			$(".attr1").html("");
    			$.each(attribs, function(index, attrib) {
    				$(".attr1").append("<option value=\"" + attrib + "\">" + attrib +"</option>");
    			});
		    },
	        error: function(xhr, status, error) {
  				$('body').html(xhr.responseText);
			}
		});
	});

	/**** Auto generate features ****/
	$('#table2_name_agf').on('change', function()  {
		var table2Name = $('#table2_name_agf').val();
		var projectName = $(this).attr("project_name");
		projectJsRoutes.controllers.project.ProjectController.getAttributes(projectName, table2Name).ajax({
			async: false,
    		success : function(data) {
    			var attribs = data["attributes"];
    			$(".attr2").html("");
    			$.each(attribs, function(index, attrib) {
    				$(".attr2").append("<option value=\"" + attrib + "\">" + attrib +"</option>");
    			});
		    },
	        error: function(xhr, status, error) {
  				$('body').html(xhr.responseText);
			}
		});
	});
	
	var attrPairCnt = 1;
	$('#btAdd_agf').click(function() {
		var projectName = $(this).attr("project_name");
		var table1Name = $("#table1_name_agf").val();
		var table2Name = $("#table2_name_agf").val();
		var attributes1;
		projectJsRoutes.controllers.project.ProjectController.getAttributes(projectName, table1Name).ajax({
			async: false,
    		success : function(data) {
    			attributes1 = data["attributes"];
		    },
	        error: function(xhr, status, error) {
  				$('body').html(xhr.responseText);
			}
		});
		var attributes2;
		projectJsRoutes.controllers.project.ProjectController.getAttributes(projectName, table2Name).ajax({
			async: false,
    		success : function(data) {
    			attributes2 = data["attributes"];
		    },
	        error: function(xhr, status, error) {
  				$('body').html(xhr.responseText);
			}
		});
		
        if(attrPairCnt == 0) {
			// Show the table header
        	$('#row_agf_0').show();
		}
        
		attrPairCnt = attrPairCnt + 1;
		// Add a row in the table
		var row_html = '<tr id="row_agf_' + attrPairCnt + '"><td>' + 
			'<select class="attr1" id="attr1_' + attrPairCnt + '" name="attr1_' + attrPairCnt +
			'" style="width:250px">';
		$.each(attributes1, function(index, attribute1) {
			row_html = row_html + '<option value="' + attribute1 + '">' + attribute1 +
						'</option>';
		});
		row_html = row_html + '</select></td><td>' +
			'<select class="attr2" id="attr2_' + attrPairCnt + '" name="attr2_' + attrPairCnt +
			'" style="width:250px">';
		$.each(attributes2, function(index, attribute2) {
			row_html = row_html + '<option value="' + attribute2 + '">' + attribute2 +
						'</option>';
		});
		row_html = row_html + '</select></td></tr>';
			
		$('#attribute_pairs_table tr:last').after(row_html);
		
		if(attrPairCnt == 1) {
			$('#btRemove_agf').removeAttr('disabled'); 
			$('#btRemove_agf').attr('class', 'bt');
			$('#btRemoveAll_agf').removeAttr('disabled'); 
			$('#btRemoveAll_agf').attr('class', 'bt');
		}
		
    });
	
	$('#btRemove_agf').click(function() {
		$('#row_agf_' + attrPairCnt).remove();
        attrPairCnt = attrPairCnt - 1;
        if (attrPairCnt == 0) {
        	// Hide the table header
        	$('#row_agf_0').hide();
        	$('#btRemove_agf').attr('class', 'bt-disable'); 
            $('#btRemove_agf').attr('disabled', 'disabled');
            $('#btRemoveAll_agf').attr('class', 'bt-disable'); 
            $('#btRemoveAll_agf').attr('disabled', 'disabled');
        }
    });
	
    $('#btRemoveAll_agf').click(function() {
        while(attrPairCnt > 0) {
        	$('#btRemove_agf').trigger('click');
        }
    });
	
    /**** Add matcher ****/
	var ruleCnt = 1;
	$('#btAdd_am').click(function() {
		var projectName = $(this).attr("project_name");
		var ruleNames;
		projectJsRoutes.controllers.project.RuleController.getAllRuleNames(projectName).ajax({
			async: false,
    		success : function(data) {
    			ruleNames = data["rules"];
    			if(ruleCnt == 0) {
    				// Show the table header
    	        	$('#row_am_0').show();
    			}
    	        
    			ruleCnt = ruleCnt + 1;
    			// Add a row in the table
    			var row_html = '<tr id="row_am_' + ruleCnt + '"><td>' + 
    				'<select class="rule_name" id="rule_name_' + ruleCnt +
    				'" name="rule_name_' + ruleCnt + '" style="width:250px">';
    			$.each(ruleNames, function(index, ruleName) {
    				row_html = row_html + '<option value="' + ruleName + '">' +
    					ruleName + '</option>';
    			});
    			row_html = row_html + '</select></td>';
    				
    			$('#matcher_table tr:last').after(row_html);
   
    			if(ruleCnt == 1) {
    				$('#btRemove_am').removeAttr('disabled'); 
    				$('#btRemove_am').attr('class', 'bt');
    				$('#btRemoveAll_am').removeAttr('disabled'); 
    				$('#btRemoveAll_am').attr('class', 'bt');
    			}
		    },
	        error: function(xhr, status, error) {
  				$('body').html(xhr.responseText);
			}
		});
    });
	
	$('#btRemove_am').click(function() {
		$('#row_am_' + ruleCnt).remove();
        ruleCnt = ruleCnt - 1;
        if (ruleCnt == 0) {
        	// Hide the table header
        	$('#row_am_0').hide();
        	$('#btRemove_am').attr('class', 'bt-disable'); 
            $('#btRemove_am').attr('disabled', 'disabled');
            $('#btRemoveAll_am').attr('class', 'bt-disable'); 
            $('#btRemoveAll_am').attr('disabled', 'disabled');
        }
    });
	
    $('#btRemoveAll_am').click(function() {
        while(ruleCnt > 0) {
        	$('#btRemove_am').trigger('click');
        }
    });
    
    /*
    $('.rule_name').on('change', function (e) {
    	var index = $(this).attr("name").substring(10);
    	alert("Triggered change by index " + index);
		var projectName = $("#table1_name_am").attr("project_name");
		//for (i = 1; i <= ruleCnt; i = i + 1) {
			//alert(i);
			var ruleName = $('#rule_name_' + index).find("option:selected").attr("value");
			//alert(ruleName);
			projectJsRoutes.controllers.project.RuleController.getRuleString(projectName,
				ruleName).ajax({
					success : function(data) {
						var ruleString = data["ruleString"];
						//alert(ruleString);
						$('#rule_string_' + index).val(ruleString);
					},
					error: function(xhr, status, error) {
						$('body').html(xhr.responseText);
					}
			});
	});
	*/
    
	$('.match-option-buttons input[type="radio"]').change(function() {
        //$('.match-option-buttons label').css('background', 'gray');   
        if ($(this).is(':checked')) {
            var idVal = $(this).attr("id"); //match_i or non_match_i or cannot_say_i
            var index = $(this).attr("name").substring(13); //match_option_
        	if ($("label[for='"+idVal+"']").text() == 'Match') {
        		$("label[for='non_match_"+index+"']").css('background', 'gray');
        		$("label[for='cannot_say_"+index+"']").css('background', 'gray');
        		$("label[for='"+idVal+"']").css('background', '#5CD65C');
        		$(".header_"+index).css('background', '#5CD65C');
        	}
        	else if ($("label[for='"+idVal+"']").text() == 'Non-match') {
        		$("label[for='match_"+index+"']").css('background', 'gray');
        		$("label[for='cannot_say_"+index+"']").css('background', 'gray');
        		$("label[for='"+idVal+"']").css('background', '#FF6666');
        		$(".header_"+index).css('background', '#FF6666');
        	}
        	else {
        		$("label[for='match_"+index+"']").css('background', 'gray');
        		$("label[for='non_match_"+index+"']").css('background', 'gray');
        		$("label[for='"+idVal+"']").css('background', 'cornflowerblue');
        		$(".header_"+index).css('background', 'cornflowerblue');
        	}
        } 
               
    });
	
});

function setRecommendedFunctions() {
	table1_name_f = $("#table1_name_f").val();
	table2_name_f = $("#table2_name_f").val();
	attr1_name_f = $("#attr1_name_f").val();
	attr2_name_f = $("#attr2_name_f").val();
	projectName = $("select[project_name]").attr("project_name");

	JsonMsg = {
	     "table1_name_f": table1_name_f,
	     "table2_name_f": table2_name_f,
	     "attr1_name_f": attr1_name_f,
	     "attr2_name_f": attr2_name_f
	};
	
	JsonMsg = JSON.stringify(JsonMsg);
	
	projectJsRoutes.controllers.project.RuleController.getRecommendedFunctions(projectName).ajax({
	
		contentType : 'application/json; charset=utf-8',	        
        data: JsonMsg,
        async: false,
		success : function(data) { 
			functionNames = eval(data["functionNames"]);   
		},
        error: function(xhr, status, error) {
			$('body').html(xhr.responseText);
		}
	});	
	
	$("#add_feature_modal").find("#function_name").html("");
	$.each(functionNames, function(index, f) {
		$("#add_feature_modal").find("#function_name").append("<option value=\"" + f + "\">" + f +"</option>");
	});
	$("#function_count").html(functionNames.length);
}

function setRecommendedFunctionsForEditFeatureGui() {
	var table1Name = $("#table1_name_f_gui").val();
	var table2Name = $("#table2_name_f_gui").val();
	var attr1Name = $("#attr1_name_f_gui").val();
	var attr2Name = $("#attr2_name_f_gui").val();
	var projectName = $("select[project_name]").attr("project_name");
	var featureName = $("#feature_name_ed_gui").val();
	
	//alert("table 1: " + table1Name + ", table 2: " + table2Name + ", attr1:" +
			//attr1Name + ", attr2: " + attr2Name + ", project: " + projectName);
	
	JsonMsg = {
	     "table1_name_f": table1Name,
	     "table2_name_f": table2Name,
	     "attr1_name_f": attr1Name,
	     "attr2_name_f": attr2Name
	};
	
	JsonMsg = JSON.stringify(JsonMsg);
	
	projectJsRoutes.controllers.project.RuleController.getRecommendedFunctions(projectName).ajax({
		contentType : 'application/json; charset=utf-8',	        
        data: JsonMsg,
        async: false,
		success : function(data) { 
			functionNames = eval(data["functionNames"]);   
		},
        error: function(xhr, status, error) {
			$('body').html(xhr.responseText);
		}
	});	
	
	$("#edit_feature_using_gui_modal").find("#function_name_gui").html("");
	$.each(functionNames, function(index, f) {
		$("#edit_feature_using_gui_modal").find("#function_name_gui").append("<option value=\"" + f + "\">" + f +"</option>");
	});
	$("#function_count_gui").html(functionNames.length);
	
	projectJsRoutes.controllers.project.RuleController.getFunctionForFeature(projectName, featureName).ajax({
		success : function(data) {
			var functionName = data["functionName"];
			//alert("function name: " + functionName);
			$('#function_name_gui').find('option[value="' + functionName + '"]').attr("selected",true);
	    },
        error: function(xhr, status, error) {
				$('body').html(xhr.responseText);
		}
	});
}

/*
function addTableRowHtml() {
	$('#rule_table tr:last').after('<tr id="row' + iCnt + '"><td>' + 
										'<select id="feature' + iCnt +
										'" name="feature' + iCnt +
										'" style="width:190px">' +
										'</select></td><td>' +
										'<select id="op' + iCnt +
										'" name="op' + iCnt +
										'" style="width:210px">' +
										'</select></td><td>' +
										'<input type="text" id="val' + iCnt +
										'" name="val' + iCnt +
										'" style="width:50px" required>' +
									'</td></tr>');
}

function addTableHeaderHtml() {
	$('#rule_table tr:last').after('<tr id="row' + iCnt + '">'
										'<th>Feature</th>' +
										'<th>Operator</th>' +
										'<th>Value</th>' +
									'</tr>');
}
*/