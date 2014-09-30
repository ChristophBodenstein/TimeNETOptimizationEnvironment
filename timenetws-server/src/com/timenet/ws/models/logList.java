package com.timenet.ws.models;



import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
ref_id	
simulation_file_name	varchar(1000)	
distribute_flag	varchar(10)	
current_time_stamp	timestamp		
upload_date	 date			
upload_sim_manager	varchar(100)
file_size
**/

/**
 * auto GeneratedValue
 * @author Veeranna
 */
@Entity
@Table(name = "log_list")
public class logList {
	

	@Id
	@GeneratedValue
	@Column(name = "ref_id") 
	Integer ref_id;         

	@Column(name = "log_file_name")       
	String log_file_name;

	@Column(name = "distribute_flag")                    
	String distribute_flag; 
	
	@Column(name = "current_time_stamp")            
	String current_time_stamp;
	
	@Column(name = "upload_sim_manager")             
	String upload_sim_manager;   

	@Column(name = "log_data")              
	byte logData[];
	
	@Column(name = "file_size")     
	Integer file_size;
	
	@Column(name = "sim_id") 
	Integer sim_id;         


	public Integer getRef_id() {
		return ref_id;
	}

	public void setRef_id(Integer ref_id) {
		this.ref_id = ref_id;
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

	public String getCurrent_time_stamp() {
		return current_time_stamp;
	}

	public void setCurrent_time_stamp(String current_time_stamp) {
		this.current_time_stamp = current_time_stamp;
	}

	public String getUpload_sim_manager() {
		return upload_sim_manager;
	}

	public void setUpload_sim_manager(String upload_sim_manager) {
		this.upload_sim_manager = upload_sim_manager;
	}

		
	public byte[] getLogData() {
		return logData;
	}

	public void setLogData(byte[] logData) {
		this.logData = logData;
	}

	public Integer getFile_size() {
		return file_size;
	}

	public void setFile_size(Integer file_size) {
		this.file_size = file_size;
	}

	public Integer getSim_id() {
		return sim_id;
	}

	public void setSim_id(Integer sim_id) {
		this.sim_id = sim_id;
	}
	
	
	

}
