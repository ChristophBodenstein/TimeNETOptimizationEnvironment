/*
 * Christoph Bodenstein
 * Model for Tabel of Parameters

 * Christoph Bodenstein
 * TU-Ilmenau, FG SSE
 */

package timenetexperimentgenerator;

import javax.swing.table.AbstractTableModel;
import org.w3c.dom.*;

/**
 *
 * @author sse
 */

public class parameterTableModel extends AbstractTableModel {
private NodeList parameterList;
private String[] columnNames ={"Name","StartValue","EndValue","Stepping"};
private String[][] parameterArray;

    /**
     *Constructor
     */
    parameterTableModel(NodeList p, MainFrame parent){
    this.parameterList=p;
    this.parameterArray=new String[p.getLength()+5][4];
    
        for(int i=0;i<parameterList.getLength();i++){
        parameterArray[i][0]=parameterList.item(i).getAttributes().getNamedItem("name").getNodeValue();
/*            if(parameterList.item(i).getAttributes().getNamedItem("dataType").getNodeValue().equals("int")){
            parameterArray[i][1]=(Integer.getInteger(parameterList.item(i).getAttributes().getNamedItem("defaultValue").getNodeValue())).toString();
            parameterArray[i][2]=(Integer.getInteger(parameterList.item(i).getAttributes().getNamedItem("defaultValue").getNodeValue())).toString();
            }else {

                    if(parameterList.item(i).getAttributes().getNamedItem("dataType").getNodeValue().equals("real")){
                    parameterArray[i][1]=(Double.valueOf(parameterList.item(i).getAttributes().getNamedItem("defaultValue").getNodeValue())).toString();
                    parameterArray[i][2]=(Double.valueOf(parameterList.item(i).getAttributes().getNamedItem("defaultValue").getNodeValue())).toString();
                    }else{
                    parameterArray[i][1]=parameterList.item(i).getAttributes().getNamedItem("defaultValue").getNodeValue();
                    parameterArray[i][2]=parameterList.item(i).getAttributes().getNamedItem("defaultValue").getNodeValue();
                    }
             }
*/
        parameterArray[i][1]=parameterList.item(i).getAttributes().getNamedItem("defaultValue").getNodeValue();
        parameterArray[i][2]=parameterList.item(i).getAttributes().getNamedItem("defaultValue").getNodeValue();
        parameterArray[i][3]="1";
        }
        int i= parameterList.getLength();
        parameterArray[i][0]="ConfidenceIntervall";
        parameterArray[i][1]=support.getString(parent.pConfidenceIntervall.getStartValue());
        parameterArray[i][2]=support.getString(parent.pConfidenceIntervall.getEndValue());
        parameterArray[i][3]=support.getString(parent.pConfidenceIntervall.getStepping());
        i= parameterList.getLength()+1;
        parameterArray[i][0]="Seed";
        parameterArray[i][1]=support.getString(parent.pSeed.getStartValue());
        parameterArray[i][2]=support.getString(parent.pSeed.getEndValue());
        parameterArray[i][3]=support.getString(parent.pSeed.getStepping());
        i++;
        parameterArray[i][0]="EndTime";
        parameterArray[i][1]=support.getString(parent.pEndTime.getStartValue());
        parameterArray[i][2]=support.getString(parent.pEndTime.getEndValue());
        parameterArray[i][3]=support.getString(parent.pEndTime.getStepping());
        i++;
        parameterArray[i][0]="MaxTime";
        parameterArray[i][1]=support.getString(parent.pMaxTime.getStartValue());
        parameterArray[i][2]=support.getString(parent.pMaxTime.getEndValue());
        parameterArray[i][3]=support.getString(parent.pMaxTime.getStepping());
        i++;
        parameterArray[i][0]="MaxRelError";
        parameterArray[i][1]=support.getString(parent.pMaxError.getStartValue());
        parameterArray[i][2]=support.getString(parent.pMaxError.getEndValue());
        parameterArray[i][3]=support.getString(parent.pMaxError.getStepping());
    }

    public String[][] getParameterArray(){
    return this.parameterArray;    
    }

    
    public int getColumnCount() {
        return 4;
    }

    public int getRowCount() {
        return this.parameterArray.length;
    }

    @Override
    public String getColumnName(int col) {
        return columnNames[col];
    }

    public Object getValueAt(int row, int col) {
        return parameterArray[row][col];
    }
    
    public double getDoubleValueAt(int row, int col) {
        return Double.valueOf(parameterArray[row][col].toString());
    }

    @Override
    public Class getColumnClass(int c) {
        return String.class;
    }

    /*
     * Don't need to implement this method unless your table's
     * editable.
     */
    @Override
    public boolean isCellEditable(int row, int col) {
        //Note that the data/cell address is constant,
        //no matter where the cell appears onscreen.
        if (col < 1) {
            return false;
        } else {
            return true;
        }
    }

    /*
     * Don't need to implement this method unless your table's
     * data can change.
     */
    @Override
    public void setValueAt(Object value, int row, int col) {
        parameterArray[row][col] = value.toString();
        fireTableCellUpdated(row, col);
    }


    /**
     * Gets the Value for StartValue, EndValue or Stepping for one parameter as String
     * The fieldname (StartValue, EndValue or Stepping) must be given as String for col
     */
    public String getValueByName(String name, String col){
    String returnValue=null;
    int count=-1;
    //Search the row for this parameter
    for(int i=0;i<this.parameterList.getLength()+5;i++){
        if(parameterArray[i][0].equals(name)){
        count=i;
        }
    }
    if(count==-1){return null;}else{

        if(col.equals("StartValue")){
        return(parameterArray[count][1]);
        }
        if(col.equals("EndValue")){
        return(parameterArray[count][2]);
        }
        if(col.equals("Stepping")){
        return(parameterArray[count][3]);
        }

    }

    return returnValue;
    }

    /**
     * Gets the Value for StartValue, EndValue or Stepping for one parameter as double
     * The fieldname (StartValue, EndValue or Stepping) must be given as String for col
     */
    public double getDoubleValueByName(String name, String col){

       return Double.valueOf(this.getValueByName(name, col));

    }


    /**
     * Set the value for StartValue, EndValue or Stepping
     * The fieldname (StartValue, EndValue or Stepping) must be given as String
     * Value must also be given as String
     */
    public boolean setValueByName(String name, String col, String value){
    boolean returnValue=false;
    int count=-1;
    //Search the row for this parameter
    for(int i=0;i<this.parameterList.getLength()+5;i++){
        if(parameterArray[i][0].equals(name)){
        count=i;
        }
    }

    if(count==-1){returnValue=false;}else{
        if(col.equals("StartValue")){
        parameterArray[count][1]=value;
        returnValue=true;
        }
        if(col.equals("EndValue")){
        parameterArray[count][2]=value;
        returnValue=true;
        }
        if(col.equals("Stepping")){
        parameterArray[count][3]=value;
        returnValue=true;
        }
    }

    return returnValue;
    }


    /**
     * Set the value for StartValue, EndValue or Stepping as double
     * The fieldname (StartValue, EndValue or Stepping) must be given as String
     * Value must also be given as String
     */
    public boolean setValueByName(String name, String col, double value){
    
        return this.setValueByName(name, col, Double.toString(value));
        
    }


}
