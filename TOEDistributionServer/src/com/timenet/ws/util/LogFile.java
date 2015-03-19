package com.timenet.ws.util;
import java.io.FileOutputStream;
import java.io.File;
import java.util.Calendar;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.HTMLLayout;
import org.apache.log4j.WriterAppender;
import org.apache.log4j.SimpleLayout;
import org.apache.log4j.FileAppender;

/*******************************
* @author Veeranna Sulikeri
********************************/

public class LogFile {

    static Logger logger = null;
    static FileOutputStream output = null; 
    /**
     *  Constructor for the LogFile object
     */
    public static void setLogger() 
    {  
        try 
        {
            Calendar cal = Calendar.getInstance();
            String strDate = "("+cal.get(Calendar.DAY_OF_MONTH)+"-"+(cal.get(Calendar.MONTH)+1)+"-"+cal.get(Calendar.YEAR)+")";
            File file = new File("RestEasy"+strDate+".html");
            
        	if (!file.exists())
        	{
        		
        		if(output != null)
        		{
        			try
        			{
       					output.close();
        			}
        			catch(Exception e)
        			{
						LogFile.error("In LogFile.java (1) Inner Exception : " + e.getMessage());
        			}
        		}
	            logger = Logger.getLogger(LogFile.class);
	
	            HTMLLayout layout = new HTMLLayout();
	            
	
	            WriterAppender appender = null;
	            output = new FileOutputStream("RestEasy"+strDate+".html",true);
                appender = new WriterAppender(layout, output);
	            logger.addAppender(appender);
	            //logger.setLevel((Level) Level.DEBUG);
        	}
        	else if(logger == null)
        	{
        		
        		if(output != null)
        		{
        			try
        			{
       					output.close();
        			}
        			catch(Exception e)
        			{
						LogFile.error("In LogFile.java (1) Inner Exception : " + e.getMessage());
        			}
        		}
	            logger = Logger.getLogger(LogFile.class);
	        	
	            HTMLLayout layout = new HTMLLayout();
	            
	
	            WriterAppender appender = null;
	            output = new FileOutputStream("RestEasy"+strDate+".html",true);
                appender = new WriterAppender(layout, output);
	            logger.addAppender(appender);
	            //logger.setLevel((Level) Level.DEBUG);
        		
        	}
    	} 
    	catch 
    	(Exception e) 
    	{
			LogFile.error("Unable to open log file."+e.getMessage());
        }
        	
    }
    
 
    /**
     *  Description of the Method
     *
     * @param  strMsg
     */
    public static void debug(String strMsg) 
    {
        setLogger();
        logger.debug(strMsg);
    }


    /**
     *  Description of the Method
     *
     * @param  strMsg
     */
    public static void info(String strMsg) 
    {

        setLogger();
        logger.info(strMsg);
    }


    /**
     *  Writes waring messages to log
     *
     * @param  strMsg
     */
    public static void warn(String strMsg) 
    {

        setLogger();
        logger.warn(strMsg);
    }


    /**
     *  Writes erros to log
     *
     * @param  strMsg
     */
    public static void error(String strMsg) 
    {


        setLogger();
        logger.error(strMsg);
    }


    /**
     *  Writes fatal erros to log
     *
     * @param  strMsg
     */
    public static void fatal(String strMsg) 
    {

        setLogger();
        logger.fatal(strMsg);
    }
}