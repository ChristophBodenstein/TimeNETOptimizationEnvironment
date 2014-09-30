package com.timenet.ws.dao;

import javax.ws.rs.FormParam;

public class FileUploadForm {

	public FileUploadForm() {
	}
	
	private byte[] data;

	public byte[] getData() {
		return data;
	}

	@FormParam("file")
	public void setData(byte[] data) {
		this.data = data;
	}

	
}