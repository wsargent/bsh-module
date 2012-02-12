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

/**
 * A generic beanshell droplet.  This class takes a beanshell script as input, and 
 * evaluates it; the beanshell script does the work of setting any output parameters
 * and calling oparams.
 * 
 * @author Will Sargent
 * @version $Id: //user/wsargent/bsh/main/src/atg/bsh/BshDroplet.java#3 $
 */
public class BshDroplet extends DynamoServlet
{
    private Interpreter mInterpreter;
    public Interpreter getInterpreter()
    {
        return mInterpreter;
    }

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
    
    private File mDocumentRoot;
    public File getDocumentRoot()
    {
        return mDocumentRoot;
    }
    
    public void setDocumentRoot(File pDocumentRoot)
    {
        mDocumentRoot = pDocumentRoot;
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
    public void service(DynamoHttpServletRequest pRequest,
                        DynamoHttpServletResponse pResponse)
        throws ServletException, IOException
    {
         try
         {
            Interpreter bsh = getInterpreter();

            String source = pRequest.getParameter("filename");
            File file = null;
            if (! StringUtils.isEmpty(source) )
            {
                File documentRoot = getDocumentRoot();
                file = new File(documentRoot, source);
                if ( isLoggingDebug() )
                {
                    String msg = "reading file from " + file.getCanonicalPath();
                    logDebug(msg);
                }                
            } else
            {
                file = getFile();
            }
            if (file == null)
            {
                String msg = "Cannot read file: ";
                throw new DropletException(msg);                
            }
            
            Reader reader = new BufferedReader(new FileReader(file));
            bsh.set("request", pRequest);
            bsh.set("response", pResponse);
            bsh.set("service", this);
            bsh.eval(reader);
        } catch (TargetError te)
        {
            /** @todo i18n */
            Throwable target = te.getTarget();
            String msg = "The script or code called by the script threw an exception: " + target;
            if (isLoggingError())
            {
                logError(msg);
            }
            throw new DropletException(msg, te);
        } catch (EvalError ee)
        {
            /** @todo i18n */
            String msg = "There was an error in evaluating the script:";
            if (isLoggingError())
            {
                logError(msg, ee);
            }
            throw new DropletException(msg, ee);
        } catch (IOException ie)
        {
            /** @todo i18n */
            String msg = "IO Exception: ";
            if (isLoggingError())
            {
                logError(msg, ie);
            }
            throw new DropletException(msg, ie);
        }
    }

}
