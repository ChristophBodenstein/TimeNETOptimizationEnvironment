package com.timenet.ws.dao;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import com.timenet.ws.models.logList;
import com.timenet.ws.models.simulationList;
import com.timenet.ws.util.HibernateTemplate;





@Path("/api")
public class DownloadLogServeice {
	private Response Response;


	@GET
	@Path("/downloads/log/{simid}")
	public Response getPreview(@PathParam("simid") String simid) {
		
		
		logList std =null;
		logList eList =null;
	    String SQLQuerry=" from "+ logList.class.getName()+" where distribute_flag=? and upload_sim_manager=?";
	    std=(logList) HibernateTemplate.findList(SQLQuerry, 0, 1, "ND", simid).get(0);
		//TODO What if list is empty??? Build empt response? get(0)->Nullpointerexception
	    
		if (std!=null) {
			ResponseBuilder response = Response.ok((Object) std.getLogData());
			response.type(MediaType.APPLICATION_XML);
			response.header("ref-id", std.getRef_id());
			response.header("simid", std.getUpload_sim_manager());
			response.header("filename", std.getLog_file_name() );
			response.header("Content-Disposition","attachment; filename="+std.getLog_file_name());
			
		System.out.println("----------Log files download to generate csv-------"+std.getLog_file_name());
		std.setDistribute_flag("D");
		HibernateTemplate.update(std);
		// Write the code to fetch the record by Id and update the Flag ND to D
		// Use Hibernate update function.
		
		return response.build();
		
		}
		else {
			Response.status(401);
			return Response;
		 }
	}
	

	
	
}