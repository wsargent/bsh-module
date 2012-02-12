package atg.bsh;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.TransactionManager;

import atg.dtm.TransactionDemarcation;
import atg.nucleus.Nucleus;
import atg.nucleus.ServiceAdminServlet;
import atg.nucleus.naming.ComponentName;
import bsh.EvalError;
import bsh.Interpreter;

/**
 * Gets the beanshell admin servlet displayed.
 * 
 * @author wsargent
 * @since Feb 9, 2006
 * @version $Revision$
 * 
 */
public class BshAdminServlet extends ServiceAdminServlet
{
    /**
     * 
     */
    private static final ComponentName TXMGR_NAME = ComponentName
            .getComponentName("/atg/dynamo/transaction/TransactionManager");

    public static int COLS = 90;
    public static int ROWS = 30;

    /**
     * @param pArg0
     * @param pArg1
     */
    public BshAdminServlet(Object pService, Nucleus pNucleus)
    {
        super(pService, pNucleus);

    }

    protected void printAdmin(HttpServletRequest pRequest,
            HttpServletResponse pResponse, ServletOutputStream pOutputStream)
            throws IOException, ServletException
    {
        super.printAdmin(pRequest, pResponse, pOutputStream);

        printForm(pRequest, pResponse, pOutputStream);
    }

    protected void printForm(HttpServletRequest pRequest,
            HttpServletResponse pResponse, ServletOutputStream pOutputStream)
            throws IOException
    {
        pOutputStream.println("<H2>Beanshell Admin Service</H2>");
        
        Nucleus n = mNucleus;
        String strActionParam = pRequest.getParameter("action");

        pOutputStream.println("<p>");
        pOutputStream.print("You can render output to this page by using the outputStream parameter, ");
        pOutputStream.println("i.e. <code>outputStream.println(\"hello world\")</code>.");
        pOutputStream.println("Other parameters are <code>nucleus</code>, <code>transactionDemarcation</code>, <code>request</code>, and <code>response</code>.");
        pOutputStream.println("<p>Script output appears below:");
        pOutputStream.print("<p>");
        
        // display the result.
        if ("edit".equals(strActionParam))
        {
            long startTime = System.currentTimeMillis();
            
            // We want a new interpreter every type, so it doesn't keep old
            // references...
            Interpreter interpreter = new Interpreter();
            try
            {
                // Allow the script to interact with what we are doing.
                interpreter.set("request", pRequest);
                interpreter.set("response", pResponse);
                interpreter.set("outputStream", pOutputStream);
                interpreter.set("nucleus", mNucleus);

                // Provide a method for people to set any other parameters...
                setInterpreterParameters(interpreter);

                String beanshellScript = pRequest.getParameter("beanshell");

                boolean rollback = true;
                TransactionManager tm = getTransactionManager();
                TransactionDemarcation td = new TransactionDemarcation();
                
                // If we need to do something special to the transaction inside
                // the script...
                interpreter.set("transactionDemarcation", td);
                
                td.begin(tm);
                try
                {
                    interpreter.eval(beanshellScript);
                    rollback = false;
                } finally
                {
                    td.end(rollback);
                }
            } catch (EvalError e)
            {
                if (n.isLoggingError())
                {
                    n.logError(e);
                }
                printEvalError(pRequest, pOutputStream, e);
            } catch (Exception e)
            {
                if (n.isLoggingError())
                {
                    n.logError(e);
                }
                printException(pRequest, pOutputStream, e);
            }
            long endTime = System.currentTimeMillis();
            
            long elapsedTime = endTime - startTime;
            pOutputStream.println("<p>Elapsed Time: " + elapsedTime + " ms<p>");
        }

        printEditForm(pRequest, pOutputStream);
        pOutputStream.println("<P>");
    }

    /**
     * Returns the transaction manager. You may have to override this if you are
     * using ATG on another J2EE server.
     * 
     * @return
     */
    protected TransactionManager getTransactionManager()
    {
        TransactionManager tm = (TransactionManager) mNucleus
                .resolveName(TXMGR_NAME);
        return tm;
    }

    /**
     * Sets the interpreter parameters. You can override this to expose useful
     * parameters.
     * 
     * @throws EvalError
     * 
     */
    public void setInterpreterParameters(Interpreter interpreter)
            throws EvalError
    {

    }

    /**
     * @param pRequest
     * @param pOutputStream
     * @param pE
     * @throws IOException
     */
    protected void printEvalError(HttpServletRequest pRequest,
            ServletOutputStream pOutputStream, EvalError pE) throws IOException
    {
        EvalError e = pE;
        String message = e.getMessage();
        int errorLineNumber = e.getErrorLineNumber();
        String errorText = e.getErrorText();
        Throwable initCause = e.getCause();

        pOutputStream.print("eval error:<p>");
        pOutputStream.print("error message: <pre>" + message + "</pre>");
        pOutputStream.print("<p>");
        pOutputStream.print("errorLineNumber: " + errorLineNumber);
        pOutputStream.print("<p>");
        pOutputStream.print("errorText: <pre>" + errorText + "</pre>");
        pOutputStream.print("<p>");
        pOutputStream.print("initCause: " + initCause);
        pOutputStream.print("<p>");
    }

    /**
     * @param pRequest
     * @param pOutputStream
     * @param pE
     * @throws IOException
     */
    protected void printException(HttpServletRequest pRequest,
            ServletOutputStream pOutputStream, Throwable pE) throws IOException
    {
        Throwable e = pE;
        String message = e.getMessage();
        Throwable initCause = e.getCause();

        pOutputStream.print("exception:<p>");
        pOutputStream.print("error message: " + message);
        pOutputStream.print("<p>");
        pOutputStream.print("initCause: " + initCause);
        pOutputStream.print("<p>");
    }

    protected void printResults(HttpServletRequest pRequest,
            ServletOutputStream pOutputStream) throws IOException
    {
        pOutputStream.print("<b>Evaluation completed.</b>");
    }

    /**
     * @param pRequest
     * @param pResponse
     * @param pOutputStream
     * @param pFuncName
     * @throws IOException
     */
    protected void printEditForm(HttpServletRequest pRequest,
            ServletOutputStream pOutputStream) throws IOException
    {
        String url = formatServiceName(pRequest.getPathInfo(), pRequest);
        pOutputStream.println("<table cellspacing=\"1\" border=\"0\">");
        pOutputStream.println("<tr><td>");
        pOutputStream.println("<form method=\"POST\" action=\"" + url
                + "?action=edit\">");
        pOutputStream.println("<textarea name=\"beanshell\" cols=\"" + COLS
                + "\" rows=\"" + ROWS + "\"></textarea></input>");
        pOutputStream.println("<br />");
        pOutputStream.println("<input type=\"submit\" name=\"submit\"/>");
        pOutputStream.println("</form>");
        pOutputStream.println("</td></tr></table>");
    }

}
