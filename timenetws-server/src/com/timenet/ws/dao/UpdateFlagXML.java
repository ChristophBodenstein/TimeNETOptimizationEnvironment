package com.timenet.ws.dao;

import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import com.timenet.ws.models.simulationLogList;
import com.timenet.ws.models.simulationXmlList;
import com.timenet.ws.util.HibernateTemplate;


@Path("/api")
public class UpdateFlagXML {
	private Response Response;


	@GET
	@Path("/update")
	public Response getPreview(@QueryParam("filename") String filename, @QueryParam("simid") String simid) {
		
		
		simulationXmlList stdXML =null;
		List stdLog =null;
		System.out.println("Flag Update XML ============="+ filename+"----"+simid);
		String HQLQuerryLog=" from "+ simulationLogList.class.getName()+" where log_file_name like ? and simulation_id=?";
	    stdLog= HibernateTemplate.findList(HQLQuerryLog, 0, 1, filename+"%", simid);
		    
		if (stdLog.size()==0) {
			String HQLQuerryXML=" from "+ simulationXmlList.class.getName()+" where simulation_file_name= ? and simulation_id= ?";
		    stdXML=(simulationXmlList) HibernateTemplate.findList(HQLQuerryXML, 0, 1, filename+".xml", simid).get(0);
			
		stdXML.setDistribute_flag("ND");
		HibernateTemplate.update(stdXML);
		System.out.println("--------Simulation XML file Flag updated D to ND---------"+stdXML.getXml_file_name());
		
		Response.status(200);
			 return Response;
		
		}
		else {
			Response.status(401);
			return Response;
		 }
	}	
}