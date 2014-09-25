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

import com.timenet.ws.models.logList;
import com.timenet.ws.util.HibernateTemplate;

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

		System.out.println("input" + inputParts.toString());
		for (InputPart inputPart : inputParts) {

			try {
				MultivaluedMap<String, String> header = inputPart.getHeaders();
				fileName = getFileName(header);
				// convert the uploaded file to inputstream
				InputStream inputStream = inputPart.getBody(InputStream.class,null);
				byte[] bytes = IOUtils.toByteArray(inputStream);
				id = saveFileInDb(bytes, fileName, simid);
				System.out.println("Done");
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		return Response.status(200).entity("uploadFile is called, Uploaded file id : " + id).build();

	}

	/**
	 * header sample { Content-Type=[image/png], Content-Disposition=[form-data;
	 * name="file"; filename="filename.extension"] }
	 **/
	// get uploaded filename, is there a easy way in RESTEasy?
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

	// save to somewhere
	private void writeFile(byte[] content, String filename) throws IOException {

		File file = new File(filename);
		if (!file.exists()) 
		{
			file.createNewFile();
		}
		logList fm = new logList();
		FileOutputStream fop = new FileOutputStream(file);
		fop.write(content);
		fop.flush();
		fop.close();

	}

	private int saveFileInDb(byte[] content, String fileName, String simid) {
		int fileId = -1;
		logList fm = new logList();
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		   //get current date time with Date()
		   Date date = new Date();
		   
		fm.setLog_file_name(fileName);
		fm.setDistribute_flag("ND");
		fm.setCurrent_time_stamp(dateFormat.format(date));
		fm.setUpload_sim_manager(simid);
		fm.setLogData(content);
		fm.setFile_size(content.length);

		fileId = (Integer) HibernateTemplate.save(fm);
		System.out.println("-------- Log file Stored in Server ---------"+fileName);
		return fileId;

	}
}