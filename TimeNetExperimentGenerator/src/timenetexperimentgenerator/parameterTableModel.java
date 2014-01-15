/*
 * Christoph Bodenstein
 * Model for Tabel of Parameters
 */

package timenetexperimentgenerator;

import java.util.ArrayList;
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
        parameterArray[i][1]=parent.pConfidenceIntervall.getStartValue();
        parameterArray[i][2]=parent.pConfidenceIntervall.getEndValue();
        parameterArray[i][3]=parent.pConfidenceIntervall.getStepping();
        i= parameterList.getLength()+1;
        parameterArray[i][0]="Seed";
        parameterArray[i][1]=parent.pSeed.getStartValue();
        parameterArray[i][2]=parent.pSeed.getEndValue();
        parameterArray[i][3]=parent.pSeed.getStepping();
        i++;
        parameterArray[i][0]="EndTime";
        parameterArray[i][1]=parent.pEndTime.getStartValue();
        parameterArray[i][2]=parent.pEndTime.getEndValue();
        parameterArray[i][3]=parent.pEndTime.getStepping();
        i++;
        parameterArray[i][0]="MaxTime";
        parameterArray[i][1]=parent.pMaxTime.getStartValue();
        parameterArray[i][2]=parent.pMaxTime.getEndValue();
        parameterArray[i][3]=parent.pMaxTime.getStepping();
        i++;
        parameterArray[i][0]="MaxRelError";
        parameterArray[i][1]=parent.pMaxError.getStartValue();
        parameterArray[i][2]=parent.pMaxError.getEndValue();
        parameterArray[i][3]=parent.pMaxError.getStepping();
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

    public String getValueByName(String Name, String col){
    String returnValue="";
    int count=-1;

    for(int i=0;i<this.parameterList.getLength()+5;i++){
        if(parameterArray[i][0].equals(Name)){
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
}
