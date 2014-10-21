package com.timenet.ws.dao;
/**
 * auto GeneratedValue
 * @author Veeranna
 */
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import com.timenet.ws.models.simulationLogList;
import com.timenet.ws.util.HibernateTemplate;
import com.timenet.ws.util.LogFile;

@Path("/api")
public class DownloadLogServeice {
	private Response Response;

	@GET
	@Path("/downloads/log/{simid}")
	public Response getPreview(@PathParam("simid") String simid) {
		ResponseBuilder response=null;	
		
		simulationLogList std =null;
	    String SQLQuerry=" from "+ simulationLogList.class.getName()+" where distribute_flag=? and simulation_id=?";
	    
	    try{
	    List stdLog= HibernateTemplate.findList(SQLQuerry, 0, 1, "ND", simid);
	
	   //  Download Log-File available List for get(0) exception handled
	    if (stdLog.size()>0) {
			std = (simulationLogList)stdLog.get(0);
			
			// Build the response with Log file as attachment.
		    response = Response.ok((Object) std.getLogData());
			response.type(MediaType.APPLICATION_XML);
			response.header("ref-id", std.getRef_id());
			response.header("simid", std.getSimulation_id());
			response.header("filename", std.getLog_file_name() );
			response.header("Content-Disposition","attachment; filename="+std.getLog_file_name());
			
			LogFile.info("----------Log files download to generate csv-------"+std.getLog_file_name());
			// Fetch the record by simID and update the Flag ND to D
			// Use of Hibernate update function.
			std.setDistribute_flag("D");
			HibernateTemplate.update(std);
			LogFile.info("----------Hibernate Function to Update Sim ID to -------"+std.getLog_file_name());
		
		
	    }else
	    {
	    	response=Response.noContent();
	    }
	    	}
	    catch(Exception e){
	    	LogFile.debug("XMLFile download exception occured");
	    	response=Response.noContent();
	    	}  
		return response.build();
	}
	
}