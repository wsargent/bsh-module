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
import java.util.ResourceBundle;
import javax.servlet.*;
import javax.servlet.http.*;
import atg.nucleus.*;
import atg.server.tcp.*;
import atg.core.util.*;
import atg.servlet.pagecompile.PageEncodingTyper;

import bsh.*;

/**
 * Override the HttpRequestServer so that we can peek and pock at the HTTP
 * connection with Beanshell.
 *
 * @author Will Sargent
 * @version $Id: //user/wsargent/bsh/main/src/atg/server/http/ModifiableHttpRequestServer.java#2 $
 */

public class ModifiableHttpRequestServer extends HttpRequestServer
{
    /**
     * The Beanshell interpreter.
     */
    Interpreter mInterpreter = new Interpreter();

    //-------------------------------------
    // property: file
    File mFile;

    /**
     * Sets property file
     */
    public void setFile(File pFile)
    {
        mFile = pFile;
    }

    /**
     * Returns property file
     */
    public File getFile()
    {
        return mFile;
    }
    
    //------------------------------------
    
    private boolean mDynamicScript;
    /**
     * Returns true if we want to re-read the script on every HTTP request.
     */
    public boolean isDynamicScript()
    {
        return mDynamicScript;
    }
    
    /**
     * Sets the dynamicScript property with pDynamicScript.
     */
    public void setDynamicScript(boolean pDynamicScript)
    {
        mDynamicScript = pDynamicScript;
    }
    
    //-------------------------------------
    /**
     * Override HttpRequestServer to provide our own connection with the
     * interpreter.
     */
    protected RequestServerHandler createHandler (ThreadGroup pGroup,
                                                  String pThreadName,
                                                  Socket pSocket)
    {
        return new ModifiableHttpConnection (pGroup, pThreadName, this,
                                             mServlet, pSocket, mInterpreter,
                                             mFile);
    }

}