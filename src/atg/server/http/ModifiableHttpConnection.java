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
 * "Dynamo" is a trademark of Art Technology Group, Inc.
 </ATGCOPYRIGHT>*/

package atg.server.http;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

import atg.core.util.*;
import atg.core.net.URLUtils;
import atg.core.io.*;
import atg.server.tcp.*;
import atg.service.perfmonitor.*;
import atg.servlet.ServletUtil;

import bsh.*;

/**
 * Override the HTTP connection so that we can write headers to the
 * output stream.
 *
 * There's no point in overriding serviceRequest, because that will
 * go through the servlet pipeline anyway.
 *
 * @author Will Sargent
 * @version $Id: //user/wsargent/bsh/main/src/atg/server/http/ModifiableHttpConnection.java#5 $
 */

public class ModifiableHttpConnection extends HttpConnection
{
    boolean mIsDynamicScript = false;

    File mFile;

    String mScript = null;

    Interpreter mInterpreter;

    public ModifiableHttpConnection(ThreadGroup pGroup,
                                    String pThreadName,
                                    ModifiableHttpRequestServer pServer,
                                    Servlet pServlet,
                                    Socket pSocket,
                                    Interpreter pInterpreter,
                                    File pFile)
    {
        super (pGroup, pThreadName, pServer, pServlet, pSocket);
        mIsDynamicScript = pServer.isDynamicScript();
        mInterpreter = pInterpreter;
        mFile = pFile;

        try
        {
            readFile();
        } catch (IOException ie)
        {
            if (pServer.isLoggingError()) pServer.logError(ie);
        }
    }

    //-------------------------------------
    /**
     * Override prepareRequest with the beanshell script.
     */
    void prepareRequest (InputStream pIn) throws IOException
    {
        // Reset the buffered input stream
        mRequestInput.setInputStream (pIn);
        mServletInput.setInputStream (mRequestInput);

        // Read the header lines
        readHeaderLines ();
        if (mNumHeaderLines < 1) {
          throw new IOException ("No header for request");
        }

        if (mIsDynamicScript) readFile();
        if ( StringUtils.isBlank(mScript) ) return;

        try
        {
            mInterpreter.set("method", "prepareRequest");
            mInterpreter.set("servlet", mServlet);
            mInterpreter.set("socket", mSocket);
            mInterpreter.set("server", mServer);
            mInterpreter.set("request", mRequest);
            mInterpreter.set("response", mResponse);
            mInterpreter.set("connection", this);
            mInterpreter.set("mNumHeaderLines", mNumHeaderLines);
            mInterpreter.set("mHeaderLines", mHeaderLines);
            mInterpreter.set("in", pIn);
            mInterpreter.eval(mScript);
            
            mHeaderLines = (ByteString[]) mInterpreter.get("mHeaderLines");
            mNumHeaderLines = ((Integer) mInterpreter.get("mNumHeaderLines")).intValue();
        } catch (TargetError te)
        {
            /** @todo i18n */
            Throwable target = te.getTarget();
            String msg = "The script or code called by the script threw an exception: " + target;
            if (mServer.isLoggingError())
            {
                mServer.logError(msg);
            }
            //throw new ServletException(msg, te);
        } catch (EvalError ee)
        {
            /** @todo i18n */
            String msg = "There was an error in evaluating the script:";
            if (mServer.isLoggingError())
            {
                mServer.logError(msg, ee);
            }
            //throw new ServletException(msg, ee);
        }

        // Parse the first line
        // parseFirstLine (mHeaderLines [0]);

        // Get the headers
        for (int i = 1; i < mNumHeaderLines; i++) {
          parseHeader (mHeaderLines [i]);
        }
    }

    //-------------------------------------
    /**
     * Override preparesResponse with our own header, and evaluate the
     * beanshell script.
     */
    void prepareResponse (OutputStream pOut)
    {
        super.prepareResponse(pOut);
        if ( StringUtils.isBlank(mScript) ) return;

        try
        {
            mInterpreter.set("method", "prepareResponse");
            mInterpreter.set("servlet", mServlet);
            mInterpreter.set("socket", mSocket);
            mInterpreter.set("server", mServer);
            mInterpreter.set("request", mRequest);
            mInterpreter.set("response", mResponse);
            mInterpreter.set("connection", this);
            mInterpreter.set("out", pOut);
            mInterpreter.eval(mScript);
        } catch (TargetError te)
        {
            /** @todo i18n */
            Throwable target = te.getTarget();
            String msg = "The script or code called by the script threw an exception: " + target;
            if (mServer.isLoggingError())
            {
                mServer.logError(msg);
            }
            //throw new ServletException(msg, te);
        } catch (EvalError ee)
        {
            /** @todo i18n */
            String msg = "There was an error in evaluating the script:";
            if (mServer.isLoggingError())
            {
                mServer.logError(msg, ee);
            }
            //throw new ServletException(msg, ee);
        } // never ever catch IOException in the servlet pipeline.

    }

    //-------------------------------------
    /**
     * Reads in the beanshell file.
     */
    void readFile() throws IOException
    {
        if ( mServer.isLoggingDebug() )
        {
            String msg = "reading script file: " + mFile;
            mServer.logDebug(msg);
        }

        BufferedReader reader = new BufferedReader(new FileReader(mFile));
        StringBuffer buf = new StringBuffer();
        String line = null;
        while ( (line = reader.readLine()) != null )
        {
            buf.append(line);
            buf.append("\n");
        }
        mScript = buf.toString();
    }

}