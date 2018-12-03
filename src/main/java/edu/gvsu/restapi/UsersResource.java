package edu.gvsu.restapi;

import java.net.ServerSocket;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import com.googlecode.objectify.ObjectifyService;

public class UsersResource  extends ServerResource {

	private List<RegistrationInfo> registeredUsers = null;
	ServerSocket serviceSkt = null;

	@SuppressWarnings("unchecked")
	@Override
	public void doInit() {

    this.registeredUsers = ObjectifyService.ofy()
        .load()
        .type(RegistrationInfo.class) // We want only Widgets
        .list();

		// these are the representation types this resource can use to describe the
		// set of widgets with.
		getVariants().add(new Variant(MediaType.TEXT_HTML));
		getVariants().add(new Variant(MediaType.APPLICATION_JSON));

	}



	/**
	 * Handle an HTTP GET. Represent the widget object in the requested format.
	 *
	 * @param variant
	 * @return
	 * @throws ResourceException
	 */
	@Get
	public Representation get(Variant variant) throws ResourceException {
		Representation result = null;
		if (null == this.registeredUsers) {
			ErrorMessage em = new ErrorMessage();
			return representError(variant, em);
		} else {

			if (variant.getMediaType().equals(MediaType.APPLICATION_JSON)) {

				JSONArray usersArray = new JSONArray();
				for(Object o : this.registeredUsers) {
					RegistrationInfo rInf = (RegistrationInfo)o;
					usersArray.put(rInf.toJSON());
				}
//				HashMap<String,RegistrationInfo> rInf = new HashMap<String,RegistrationInfo>();
//				for(RegistrationInfo o : this.registeredUsers) {
//					rInf.put(o.getUserName(), o);
//					//usersArray.put(rInf.toJSON());
//				}

				result = new JsonRepresentation(usersArray);

			} else {

				// create a plain text representation of our list of widgets
				StringBuffer buf = new StringBuffer("<html><head><title>Registered Users</title><head><body><h1>Users</h1>");
				buf.append("<form name=\"input\" action=\"/users\" method=\"POST\">");
				buf.append("User Input: ");
				buf.append("<input type=\"text\" name=\"userInput\" />");
				buf.append("<input type=\"hidden\" name=\"host\" />");
				buf.append("<input type=\"hidden\" name=\"port\" />");
				buf.append("<input type=\"hidden\" name=\"status\" />");
				buf.append("<input type=\"submit\" value=\"Submit\" />");
				buf.append("</form>");
				buf.append("<br/><h2> There are " + this.registeredUsers.size() + " total.</h2>");
				for(Object o : this.registeredUsers) {
					RegistrationInfo rInf = (RegistrationInfo)o;
					buf.append(rInf.toHtml(true));
				}
				buf.append("</body></html>");
				result = new StringRepresentation(buf.toString());
				result.setMediaType(MediaType.TEXT_HTML);
			}
		}
		return result;
	}

	/**
	 * Handle a POST Http request. Create a new user
	 *
	 * @param entity
	 * @throws ResourceException
	 */
	@Post
	public Representation post(Representation entity, Variant variant)
		throws ResourceException
	{

		Representation rep = null;

		// We handle only a form request in this example. Other types could be
		// JSON or XML.
		try {
			if (entity.getMediaType().equals(MediaType.APPLICATION_WWW_FORM,
					true))
			{
				// Use the incoming data in the POST request to create/store a new widget resource.
				Form form = new Form(entity);
				RegistrationInfo rInf = new RegistrationInfo();
				rInf.setUserName(form.getFirstValue("userInput"));
				rInf.setHost(form.getFirstValue("host"));
				rInf.setPort(Integer.parseInt(form.getFirstValue("port")));
				rInf.setStatus(true);

				// persist updated object
				ObjectifyService.ofy().save().entity(rInf).now();

				getResponse().setStatus(Status.SUCCESS_OK);
				rep = new StringRepresentation(rInf.toHtml(false));
				rep.setMediaType(MediaType.TEXT_HTML);
				getResponse().setEntity(rep);

			} else {
				getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			}
		} catch (Exception e) {
			getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
		}
		return rep;
	}

	/**
	 * Represent an error message in the requested format.
	 *
	 * @param variant
	 * @param em
	 * @return
	 * @throws ResourceException
	 */
	private Representation representError(Variant variant, ErrorMessage em)
	throws ResourceException {
		Representation result = null;
		if (variant.getMediaType().equals(MediaType.APPLICATION_JSON)) {
			result = new JsonRepresentation(em.toJSON());
		} else {
			result = new StringRepresentation(em.toString());
		}
		return result;
	}

	protected Representation representError(MediaType type, ErrorMessage em)
	throws ResourceException {
		Representation result = null;
		if (type.equals(MediaType.APPLICATION_JSON)) {
			result = new JsonRepresentation(em.toJSON());
		} else {
			result = new StringRepresentation(em.toString());
		}
		return result;
	}
}
