package com.timenet.ws.dao;

import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import com.timenet.ws.models.simulationLogList;
import com.timenet.ws.models.simulationXmlList;
import com.timenet.ws.util.HibernateTemplate;
import com.timenet.ws.util.LogFile;





@Path("/api")
public class DownloadXMLService {
	private Response Response;


	@GET
	@Path("/downloads/{flag}")
	public Response getPreview(@PathParam("flag") String flag) {
		ResponseBuilder response=null;
		
		simulationXmlList std =null;
		simulationXmlList eList =null;
	    String SQLQuerry=" from "+ simulationXmlList.class.getName()+" where distribute_flag=?";
	    
		
		try{
			List stdXml=HibernateTemplate.findList(SQLQuerry, 0, 1, flag);

			//  Download XML-File available List for get(0) exception handled
		    if ( stdXml.size()>0) {
			std = (simulationXmlList)stdXml.get(0);
			response = Response.ok((Object) std.getXmlData());
			response.type(MediaType.APPLICATION_XML);
			response.header("ref-id", std.getRef_id());
			response.header("simid", std.getSimulation_id());
			response.header("filename", std.getXml_file_name());
			response.header("Content-Disposition","attachment; filename="+std.getXml_file_name());
			
			LogFile.info("--------Simulation XML file download to generate log file---------"+std.getXml_file_name());
			std.setDistribute_flag("D");
			HibernateTemplate.update(std);
			// Write the code to fetch the record by Id and update the Flag ND to D
			// Use Hibernate update function.
		    }
		}catch(Exception e){
			LogFile.debug("LogFile download exception occured");
			response=Response.noContent();
		 }
		return response.build();
		
	}
	

	
	
}