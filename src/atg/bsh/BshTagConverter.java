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

import atg.repository.*;

import java.io.*;
import javax.servlet.ServletException;

import atg.droplet.*;
import atg.nucleus.GenericService;
import java.util.Properties;

public class BshTagConverter extends GenericService
implements TagConverter
{
    //-------------------------------------
    // Constants
    //-------------------------------------

    // Error codes.
    public static final String TARGET_ERROR = "TARGET_ERROR";
    public static final String IO_EXCEPTION = "IO_EXCEPTION";
    public static final String EVAL_ERROR = "EVAL_ERROR";

    //-------------------------------------
    // Properties
    //-------------------------------------

    private Interpreter mInterpreter;
    public Interpreter getInterpreter()
    {
        return mInterpreter;
    }

    //-------------------------------------

    String mFilename;
    public String getFilename()
    {
        return mFilename;
    }
    public void setFilename(String pFilename)
    {
        mFilename = pFilename;
    }

    //-------------------------------------

    String mName;
    public String getName()
    {
        return mName;
    }

    public void setName(String pName)
    {
        mName = pName;
    }

    //-------------------------------------
    /**
     *
     */
    public TagAttributeDescriptor[] getTagAttributeDescriptors()
    {
        String name = getName();
        String desc = "A string that defines the converter.";
        boolean optional = false;
        boolean auto = false;
        TagAttributeDescriptor[] args = {
              new TagAttributeDescriptor(name, desc, optional, auto)
        };
        return args;
    }

    //-------------------------------------
    /**
     * Creates a new interpreter and registers the tag converter with the
     * TagConverterManager.
     */
    public void doStartService()
    {
        if (isLoggingDebug())
        {
            logDebug("Starting converter: " + getName());
        }
        mInterpreter = new Interpreter();
        TagConverterManager.registerTagConverter(this);
    }

    //-------------------------------------
    /**
     * Calls evaluate() with the method set to "convertStringToObject" and
     * returns the result.
     */
    public Object convertStringToObject( DynamoHttpServletRequest pRequest,
                                         String pValue, Properties pAttributes)
        throws TagConversionException
    {
        String method = "convertStringToObject";
        Object result = evaluate(method, pRequest, pValue, pAttributes);

        if (isLoggingDebug())
        {
            String msg = "pValue = [" + pValue + "], "
                         + "pAttributes = [" + pAttributes + "], "
                         + "result = [" + result + "]";
            logDebug(msg);
        }

        return result;
    }

    //-------------------------------------
    /**
     * Calls evaluate() with the method set to "convertObjectToString", and
     * returns the result.
     */
    public String convertObjectToString( DynamoHttpServletRequest pRequest,
                                         Object pValue, Properties pAttributes)
        throws TagConversionException
    {
        String method = "convertObjectToString";
        Object result = evaluate(method, pRequest, pValue, pAttributes);

        if (isLoggingDebug())
        {
            String msg = "pValue = [" + pValue + "], "
                         + "pAttributes = [" + pAttributes + "], "
                         + "result = [" + result + "]";
            logDebug(msg);
        }

        if ( !(result instanceof String) )
        {
            String msg = "Cannot convert: returned value not of type String.";
            throw new TagConversionException(msg);
        }
        return (String) result;
    }

    //-------------------------------------
    /**
     * Evaluates the tag converter according to the appropriate method.  The bsh
     * script can access the converter itself by calling "converter".
     * @param pMethod the name of the method which called us.  Set as "method".
     * @param pRequest the request.  Set as "request".
     * @param pValue the value to be converted.  Set as "value".
     * @param pAttributes the attributes associated with the tag converter.  Set as "attributes".
     * @return the converted value, pull from the bsh variable "result".
     * @throws TagConversionException if an error occured.
     */
    public Object evaluate(String pMethod, DynamoHttpServletRequest pRequest,
                           Object pValue, Properties pAttributes)
        throws TagConversionException
    {
        try
        {
            Interpreter bsh = getInterpreter();
            String filename = getFilename();

            File file = new File(filename);
            Reader reader = new BufferedReader(new FileReader(file));
            bsh.set("method", pMethod);
            bsh.set("request", pRequest);
            bsh.set("value", pValue);
            bsh.set("attributes", pAttributes);
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
            throw new TagConversionException(msg, te, errorCode);
        } catch (EvalError ee)
        {
            /** @todo i18n */
            String msg = "There was an error in evaluating the script:";
            String errorCode = EVAL_ERROR;
            if (isLoggingError())
            {
                logError(msg, ee);
            }
            throw new TagConversionException(msg, ee, errorCode);
        } catch (IOException ie)
        {
            /** @todo i18n */
            String msg = "IO Exception: ";
            String errorCode = IO_EXCEPTION;
            if (isLoggingError())
            {
                logError(msg, ie);
            }
            throw new TagConversionException(msg, ie, errorCode);
        }
    }
}
