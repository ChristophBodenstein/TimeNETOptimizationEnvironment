/*
 * Starts simulations
 * Needs: Path to timenet, Set of Parameters, Path to original-File
 * Starts Simulations local or remote
 * Returns Set of measurements, corresponding to given Set of Parameters
 */

package timenetexperimentgenerator;

import java.io.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.*;

/**
 *
 * @author Christoph Bodenstein
 */
public class simpleLocalSimulator implements Runnable, Simulator{
ArrayList<parameter[]> listOfParameterSets;
ArrayList<parser> listOfCompletedSimulationParsers;
String originalFilename;
String pathToTimeNet;
boolean remote=false;
String remoteAddress;
String tmpFilePath;
private int status=0; //Status of simulations, 0..100%
private int simulationCounter=0;//Startvalue for count of simulations, will be in the filename of sim and log
boolean cancelSimulations=false;
String logFileName;
String actualSimulationLogFile="";//actual log-file for one local simulation
private String nameOfTempDirectory="14623786483530251523506521233052";

    //Constructor
    public simpleLocalSimulator(){
    
    }


    /*
    if simulationCounter is set to less then 0, the old value wil be used and continouusly increased
    */
    public void initSimulator(ArrayList<parameter[]> listOfParameterSetsTMP, String originalFilenameTMP, String pathToTimeNetTMP, String tmpFilePathTMP,boolean remoteTMP, int simulationCounterTMP){
    this.listOfParameterSets=listOfParameterSetsTMP;
    this.originalFilename=originalFilenameTMP;
    this.pathToTimeNet=pathToTimeNetTMP;
    this.remote=remoteTMP;
    this.tmpFilePath=tmpFilePathTMP;
        if(simulationCounterTMP>=0){
        this.simulationCounter=simulationCounterTMP;}

    //Start this thread
    new Thread(this).start();

    }



    /**
     Run Method to start simulations and collect the data
     */
    public void run() {
    this.cancelSimulations=false;
        if(this.remote){
        this.simulateRemote();
        }else{
        this.simulateLocal();
        }


    }


    private void simulateLocal(){
    this.status=0;
    this.listOfCompletedSimulationParsers=new ArrayList<parser>();
    String line="";
    int numberOfSimulations=0;
        if(checkTimeNetPath()){
            try{
            System.out.println("Timenet-Path ok, starting local simulations.");
            logFileName=tmpFilePath+File.separator+"SimLog"+Calendar.getInstance().getTimeInMillis()+".csv";
            System.out.println("Logfilename is:"+logFileName);
            //Öffnen des Logfiles und Schreiben der ersten Zeile
            FileWriter fw;
            


                if(listOfParameterSets.size()>0){
                    for(int i=0;i<listOfParameterSets.size();i++){
                    fw = new FileWriter(logFileName, true);
                    if(cancelSimulations) return;
                    parameter[] actualParameterSet=listOfParameterSets.get(i);//Aktuellen Paramstersatz entnehmen
                    String actualParameterFileName=createLocalSimulationFile(actualParameterSet, this.simulationCounter);//Aktuelle xml-file bauen und in tmp-ordner speichern
                    System.out.println("Simulating file:"+actualParameterFileName);
                    startLocalSimulation(actualParameterFileName);//Returns, when Simulation has ended
                    parser myParser=new parser();//Neuen Log-Parser anlegen
                    boolean parseResult=myParser.parse(actualSimulationLogFile);//Log-file und xml-file parsen
                        if(parseResult){
                        System.out.println("Parsing successful.");
                            if(i==0){
                            //Schreiben der ersten Zeile, vorher check, welche Measures verfügbar sind
                            MeasureType exportMeasure=myParser.getMeasures().get(0);//Dummy, es wird das erste Measure abgefragt und die Paramsterliste
                            line="MeasureName;Mean Value;Variance;Conf.Interval-Min;Conf.Interval-Max;Epsilon;"+"Simulation Time";
                                for(int i1=0;i1<exportMeasure.getParameterList().size();i1++){
                                line=line+";"+exportMeasure.getParameterList().get(i1).getName();
                                }
                                try {
                                    fw.write(line);
                                    fw.append( System.getProperty("line.separator") );
                                } catch (IOException ex) {
                                    System.out.println("Error writing line to log-file.");
                                    ex.printStackTrace();
                                }
                            }

                            //Schreiben der nächsten Zeile ins Logfile
                             try{
                              //fw.write(line);
                              //fw.append( System.getProperty("line.separator") );
                                for(int i1=0;i1<myParser.getMeasures().size();i1++){//Alle Measure schreiben
                                MeasureType exportMeasure=myParser.getMeasures().get(i1);
                                line=exportMeasure.getMeasureName()+";"+getCommaFloat(exportMeasure.getMeanValue())+";"+getCommaFloat(exportMeasure.getVariance())+";"+getCommaFloat(exportMeasure.getConfidenceInterval()[0])+";"+getCommaFloat(exportMeasure.getConfidenceInterval()[1])+";"+getCommaFloat(exportMeasure.getEpsilon())+";"+getCommaFloat(myParser.getSimulationTime());
                                    for(int c=0;c<exportMeasure.getParameterList().size();c++){
                                    line=line+";"+getCommaFloat(exportMeasure.getParameterList().get(c).getValue());
                                    }
                                fw.write(line);
                                fw.append( System.getProperty("line.separator") );
                                }

                             }catch(Exception e){e.printStackTrace();}
                        
                        this.listOfCompletedSimulationParsers.add(myParser);//Parser inkl. gesammelter infos an Ergebnisliste anhängen
                        }else{
                        System.out.println("Error Parsing the Simulation results. Maybe Simulation failure?");
                        }
                    numberOfSimulations++;//Lokale Simulationsnnummer inkrementieren
                    
                    this.status=numberOfSimulations*100 / listOfParameterSets.size(); //Prozentsatz der abgearbeiteten Simulationen setzen
                    this.simulationCounter++;//Simulationscounter erhöhen, wichtig für Dateinamen

                    fw.close();
                    }

                }
                
               }catch(Exception e){
               e.printStackTrace();
               }

        }else{
        System.out.println("Timenet-Path NOT ok!");
        }
        
    }

    private void simulateRemote(){
    if(cancelSimulations) return;

    }


    /**
     creates the local file for simulation, including all information in file and filename
     */
    private String createLocalSimulationFile(parameter[] p, int simulationNumber){
    String fileNameOfLocalSimulationFile="";
    File f = new File(this.originalFilename);
    try{
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.parse(this.originalFilename);
        NodeList parameterList=doc.getElementsByTagName("parameter");
        String ConfidenceIntervall="90", Seed="0", EndTime="0", MaxTime="0",MaxRelError="5";
            for(int parameterNumber=0; parameterNumber< p.length;parameterNumber++){
                        if(!p[parameterNumber].isExternalParameter()){
                            for(int i=0;i<parameterList.getLength();i++){
                                if(parameterList.item(i).getAttributes().getNamedItem("name").getNodeValue().equals(p[parameterNumber].getName())){
                                parameterList.item(i).getAttributes().getNamedItem("defaultValue").setNodeValue(p[parameterNumber].getValue());
                                }
                                //System.out.println(parameterList.item(i).getAttributes().getNamedItem("name").getNodeValue());
                            }
                        }else{
                            if(p[parameterNumber].getName().equals("MaxTime")){
                            MaxTime=p[parameterNumber].getValue();
                            }
                            if(p[parameterNumber].getName().equals("EndTime")){
                            EndTime=p[parameterNumber].getValue();
                            }
                            if(p[parameterNumber].getName().equals("Seed")){
                            Seed=p[parameterNumber].getValue();
                            }
                            if(p[parameterNumber].getName().equals("ConfidenceIntervall")){
                            ConfidenceIntervall=p[parameterNumber].getValue();
                            }
                            if(p[parameterNumber].getName().equals("MaxRelError")){
                            MaxRelError=p[parameterNumber].getValue();
                            }

                        }
                    }
                
                //Dateiname bilden
                String exportFileName=this.tmpFilePath+File.separator+support.removeExtention(f.getName())+"_n_"+simulationNumber+"_MaxTime_"+MaxTime+"_EndTime_"+EndTime+"_Seed_"+Seed+"_ConfidenceIntervall_"+ConfidenceIntervall+"_MaxRelError_"+MaxRelError+"_.xml";
                //Exportieren
                System.out.println("File to export: "+exportFileName);

                TransformerFactory tFactory =
                TransformerFactory.newInstance();
                Transformer transformer = tFactory.newTransformer();

                DOMSource source = new DOMSource(doc);
                StreamResult result = new StreamResult(new File(exportFileName));
                transformer.transform(source, result);

                return exportFileName;

    }catch(Exception e){
        return fileNameOfLocalSimulationFile;
    }finally{}
    
    }

    /*
     starts the local simulation run, returns, when simulation has ended
     */
    private void startLocalSimulation(String exportFileName){
        try {
        // Execute command
        java.lang.ProcessBuilder processBuilder = new java.lang.ProcessBuilder("java","-jar", "TimeNET.jar",exportFileName, "autostart=true", "autostop=true", "secmax="+getIntValueFromFileName(exportFileName, "MaxTime"), "endtime="+getIntValueFromFileName(exportFileName, "EndTime") , "seed="+ getIntValueFromFileName(exportFileName, "Seed"), "confidence="+getIntValueFromFileName(exportFileName, "ConfidenceIntervall"), "epsmax="+getValueFromFileName(exportFileName, "MaxRelError"));
        processBuilder.directory(new java.io.File(this.pathToTimeNet));
        System.out.println("Command is: "+processBuilder.command().toString());

        // Start new process
        long timeStamp=Calendar.getInstance().getTimeInMillis();


        java.lang.Process p = processBuilder.start();
        
        java.util.Scanner s = new java.util.Scanner( p.getInputStream() ).useDelimiter( "\\Z" );//Scans output of process
        System.out.println( s.next() );//prints output of process into System.out
            try {
                p.waitFor();
            } catch (InterruptedException ex) {
                Logger.getLogger(simpleLocalSimulator.class.getName()).log(Level.SEVERE, null, ex);
            }
        timeStamp=(Calendar.getInstance().getTimeInMillis()-timeStamp) / 1000;//Time for calculation in seconds


        //Copy results.log
        String sourceFile=support.removeExtention(exportFileName)+".result"+File.separator+"results.log";
        String sinkFile=support.removeExtention(exportFileName)+"simTime_"+timeStamp+".log";
            if (copyFile(sourceFile, sinkFile, false)){
            System.out.println("Coppied log-file. Now delete the directory and original log-file.");
            File tmpFile=new File(sourceFile);
            tmpFile.delete();
            tmpFile=new File(support.removeExtention(exportFileName)+".result");
            tmpFile.delete();
            System.out.println("Deleted original Log-file and directory.");
            this.actualSimulationLogFile=sinkFile;
            }
        File file = new File(exportFileName);
        String path=file.getAbsolutePath().substring(0,file.getAbsolutePath().lastIndexOf(File.separator)) +File.separator+nameOfTempDirectory;
    
        System.out.println("Delete Path to tmp files: "+path);
        this.del(new File(path));
        
        
        
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public String getReMoteAddress(){
    if(this.remoteAddress==null)return "";else return this.remoteAddress;
    }
    public void setRemoteAddress(String s){
    this.remoteAddress=s;
    }


    /**
    * Checks, if Timenet is availabel at giben Path, otherwise simulation run is not possible
    */
    public boolean checkTimeNetPath(){

    File tmpFile=new File(this.pathToTimeNet+File.separator+"TimeNET.jar");
    System.out.println("Check if Timenet is here:"+tmpFile.toString());
        if(tmpFile.exists()){
        return true;
        }else{
        return false;
        }
    }



    /**
     Checks, if given TimeNet-Simulation-Deployment-Server Available
     */
    private boolean checkTimeNetServerAvailable(){
    return false;
    }

    public ArrayList<parser> getListOfCompletedSimulationParsers(){
    return this.listOfCompletedSimulationParsers;
    }


    /*
     Delete content of tmp-folder, all xml-files, log-files and generated source-code
     */
    public void deleteTmpFiles(){
    File[] listOfFile=new File(this.tmpFilePath).listFiles();
        for(int i=0;i<listOfFile.length;i++){
        del(listOfFile[i]);
        }
    }

    /*
     Deletes a file or directory recursive
     */
    public boolean del(File dir){
        if (dir.isDirectory()){
        File[] files = dir.listFiles();
            for (File aktFile: files){
            del(aktFile);
            }
        }
    return dir.delete();
    }

    /**
     *
     * cancels all remaining simulations and aborts the actual one
     */
    private void cancelSimulations(){
    this.cancelSimulations=true;
    }



    /**
     * searches for values of corresponding parameters, whicht are set in the filename
     */
    private String getValueFromFileName(String fileName, String needle){
    String[] stringList=fileName.split("_");

        for(int i=0; i<stringList.length;i++){
            if(stringList[i].equals(needle)){
            return stringList[i+1];
            }
        }
    return "";
    }

    private String getIntValueFromFileName(String fileName, String needle){
    String tmpString=getValueFromFileName(fileName, needle);
    return String.valueOf(Float.valueOf(tmpString).intValue());
    }


    /*
     *copies a file from source to sink
     **/
     public boolean copyFile(String source, String sink, boolean append){
    try{
          File f1 = new File(source);
          File f2 = new File(sink);
          InputStream in = new FileInputStream(f1);
          OutputStream out;
          if(append){
          //For Append the file.
          out = new FileOutputStream(f2,true);
          } else{
            //For Overwrite the file.
            out = new FileOutputStream(f2);
            }

          byte[] buf = new byte[1024];
          int len;
          while ((len = in.read(buf)) > 0){
            out.write(buf, 0, len);
          }
          in.close();
          out.close();
          System.out.println("File copied.");
          return true;
        }
        catch(FileNotFoundException ex){
          System.out.println(ex.getMessage() + " in the specified directory.");
          return false;
        }
        catch(IOException e){
          System.out.println(e.getMessage());
          return false;
        }
    }


    public static ProcMon createProcMon(Process proc) {
    ProcMon procMon = new ProcMon(proc);
    Thread t = new Thread(procMon);
    t.start();
    return procMon;
    }

    
    //Returns a String with float value, where comma is used instead of point as decimal-separator
    public String getCommaFloat(float f){
    //System.out.print("UnFormated float is "+f);
    String returnValue=getCommaFloat( Float.toString(f) ) ;
    //System.out.println("  --  Formated float is "+returnValue);
    return returnValue;
    }
    public String getCommaFloat(String f){
    //System.out.print("UnFormated String is "+f);
    String returnValue=f.replace(".", ",");
    //System.out.println("  --  Formated String is "+returnValue);

    return returnValue;
    }

    public int getSimulationCounter(){
    return this.simulationCounter;
    }
    public void setSimulationCounter(int i){
    this.simulationCounter=i;
    }

    public int getStatus(){
    return this.status;
    }
    

}
