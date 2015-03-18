package com.timenet.ws.models;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;


/**
 * @author Veeranna
 */
@Entity
@Table(name = "simulation_xml_list")
public class simulationXmlList {
	

	@Id
	@GeneratedValue
	@Column(name = "ref_id") 
	Integer ref_id;         

	@Column(name = "simulation_id")             
	String simulation_id; 
	
	@Column(name = "xml_file_name")       
	String xml_file_name;

	@Column(name = "distribute_flag")                    
	String distribute_flag; 
	
	@Column(name = "xml_data")              
	byte xmlData[];
	
	@Column(name = "current_time_stamp")            
	String current_time_stamp;
	
	@Column(name = "xml_size")     
	Integer xml_size;

	public Integer getRef_id() {
		return ref_id;
	}
	public void setRef_id(Integer ref_id) {
		this.ref_id = ref_id;
	}

	public String getSimulation_id() {
		return simulation_id;
	}
	public void setSimulation_id(String simulation_id) {
		this.simulation_id = simulation_id;
	}

	public String getXml_file_name() {
		return xml_file_name;
	}
	public void setXml_file_name(String xml_file_name) {
		this.xml_file_name = xml_file_name;
	}

	public String getDistribute_flag() {
		return distribute_flag;
	}
	public void setDistribute_flag(String distribute_flag) {
		this.distribute_flag = distribute_flag;
	}

	public byte[] getXmlData() {
		return xmlData;
	}
	public void setXmlData(byte[] xmlData) {
		this.xmlData = xmlData;
	}

	public String getCurrent_time_stamp() {
		return current_time_stamp;
	}
	public void setCurrent_time_stamp(String current_time_stamp) {
		this.current_time_stamp = current_time_stamp;
	}

	public Integer getXml_size() {
		return xml_size;
	}
	public void setXml_size(Integer xml_size) {
		this.xml_size = xml_size;
	}    

}
