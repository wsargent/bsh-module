/*<ATGCOPYRIGHT>
 * Copyright (C) 2001 Art Technology Group, Inc.
 * All Rights Reserved.  No use, copying or distribution of this
 * work may be made except in accordance with a valid license
 * agreement from Art Technology Group.  This notice must be
 * included on all copies, modifications and derivatives of this
 * work.
 *
 * Art Technology Group (ATG) MAKES NO REPRESENTATIONS OR WARRANTIES
 * ABOUT THE SUITABILITY OF THE SOFTWARE, EITHER EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE, OR NON-INFRINGEMENT. ATG SHALL NOT BE
 * LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING,
 * MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 *
 * Dynamo is a trademark of Art Technology Group, Inc.
 </ATGCOPYRIGHT>*/

package atg.bsh;

import bsh.*;

import atg.droplet.*;
import atg.servlet.*;
import atg.core.util.StringUtils;

import java.io.*;
import java.util.*;
import javax.servlet.ServletException;

import atg.droplet.GenericFormHandler;

/**
 * This is a form handler which accesses a bsh script on a handle submit
 * method.
 *
 * @author Will Sargent
 * @version $Id: //user/wsargent/bsh/main/src/atg/bsh/BshFormHandler.java#2 $
 */

public class BshFormHandler extends GenericFormHandler
{
    //-------------------------------------
    // Constants
    //-------------------------------------

    // Error codes.
    public static final String TARGET_ERROR = "TARGET_ERROR";
    public static final String IO_EXCEPTION = "IO_EXCEPTION";
    public static final String EVAL_ERROR = "EVAL_ERROR";

    private Interpreter mInterpreter;
    public Interpreter getInterpreter()
    {
        return mInterpreter;
    }

    //-------------------------------------
    // Properties
    //-------------------------------------

    File mFile;
    public File getFile()
    {
        return mFile;
    }
    public void setFile(File pFile)
    {
        mFile = pFile;
    }

    //-------------------------------------

    Map mValues = new TreeMap();

    public Map getValues()
    {
        return mValues;
    }
    
    public void setValues(Map pValues)
    {
        mValues = pValues;    
    }
    
    public void resetValues()
    {
        mValues = new TreeMap();    
    }
    
    public boolean handleReset( DynamoHttpServletRequest pRequest,
                                 DynamoHttpServletResponse pResponse)
        throws ServletException, IOException
    {
        resetValues();
        return true;
    }
        
    //-------------------------------------

    public void doStartService()
    {
        mInterpreter = new Interpreter();
    }

    //-------------------------------------
    /**
     *
     */
    public boolean handleSubmit( DynamoHttpServletRequest pRequest,
                                 DynamoHttpServletResponse pResponse)
        throws ServletException, IOException
    {
        Map values = getValues();
        Boolean keepGoing = (Boolean) evaluate("submit", pRequest, pResponse, values);
        if (keepGoing != null)
        {
            return keepGoing.booleanValue();
        }

        return true;
    }


    //-------------------------------------
    /**
     * Evaluates the tag converter according to the appropriate method.  The bsh
     * script can access the converter itself by calling "converter".
     * @param pMethod the name of the method which called us.  Set as "method".
     * @param pRequest the request.  Set as "request".
     * @param pResponse the response.  Set as "response".
     * @param pValues a Map of values that the script can get to.  Set as "values".
     * @return the converted value, pull from the bsh variable "result".
     * @throws TagConversionException if an error occured.
     */
    public Object evaluate(String pMethod,
                           DynamoHttpServletRequest pRequest,
                           DynamoHttpServletResponse pResponse,
                           Map pValues)
        throws DropletFormException
    {
        try
        {
            Interpreter bsh = getInterpreter();
            File file = getFile();
            
            if ( isLoggingDebug() )
            {
                String msg = "Evaluating " + pMethod + " using " + file.getPath();
                logDebug(msg);    
            }
            
            Reader reader = new BufferedReader(new FileReader(file));
            bsh.set("method", pMethod);
            bsh.set("request", pRequest);
            bsh.set("response", pResponse);
            bsh.set("values", pValues);
            bsh.set("service", this);
            bsh.eval(reader);
            return bsh.get("result");
        } catch (TargetError te)
        {
            /** @todo i18n */
            Throwable target = te.getTarget();
            String msg = "The script or code called by the script threw an exception: " + target;
            String errorCode = TARGET_ERROR;
            if (isLoggingError())
            {
                logError(msg);
            }
            throw new DropletFormException(msg, te, errorCode);
        } catch (EvalError ee)
        {
            /** @todo i18n */
            String msg = "There was an error in evaluating the script:";
            String errorCode = EVAL_ERROR;
            if (isLoggingError())
            {
                logError(msg, ee);
            }
            throw new DropletFormException(msg, ee, errorCode);
        } catch (IOException ie)
        {
            /** @todo i18n */
            String msg = "IO Exception: ";
            String errorCode = IO_EXCEPTION;
            if (isLoggingError())
            {
                logError(msg, ie);
            }
            throw new DropletFormException(msg, ie, errorCode);
        }
    }

}
