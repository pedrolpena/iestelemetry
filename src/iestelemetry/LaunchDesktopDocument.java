
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package iestelemetry;

import java.awt.Desktop;
import java.io.File;



/**
 *
 * @author Pedro.Pena
 */


/*
 This class will use the host OS to launch registered documents
 */

public class LaunchDesktopDocument {

    public LaunchDesktopDocument(File f)throws Exception{
        Desktop dt = Desktop.getDesktop();
        if(dt.isDesktopSupported()){
            try{
                dt.open(f);
            }// end try
            catch(Exception e){

                throw e;
            }// end catch


        }//end if

    }// end constructor

}
