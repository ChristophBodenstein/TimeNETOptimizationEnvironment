/*
 * Provides some supporting methods to convert things...
 */

package timenetexperimentgenerator;

import java.io.File;

/**
 *
 * @author Christoph Bodenstein
 */
public class support {
public static final float DEFAULT_STEPPING=(float)1.0;

    public final static float getFloatFromString(String s){
    return Float.parseFloat(s.replace(',', '.'));
    }

    public final static String translateParameterNameFromLogFileToTable(String s){
        if(s.equals("Configured-ConfidenceIntervall")){
        return "ConfidenceIntervall";
        }
        
    return s;
    }

    public static final String removeExtention(String filePath) {
    File f = new File(filePath);
        if (f.isDirectory()) {return filePath;}
        String name = f.getName();
        final int lastPeriodPos = name.lastIndexOf('.');
        if (lastPeriodPos <= 0)
        {return filePath;}
        else
        {File renamed = new File(f.getParent(), name.substring(0, lastPeriodPos));
         return renamed.getPath();
        }
    }
}
