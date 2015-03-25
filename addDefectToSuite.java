import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.rallydev.rest.RallyRestApi;
import com.rallydev.rest.request.CreateRequest;
import com.rallydev.rest.request.QueryRequest;
import com.rallydev.rest.request.UpdateRequest;
import com.rallydev.rest.response.CreateResponse;
import com.rallydev.rest.response.QueryResponse;
import com.rallydev.rest.response.UpdateResponse;
import com.rallydev.rest.util.Fetch;
import com.rallydev.rest.util.QueryFilter;
import com.rallydev.rest.util.Ref;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class addDefectToSuite {

	public static void main(String[] args) throws URISyntaxException, IOException {

		String host = "https://rally1.rallydev.com";
	        String username = "user@company.com";
	        String password = "secret";
	        String wsapiVersion = "v2.0";
	        String projectRef = "/project/12352608219";      
	        String workspaceRef = "/workspace/12352608129"; 
	        String applicationName = "Create defect, add to a defectsuite";
	        
		
        RallyRestApi restApi = new RallyRestApi(
        		new URI(host),
        		username,
        		password);
        restApi.setWsapiVersion(wsapiVersion);
        restApi.setApplicationName(applicationName);   
        
        QueryRequest defectSuiteRequest = new QueryRequest("DefectSuite");
        defectSuiteRequest.setFetch(new Fetch("FormattedID","Name", "Defects"));
        defectSuiteRequest.setWorkspace(workspaceRef);
        defectSuiteRequest.setQueryFilter(new QueryFilter("FormattedID", "=", "DS1"));
        QueryResponse defectSuiteQueryResponse = restApi.query(defectSuiteRequest);
        JsonObject defectSuiteJsonObject = defectSuiteQueryResponse.getResults().get(0).getAsJsonObject();
        System.out.println("defectSuiteJsonObject" + defectSuiteJsonObject);
        String defectSuiteRef = defectSuiteJsonObject.get("_ref").getAsString(); 
        int numberOfDefects = defectSuiteJsonObject.getAsJsonObject("Defects").get("Count").getAsInt();
        System.out.println(defectSuiteJsonObject.get("Name") + " ref: " + defectSuiteRef + "number of defects: " + numberOfDefects + " " + defectSuiteJsonObject.get("Defects"));
       
        
        try {
        	JsonObject defect = new JsonObject();
        	defect.addProperty("Name", "bad defect 668");
  
            CreateRequest createRequest = new CreateRequest("defect", defect);
            CreateResponse createResponse = restApi.create(createRequest);
            if (createResponse.wasSuccessful()) {
            	JsonObject defectJsonObject = createResponse.getObject();
            	String defectRef = Ref.getRelativeRef(createResponse.getObject().get("_ref").getAsString());
            	System.out.println(String.format("Created %s", defectRef));  
            	JsonObject defectSuitesOfThisDefect = (JsonObject) defectJsonObject.get("DefectSuites");
            	int numberOfSuites = defectSuitesOfThisDefect.get("Count").getAsInt();
            	System.out.println("number of defect suites this defect is part of: " + numberOfSuites);
            	QueryRequest defectSuitesOfThisDefectRequest = new QueryRequest(defectSuitesOfThisDefect);
            	JsonArray suites = restApi.query(defectSuitesOfThisDefectRequest).getResults();
            	System.out.println("suites: " + suites);
    	        suites.add(defectSuiteJsonObject);
    	        System.out.println("suites after add: " + suites);
	            //Update defect: add to defectsutites collection
	            JsonObject defectUpdate = new JsonObject();
		        defectUpdate.add("DefectSuites", suites);
		        UpdateRequest updateDefectRequest = new UpdateRequest(defectRef,defectUpdate);
		        UpdateResponse updateResponse = restApi.update(updateDefectRequest);
		        if (updateResponse.wasSuccessful()) {
					System.out.println("Successfully updated defect: " + defectJsonObject.get("FormattedID"));
		        }
		        else {
		        	String[] updateErrors;
	            	updateErrors = updateResponse.getErrors();
	        		System.out.println("Error");
	            	for (int i=0; i<updateErrors.length;i++) {
	            		System.out.println(updateErrors[i]);
	            	}
		        }
		        
            } else {
            	System.out.println("error");
            	String[] createErrors;
            	createErrors = createResponse.getErrors();
            	for (int i=0; i<createErrors.length;i++) {
            		System.out.println(createErrors[i]);
            	}
            }
	
        } finally {
            restApi.close();
        }   

	} 
}
