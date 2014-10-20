<%@ page import ="java.sql.*" %>
<%@ page import ="javax.sql.*" %>


<HTML>
    <HEAD>
        <title>TimeNet Simulation Portal</title>
    </HEAD>

    <BODY>
        <h1>TimeNet Simulation Portal</h1>

        <% 

            String connectionURL = "jdbc:mysql://localhost:3306/timenetws_server";
            Connection connection = null; 
            Class.forName("com.mysql.jdbc.Driver").newInstance(); 
            connection = DriverManager.getConnection(connectionURL, "root", "root");
            if(!connection.isClosed()) 
                 out.println("Successfully connected to " + "MySQL server using TCP/IP...");
            
            Statement statement = connection.createStatement();

            String id = request.getParameter("simid");  
            if(id != null)
            {
            	statement.execute("delete from `simulation_xml_list` where `simulation_id` = " + id);
            	statement.execute("delete from `simulation_log_list` where `simulation_id` = " + id);
            }

            ResultSet resultset = 
            		statement.executeQuery("Select T.sid,T.All,IFNULL(D.delivered,0) as 'Delivered',IFNULL(P.finished,0) as 'Finished' from ( SELECT `simulation_id` as sid,count(*) as 'All' FROM `simulation_xml_list` group by `simulation_id`) as T LEFT OUTER JOIN (( SELECT `simulation_id` as sod, count(*) as finished FROM `simulation_log_list` group by `simulation_id` ) as P , (SELECT `simulation_id` as sid,count(*) as delivered FROM `simulation_xml_list` group by `simulation_id`,`distribute_flag` having `distribute_flag` = 'D') as D ) on (T.sid = P.sod and T.sid = D.sid) order by sid desc") ;
%>
        <TABLE BORDER="1">
            <TR>
               <TH>time</TH>
               <TH>All</TH>
               <TH>Delivered</TH>
               <TH>Processed</TH>
               <TH></TH>
             
               
           </TR>
                   <% 
            while(resultset.next()) {

            long t = Long.parseLong(resultset.getString(1));
            Timestamp ts = new Timestamp(t);
            java.util.Date temp = new java.util.Date(ts.getTime());
        %>



           <TR>
               <TD> <%= temp.toString() %> </TD>
               <TD> <%= resultset.getString(2) %> </TD>
               <TD> <%= resultset.getString(3) %> </TD>
               <TD> <%= resultset.getString(4) %> </TD>
               <TD><form ACTION="?simid=<%= resultset.getString(1) %>" METHOD="POST"><input type="submit" value="delete"></form> </TD>
           </TR>
       <% 
           } 
            connection.close();
       %>
       </TABLE>
       <BR>
           </BODY>
</HTML>