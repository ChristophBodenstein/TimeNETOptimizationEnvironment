package com.timenet.ws.dao;
/**
 * auto GeneratedValue
 * @author Veeranna
 */

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import com.timenet.ws.models.simulationLogList;
import com.timenet.ws.util.HibernateTemplate;
import com.timenet.ws.util.LogFile;

@Path("/log")
public class UploadLogFileService {

	@POST
	@Path("/upload")
	@Consumes("multipart/form-data")
	public Response uploadFile(MultipartFormDataInput input) throws IOException {

		int id = -1;

		// Get API input data
		Map<String, List<InputPart>> uploadForm = input.getFormDataMap();
		// Get file name
		String fileName = uploadForm.get("fileName").get(0).getBodyAsString();
		// Get sim id name
		String simid = uploadForm.get("simid").get(0).getBodyAsString();
		// Get file data to save
		List<InputPart> inputParts = uploadForm.get("attachment");
		
		// Check for null pointer exception
		if(fileName!= null && simid!=null && inputParts!=null)
		{
		for (InputPart inputPart : inputParts) {

			try {
				MultivaluedMap<String, String> header = inputPart.getHeaders();
				fileName = getFileName(header);
				// convert the uploaded file to inputstream
				InputStream inputStream = inputPart.getBody(InputStream.class,null);
				byte[] bytes = IOUtils.toByteArray(inputStream);
				id = saveFileInDb(bytes, fileName, simid);
				LogFile.info(fileName+"Saved in Database");
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		} else {
			// http status 412 Precondition Failed
			return Response.status(412).entity("Log File Unable to Store in Databse: Network Failure").build();
		}
		return Response.status(200).entity("Log File Is Stored In Database SimID : " + id).build();

	}


	// Get Uploaded Filename From Header
	private String getFileName(MultivaluedMap<String, String> header) {
		String[] contentDisposition = header.getFirst("Content-Disposition").split(";");
		for (String filename : contentDisposition) {
			if ((filename.trim().startsWith("filename"))) {
				String[] name = filename.split("=");
				String finalFileName = name[1].trim().replaceAll("\"", "");
				return finalFileName;
			}
		}
		return "unknown";
	}

   // Save Log File in Database using Hibernate Function
	private int saveFileInDb(byte[] content, String fileName, String simid) {
		int fileId = -1;
		simulationLogList fm = new simulationLogList();
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		//get current date time with Date()
		Date date = new Date();
		 try {
		fm.setLog_file_name(fileName);
		fm.setDistribute_flag("ND");
		fm.setCurrent_time_stamp(dateFormat.format(date));
		fm.setSimulation_id(simid);
		fm.setLogData(content);
		fm.setLog_size(content.length);

		fileId = (Integer) HibernateTemplate.save(fm);
		LogFile.info("-------- Log file Stored in Database simulation_xml_list table ---------"+fileName);
		 }catch(Exception e){
		    	LogFile.debug("Exception Occured While Storing Log File");
		    	
		    }  
		return fileId;

	}
}