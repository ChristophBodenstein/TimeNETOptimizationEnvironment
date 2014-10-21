package com.timenet.ws.dao;
/**
 * auto GeneratedValue
 * @author Veeranna
 */
import java.io.File;
import java.io.FileOutputStream;
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

import com.timenet.ws.models.simulationXmlList;
import com.timenet.ws.util.HibernateTemplate;
import com.timenet.ws.util.LogFile;

@Path("/file")
public class UploadXMLFileService {

	@POST
	@Path("/upload")
	@Consumes("multipart/form-data")
	public Response uploadFile(MultipartFormDataInput input) throws IOException {

		int id = -1;

		// Get API input data
		Map<String, List<InputPart>> uploadForm = input.getFormDataMap();
		// Get file name
		String fileName = uploadForm.get("fileName").get(0).getBodyAsString();
		// Get file name
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
		return Response.status(412).entity("XML File Unable to Store in Databse: Network Failure").build();
	}
	return Response.status(200).entity("XML File Is Stored In Database SimID : " + id).build();

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

	// Save To Database
	private void writeFile(byte[] content, String filename) throws IOException {

		File file = new File(filename);
		if (!file.exists()) 
		{
			file.createNewFile();
		}
		simulationXmlList fm = new simulationXmlList();
		FileOutputStream fop = new FileOutputStream(file);
		fop.write(content);
		fop.flush();
		fop.close();

	}

	private int saveFileInDb(byte[] content, String fileName, String simid) {
		int fileId = -1;
		simulationXmlList fm = new simulationXmlList();
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		   //get current date time with Date()
		   Date date = new Date();
		   try { 
		fm.setXml_file_name(fileName);
		fm.setDistribute_flag("ND");
		fm.setCurrent_time_stamp(dateFormat.format(date));
		fm.setSimulation_id(simid);
		fm.setXmlData(content);
		fm.setXml_size(content.length);

		fileId = (Integer) HibernateTemplate.save(fm);
		LogFile.info("-------- Log file Stored in Database simulation_log_list table ---------"+fileName);
	 }catch(Exception e){
	    	LogFile.debug("Exception Occured While Storing Log File");
	    	
	    }  
		return fileId;

	}
}