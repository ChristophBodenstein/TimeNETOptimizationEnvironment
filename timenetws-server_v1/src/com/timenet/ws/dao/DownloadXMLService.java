package com.timenet.ws.dao;
/**
 * auto GeneratedValue
 * @author Veeranna
 */
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import com.timenet.ws.models.simulationList;
import com.timenet.ws.util.HibernateTemplate;

@Path("/api")
public class DownloadXMLService {
	private Response Response;


	@GET
	@Path("/downloads/{flag}")
	public Response getPreview(@PathParam("flag") String flag) {
		
		
		simulationList std =null;
		simulationList eList =null;
	    String SQLQuerry=" from "+ simulationList.class.getName()+" where distribute_flag=?";
	    std=(simulationList) HibernateTemplate.findList(SQLQuerry, 0, 1, flag).get(0);
		
		if (std!=null) {
			ResponseBuilder response = Response.ok((Object) std.getFileData());
			response.type(MediaType.APPLICATION_XML);
			response.header("Content-Disposition","attachment; filename="+std.getSimulation_file_name());
		System.out.println("**********Simulation File Download **********"+std.getSimulation_file_name());
		
		// Write the code to fetch the record by Id and update the Flag ND to D
		// Use Hibernate update function.
		std.setDistribute_flag("D");
		HibernateTemplate.update(std);
		System.out.println("**********Distribute_flag set as DELIVERED **********"+std.getSimulation_file_name());
		return response.build();
		
		}
		else {
			Response.status(401);
			return Response;
		 }
	}
	

	
	
}