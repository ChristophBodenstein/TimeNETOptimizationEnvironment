/*
 * Exports a list of Experiments as xml-files into a given directory.

 * Christoph Bodenstein
 * TU-Ilmenau, FG SSE
 */

package timenetexperimentgenerator;

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
ArrayList<parameter[]> ListOfParameterSetsToBeWritten;
String filename;
JLabel infoLabel;
MainFrame parent;

    exporter(ArrayList<parameter[]> ListOfParameterSetsToBeWritten){
    this.ListOfParameterSetsToBeWritten=ListOfParameterSetsToBeWritten;
    this.filename=support.getOriginalFilename();// filename;
    this.infoLabel=support.getStatusLabel();//infoLabel;
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
                    parameter[] tmpParameterSet=(parameter[])ListOfParameterSetsToBeWritten.get(c);
                    for(int parameterNumber=0; parameterNumber< tmpParameterSet.length;parameterNumber++){
                        if(!tmpParameterSet[parameterNumber].isExternalParameter()){
                            for(int i=0;i<parameterList.getLength();i++){
                                if(parameterList.item(i).getAttributes().getNamedItem("name").getNodeValue().equals(tmpParameterSet[parameterNumber].getName())){
                                parameterList.item(i).getAttributes().getNamedItem("defaultValue").setNodeValue(support.getString(tmpParameterSet[parameterNumber].getValue()));
                                }
                                //support.log(parameterList.item(i).getAttributes().getNamedItem("name").getNodeValue());
                            }
                        }else{
                            if(tmpParameterSet[parameterNumber].getName().equals("MaxTime")){
                            MaxTime=support.getString(tmpParameterSet[parameterNumber].getValue());
                            }
                            if(tmpParameterSet[parameterNumber].getName().equals("EndTime")){
                            EndTime=support.getString(tmpParameterSet[parameterNumber].getValue());
                            }
                            if(tmpParameterSet[parameterNumber].getName().equals("Seed")){
                            Seed=support.getString(tmpParameterSet[parameterNumber].getValue());
                            }
                            if(tmpParameterSet[parameterNumber].getName().equals("ConfidenceIntervall")){
                            ConfidenceIntervall=support.getString(tmpParameterSet[parameterNumber].getValue());
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
      parent.cancelOperation=true;
      }


    }
}



class generator extends Thread{
ArrayList<parameter[]> ListOfParameterSetsToBeWritten;
String filename;
JLabel infoLabel;
MainFrame parent;
JTable jTableParameterList;
boolean isRunning=false;

    generator(ArrayList<parameter[]> ListOfParameterSetsToBeWritten, String filename, JLabel infoLabel, MainFrame parent, JTable jTableParameterList){
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

    //ArrayListe aufbauen und Funktion mit dieser Liste aufrufen
    ArrayList <parameter>ListOfParameterAsFromTable=new ArrayList();//wird in rekursiver Funktion verkleinert
        for (int i=0; i<tModel.getRowCount();i++){
        parameter tmpParameter=new parameter();
        tmpParameter.setName(tModel.getValueAt(i, 0).toString());
        tmpParameter.setValue(tModel.getDoubleValueAt(i, 1));
        tmpParameter.setStartValue(tModel.getDoubleValueAt(i, 1));//=StartValue
        tmpParameter.setEndValue(tModel.getDoubleValueAt(i, 2));
        tmpParameter.setStepping(tModel.getDoubleValueAt(i, 3));
        ListOfParameterAsFromTable.add(tmpParameter);
        }
    //StartParametersatz bilden
    parameter[] lastParameterSet=new parameter[tModel.getRowCount()];
    double[][] parameterValues=new double[tModel.getRowCount()][4];
                //Je Parameter: 0-> Value, 1->StartValue, 2->EndValue, 3->Stepping

        for (int i=0; i<tModel.getRowCount();i++){
        lastParameterSet[i]=new parameter();
        lastParameterSet[i].setName(tModel.getValueAt(i, 0).toString());
        lastParameterSet[i].setValue(tModel.getDoubleValueAt(i, 1));
        parameterValues[i][0]=Double.valueOf(tModel.getValueAt(i, 1).toString());

        lastParameterSet[i].setStartValue(tModel.getDoubleValueAt(i, 1));//=StartValue
        parameterValues[i][1]=Double.valueOf(tModel.getValueAt(i, 1).toString());


        lastParameterSet[i].setEndValue(tModel.getDoubleValueAt(i, 2));
        parameterValues[i][2]=Double.valueOf(tModel.getValueAt(i, 2).toString());
        
        lastParameterSet[i].setStepping(tModel.getDoubleValueAt(i, 3));
        parameterValues[i][3]=Double.valueOf(tModel.getValueAt(i, 3).toString());
        }

        parameter[] tmpParameterSet=new parameter[tModel.getRowCount()];
        //for(int i=0; i<tModel.getRowCount();i++){

        //}


    //Gesamtgröße errechnen lassen
    parent.calculateDesignSpace();
    //Rekursive Funktion aufrufen!
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
    float designSpaceSize=1;
    //ArrayListe aufbauen und Funktion mit dieser Liste aufrufen
    //ArrayList <parameter>ListOfParameterAsFromTable=new ArrayList();//wird in rekursiver Funktion verkleinert
        for (int i=0; i<tModel.getRowCount();i++){
        parameter tmpParameter=new parameter();
        tmpParameter.setName(tModel.getValueAt(i, 0).toString());
        tmpParameter.setValue(tModel.getDoubleValueAt(i, 1));
        tmpParameter.setStartValue(tModel.getDoubleValueAt(i, 1));//=StartValue
        tmpParameter.setEndValue(tModel.getDoubleValueAt(i, 2));
        tmpParameter.setStepping(tModel.getDoubleValueAt(i, 3));
        //ListOfParameterAsFromTable.add(tmpParameter);

        float start,end,step,spaceCounter=1;
        start=Float.parseFloat(tModel.getValueAt(i, 1).toString());
        end=Float.parseFloat(tModel.getValueAt(i, 2).toString());
        step=Float.parseFloat(tModel.getValueAt(i, 3).toString());
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
