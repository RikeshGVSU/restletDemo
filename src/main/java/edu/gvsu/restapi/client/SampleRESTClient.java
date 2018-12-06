package edu.gvsu.restapi.client;

import java.io.IOException;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Client;
import org.restlet.data.*;
import org.restlet.*;
import org.restlet.representation.Representation;

/**
 * Sample client program that uses the RESTlet framework to access a RESTful web service.
 * @author Jonathan Engelsma (http://themobilemontage.com)
 *
 */
public class SampleRESTClient
{

	// The base URL for all requests.
    public static final String APPLICATION_URI = "http://localhost:8080";

    public static void main(String args[]) {

    	
    	
    	

		// EXAMPLE HTTP REQUEST #1 - Let's create a new widget!
		// This is how you create a www form encoded entity for the HTTP POST request.
	    Form form = new Form();
	    form.add("userInput","NewTest2");
	    form.add("host","myHost2");
	    form.add("port","102");

	    // construct request to create a new widget resource
	    String widgetsResourceURL = APPLICATION_URI + "/users";
	    Request request = new Request(Method.POST,widgetsResourceURL);

	    // set the body of the HTTP POST command with form data.
	    request.setEntity(form.getWebRepresentation());

	    // Invoke the client HTTP connector to send the POST request to the server.
	    System.out.println("Sending an HTTP POST to " + widgetsResourceURL + ".");
	    Response resp = new Client(Protocol.HTTP).handle(request);

	    // now, let's check what we got in response.
	    System.out.println(resp.getStatus());
	    Representation responseData = resp.getEntity();
	    try {
			System.out.println(responseData.getText());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}



		// EXAMPLE HTTP REQUEST #2
		// Let's do an HTTP GET of widget 1 and ask for JSON response.
//		widgetsResourceURL = APPLICATION_URI + "/users/NewTest";
//	    request = new Request(Method.GET,widgetsResourceURL);
//
//	    // We need to ask specifically for JSON
//        request.getClientInfo().getAcceptedMediaTypes().
//        add(new Preference(MediaType.APPLICATION_JSON));
//
//	    // Now we do the HTTP GET
//	    System.out.println("Sending an HTTP GET to " + widgetsResourceURL + ".");
//		resp = new Client(Protocol.HTTP).handle(request);
//
//		// Let's see what we got!
//		if(resp.getStatus().equals(Status.SUCCESS_OK)) {
//			responseData = resp.getEntity();
//			System.out.println("Status = " + resp.getStatus());
//			try {
//				String jsonString = responseData.getText().toString();
//				System.out.println("result text=" + jsonString);
//				JSONObject jObj = new JSONObject(jsonString);
//				System.out.println("name=" + jObj.getString("userName") + " host=" + jObj.getString("host") + " port=" + jObj.getInt("port") + " status=" + jObj.getBoolean("status"));
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (JSONException je) {
//				je.printStackTrace();
//			}
//		}

		// TODO: EXAMPLE HTTP REQUEST #3
		// Do an HTTP PUT to change the name of widget 1 to "An Old Stale Widget".


		// TODO: EXAMPLE HTTP REQUEST #4
		// Do an HTTP DELETE to delete widget 1 from the server.
		
//		widgetsResourceURL = APPLICATION_URI + "/users/NewTest2";
//	    request = new Request(Method.DELETE,widgetsResourceURL);
//	    
//	    request.getClientInfo().getAcceptedMediaTypes().
//        add(new Preference(MediaType.APPLICATION_JSON));
//
//	    // Now we do the HTTP GET
//	    System.out.println("Sending an HTTP DELETE to " + widgetsResourceURL + ".");
//		resp = new Client(Protocol.HTTP).handle(request);

		// TODO: Example HTTP REQUEST #5
		// DO an HTTP GET for a resource with id=999.
		
		
		// TODO: Get the list of all the users
		
		widgetsResourceURL = APPLICATION_URI + "/users";
	    request = new Request(Method.GET,widgetsResourceURL);
	    
	    request.getClientInfo().getAcceptedMediaTypes().
        add(new Preference(MediaType.APPLICATION_JSON));

	    // Now we do the HTTP GET
	    System.out.println("Sending an HTTP DELETE to " + widgetsResourceURL + ".");
		resp = new Client(Protocol.HTTP).handle(request);
		
		if(resp.getStatus().equals(Status.SUCCESS_OK)) {
			responseData = resp.getEntity();
			System.out.println("Status = " + resp.getStatus());
			try {
				String jsonString= responseData.getText().toString();
				System.out.println("result text=" + jsonString);
				//JSONObject jObj = new JSONObject(responseData.getText().toString());
				JSONArray jsonArray = new JSONArray(jsonString);
				for (int i = 0; i < jsonArray.length(); i++) {
					JSONObject jObj = jsonArray.getJSONObject(i);
//					String userName = jObj.getString("userName");
//					System.out.println(jObj);
				
					System.out.println("name=" + jObj.getString("userName") + " host=" + jObj.getString("host") + " port=" + jObj.getInt("port") + " status=" + jObj.getBoolean("status"));
				}
//				while(keys.hasNext()) {
//					System.out.println("has nw=ext json");s
//				    String key = keys.next();
//				    //if (jObj.get(key) instanceof JSONObject) {
//				    	System.out.println("name=" + jObj.getString("userName") + " host=" + jObj.getString("host") + " port=" + jObj.getInt("port") + " status=" + jObj.getBoolean("status"));
//
//				    //}
//				}
				//System.out.println("name=" + jObj.getString("userName") + " host=" + jObj.getString("host") + " port=" + jObj.getInt("port") + " status=" + jObj.getBoolean("status"));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException je) {
				je.printStackTrace();
			}
		}
		
		//Update
	 // EXAMPLE HTTP REQUEST #1 - Let's create a new widget!
//		// This is how you create a www form encoded entity for the HTTP POST request.
//	    Form form = new Form();
//	    form.add("userInput","busy");
//
//	    // construct request to create a new widget resource
//	    widgetsResourceURL = APPLICATION_URI + "/users/NewTest";
//	    request = new Request(Method.PUT,widgetsResourceURL);
//
////	    // set the body of the HTTP POST command with form dat
//	    request.setEntity(form.getWebRepresentation());
////
////	    // Invoke the client HTTP connector to send the POST request to the server.
////	    System.out.println("Sending an HTTP POST to " + widgetsResourceURL + ".");
//	    resp = new Client(Protocol.HTTP).handle(request);
////
////	    // now, let's check what we got in response.
////	    System.out.println(resp.getStatus());
//	    responseData = resp.getEntity();
//	    try {
//			System.out.println(responseData.getText());
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
    }
}
