/*
 * Christoph Bodenstein
 * Model for Table of Parameters

 * Christoph Bodenstein
 * TU-Ilmenau, FG SSE
 */
package toe.helper;

import java.util.ArrayList;
import javax.swing.table.AbstractTableModel;
import org.w3c.dom.*;
import toe.MainFrame;
import toe.datamodel.parameter;
import toe.support;
import toe.typedef.typeOfLogLevel;

/**
 *
 * @author Christoph Bodenstein
 */
public class parameterTableModel extends AbstractTableModel {

    private final NodeList parameterList;
    private final String[] columnNames = {"Name", "StartValue", "EndValue", "Stepping"};
    private final String[][] parameterArray;

    /**
     * Constructor
     *
     * @param p Nodelist from XML-File
     * @param parent Parent Frame to show some information on infoLabel etc.
     */
    public parameterTableModel(NodeList p, MainFrame parent) {
        this.parameterList = p;
        this.parameterArray = new String[p.getLength() + 5][4];

        for (int i = 0; i < parameterList.getLength(); i++) {
            parameterArray[i][0] = parameterList.item(i).getAttributes().getNamedItem("name").getNodeValue();
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
            //Use double conversion to map all paramters to double (1.0)
            parameterArray[i][1] = support.getString(support.getDouble(parameterList.item(i).getAttributes().getNamedItem("defaultValue").getNodeValue()));
            parameterArray[i][2] = support.getString(support.getDouble(parameterList.item(i).getAttributes().getNamedItem("defaultValue").getNodeValue()));
            parameterArray[i][3] = "1.0";
        }
        int i = parameterList.getLength();
        parameterArray[i][0] = "ConfidenceIntervall";
        parameterArray[i][1] = support.getString(parent.getpConfidenceIntervall().getStartValue());
        parameterArray[i][2] = support.getString(parent.getpConfidenceIntervall().getEndValue());
        parameterArray[i][3] = support.getString(parent.getpConfidenceIntervall().getStepping());
        i = parameterList.getLength() + 1;
        parameterArray[i][0] = "Seed";
        parameterArray[i][1] = support.getString(parent.getpSeed().getStartValue());
        parameterArray[i][2] = support.getString(parent.getpSeed().getEndValue());
        parameterArray[i][3] = support.getString(parent.getpSeed().getStepping());
        i++;
        parameterArray[i][0] = "EndTime";
        parameterArray[i][1] = support.getString(parent.getpEndTime().getStartValue());
        parameterArray[i][2] = support.getString(parent.getpEndTime().getEndValue());
        parameterArray[i][3] = support.getString(parent.getpEndTime().getStepping());
        i++;
        parameterArray[i][0] = "MaxTime";
        parameterArray[i][1] = support.getString(parent.getpMaxTime().getStartValue());
        parameterArray[i][2] = support.getString(parent.getpMaxTime().getEndValue());
        parameterArray[i][3] = support.getString(parent.getpMaxTime().getStepping());
        i++;
        parameterArray[i][0] = "MaxRelError";
        parameterArray[i][1] = support.getString(parent.getpMaxError().getStartValue());
        parameterArray[i][2] = support.getString(parent.getpMaxError().getEndValue());
        parameterArray[i][3] = support.getString(parent.getpMaxError().getStepping());
    }

    /**
     * Returns raw String values from Table cells as Array
     *
     * @return 2-Dimensional Array of Strings with table contents
     */
    public String[][] getParameterArray() {
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

    @Override
    public Object getValueAt(int row, int col) {
        String result = Double.toString(support.DEFAULT_DOUBLE_VALUE);
        try {
            result = parameterArray[row][col];
        } catch (Exception e) {
            support.log("Problem reading String value from table.", typeOfLogLevel.ERROR);
            setValueAt(result, row, col);
        }
        return result;
    }

    public double getDoubleValueAt(int row, int col) {
        double result = support.DEFAULT_DOUBLE_VALUE;
        try {
            result = support.round(support.getDouble(parameterArray[row][col]), 3);
        } catch (Exception e) {
            support.log("Poblem reading float value from table.", typeOfLogLevel.ERROR);
            setValueAt(Double.toString(result), row, col);
        }
        return result;
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
        return col >= 1;
    }

    /*
     * Don't need to implement this method unless your table's
     * data can change.
     */
    @Override
    public void setValueAt(Object value, int row, int col) {
        String stringValue = "1.0";
        try {
            //Double conversion to get double-Strings (1.0)
            stringValue = support.getString(support.getDouble(value.toString()));
        } catch (Exception e) {
            support.log("Error setting value in table.", typeOfLogLevel.ERROR);
            stringValue = "1.0";
        }
        parameterArray[row][col] = stringValue;
        fireTableCellUpdated(row, col);
    }

    /**
     * Gets the Value for StartValue, EndValue or Stepping for one parameter as
     * String The fieldname (StartValue, EndValue or Stepping) must be given as
     * String for col
     *
     * @param name name of the parameter
     * @param col column of the parameter (StartValue, EndValue, Stepping)
     * @return The Value of one table cell
     */
    public String getValueByName(String name, String col) {
        String returnValue = null;
        int count = -1;
        //Search the row for this parameter
        for (int i = 0; i < this.parameterList.getLength() + 5; i++) {
            if (parameterArray[i][0].equals(name)) {
                count = i;
            }
        }
        if (count == -1) {
            return null;
        } else {

            if (col.equals("StartValue")) {
                return (parameterArray[count][1]);
            }
            if (col.equals("EndValue")) {
                return (parameterArray[count][2]);
            }
            if (col.equals("Stepping")) {
                return (parameterArray[count][3]);
            }

        }

        return returnValue;
    }

    /**
     * Gets the Value for StartValue, EndValue or Stepping for one parameter as
     * double The fieldname (StartValue, EndValue or Stepping) must be given as
     * String for col
     *
     * @param name name of the parameter
     * @param col column of the parameter (StartValue, EndValue, Stepping)
     * @return Double value of table cell
     */
    public double getDoubleValueByName(String name, String col) {

        return support.getDouble(this.getValueByName(name, col));

    }

    /**
     * Set the value for StartValue, EndValue or Stepping The fieldname
     * (StartValue, EndValue or Stepping) must be given as String Value must
     * also be given as String
     *
     * @param name name of the parameter
     * @param col column of the parameter (StartValue, EndValue, Stepping)
     * @param value the value to be set
     * @return true if successfull, else false
     */
    public boolean setValueByName(String name, String col, String value) {
        boolean returnValue = false;
        int count = -1;
        //Search the row for this parameter
        for (int i = 0; i < this.parameterList.getLength() + 5; i++) {
            if (parameterArray[i][0].equals(name)) {
                count = i;
            }
        }

        if (count == -1) {
            returnValue = false;
        } else {
            if (col.equals("StartValue")) {
                parameterArray[count][1] = value;
                returnValue = true;
            }
            if (col.equals("EndValue")) {
                parameterArray[count][2] = value;
                returnValue = true;
            }
            if (col.equals("Stepping")) {
                parameterArray[count][3] = value;
                returnValue = true;
            }
        }

        return returnValue;
    }

    /**
     * Set the value for StartValue, EndValue or Stepping as double The
     * fieldname (StartValue, EndValue or Stepping) must be given as String
     * Value must also be given as Double
     *
     * @param name name of the parameter
     * @param col column of the parameter (StartValue, EndValue, Stepping)
     * @param value the value to be set
     * @return true if successfull, else false
     */
    public boolean setValueByName(String name, String col, double value) {

        return this.setValueByName(name, col, Double.toString(value));

    }

    /**
     * Returns ArrayList of Parameters as shown in table, used as base
     * parameterset
     *
     * @return ArrayList of Parameters, Base Parameterset for Optimization and
     * Batch-Simulation
     */
    public ArrayList<parameter> getListOfParameter() {
        //Build initial ArrayList of parameters
        ArrayList<parameter> ListOfParameterAsFromTable = new ArrayList();//will be reduced recursively
        for (int i = 0; i < this.getRowCount(); i++) {
            parameter tmpParameter = new parameter();
            tmpParameter.setName(this.getValueAt(i, 0).toString());
            tmpParameter.setValue(this.getDoubleValueAt(i, 1));
            tmpParameter.setStartValue(this.getDoubleValueAt(i, 1));//=StartValue
            tmpParameter.setEndValue(this.getDoubleValueAt(i, 2));
            //If StartValue>EndValue --> exchange them
            if (tmpParameter.getStartValue() > tmpParameter.getEndValue()) {
                double tmpValue = tmpParameter.getStartValue();
                tmpParameter.setStartValue(tmpParameter.getEndValue());
                tmpParameter.setEndValue(tmpValue);
            }
            tmpParameter.setStepping(this.getDoubleValueAt(i, 3));
            ListOfParameterAsFromTable.add(tmpParameter);
        }
        return ListOfParameterAsFromTable;
    }
}
