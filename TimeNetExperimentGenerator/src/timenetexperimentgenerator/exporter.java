/*
 * Exports a list of Experiments as xml-files into a given directory.

 * Christoph Bodenstein
 * TU-Ilmenau, FG SSE
 */

package timenetexperimentgenerator;

import timenetexperimentgenerator.helper.parameterTableModel;
import timenetexperimentgenerator.datamodel.parameter;
import java.io.File;
import java.util.ArrayList;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.*;

/**
 *
 * @author Christoph Bodenstein
 */
public class exporter implements Runnable{
ArrayList< ArrayList<parameter> > ListOfParameterSetsToBeWritten;
String filename;
JLabel infoLabel;
MainFrame parent;

    exporter(ArrayList< ArrayList<parameter> > ListOfParameterSetsToBeWritten){
    this.ListOfParameterSetsToBeWritten=ListOfParameterSetsToBeWritten;
    this.filename=support.getOriginalFilename();// filename;
    this.parent=support.getMainFrame();//parent;
    
    new Thread(this).start();
    }

    public void run() {
    File f = new File(this.filename);
    JFileChooser fileChooser = new JFileChooser(f.getParent());
    fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
    fileChooser.setControlButtonsAreShown(true);
    fileChooser.setCurrentDirectory(f);
      fileChooser.setDialogTitle("Dir for export of "+ListOfParameterSetsToBeWritten.size() +" Experiments. Go INTO the dir to choose it!");



      if (fileChooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
        support.log("getCurrentDirectory(): "
         +  fileChooser.getCurrentDirectory());
        support.log("getSelectedFile() : "
         +  fileChooser.getSelectedFile());

                try{

                for(int c=0;c<ListOfParameterSetsToBeWritten.size();c++){
                DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
                Document doc = docBuilder.parse(this.filename);
                NodeList parameterList=doc.getElementsByTagName("parameter");
                String ConfidenceIntervall="", Seed="", EndTime="", MaxTime="";
                    ArrayList<parameter> tmpParameterSet = ListOfParameterSetsToBeWritten.get(c);
                    for(int parameterNumber=0; parameterNumber< tmpParameterSet.size();parameterNumber++){
                        if(!tmpParameterSet.get(parameterNumber).isExternalParameter()){
                            for(int i=0;i<parameterList.getLength();i++){
                                if(parameterList.item(i).getAttributes().getNamedItem("name").getNodeValue().equals(tmpParameterSet.get(parameterNumber).getName())){
                                parameterList.item(i).getAttributes().getNamedItem("defaultValue").setNodeValue(support.getString(tmpParameterSet.get(parameterNumber).getValue()));
                                }
                                //support.log(parameterList.item(i).getAttributes().getNamedItem("name").getNodeValue());
                            }
                        }else{
                            if(tmpParameterSet.get(parameterNumber).getName().equals("MaxTime")){
                            MaxTime=support.getString(tmpParameterSet.get(parameterNumber).getValue());
                            }
                            if(tmpParameterSet.get(parameterNumber).getName().equals("EndTime")){
                            EndTime=support.getString(tmpParameterSet.get(parameterNumber).getValue());
                            }
                            if(tmpParameterSet.get(parameterNumber).getName().equals("Seed")){
                            Seed=support.getString(tmpParameterSet.get(parameterNumber).getValue());
                            }
                            if(tmpParameterSet.get(parameterNumber).getName().equals("ConfidenceIntervall")){
                            ConfidenceIntervall=support.getString(tmpParameterSet.get(parameterNumber).getValue());
                            }
                        }
                    }
                //Statusausgabe
                infoLabel.setText("Export of file "+(c+1)+"/"+ListOfParameterSetsToBeWritten.size());
                infoLabel.updateUI();
                //Dateiname bilden
                String tmpDirName="";
                support.log("Choosen File: "+fileChooser.getSelectedFile());
                String exportFileName=fileChooser.getCurrentDirectory()+File.separator+support.removeExtention(f.getName())+"_n_"+c+"_MaxTime_"+MaxTime+"_EndTime_"+EndTime+"_Seed_"+Seed+"_ConfidenceIntervall_"+ConfidenceIntervall+"_.xml";
                //Exportieren
                support.log("File to export: "+exportFileName);

                TransformerFactory tFactory =
                TransformerFactory.newInstance();
                Transformer transformer = tFactory.newTransformer();

                DOMSource source = new DOMSource(doc);
                StreamResult result = new StreamResult(new File(exportFileName));
                transformer.transform(source, result);

                }
            }catch(Exception e){
            e.printStackTrace();
            JOptionPane.showMessageDialog(parent, "Es gab einen Fehler beim Export der Experimente.");
            }
      JOptionPane.showMessageDialog(parent, "Es wurden "+ListOfParameterSetsToBeWritten.size()+" Dateien erzeugt.");
      infoLabel.setText("");
      }
    else {
      support.log("No Selection ");
      //parent.cancelOperation=true;
      }


    }
}



class generator extends Thread{
ArrayList< ArrayList<parameter> > ListOfParameterSetsToBeWritten;
String filename;
JLabel infoLabel;
MainFrame parent;
JTable jTableParameterList;
boolean isRunning=false;

    generator(ArrayList< ArrayList<parameter> > ListOfParameterSetsToBeWritten, String filename, JLabel infoLabel, MainFrame parent, JTable jTableParameterList){
    this.ListOfParameterSetsToBeWritten=ListOfParameterSetsToBeWritten;
    this.filename=filename;
    this.infoLabel=infoLabel;
    this.parent=parent;
    this.jTableParameterList=jTableParameterList;

    //new Thread(this).start();
    }

    @Override
    public void run() {
    isRunning=true;
    parent.deactivateExportButtons();
    infoLabel.setText("Generating all Experiments.");
    int parameterCount=this.jTableParameterList.getModel().getRowCount();
    parameterTableModel tModel=(parameterTableModel) this.jTableParameterList.getModel();
    String [][] parameterArray=tModel.getParameterArray();

    //Build initial ArrayList of parameters
    ArrayList <parameter>ListOfParameterAsFromTable=tModel.getListOfParameter();
    //build first parameterset
    ArrayList<parameter> lastParameterSet = support.getCopyOfParameterSet(ListOfParameterAsFromTable);

    //Calculate size of designspace
    parent.calculateDesignSpace();
    support.setStatusText("Designspace-Size:"+ this.getSizeOfDesignspace());
    
    //call recursive generation-function!
    parent.buildListOfParameterSetsToExport(ListOfParameterSetsToBeWritten, ListOfParameterAsFromTable, lastParameterSet, infoLabel);



    support.log("Designspace-Size (incl. duplicates): "+ListOfParameterSetsToBeWritten.size());
    infoLabel.setText("Size is about: "+ ListOfParameterSetsToBeWritten.size() +"Removing Duplicates.");

    //ListOfParameterSetsToBeWritten=parent.removeDuplicates(ListOfParameterSetsToBeWritten, infoLabel);
        if(ListOfParameterSetsToBeWritten!=null){
        infoLabel.setText("Designspace-Size:"+ListOfParameterSetsToBeWritten.size());
        support.log("Designspace without duplicates: "+ListOfParameterSetsToBeWritten.size());
        parent.activateExportButtons();
        }
    isRunning=false;
    }


    public int getSizeOfDesignspace(){
    //Calculate the size of Design space and return just this number
    support.log("Calculating Size of Design Space.");
    int parameterCount=this.jTableParameterList.getModel().getRowCount();
    parameterTableModel tModel=(parameterTableModel) this.jTableParameterList.getModel();
    String [][] parameterArray=tModel.getParameterArray();
    double designSpaceSize=1;
    //ArrayListe aufbauen und Funktion mit dieser Liste aufrufen
    //ArrayList <parameter>ListOfParameterAsFromTable=new ArrayList();//wird in rekursiver Funktion verkleinert
        for (int i=0; i<tModel.getRowCount();i++){
        parameter tmpParameter=new parameter();
        tmpParameter.setName(tModel.getValueAt(i, 0).toString());
        tmpParameter.setValue(tModel.getDoubleValueAt(i, 1));
        tmpParameter.setStartValue(tModel.getDoubleValueAt(i, 1));//=StartValue
        tmpParameter.setEndValue(tModel.getDoubleValueAt(i, 2));
            //If StartValue>EndValue --> exchange them
            if(tmpParameter.getStartValue()>tmpParameter.getEndValue()){
            double tmpValue=tmpParameter.getStartValue();
            tmpParameter.setStartValue(tmpParameter.getEndValue());
            tmpParameter.setEndValue(tmpValue);
            }
        
        
        tmpParameter.setStepping(tModel.getDoubleValueAt(i, 3));
        //ListOfParameterAsFromTable.add(tmpParameter);

        double start,end,step,spaceCounter=1;
        start=support.getDouble(tModel.getValueAt(i, 1).toString());
        end=support.getDouble(tModel.getValueAt(i, 2).toString());
        
            //If start>end then exchange them
            if(start>end){
            step=start;
            start=end;
            end=step;
            }
        step=support.getDouble(tModel.getValueAt(i, 3).toString());
            if((end-start)>0 &&(step!=0) ){
                spaceCounter=(end-start)/step +1;
            }
        support.log(tmpParameter.getName()+" Start: "+start +", End:"+end+", Stepping:"+step+", Counts:"+spaceCounter);
        designSpaceSize=designSpaceSize*(spaceCounter);
        }
    return (int)designSpaceSize;
    }

    public boolean isRunning(){
    return isRunning;
    }

}
