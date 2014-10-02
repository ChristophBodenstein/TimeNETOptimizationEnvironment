package com.timenet.ws.dao;

import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import com.timenet.ws.models.logList;
import com.timenet.ws.models.simulationList;
import com.timenet.ws.util.HibernateTemplate;


@Path("/api")
public class UpdateFlagXML {
	private Response Response;


	@GET
	@Path("/update")
	public Response getPreview(@QueryParam("filename") String filename, @QueryParam("simid") String simid) {
		
		
		simulationList stdXML =null;
		List stdLog =null;
		System.out.println("Flag Update XML ============="+ filename+"----"+simid);
		String SQLQuerryLog=" from "+ logList.class.getName()+" where log_file_name like ? and upload_sim_manager=?";
	    stdLog= HibernateTemplate.findList(SQLQuerryLog, 0, 1, filename+"%", simid);
		    
		if (stdLog.size()==0) {
			String SQLQuerryXML=" from "+ simulationList.class.getName()+" where simulation_file_name= ? and upload_sim_manager= ?";
		    stdXML=(simulationList) HibernateTemplate.findList(SQLQuerryXML, 0, 1, filename+".xml", simid).get(0);
			
		stdXML.setDistribute_flag("ND");
		HibernateTemplate.update(stdXML);
		System.out.println("--------Simulation XML file Flag updated D to ND---------"+stdXML.getSimulation_file_name());
		
		Response.status(200);
			 return Response;
		
		}
		else {
			Response.status(401);
			return Response;
		 }
	}	
}