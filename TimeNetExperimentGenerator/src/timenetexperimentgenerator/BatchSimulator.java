/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package timenetexperimentgenerator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.JOptionPane;


/**
 *
 * @author bode
 */
public class BatchSimulator implements Runnable{
ArrayList<parameter[]> ListOfParameterSetsToBeWritten;
String filename;
JLabel infoLabel;
MainFrame parent;

    BatchSimulator(ArrayList<parameter[]> ListOfParameterSetsToBeWritten){
    this.ListOfParameterSetsToBeWritten=ListOfParameterSetsToBeWritten;
    this.filename=support.getOriginalFilename();// filename;
    this.infoLabel=support.getStatusLabel();//infoLabel;
    this.parent=support.getMainFrame();//parent;
    
    new Thread(this).start();
    }

    public void run() {
    String outputDir=support.getTmpPath();
    File f = new File(this.filename);
    /*JFileChooser fileChooser = new JFileChooser(f.getParent());
    fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
    fileChooser.setControlButtonsAreShown(true);
    fileChooser.setCurrentDirectory(f);
    fileChooser.setDialogTitle("Dir for export of "+ListOfParameterSetsToBeWritten.size() +" Experiments. Go INTO the dir to choose it!");
    */


      //if (fileChooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
    if ((outputDir!=null) &&(!outputDir.equals(""))) {
     /*   if(fileChooser.getSelectedFile().isDirectory() ){
            outputDir=fileChooser.getSelectedFile().toString();
        }else{
            outputDir=fileChooser.getCurrentDirectory().toString();
        }
        support.log("choosen outputdir: "+outputDir);
            */
        try{
            Simulator myGenericSimulator=SimOptiFactory.getSimulator();
            myGenericSimulator.initSimulator(ListOfParameterSetsToBeWritten, 0);
            while(myGenericSimulator.getStatus()<100){
            //Wait for End of all simulations
            Thread.sleep(500);
            this.infoLabel.setText("Done "+ myGenericSimulator.getStatus() +"%");
            }

        }catch(Exception e){
            e.printStackTrace();
            JOptionPane.showMessageDialog(parent, "Es gab einen Fehler beim Export oder der Simulation.");
            }
      JOptionPane.showMessageDialog(parent, "Es wurden "+ListOfParameterSetsToBeWritten.size()+" Simulationen gestartet.");
      infoLabel.setText("");
      }
    else {
      support.log("No TMP Path given.");
      parent.cancelOperation=true;
      }


    }


    public String removeExtention(String filePath) {
    // These first few lines the same as Justin's
    File f = new File(filePath);

        // if it's a directory, don't remove the extention
        if (f.isDirectory()) {
            return filePath;
        }

        String name = f.getName();

        // Now we know it's a file - don't need to do any special hidden
        // checking or contains() checking because of:
        final int lastPeriodPos = name.lastIndexOf('.');
        if (lastPeriodPos <= 0)
        {
            // No period after first character - return name as it was passed in
            return filePath;
        }
        else
        {
            // Remove the last period and everything after it
            File renamed = new File(f.getParent(), name.substring(0, lastPeriodPos));
            return renamed.getPath();
        }
    }

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
          support.log("File copied.");
          return true;
        }
        catch(FileNotFoundException ex){
          support.log(ex.getMessage() + " in the specified directory.");
          return false;
        }
        catch(IOException e){
          support.log(e.getMessage());
          return false;
        }
    }

}

class killProcessTimer implements Runnable{
long time=0;
Process p=null;
    killProcessTimer(long timeInMillis, Process p){
        this.time=timeInMillis;
        this.p=p;
    new Thread(this).start();
    }

    public void run() {
        try {
            this.wait(time);
            p.destroy();
        } catch (InterruptedException ex) {
            Logger.getLogger(killProcessTimer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
