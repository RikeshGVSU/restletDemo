package edu.gvsu.restapi.client;

/**
 * <p>Title: Lab2</p>
 * <p>Description: Old School Instant Messaging Application </p>
 * @author Jonathan Engelsma
 * @version 1.0
 */

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.Scanner;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Client;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Preference;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.representation.Representation;

import java.net.*;
import java.io.*;

/**
 * A simple chat client.
 */
public class ChatClient 
{

	// The base URL for all requests.
    public static final String APPLICATION_URI = "https://cis656lab5-224401.appspot.com";
//    public static final String APPLICATION_URI = "http://localhost:8080";
	//PresenceService nameServer;
    ServerSocket serviceSkt = null;
    SvrThread svrThread;
    RegistrationInfo regInfo;

    /**
     * Constructor.
     * @param uname The name of the chimp running the client.
     * @parm hostPortStr The host/port string in the form host:port
     * where the ":port" portion is optional. This is the host/port
     * of the presence service we are connecting to.  If set to null,
     * we'll attempt to connect to port 1099 on the localhost.
     */
    public ChatClient(String uname,String hostPortStr)
    {
        // Step 0. Figure out local host name.
        String myHost;
        try {
            myHost = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            myHost = "localhost";
        }

        // Step 1. We need to establish a server socket where we will listen for
        // incoming chat requests.
        try {
            this.serviceSkt = new ServerSocket(0);
        } catch (IOException e) {
            System.out.println("Error: Couldn't allocate local socket endpoint.");
            System.exit(-1);
        }


        // Step 2. we bind to the nameserver so we can register our client.
        if(hostPortStr == null) {
            hostPortStr = myHost;
        }
        System.out.println("User name is " + uname);


        System.out.println("Registering...");

     // EXAMPLE HTTP REQUEST #1 - Let's create a new user!
		// This is how you create a www form encoded entity for the HTTP POST request.
        int port = this.serviceSkt.getLocalPort();
        regInfo = new RegistrationInfo(uname,myHost,port,true);
	    Form form = new Form();
	    form.add("userInput",uname);
	    form.add("host",myHost);
	    form.add("port",Integer.toString(port));

	    // construct request to create a new user resource
	    String userResourceURL = APPLICATION_URI + "/users";
	    Request request = new Request(Method.POST,userResourceURL);

	    // set the body of the HTTP POST command with form data.
	    request.setEntity(form.getWebRepresentation());

	    // Invoke the client HTTP connector to send the POST request to the server.
	    //System.out.println("Sending an HTTP POST to " + userResourceURL + ".");
	    Response resp = new Client(Protocol.HTTP).handle(request);

	    // now, let's check what we got in response.
	    //System.out.println(resp.getStatus());
	    Representation responseData = resp.getEntity();
//	    try {
//			System.out.println(responseData.getText());
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

        // Step 5. Kick off a separate thread to listen to incoming requests on the
        // Server socket.
        this.svrThread = new SvrThread();
        Thread t = new Thread(this.svrThread);
        t.start();
    }



    /**
     * Simple command shell that interprets commands from the user.
     */
    public void runCmdShell()
    {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        boolean done = false;
        String cmd;

        // Read and process commands from standard input, until done.
        while(!done) {

        	this.promptUser();

            try {

                // Read an input line.
                cmd = in.readLine();

                // If we have a valid command, try to parse/process it.
                if(cmd!=null) {

                    if(cmd.toLowerCase().trim().startsWith("chat")) {
                        // look up user in the presence server.
                        String str = cmd.toLowerCase().trim();
                        int pos = str.indexOf(' ');
                        if(pos == -1) {
                            System.out.println("Missing userName.  Enter: chat {username} {msg}");
                            continue;
                        }
                        str = str.substring(pos+1);
                        pos = str.indexOf(' ');
                        String name;
                        String msg;
                        if(pos == -1) {
                            name = str;
                            msg = "";
                        } else {
                            name = str.substring(0,pos);
                            msg = str.substring(pos+1);
                        }

                        if(!lookupAndSendMsg(name,msg)) {
                            System.out.println("'" + name + "' is not currently online or is unavailable.");
                            continue;
                        }

                    } else if(cmd.toLowerCase().trim().startsWith("friends")) {

                    	String userResourceURL = APPLICATION_URI + "/users";
                    	Request request = new Request(Method.GET,userResourceURL);
                	    
                	    request.getClientInfo().getAcceptedMediaTypes().
                        add(new Preference(MediaType.APPLICATION_JSON));

                	    // Now we do the HTTP GET
                	    System.out.println("Sending an HTTP DELETE to " + userResourceURL + ".");
                	    Response resp = new Client(Protocol.HTTP).handle(request);
                		
                		if(resp.getStatus().equals(Status.SUCCESS_OK)) {
                			Representation responseData = resp.getEntity();
                			System.out.println("Status = " + resp.getStatus());
                			try {
                				String jsonString= responseData.getText().toString();
                				//System.out.println("result text=" + jsonString);
                				JSONArray jsonArray = new JSONArray(jsonString);
                				for (int i = 0; i < jsonArray.length(); i++) {
                					JSONObject jObj = jsonArray.getJSONObject(i);
                					String status = jObj.getBoolean("status")? "Available":"Busy";
                					System.out.println("name: " + jObj.getString("userName") + " - " + status);
                				}
                			} catch (IOException e) {
                				// TODO Auto-generated catch block
                				e.printStackTrace();
                			} catch (JSONException je) {
                				je.printStackTrace();
                			}
                		}

                    } else if(cmd.toLowerCase().trim().startsWith("broadcast")) {

                        int pos = cmd.indexOf(' ');
                        if(pos == -1) {
                            System.out.println("Missing the message.  Enter: broadcast {msg}");
                            continue;
                        }
                        String msg = cmd.substring(pos+1);
                        String userResourceURL = APPLICATION_URI + "/users";
                    	Request request = new Request(Method.GET,userResourceURL);
                	    
                	    request.getClientInfo().getAcceptedMediaTypes().
                        add(new Preference(MediaType.APPLICATION_JSON));

                	    // Now we do the HTTP GET
                	    System.out.println("Sending an HTTP DELETE to " + userResourceURL + ".");
                	    Response resp = new Client(Protocol.HTTP).handle(request);
                		
                		if(resp.getStatus().equals(Status.SUCCESS_OK)) {
                			Representation responseData = resp.getEntity();
                			System.out.println("Status = " + resp.getStatus());
                			try {
	            				String jsonString= responseData.getText().toString();
	            				//System.out.println("result text=" + jsonString);
	            				//JSONObject jObj = new JSONObject(responseData.getText().toString());
	            				JSONArray jsonArray = new JSONArray(jsonString);
	            				for (int i = 0; i < jsonArray.length(); i++) {
	            					JSONObject jObj = jsonArray.getJSONObject(i);
	            					System.out.println("name=" + jObj.getString("userName") + " status=" + jObj.getBoolean("status"));
	            					String userName = jObj.getString("userName");
	                                // Don't broadcast to the local client!
	                                if(!userName.equals(this.regInfo.getUserName())) {
	                                	RegistrationInfo client = new RegistrationInfo(userName,jObj.getString("host"),jObj.getInt("port"),jObj.getBoolean("status"));
	                                    System.out.print("Sending message to " + userName + " ... " );
	                                    if(!this.sendMsgToKnownUser(client, msg)) {
	                                        System.out.println("failed (unavailable).");
	                                    } else {
	                                        System.out.println("Done!");
	                                    }
	                                }
	            				}
	            			} catch (IOException e) {
	            				// TODO Auto-generated catch block
	            				e.printStackTrace();
	            			} catch (JSONException je) {
	            				je.printStackTrace();
	            			}
                		}
                        
                    } else if(cmd.toLowerCase().trim().startsWith("busy")) { 
                    	 Form form = new Form();
                 	    form.add("userInput","busy");

                 	    // construct request to create a new user resource
                 	    String userResourceURL = APPLICATION_URI + "/users/" + this.regInfo.getUserName();
                 	    Request request = new Request(Method.PUT,userResourceURL);

                 	    // set the body of the HTTP PUT command with form data
                 	    request.setEntity(form.getWebRepresentation());
                 
                 	    // Invoke the client HTTP connector to send the POST request to the server.
                 	    System.out.println("Sending an HTTP PUT to " + userResourceURL + ".");
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
                    		
                    } else if(cmd.toLowerCase().trim().startsWith("available")) { 
                    	Form form = new Form();
                 	    form.add("userInput","available");

                 	    // construct request to create a new user resource
                 	    String userResourceURL = APPLICATION_URI + "/users/" + this.regInfo.getUserName();
                 	    Request request = new Request(Method.PUT,userResourceURL);


                 	    // set the body of the HTTP POST command with form data
                 	    request.setEntity(form.getWebRepresentation());

                 	    // Invoke the client HTTP connector to send the POST request to the server.
                 	    //System.out.println("Sending an HTTP POST to " + userResourceURL + ".");
                 	    Response resp = new Client(Protocol.HTTP).handle(request);

                 	    // now, let's check what we got in response.
                 	    //System.out.println(resp.getStatus());
                 	   Representation responseData = resp.getEntity();
                 	    try {
                 			System.out.println(responseData.getText());
                 		} catch (IOException e) {
                 			// TODO Auto-generated catch block
                 			e.printStackTrace();
                 		}
                    			

                    } else if(cmd.toLowerCase().trim().startsWith("exit")) {

                    	String userResourceURL = APPLICATION_URI + "/users/" + this.regInfo.getUserName();
                	    Request request = new Request(Method.DELETE,userResourceURL);
                	    
                	    request.getClientInfo().getAcceptedMediaTypes().
                        add(new Preference(MediaType.APPLICATION_JSON));
                
                	    // Now we do the HTTP GET
                	    System.out.println("Sending an HTTP DELETE to " + userResourceURL + ".");
                		Response resp = new Client(Protocol.HTTP).handle(request);
                		System.out.println("Have a good day!");
                		done = true;
                		System.exit(0);
                    } else {
                        System.out.println("Hmm, not sure what you meant there. Try again.");
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Send a message to a user with the given name.
     * @param userName The name of the user you wish to send the message to.
     * @param msg The msg string you wish to send the user.
     * @return true if the message was sent, false otherwise.
     */
    private boolean lookupAndSendMsg(String userName, String msg)
    {
        boolean retval = false;
        RegistrationInfo regInfSendMsg = null;
        String userResourceURL = APPLICATION_URI + "/users/" + userName;
		Request request = new Request(Method.GET,userResourceURL);
   
		// We need to ask specifically for JSON
		request.getClientInfo().getAcceptedMediaTypes().
		add(new Preference(MediaType.APPLICATION_JSON));
   
		// Now we do the HTTP GET
		//System.out.println("Sending an HTTP GET to " + userResourceURL + ".");
		Response resp = new Client(Protocol.HTTP).handle(request);
   
		// Let's see what we got!
		if(resp.getStatus().equals(Status.SUCCESS_OK)) {
			Representation responseData = resp.getEntity();
			//System.out.println("Status = " + resp.getStatus());
			
			try {
				String jsonString = responseData.getText().toString();
				//System.out.println("result text=" + jsonString);
				JSONObject jObj = new JSONObject(jsonString);
				if (jObj.has("userName")) {
					regInfSendMsg = new RegistrationInfo(jObj.getString("userName"),jObj.getString("host"),jObj.getInt("port"),jObj.getBoolean("status"));
					retval = this.sendMsgToKnownUser(regInfSendMsg, msg);
					}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException je) {
				je.printStackTrace();
			}
		}
        return retval;
    }
    
    /**
     * Send a message to a user with the given RegistrationInfo.
     * @param reg The RegistrationInfo of the user you wish to send the message to.
     * @param msg The msg string you wish to send the user.
     * @return true if the message was sent, false otherwise.
     */
    private boolean sendMsgToKnownUser(RegistrationInfo reg, String msg)
    {
    	boolean retval = true;
        if(reg==null || !reg.getStatus()) {
            // User is not registered!
            retval = false;
        } else {
            try {
                // open a socket connection remote user's client and send message.
                Socket skt = new Socket(reg.getHost(),reg.getPort());
                String completeMsg = "Message from " + this.regInfo.getUserName() + ": " + msg + "\n";
                skt.getOutputStream().write(completeMsg.getBytes());
                skt.close();
            } catch (Exception e) {
                // hmmm, user was registered, but it looks like they suddenly went away.
                //e.printStackTrace();
                retval = false;
            }
        }
        return retval;
    }

    private void promptUser()
    {
    	if(this.regInfo.getStatus()) {
    		System.out.println(this.regInfo.getUserName() + ": Enter command (friends, chat, broadcast, busy, or exit):");
    	} else {
    		System.out.println(this.regInfo.getUserName() + ": Enter command (friends, chat, broadcast, available, or exit):");
    	}
    }
    /**
     * Simple inner class that implements the thread that will be responsible
     * for handling incoming chat messages.
     */
    class SvrThread implements Runnable
    {
        // We'll use this to flag when the thread can stop accepting new
        // connections and exit.
        boolean done = false;

        /**
         * Thread's entry point.
         */
        public void run()
        {

            //
            // wait for incoming requests.
            //
            while(!done) {
                Socket clientSocket = null;
                try {
                    clientSocket = serviceSkt.accept();
                } catch (IOException e) {
                    System.out.println("Error: failed to accept remote connection.");
                }


                //
                // Might have shutdown while  waiting for request...
                //
                if(done) {
                    break;
                }

                //
                // Process incoming chat message right here.
                //
                byte buf[] = new byte[2048];
                try {
                    int cnt = clientSocket.getInputStream().read(buf,0,2048);
                    String msg = new String(buf,0,cnt);

                    // We'll refresh the prompt, lest the chimp on the console
                    // get's confused.
                    System.out.println(msg);
                    ChatClient.this.promptUser();
                    clientSocket.close();

                } catch (IOException ie) {
                }

            }

            // ok, we're outta here.  Turn the lights out before you leave.
            try {
                ChatClient.this.serviceSkt.close();
            } catch (IOException e) {
                System.out.println("Warning: caught IOException while closing socket.");
            }
            System.out.println("Server thread is exiting.");
        }
        

        /**
         * This is how we signal the ServerSocket thread which is likely to
         * be happily camped out on an accept() when the chimp types exit.
         */
        public void stop()
        {
            // set done to true.
            done = true;

            //
            // Just in case svr thread is blocked on accept, we give it a nudge.
            //
            Socket skt;
            try  {
                skt = new Socket(InetAddress.getLocalHost(),ChatClient.this.regInfo.getPort());
                skt.close();
            } catch (Exception e) {
            }
        }
    }

    /**
     * Main routine for client process.
     */
    public static void main(String[] args)
    {
//        if(args.length == 0) {
//            System.out.println("Usage: \n\tjava ChatClient userName [host[:port]]\n");
//            System.out.println("\nwhere userName is your username, and host/port is the name service.");
//            System.exit(0);
//        }
    	
    	System.out.println("User Name: ");
    	Scanner s = new Scanner(System.in);
    	String username = s.nextLine();
    	

        // Create a client object.
        ChatClient myClient;
        myClient = new ChatClient(username,null);
//        if(args.length > 1) {
//            myClient = new ChatClient(args[0], args[1]);
//        } else {
//            myClient = new ChatClient(args[0],null);
//        }

        // Now we will process chat commands.
        myClient.runCmdShell();
    }
}
