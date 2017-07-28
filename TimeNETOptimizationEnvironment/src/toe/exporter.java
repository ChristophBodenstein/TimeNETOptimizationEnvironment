/*
 * Exports a list of Experiments as xml-files into a given directory.
 * Calculates the design space
 *
 * Christoph Bodenstein
 * TU-Ilmenau, FG SSE
 */
package toe;

import toe.helper.parameterTableModel;
import toe.datamodel.parameter;
import java.io.File;
import java.util.ArrayList;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.*;
import toe.typedef.*;

/**
 *
 * @author Christoph Bodenstein
 */
public class exporter implements Runnable {

    ArrayList< ArrayList<parameter>> ListOfParameterSetsToBeWritten;
    String filename;
    MainFrame parent;

    exporter(ArrayList< ArrayList<parameter>> ListOfParameterSetsToBeWritten) {
        this.ListOfParameterSetsToBeWritten = ListOfParameterSetsToBeWritten;
        this.filename = support.getOriginalFilename();// filename;
        this.parent = support.getMainFrame();//parent;

        new Thread(this).start();
    }

    @Override
    public void run() {
        File f = new File(this.filename);
        JFileChooser fileChooser = new JFileChooser(f.getParent());
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
        fileChooser.setControlButtonsAreShown(true);
        fileChooser.setCurrentDirectory(f);
        fileChooser.setDialogTitle("Dir for export of " + ListOfParameterSetsToBeWritten.size() + " Experiments. Go INTO the dir to choose it!");

        if (fileChooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
            support.log("getCurrentDirectory(): "
                    + fileChooser.getCurrentDirectory(), typeOfLogLevel.INFO);
            support.log("getSelectedFile() : "
                    + fileChooser.getSelectedFile(), typeOfLogLevel.INFO);

            try {

                for (int c = 0; c < ListOfParameterSetsToBeWritten.size(); c++) {
                    DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
                    Document doc = docBuilder.parse(this.filename);
                    NodeList parameterList = doc.getElementsByTagName("parameter");
                    String ConfidenceIntervall = "", Seed = "", EndTime = "", MaxTime = "";
                    ArrayList<parameter> tmpParameterSet = ListOfParameterSetsToBeWritten.get(c);
                    for (int parameterNumber = 0; parameterNumber < tmpParameterSet.size(); parameterNumber++) {
                        if (!tmpParameterSet.get(parameterNumber).isExternalParameter()) {
                            for (int i = 0; i < parameterList.getLength(); i++) {
                                if (parameterList.item(i).getAttributes().getNamedItem("name").getNodeValue().equals(tmpParameterSet.get(parameterNumber).getName())) {
                                    parameterList.item(i).getAttributes().getNamedItem("defaultValue").setNodeValue(support.getString(tmpParameterSet.get(parameterNumber).getValue()));
                                }
                                //support.log(parameterList.item(i).getAttributes().getNamedItem("name").getNodeValue());
                            }
                        } else {
                            if (tmpParameterSet.get(parameterNumber).getName().equals("MaxTime")) {
                                MaxTime = support.getString(tmpParameterSet.get(parameterNumber).getValue());
                            }
                            if (tmpParameterSet.get(parameterNumber).getName().equals("EndTime")) {
                                EndTime = support.getString(tmpParameterSet.get(parameterNumber).getValue());
                            }
                            if (tmpParameterSet.get(parameterNumber).getName().equals("Seed")) {
                                Seed = support.getString(tmpParameterSet.get(parameterNumber).getValue());
                            }
                            if (tmpParameterSet.get(parameterNumber).getName().equals("ConfidenceIntervall")) {
                                ConfidenceIntervall = support.getString(tmpParameterSet.get(parameterNumber).getValue());
                            }
                        }
                    }
                    //set status text
                    support.setStatusText("Export of file " + (c + 1) + "/" + ListOfParameterSetsToBeWritten.size());
                    //Create filename
                    String tmpDirName = "";
                    support.log("Choosen File: " + fileChooser.getSelectedFile(), typeOfLogLevel.INFO);
                    String exportFileName = fileChooser.getCurrentDirectory() + File.separator + support.removeExtention(f.getName()) + "_n_" + c + "_MaxTime_" + MaxTime + "_EndTime_" + EndTime + "_Seed_" + Seed + "_ConfidenceIntervall_" + ConfidenceIntervall + "_.xml";
                    //Exportieren
                    support.log("File to export: " + exportFileName, typeOfLogLevel.INFO);

                    TransformerFactory tFactory
                            = TransformerFactory.newInstance();
                    Transformer transformer = tFactory.newTransformer();

                    DOMSource source = new DOMSource(doc);
                    StreamResult result = new StreamResult(new File(exportFileName));
                    transformer.transform(source, result);

                }
            } catch (Exception e) {
                e.printStackTrace();
                support.log("Error exporting files. Will print stacktrace.", typeOfLogLevel.ERROR);
                support.log(e.getLocalizedMessage(), typeOfLogLevel.ERROR);
                JOptionPane.showMessageDialog(parent, "Error exporting files.");
            }
            support.log("Successfully exported " + ListOfParameterSetsToBeWritten.size() + " files", typeOfLogLevel.INFO);
            JOptionPane.showMessageDialog(parent, "Successfully exported " + ListOfParameterSetsToBeWritten.size() + " files.");
            support.setStatusText("");
        } else {
            support.log("No Selection ", typeOfLogLevel.INFO);
            //parent.cancelOperation=true;
        }

    }
}

class generator extends Thread {

    ArrayList< ArrayList<parameter>> ListOfParameterSetsToBeWritten;
    String filename;
    MainFrame parent;
    JTable jTableParameterList;
    boolean isRunning = false;

    /**
     * Constructor
     *
     * @param ListOfParameterSetsToBeWritten List of Parametersets to be
     * exported as files (the list ist calculated with
     * buildListOfParameterSetsToExport of Mainrame
     * @param filename name of simulation file (master file to be copied)
     * @param parent parent frame (will be removed later)
     * @param jTableParameterList List of all Parameters from table
     */
    generator(ArrayList< ArrayList<parameter>> ListOfParameterSetsToBeWritten, String filename, MainFrame parent, JTable jTableParameterList) {
        this.ListOfParameterSetsToBeWritten = ListOfParameterSetsToBeWritten;
        this.filename = filename;
        this.parent = parent;
        this.jTableParameterList = jTableParameterList;
    }

    @Override
    public void run() {
        isRunning = true;
        parent.deactivateExportButtons();
        support.setStatusText("Generating all Experiments.");
        parameterTableModel tModel = (parameterTableModel) this.jTableParameterList.getModel();

        //Build initial ArrayList of parameters
        ArrayList<parameter> ListOfParameterAsFromTable = tModel.getListOfParameter();
        //build first parameterset
        ArrayList<parameter> lastParameterSet = support.getCopyOfParameterSet(ListOfParameterAsFromTable);

        //Calculate size of designspace
        parent.calculateDesignSpace();
        support.setStatusText("Designspace-Size:" + this.getSizeOfDesignspace());

        //call recursive generation-function!
        parent.buildListOfParameterSetsToExport(ListOfParameterSetsToBeWritten, ListOfParameterAsFromTable, lastParameterSet);

        support.log("Designspace-Size (incl. duplicates): " + ListOfParameterSetsToBeWritten.size(), typeOfLogLevel.INFO);
        support.setStatusText("Size is about: " + ListOfParameterSetsToBeWritten.size() + "Removing Duplicates.");

        //ListOfParameterSetsToBeWritten=parent.removeDuplicates(ListOfParameterSetsToBeWritten, infoLabel);
        if (ListOfParameterSetsToBeWritten != null) {
            support.setStatusText("Designspace-Size:" + ListOfParameterSetsToBeWritten.size());
            support.log("Designspace without duplicates: " + ListOfParameterSetsToBeWritten.size(), typeOfLogLevel.INFO);
            parent.activateExportButtons();
        }
        isRunning = false;
    }

    /**
     * Calculates the size of design space, defined by parameter table
     *
     * @return Size of design space as int
     */
    public int getSizeOfDesignspace() {
        support.log("Calculating Size of Design Space.", typeOfLogLevel.INFO);
        parameterTableModel tModel = (parameterTableModel) this.jTableParameterList.getModel();
        double designSpaceSize = 1;

        for (int i = 0; i < tModel.getRowCount(); i++) {
            parameter tmpParameter = new parameter();
            tmpParameter.setName(tModel.getValueAt(i, 0).toString());
            tmpParameter.setValue(tModel.getDoubleValueAt(i, 1));
            tmpParameter.setStartValue(tModel.getDoubleValueAt(i, 1));//=StartValue
            tmpParameter.setEndValue(tModel.getDoubleValueAt(i, 2));
            //If StartValue>EndValue --> exchange them
            if (tmpParameter.getStartValue() > tmpParameter.getEndValue()) {
                double tmpValue = tmpParameter.getStartValue();
                tmpParameter.setStartValue(tmpParameter.getEndValue());
                tmpParameter.setEndValue(tmpValue);
            }

            tmpParameter.setStepping(tModel.getDoubleValueAt(i, 3));

            double start, end, step, spaceCounter = 1;
            start = tModel.getDoubleValueAt(i, 1);
            end = tModel.getDoubleValueAt(i, 2);

            //If start>end then exchange them
            if (start > end) {
                step = start;
                start = end;
                end = step;
            }
            step = tModel.getDoubleValueAt(i, 3);
            if ((end - start) > 0 && (step != 0)) {
                spaceCounter = (end - start) / step + 1;
            }
            support.log(tmpParameter.getName() + " Start: " + start + ", End:" + end + ", Stepping:" + step + ", Counts:" + spaceCounter, typeOfLogLevel.INFO);
            designSpaceSize = designSpaceSize * (spaceCounter);
        }
        return (int) designSpaceSize;
    }

    public boolean isRunning() {
        return isRunning;
    }

}
