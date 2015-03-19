package com.timenet.ws.models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;


/**
 * auto GeneratedValue
 * @author Veeranna
 */
@Entity
@Table(name = "simulation_log_list")
public class simulationLogList {
	

	@Id
	@GeneratedValue
	@Column(name = "ref_id") 
	Integer ref_id; 
	
	@Column(name = "simulation_id")             
	String simulation_id;   
	
	@Column(name = "log_file_name")       
	String log_file_name;

	@Column(name = "distribute_flag")                    
	String distribute_flag; 
	
	@Column(name = "log_data")              
	byte logData[];
	
	@Column(name = "current_time_stamp")            
	String current_time_stamp;
	
	@Column(name = "log_size")     
	Integer log_size;

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

	public String getLog_file_name() {
		return log_file_name;
	}

	public void setLog_file_name(String log_file_name) {
		this.log_file_name = log_file_name;
	}

	public String getDistribute_flag() {
		return distribute_flag;
	}

	public void setDistribute_flag(String distribute_flag) {
		this.distribute_flag = distribute_flag;
	}

	public byte[] getLogData() {
		return logData;
	}

	public void setLogData(byte[] logData) {
		this.logData = logData;
	}

	public String getCurrent_time_stamp() {
		return current_time_stamp;
	}

	public void setCurrent_time_stamp(String current_time_stamp) {
		this.current_time_stamp = current_time_stamp;
	}

	public Integer getLog_size() {
		return log_size;
	}

	public void setLog_size(Integer log_size) {
		this.log_size = log_size;
	}

 

}
