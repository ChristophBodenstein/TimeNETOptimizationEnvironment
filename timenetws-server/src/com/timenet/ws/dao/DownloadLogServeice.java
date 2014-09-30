package com.timenet.ws.dao;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import com.timenet.ws.models.logList;
import com.timenet.ws.util.HibernateTemplate;
import com.timenet.ws.util.LogFile;





@Path("/api")
public class DownloadLogServeice {
	private Response Response;


	@GET
	@Path("/downloads/log/{simid}")
	public Response getPreview(@PathParam("simid") String simid) {
		ResponseBuilder response=null;	
		
		logList std =null;
		//logList eList =null;
	    String SQLQuerry=" from "+ logList.class.getName()+" where distribute_flag=? and upload_sim_manager=?";
	    
	    try{
	    std=(logList) HibernateTemplate.findList(SQLQuerry, 0, 1, "ND", simid).get(0);
	    response = Response.ok((Object) std.getLogData());
		response.type(MediaType.APPLICATION_XML);
		response.header("ref-id", std.getRef_id());
		response.header("simid", std.getUpload_sim_manager());
		response.header("filename", std.getLog_file_name() );
		response.header("Content-Disposition","attachment; filename="+std.getLog_file_name());
		
		LogFile.info("----------Log files download to generate csv-------"+std.getLog_file_name());
		std.setDistribute_flag("D");
		HibernateTemplate.update(std);
		// Write the code to fetch the record by Id and update the Flag ND to D
		// Use Hibernate update function.
	    }catch(Exception e){
	    	LogFile.debug("No Logfile in List for Downloads (get(0) caused Exception).");
	    	response=Response.noContent();
	    }  
	    
		return response.build();
	}
	

	
	
}